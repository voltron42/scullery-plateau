(ns fight-club-5.explore
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [cheshire.core :as json])
  (:import (java.io File FileInputStream)))

(defn- simplify-recursive [out {:keys [tag attrs content]}]
  (println tag)
  (let [content (if (empty? content)
                  ""
                  (if (and (= 1 (count content)) (string? (first content)))
                    (first content)
                    (reduce simplify-recursive {} content)))
        _ (println "content")
        _ (println (type content))
        new-value (if (empty? attrs) content (assoc attrs ::content content))
        _ (println "new-value")
        _ (println (type new-value))
        current-value (get out tag)
        _ (println "current-value")
        _ (println (type current-value))
        value (if (empty? current-value)
                new-value
                (if (or (list? current-value) (vector? current-value))
                  (if (or (list? new-value) (vector? new-value))
                    (vec (concat current-value new-value))
                    (conj (vec current-value) new-value))
                  (if (or (list? new-value) (vector? new-value))
                    (vec (concat (vector current-value) new-value))
                    (vector current-value new-value))))
        ]
    (println "value")
    (println (type value))
    (assoc out tag value)))

(defn simplify [node]
  (simplify-recursive {} node))

(defn -main [& args]
  (let [folder (File. "resources/fight_club_data")
        file (File. folder "Archive/Class.xml")
        content (zip/xml-zip (xml/parse (FileInputStream. file)))
        simple (simplify (first content))]
    (println (type content))
    (println (first content))
    (println simple)
    (spit "resources/fight_club_5.json" (json/generate-string simple {:pretty true}))
    ))
