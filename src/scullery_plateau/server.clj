(ns scullery-plateau.server
  (:require [org.httpkit.server :as http]
            [scullery-plateau.routes :as r]
            [environ.core :refer [env]]))

(defn -main [& [port]]
  (let [my-app (r/build-app)
        port (Integer. ^int (or port (env :port) 5000))]
    (http/run-server my-app {:port port :join? false})))