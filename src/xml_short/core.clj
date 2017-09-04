(ns xml-short.core
  (:require [clojure.string :as s]))

(defn- name-to-keyword [tag]
  (keyword (str (if (nil? (namespace tag)) "" (str (namespace tag) ":")) (name tag))))

(defn- encode-html-entities [s]
  (s/escape s {\< "&lt;"
               \> "&gt;"
               \& "&amp;"
               \' "&apos;"
               \" "&quot;"}))

(defn x-pand [body]
  (let [bodynext (if (symbol? body) [body] body)
        [tag attrs & content] bodynext
        tag (name-to-keyword tag)
        attrs (if (nil? attrs) {} attrs)
        [content attrs] (if (map? attrs)
                          [content (reduce (fn [out [k v]] (assoc out (name-to-keyword k) v)) {} attrs)]
                          [(into [attrs] content) {}])
        attrs (reduce (fn [out [k v]] (assoc out k (encode-html-entities (str v)))) {} attrs)
        content (if (or (nil? content) (empty? content))
                  content
                  (mapv #(if (or (vector? %) (symbol? %)) (x-pand %) (str %)) content))]
    (reduce (fn [out [k v]] (if (or (empty? v) (nil? v)) out (assoc out k v)))
            {:tag tag}
            {:attrs attrs :content content})))
