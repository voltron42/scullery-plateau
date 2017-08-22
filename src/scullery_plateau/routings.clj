(ns scullery-plateau.routings
  (:require [routings.core :as r]
            [schema.core :as s]))

(defn build-app []
  (r/build-api
    (r/GET ["api" "plus"] {} {}
           {:query [{s/Keyword s/Int} #(reduce (fn [out [k v]] (assoc out (keyword k) (Integer/parseInt v))) {} %)]}
           (fn [{:keys [query]}]
             (let [{:keys [x y]} query]
               (+ x y))))
    (r/POST ["api" "edn"]
            {"Content-Type" "application/edn"}
            {"Content-Type" "text/plain"}
            {:body [s/Any identity]}
            #(type %))
    (r/POST ["api" "svg"]
            {"Content-Type" "application/edn"}
            {"Content-Type" "application/xml"}
            {:body [s/Any identity]}
            identity)))