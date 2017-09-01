(ns template.core)

(def ^:private default-ops {'entry-set (fn [my-map] (mapv (fn [[k v]] {:k k :v v}) (sorted-map my-map)))})

(defn- resolve-col [col op-map data recurse]
  (let [recursive-call #(resolve-col % op-map data recurse)]
    (cond
      (keyword? col) (data col)
      (list? col) (recurse op-map data [] col)
      (vector? col) (mapv recursive-call col)
      (map? col) (reduce (fn [out [k v]] (assoc out k (recursive-call v))) {} col)
      :else col)))

(defn- label-data [label data]
  (cond
    (vector? data) (mapv (partial label-data label) data)
    (map? data) (reduce (fn [out [k v]] (assoc out (keyword (name label) (name k)) v)) {} data)
    :else (assoc {} label data)))

(defn- resolve-tpl [op-map data out-data tpl]
  (cond
    (map? tpl) (reduce (fn [out [k v]] (assoc out k (resolve-tpl op-map data [] v))) {} tpl)
    (vector? tpl) (let [[tag & attrs-content] tpl
                        attrs-content (filter some? (reduce #(into %1 (let [out (resolve-tpl op-map data out-data %2)]
                                                                        (if (and (vector? out) (every? vector? out)) out [out])))
                                                            []
                                                            attrs-content))
                        attrs (apply merge (filter map? attrs-content))
                        attrs (when-not (nil? attrs) [attrs])
                        content (filter #(not (map? %)) attrs-content)]
                    (reduce #(if (nil? %2) %1 (into %1 %2)) [tag] [attrs content]))
    (list? tpl) (let [[op & args] tpl]
                  (cond
                    (map? op) (let [[label col] (first op)
                                    col (resolve-col col op-map data resolve-tpl)
                                    new-data (label-data label col)
                                    append (fn [out item] (reduce #(into %1 [(resolve-tpl op-map (merge data item) [] %2)]) out args))
                                    output (if (vector? new-data)
                                             (reduce append out-data new-data)
                                             (append out-data new-data))]
                                output)
                    (list? op) (when (resolve-tpl op-map data [] op)
                                 (reduce (partial resolve-tpl op-map data) out-data args))
                    (symbol? op) (let [func (let [temp-func (resolve op)] (if-not (nil? temp-func)
                                                                            temp-func
                                                                            (op-map op)))
                                       params (map (partial resolve-tpl op-map data []) args)]
                                   (apply func params))
                    (keyword? tpl) (tpl data)))
    (keyword? tpl) (tpl data)
    :else tpl))

(defn flatten-output
  ([input] (flatten-output (empty input) input))
  ([output input]
   (let [init (empty input)
         wrap #(conj init %)]
     (vec
       (reduce (fn [out in]
                 (if (coll? in)
                   (if (every? coll? in)
                     (if (= 1 (count in))
                       (concat out (flatten-output in))
                       (reduce #(concat %1 (wrap (flatten-output %2))) out in))
                     (concat out (wrap (map #(if (coll? %) (flatten-output %) %) in))))
                   (concat out (wrap in))))
               output input)))))

(defn build-template-factory [op-map]
  (fn build-template [tpl]
    (fn resolve-template [data]
      (let [resolved (resolve-tpl (merge op-map default-ops) data [] tpl)]
        (flatten-output resolved)))))
