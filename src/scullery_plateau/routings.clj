(ns scullery-plateau.routings
  (:require [routings.core :as r]
            [schema.core :as schema]
            [scullery-plateau.raster :as img]
            [ring.util.io :as io]
            [clj-pdf.core :as pdf]
            [clojure.edn :as edn]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.string :as s]
            [template.core :as tpl]
            [xml-short.core :as short]
            [cheshire.core :as json]
            [scullery-plateau.draw :as draw])
  (:import (java.io File OutputStream ByteArrayOutputStream ByteArrayInputStream FileInputStream)
           (java.net URLDecoder)))

(defn- parse-int [val] (Integer/parseInt val))

(defn- raster-svg [type svg]
  (let [out (ByteArrayOutputStream.)]
    (img/rasterize type {} svg out)
    (ByteArrayInputStream. (.toByteArray out))))

(defn- raster-img [type]
  (fn [{:keys [body]}]
    (let [{:keys [svg]} body
          svg (edn/read-string (URLDecoder/decode svg))]
      (raster-svg type svg))))

(def ^:private build-template (tpl/build-template-factory {}))

(defn- build-page [filepath data]
  (let [tpl (->> (str filepath ".edn") (slurp) (edn/read-string) (build-template))]
    (->> data (tpl) (short/x-pand) (xml/emit-element) (with-out-str))))

(def ^:private page404 (build-page "resources/tpl/404" {}))

(defn build-app []
            (r/build-api
              {"/favicon.ico" ["resources/icon/favicon.ico"]
               "/script" ["/resources/js"]
               "/pages" ["/resources/pages"]
               "/css" ["/resources/css"]
               "/jquery" ["/resources/jquery"]
               "/edn" ["/resources/edn" #(build-page % {})]}
              (r/context "/sample"
                         (r/GET "/plus" {}
                                {"content-type" "text/plain"}
                                {:query [{:x schema/Int :y schema/Int} {:x parse-int :y parse-int}]}
                                (fn [{:keys [query]}]
                                  (let [{:keys [x y]} query]
                                    (+ x y)))))
              (r/context "/apps"
                         (r/GET "/:app"
                                {}
                                {"content-type" "text/html"}
                                {:path [{:app schema/Keyword} {:app keyword}]}
                                (fn [{:keys [path]}]
                                  (let [apps (->> "resources/tpl/apps.edn"
                                                  (slurp)
                                                  (edn/read-string))
                                        {:keys [app]} path]
                                    (if-not (contains? apps app)
                                      page404
                                      (build-page "resources/tpl/index" (apps app))
                                      )))))
              (r/context "/api"
                         (r/context "/svg"
                                    (r/POST "/png"
                                            {"content-type" "application/x-www-form-urlencoded"}
                                            {"content-type" "image/png"}
                                            {:body [schema/Any identity]}
                                            (raster-img :png))
                                    (r/POST "/jpeg"
                                            {"content-type" "application/x-www-form-urlencoded"}
                                            {"content-type" "image/jpeg"}
                                            {:body [schema/Any identity]}
                                            (raster-img :jpeg)))
                         (r/POST "/pdf"
                                 {"content-type" "application/x-www-form-urlencoded"}
                                 {"content-type" "application/pdf"}
                                 {:body [schema/Any identity]}
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
                                      :contents contents}))))
              (r/context "/draw"
                         (r/GET "/pixel"
                                {}
                                {"content-type" "text/html"}
                                {}
                                (fn [_]
                                  (build-page "resources/tpl/pixelart"
                                              {:data (json/generate-string {:width 10
                                                                            :height 10
                                                                            :palette ["white"]
                                                                            :grid {}})
                                               :filename "new.json"})))
                         (r/Multipart "/pixel"
                                      {}
                                      {"content-type" "text/html"}
                                      {}
                                      (fn [{{:keys [file]} :multipart}]
                                        (->> file
                                             :tempfile
                                             (slurp)
                                             (assoc (select-keys file [:filename]) :data)
                                             (build-page "resources/tpl/pixelart"))))
                         (r/POST "/pixel/:filename"
                                 {"content-type" "application/x-www-form-urlencoded"}
                                 {"content-type" "application/json"}
                                 {}
                                 (fn [{{:keys [savedata]} :body}]
                                   (->> savedata
                                        (URLDecoder/decode)
                                        (json/parse-string))))
                         (r/POST "/pixel/png/art.png"
                                 {"content-type" "application/x-www-form-urlencoded"}
                                 {"content-type" "image/png"}
                                 {}
                                 (fn [{:keys [body]}]
                                   (println "here")
                                   (println "body: " body)
                                   (let [{:keys [pngdata pixelsize]} body
                                         size (Integer/parseInt pixelsize)
                                         _ (println "data: " pngdata)
                                         pngdata (URLDecoder/decode pngdata)
                                         _ (println "data: " pngdata)
                                         pngdata (json/parse-string pngdata true)
                                         _ (println "data: " pngdata)
                                         {:keys [width height palette grid]} pngdata
                                         _ (println "width: " width)
                                         _ (println "height: " height)
                                         grid (mapv (fn [[k v]]
                                                      (let [colorIndex (Integer/parseInt v)
                                                            [x y] (map #(Integer/parseInt %) (s/split (name k) #"-"))]
                                                        {:x x :y y :c colorIndex})) grid)]
                                     (raster-svg :png (draw/draw-pixels size width height palette grid))))))
              (r/context "/state"
                         (r/GET "/test"
                                {}
                                {"content-type" "text/html"}
                                {}
                                (fn [_]
                                  (build-page "resources/tpl/testapp"
                                              {:data "{}"
                                               :filename "new.json"})))
                         (r/Multipart "/test"
                                      {}
                                      {"content-type" "text/html"}
                                      {}
                                      (fn [{:keys [multipart]}]
                                        (->> multipart
                                             :file
                                             :tempfile
                                             (slurp)
                                             (assoc (select-keys (:file multipart) [:filename]) :data)
                                             (build-page "resources/tpl/testapp"))))
                         (r/POST "/test/:filename"
                                      {"content-type" "application/x-www-form-urlencoded"}
                                      {"content-type" "application/json"}
                                      {}
                                      (fn [{:keys [body]}]
                                        (->> body
                                             :savedata
                                             (URLDecoder/decode)
                                             (json/parse-string)))))))
