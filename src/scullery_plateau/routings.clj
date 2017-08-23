(ns scullery-plateau.routings
  (:require [routings.core :as r]
            [schema.core :as s]
            [scullery-plateau.raster :as img]
            [ring.util.io :as io]
            [clj-pdf.core :as pdf]))

(defn- parse-int [val] (Integer/parseInt val))

(defn build-app []
  (r/build-api
    (r/GET "/api/plus" {}
           {"content-type" "text/plain"}
           {:query [{:x s/Int :y s/Int} {:x parse-int :y parse-int}]}
           (fn [{:keys [query]}]
             (let [{:keys [x y]} query]
               (+ x y))))
    (r/POST "/api/edn"
            {"content-type" "application/edn"}
            {"content-type" "text/plain"}
            {:body [s/Any identity]}
            #(type %))
    (r/POST "/api/svg"
            {"content-type" "application/edn"}
            {"content-type" "application/xml"}
            {:body [s/Any identity]}
            identity)
    (r/POST "/api/svg"
            {"content-type" "application/edn"}
            {"content-type" "application/xml"}
            {:body [s/Any identity]}
            identity)
    (r/POST "/api/svg/png"
            {"content-type" "application/edn"}
            {"content-type" "image/png"}
            {:body [s/Any identity]}
            (fn [{:keys [body]}]
              (println body)
              (io/piped-input-stream
                (fn [out]
                  (img/rasterize :png {} body out)))))
    (r/POST "/api/svg/jpeg"
            {"content-type" "application/edn"}
            {"content-type" "image/jpeg"}
            {:body [s/Any identity]}
            (fn [{:keys [body]}]
              (io/piped-input-stream
                (fn [out]
                  (img/rasterize :jpeg {} body out)))))
    (r/POST "/api/pdf"
            {"content-type" "application/edn"}
            {"content-type" "application/pdf"}
            {:body [s/Any identity]}
            (fn [{:keys [body]}]
              (io/piped-input-stream
                (fn [out]
                  (pdf/pdf body out)))))))