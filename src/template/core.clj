(ns template.core)

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
                                    col (data col)
                                    results (reduce (fn [out elem]
                                                      (let [new-data (if (map? elem)
                                                                       (reduce (fn [obj [k v]]
                                                                                 (assoc obj (keyword (name label) (name k)) v))
                                                                               data elem)
                                                                       (assoc data label elem))]
                                                        (reduce #(into %1 [(resolve-tpl op-map new-data [] %2)]) out args)))
                                                    out-data col)]
                                results)
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

(defn build-template-factory [op-map]
  (fn build-template [tpl]
    (fn resolve-template [data]
      (resolve-tpl op-map data [] tpl))))
