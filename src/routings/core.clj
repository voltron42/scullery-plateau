(ns routings.core
  (:require [clojure.string :as s]
            [cheshire.core :as json]
            [ring.util.http-response :as r]
            [clojure.edn :as edn]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [schema.core :as schema])
  (:import (java.io ByteArrayInputStream)
           (clojure.lang ExceptionInfo)))

(defn- split-path [path-str]
  (if (= path-str "/") [] (clojure.string/split (clojure.string/join "" (drop 1 path-str)) #"/")))

(defn- method [req]
  (->> req
       (:request-method)
       (name)
       (s/upper-case)
       (keyword)))

(defn- step-match [step req-step]
  (or (keyword? step)
      (= step req-step)))

(defn- match-route [route req]
  (and (= (:method req) (:method route))
       (let [{:keys [headers]} route]
         (= (select-keys (:headers req) (keys headers)) headers))
       (every? true? (mapv step-match (:path route) (:path req)))
       (= (count (:path route)) (count (:path req)))))

(defn- matcher [req-path req-method req-headers]
  (fn [[{:keys [path method headers] :as route} _]]
    (let [matches? (match-route route {:path req-path :method req-method :headers req-headers})]
      matches?)))

(defn- build-path-params [path req-path]
  (reduce (fn [params {:keys [route req]}]
            (if (keyword? route)
              (assoc params route req)
              params))
          {}
          (mapv #(-> {:route %1 :req %2}) path req-path)))

(defn- get-route [routing req-path method headers]
  (let [routes (filter (matcher req-path method headers) routing)]
    (if (not= 1 (count routes))
      nil
      (second (first routes)))))

(defn- parse-query [query]
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

(def ^:private mime-types
  {"application/json"                  [json/parse-string
                                        json/generate-string]
   "application/xml"                   [#(->> %
                                              (.getBytes)
                                              (ByteArrayInputStream.)
                                              (xml/parse)
                                              (zip/xml-zip))
                                        #(with-out-str (xml/emit-element %))]
   "application/edn"                   [edn/read-string
                                        str]
   "application/x-www-form-urlencoded" [parse-query identity]
   "text/plain" [str str]})

(defn- get-body-fn [pos {:keys [Content-Type]}]
  (if (or (nil? Content-Type) (contains? mime-types Content-Type))
    identity
    (pos (mime-types Content-Type))))

(def ^:private get-body-parser (partial get-body-fn first))

(def ^:private get-body-builder (partial get-body-fn second))

(def ^:private schema-fields [:query :path :headers :body])

(defn- schema-map-reducer [pos default-fn schema]
  (fn [obj key]
    (assoc obj key
               (if (contains? schema key)
                 (pos (schema key))
                 default-fn))))

(defn build-schemafier [pos default-fn schema]
  (let [reduced-map (reduce (schema-map-reducer pos default-fn schema) {} schema-fields)]
    (fn [inputs]
      (reduce (fn [out [field input]]
                (assoc out field ((reduced-map field) input)))
              {}
              inputs))))

(def ^:private build-validator (partial build-schemafier #(partial schema/validate (first %)) (constantly nil)))

(def ^:private build-coercer (partial build-schemafier second identity))

(defn- build-handler [action schema req-head resp-head route-path]
  (let [parse-body (get-body-parser req-head)
        build-body (get-body-builder resp-head)
        validate (build-validator schema)
        coerce (build-coercer schema)]
    (fn [split-uri {:keys [headers body query-string request-method content-type] :as req}]
      (let [body-str (if (nil? body) "" (slurp body))
            body (parse-body body-str)
            query (if (nil? query-string) {} (parse-query query-string))
            path (build-path-params route-path split-uri)
            new-req (coerce {:query query
                             :path path
                             :headers headers
                             :body body})
            response (try
                       (validate new-req)
                       (r/ok (build-body (action new-req)))
                       (catch ExceptionInfo e
                         (r/bad-request (build-body (.getData e))))
                       (catch IllegalArgumentException e
                         (r/bad-request (build-body (.getMessage e))))
                       (catch Throwable e
                         (r/internal-server-error (build-body (.getMessage e)))))]
        (assoc response :headers resp-head)))))

(defn- index-routing [routes]
  (reduce
    (fn [routing [method path req-head resp-head schema action]]
      (assoc routing
        {:method method
         :path path
         :headers req-head}
        (build-handler action schema req-head resp-head path)))
    {}
    routes))

(defn- build-route [method path req-head resp-head schema action]
  [method path req-head resp-head schema action])

(def GET (partial build-route :GET))

(def POST (partial build-route :POST))

(def PUT (partial build-route :PUT))

(def DELETE (partial build-route :DELETE))

(def OPTIONS (partial build-route :OPTIONS))

(def HEAD (partial build-route :HEAD))

(defn build-api [& routes]
  (let [routing (index-routing routes)]
    (fn [req]
      (try
        (let [path (split-path (:uri req))
              route (get-route routing path (method req) (:headers req))]
          (if (nil? route)
            {:status 404 :body (str "Not Found")}
            (route path req)))
        (catch Throwable t
          (println (.getMessage t))
          (.printStackTrace t))))))
