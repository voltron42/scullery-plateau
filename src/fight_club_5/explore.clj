(ns fight-club-5.explore
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File FileInputStream)))

(defn- update-map-with [my-map my-key my-value]
  (let [current-value (get my-map my-key)
        value (if (empty? current-value)
                my-value
                (if (or (list? current-value) (vector? current-value))
                  (if (or (list? my-value) (vector? my-value))
                    (vec (concat current-value my-value))
                    (conj (vec current-value) my-value))
                  (if (or (list? my-value) (vector? my-value))
                    (vec (concat (vector current-value) my-value))
                    (vector current-value my-value))))]
    (assoc my-map my-key value)))

(defn- simplify-recursive [out {:keys [tag attrs content]}]
  (let [content (if (empty? content)
                  ""
                  (if (and (= 1 (count content)) (string? (first content)))
                    (first content)
                    (reduce simplify-recursive {} content)))
        new-value (if (empty? attrs) content (assoc attrs ::content content))]
    (update-map-with out tag new-value)
    ))

(defn simplify [node]
  (simplify-recursive {} node))

(defn -main1 [& args]
  (let [folder (File. "resources/fight_club_data")
        file (File. folder "Archive/Class.xml")
        content (zip/xml-zip (xml/parse (FileInputStream. file)))
        simple (simplify (first content))]
    (spit "resources/fight_club_5.json" (json/generate-string simple {:pretty true}))))

(defn -main [& args]
  (let [filelist
        (filter
          #(str/ends-with? % ".xml")
          (map
            #(.getAbsolutePath %)
            (file-seq (io/file "resources/fight_club_data"))))]
    (reduce (fn [_ f]
              (println f))
            nil
            filelist)
    (spit "resources/compendium.json"
          (json/generate-string
            (reduce
              (fn [out ^String f]
                (println f)
                (let [content (-> f
                                  (File.)
                                  (FileInputStream.)
                                  (xml/parse)
                                  (zip/xml-zip))
                      _ (println "content: " content)
                      simple (-> content
                                 (first)
                                 (simplify)
                                 (:compendium)
                                 (::content))
                      _ (println "simple: " simple)
                      reduced (reduce-kv update-map-with out simple)
                      _ (println "reduced: " reduce)]
                  reduced))
              {}
              filelist)
            {:pretty true}))))
