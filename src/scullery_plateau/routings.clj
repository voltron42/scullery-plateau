(ns scullery-plateau.routings
  (:require [routings.core :as r]
            [schema.core :as s]
            [scullery-plateau.raster :as img]
            [ring.util.io :as io]
            [clj-pdf.core :as pdf]
            [clojure.edn :as edn])
  (:import (java.io File OutputStream)
           (java.net URLDecoder)))

(defn- parse-int [val] (Integer/parseInt val))

(defn build-app []
  (r/static "/local" "/resources/web"
            (r/build-api
              (r/context "/sample"
                         (r/GET "/plus" {}
                                {"content-type" "text/plain"}
                                {:query [{:x s/Int :y s/Int} {:x parse-int :y parse-int}]}
                                (fn [{:keys [query]}]
                                  (let [{:keys [x y]} query]
                                    (+ x y)))))
              (r/context "/api"
                         (r/context "/svg"
                                    (r/POST "/png"
                                            {"content-type" "application/x-www-form-urlencoded"}
                                            {"content-type" "image/png"}
                                            {:body [s/Any identity]}
                                            (fn [{:keys [body]}]
                                              (let [{:keys [pdf]} body
                                                    pdf (edn/read-string (URLDecoder/decode pdf))]
                                                (io/piped-input-stream
                                                  (fn [out]
                                                    (img/rasterize :png {} pdf out)
                                                    (.flush out))))))
                                    (r/POST "/jpeg"
                                            {"content-type" "application/x-www-form-urlencoded"}
                                            {"content-type" "image/jpeg"}
                                            {:body [s/Any identity]}
                                            (fn [{:keys [body]}]
                                              (let [{:keys [pdf]} body
                                                    pdf (edn/read-string (URLDecoder/decode pdf))]
                                                (io/piped-input-stream
                                                  (fn [out]
                                                    (img/rasterize :jpeg {} pdf out)
                                                    (.flush out)))))))
                         (r/POST "/pdf"
                                 {"content-type" "application/x-www-form-urlencoded"}
                                 {"content-type" "application/pdf"}
                                 {:body [s/Any identity]}
                                 (fn [{:keys [body]}]
                                   (let [{:keys [pdf]} body
                                         pdf (edn/read-string (URLDecoder/decode pdf))]
                                     (io/piped-input-stream
                                       (fn [^OutputStream out]
                                         (pdf/pdf pdf out)
                                         (.flush out))))))
                         ))))
