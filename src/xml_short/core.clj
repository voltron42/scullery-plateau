(ns xml-short.core)

(defn- name-to-keyword [tag]
  (keyword (str (if (nil? (namespace tag)) "" (str (namespace tag) ":")) (name tag))))

(defn x-pand [body]
  (let [bodynext (if (symbol? body) [body] body)
        [tag attrs & content] bodynext
        tag (name-to-keyword tag)
        attrs (if (nil? attrs) {} attrs)
        [content attrs] (if (map? attrs)
                          [content (reduce (fn [out [k v]] (assoc out (name-to-keyword k) v)) {} attrs)]
                          [(into [attrs] content) {}])
        content (if (or (nil? content) (empty? content))
                  content
                  (mapv #(if (or (vector? %) (symbol? %)) (x-pand %) %) content))]
    (reduce (fn [out [k v]] (if (or (empty? v) (nil? v)) out (assoc out k v)))
            {:tag tag}
            {:attrs attrs :content content})))
