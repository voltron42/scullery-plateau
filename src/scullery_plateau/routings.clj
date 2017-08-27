(ns scullery-plateau.routings
  (:require [routings.core :as r]
            [schema.core :as s]
            [scullery-plateau.raster :as img]
            [ring.util.io :as io]
            [clj-pdf.core :as pdf]
            [clojure.edn :as edn])
  (:import (java.io File OutputStream ByteArrayOutputStream ByteArrayInputStream)
           (java.net URLDecoder)))

(defn- parse-int [val] (Integer/parseInt val))

(defn- raster-img [type]
  (fn [{:keys [body]}]
    (let [{:keys [svg]} body
          svg (edn/read-string (URLDecoder/decode svg))
          out (ByteArrayOutputStream.)]
      (img/rasterize :png {} svg out)
      (ByteArrayInputStream. (.toByteArray out)))))

(defn build-app []
            (r/build-api
              {"/local" "/resources/web"}
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
                                            (raster-img :png))
                                    (r/POST "/jpeg"
                                            {"content-type" "application/x-www-form-urlencoded"}
                                            {"content-type" "image/jpeg"}
                                            {:body [s/Any identity]}
                                            (raster-img :jpeg)))
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
                         (r/POST "/form"
                                 {"content-type" "application/x-www-form-urlencoded"}
                                 {"content-type" "application/json"}
                                 {}
                                 identity))))
