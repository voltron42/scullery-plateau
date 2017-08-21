(ns routings.core
  (:require [clojure.string :as s]
            [cheshire.core :as json]))

(defn split-path [pathstr]
  (if (= pathstr "/") [] (clojure.string/split (clojure.string/join "" (drop 1 pathstr)) #"/")))

(defn method [req]
  (->> req
       (:request-method)
       (name)
       (s/upper-case)
       (keyword)))

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
                     :body   (json/generate-string (action (merge req (build-path-params path req-path))))}
                    (catch Throwable t
                      (println (.getMessage t))
                      (.printStackTrace t)
                      {:status 500 :body (str t)})))))))

(defn build-handler [action schema resp-head path]
  (fn [req]

    ))

(defn index-routing [routes]
  (reduce
    (fn [routing [method path req-head resp-head schema action]]
      (assoc routing
        {:method method
         :path path
         :headers req-head}
        (build-handler action schema resp-head path)))
    {}
    routes))

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
          (.printStackTrace t)
          )))))