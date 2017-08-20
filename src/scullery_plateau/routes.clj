(ns scullery-plateau.routes
  (:require [compojure.api.sweet :as s]
            [ring.util.http-response :as r]))

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
        (r/ok (+ x y))))))