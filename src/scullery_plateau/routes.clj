(ns scullery-plateau.routes
  (:require [compojure.api.sweet :as s]
            [clj-pdf.core :as pdf]
            [ring.util.io :as io]
            [ring.util.http-response :as r]
            [clojure.tools.reader.edn :as edn]
            [clojure.xml :as xml]
            [schema.core :as schema]
            [scullery-plateau.raster :as img]))

(defn build-app []
  (s/api
    {:swagger
     {:ui "/api-docs"
      :spec "/swagger.json"
      :data {:info {:title "Sample API"
                    :description "Compojure Api example"}
             :tags [{:name "api" :description "some apis"}]}}}
    (s/context "/api" []
      :tags ["api"]

      (s/GET "/plus" []
        :return Long
        :query-params [x :- Long, y :- Long]
        :summary ""
        (r/ok (+ x y)))

      (s/POST "/pdf" []
        :body [body String]
        (r/ok (io/piped-input-stream
                (fn [out]
                  (pdf/pdf (edn/read-string body) out)))))

      (s/POST "/svg" []
        :body [body String]
        (r/ok (with-out-str (xml/emit-element (edn/read-string body)))))

      (s/POST "/svg/:type" []
        :body [body String]
        :path-params [type :- (schema/enum "png" "jpeg")]
        (r/ok (io/piped-input-stream
                (fn [out]
                  (let [svg (edn/read-string body)]
                    (img/rasterize (keyword type) {} svg out))))))
      )))
