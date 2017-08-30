(ns scullery-plateau.routings
  (:require [routings.core :as r]
            [schema.core :as s]
            [scullery-plateau.raster :as img]
            [ring.util.io :as io]
            [clj-pdf.core :as pdf]
            [clojure.edn :as edn]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [template.core :as tpl]
            [xml-short.core :as short])
  (:import (java.io File OutputStream ByteArrayOutputStream ByteArrayInputStream FileInputStream)
           (java.net URLDecoder)))

(defn- parse-int [val] (Integer/parseInt val))

(defn- raster-img [type]
  (fn [{:keys [body]}]
    (let [{:keys [svg]} body
          svg (edn/read-string (URLDecoder/decode svg))
          out (ByteArrayOutputStream.)]
      (img/rasterize :png {} svg out)
      (ByteArrayInputStream. (.toByteArray out)))))

(def ^:private page404 "Not Found")

(def ^:private build-template (tpl/build-template-factory {}))

(defn build-app []
            (r/build-api
              {"/script" ["/resources/js"]
               "/edn" ["/resources/edn" #(let [tpl (->> (str % ".edn") (slurp) (edn/read-string) (build-template))]
                                             (->> {} (tpl) (short/x-pand) (xml/emit-element) (with-out-str)))]}
              (r/GET "/favicon.ico" {} {} {} (fn [_] (FileInputStream. "resources/icon/favicon.ico")))
              (r/context "/sample"
                         (r/GET "/plus" {}
                                {"content-type" "text/plain"}
                                {:query [{:x s/Int :y s/Int} {:x parse-int :y parse-int}]}
                                (fn [{:keys [query]}]
                                  (let [{:keys [x y]} query]
                                    (+ x y)))))
              (r/context "/apps"
                         (r/GET "/:app"
                                {}
                                {"content-type" "text/html"}
                                {:path [{:app s/Keyword} {:app keyword}]}
                                (fn [{:keys [path]}]
                                  (let [apps (->> "resources/tpl/apps.edn" (slurp) (edn/read-string))
                                        {:keys [app]} path]
                                    (if-not (contains? apps app)
                                      page404
                                      (let [tpl (->> "resources/tpl/index.edn"
                                                     (slurp)
                                                     (edn/read-string)
                                                     (build-template))]
                                        (->> (apps app)
                                             (tpl)
                                             (short/x-pand)
                                             (xml/emit-element)
                                             (with-out-str))))))))
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
                         (r/Multipart "/form"
                                 {}
                                 {"content-type" "application/json"}
                                 {}
                                 (fn [{:keys [headers multipart]}]
                                   (let [contents (->> multipart
                                                       :file
                                                       :tempfile
                                                       (slurp))]
                                     {:multipart multipart
                                      :contents contents}))))))
