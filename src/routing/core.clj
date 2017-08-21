(ns routing.core
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [cheshire.core :as json]
            [clojure.string :as s]
            [clojure.set :as set])
  (:import (java.io ByteArrayInputStream)))

(defn step-match [step req-step]
  (or (keyword? step)
      (= step req-step)))

(defn match-route [route req]
  (and (= (:method req) (:method route))
       (let [{:keys [headers]} route]
         (= (select-keys (:headers req) (keys headers)) headers))
       (every? true? (mapv step-match (:path route) (:path req)))
       (= (count (:path route)) (count (:path req)))))

(defn matcher [req-path req-method req-headers]
  (fn [[{:keys [path method headers] :as route} _]]
    (let [matches? (match-route route {:path req-path :method req-method :headers req-headers})]
      matches?)))

(def req-keys [:headers :body :path :content-type :method :query :params :json :xml])

(defn validate [routes ctrl]
  (let [expr-keys (set (concat (keys ctrl) (map #(symbol "request" (name %)) req-keys)))
        indexed (mapv (fn [[method path headers action] index]
                        {:method  method
                         :path    path
                         :headers headers
                         :action  (clojure.set/difference
                                    (set (filter #(or (keyword? %) (symbol? %)) (flatten action)))
                                    (set (filter keyword? path)))
                         :index index})
                      routes
                      (range))
        invalid-symbols (->> expr-keys
                             (clojure.set/difference (->> indexed
                                                          (map :action)
                                                          (apply concat)
                                                          (set)))
                             (map #(-> [% (resolve %)]))
                             (filter #(->> % (second) (nil?)))
                             (map first))
        indexed (map #(dissoc % :action) indexed)]
    (when (< 0 (count invalid-symbols))
      (throw (IllegalArgumentException. (str "Using Invalid symbols in routing expressions: " invalid-symbols))))
    (let [
          matches (for [x indexed y indexed
                        :when (and (< (:index x) (:index y)) (or (match-route x y) (match-route y x)))]
                    [(:path x) (:path y)])]
      (when (< 0 (count matches))
        (throw (IllegalArgumentException. (str "Ambiguous routings: " matches)))))
    )
  )

(defn resolve-expression [state expr]
  (cond
    (list? expr) (let [[func & args] expr
                       myfunc (if (contains? state func) (state func) (resolve func))]
                   (apply myfunc (map (partial resolve-expression state) args)))
    (or (keyword? expr) (symbol? expr)) (if (contains? state expr) (state expr) expr)
    (string? expr) expr
    :else expr))

(defn build-handler [expr ctrl]
  (fn [req] (resolve-expression (merge ctrl req) expr)))

(defn build-routing [routes ctrl]
  (validate routes ctrl)
  (reduce
    (fn [routing [method path headers action]]
      (assoc routing
        {:method method
         :path path
         :headers headers}
        (build-handler action ctrl)))
    {}
    routes))

(defn build-path-params [path req-path]
  (reduce (fn [params {:keys [route req]}]
            (if (keyword? route)
              (assoc params route req)
              params))
          {}
          (mapv #(-> {:route %1 :req %2}) path req-path)))

(defn get-route [routing req-path method headers]
  (let [routes (filter (matcher req-path method headers) routing)]
    (if (not= 1 (count routes))
      nil
      (let [[{:keys [path]} action] (first routes)]
        (fn [req] (try
                    {:status 200
                     :body (json/generate-string (action (merge req (build-path-params path req-path))))}
                    (catch Throwable t
                      (println (.getMessage t))
                      (.printStackTrace t)
                      {:status 500 :body (str t)})))))))

(defn flatten-controller [ctrl]
  (reduce (fn [end [label mymap]]
            (reduce (fn [out [k v]]
                      (assoc out (symbol (name label) (name k)) v))
                    end
                    mymap))
          {}
          ctrl))

(defn parse-query [query]
  (reduce
    (fn [out pair]
      (let [[k v] (s/split pair #"=")]
        (assoc out k
                   (if (contains? out k)
                     (if (vector? (out k))
                       (conj (out k) v)
                       (vector (out k) v))
                     v))))
    {}
    (s/split query #"&")))

(defn prep-request [req]
  (let [{:keys [headers body uri query-string request-method content-type]} req
        bodystr (if (nil? body) "" (slurp body))
        request {'request/headers       headers
                 'request/body          bodystr
                 'request/path          uri
                 'request/content-type  content-type
                 'request/method        request-method
                 'request/query (if (nil? query-string) {} (parse-query query-string))
                 'request/params (if (= content-type "application/x-www-form-urlencoded") (parse-query bodystr) {})
                 'request/json (try
                                 (json/parse-string bodystr)
                                 (catch Throwable t
                                   {}))
                 'request/xml (if (empty? bodystr)
                                []
                                (try
                                  (->> bodystr
                                       (.getBytes)
                                       (ByteArrayInputStream.)
                                       (xml/parse)
                                       (zip/xml-zip))
                                  (catch Throwable t
                                    [])))
                 }]
    request))

(defn split-path [pathstr]
  (if (= pathstr "/") [] (clojure.string/split (clojure.string/join "" (drop 1 pathstr)) #"/")))

(defn method [req]
  (->> req
       (:request-method)
       (name)
       (s/upper-case)
       (symbol)))

(defn build-app [routes ctrl]
  (let [routing (build-routing routes (flatten-controller ctrl))]
    (fn [req]
      (try
        (let [path (split-path (:uri req))
              route (get-route routing path (method req) (:headers req))]
          (if (nil? route)
            {:status 404 :body (str "Not Found")}
            (route (prep-request (assoc req :uri path)))))
        (catch Throwable t
          (println (.getMessage t))
          (.printStackTrace t))))))