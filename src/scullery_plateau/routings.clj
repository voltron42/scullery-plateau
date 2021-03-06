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

(defn- stringify-key [k]
  (if (or (keyword? k) (symbol? k))
    (name k)
    (str k)))

(defn- stringify-map [delim tpl-fn key-fn val-fn]
  (fn [col]
    (s/join delim
            (mapv (fn [[k v]]
                    (tpl-fn key-fn (val-fn v)))
                  col))))

(def ^:private build-template (tpl/build-template-factory {'css (fn [css]
                                                                  (stringify-map
                                                                    "\n" #(str %1 "{\n" %2 "\n}") stringify-key
                                                                    (stringify-map
                                                                      "\n" #(str %1 ": " %2 ";") stringify-key stringify-key)))}))

(defn- build-page [filepath data]
  (let [tpl (->> (str filepath ".edn") (slurp) (edn/read-string) (build-template))]
    (->> data (tpl) (short/x-pand) (xml/emit-element) (with-out-str))))

(def ^:private page404 (build-page "resources/tpl/404" {}))

(defn build-app []
  (r/build-api
    {:page404 page404
     :static {"/favicon.ico" ["/resources/icon/favicon.ico"]
              "/script" ["/resources/js"]
              "/pages" ["/resources/pages"]
              "/css" ["/resources/css"]
              "/jquery" ["/resources/jquery"]
              "/edn" ["/resources/edn" #(build-page % {})]}}
    (r/GET "/" {}
           {"content-type" "text/html"}
           {}
           (fn [_]
             (build-page "resources/tpl/home"
                         {:links [
                                  {:link "api/pdf" :label "PDF Builder Demo"}
                                  {:link "api/svg/png" :label "PNG SVG Builder Demo"}
                                  {:link "api/svg/jpeg" :label "JPEG SVG Builder Demo"}
                                  {:link "api/pdf" :label "PDF Builder Demo"}
                                  {:link "draw/pixel" :label "Pixel Art"}
                                  {:link "apps/letterer" :label "Letterer"}
                                  {:link "apps/tilebuilder" :label "Tile Builder"}
                                  {:link "pages/tilemap.html" :label "Tile Builder JQuery UI"}
                                  {:link "pages/five-points.html" :label "Five Points JQuery UI"}
                                  {:link "pages/color.html" :label "Color Picker Demo UI"}
                                  ]})))
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
                            (build-page "resources/tpl/index" (apps app)))))))
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
                               pngdata (URLDecoder/decode pngdata)
                               pngdata (json/parse-string pngdata true)
                               {:keys [width height palette grid]} pngdata
                               grid (mapv (fn [[k v]]
                                            (let [colorIndex (Integer/parseInt v)
                                                  [x y] (map #(Integer/parseInt %) (s/split (name k) #"-"))]
                                              {:x x :y y :c colorIndex})) grid)]
                           (raster-svg :png (draw/draw-pixels size width height palette grid))))))
))
