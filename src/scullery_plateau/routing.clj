(ns scullery-plateau.routing
  (:require [routing.core :as r]
            [clojure.edn :as edn]
            [clojure.xml :as xml]))

(defn build-app []
  (r/build-app '[[GET ["api" "plus"] {} (ctrl/add-xy request/query)]
                 [POST ["api" "edn"] {} (ctrl/body-type request/body)]
                 [POST ["api" "svg"] {} (ctrl/xml-ify request/body)]
                 ]
               {:ctrl {:add-xy    #(+ (Integer/parseInt (% "x")) (Integer/parseInt (% "y")))
                       :body-type #(str (type %))
                       :xml-ify   #(let [obj (edn/read-string %)]
                                     (println %)
                                     (println obj)
                                     (let [xml-str (with-out-str (xml/emit-element obj))]
                                       (println xml-str)
                                       xml-str))}}))
