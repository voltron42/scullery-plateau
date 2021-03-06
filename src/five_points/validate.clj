(ns five-points.validate)

(defn validate-ids [{:keys [errors compendium]}]
  {:compendium compendium
   :errors (assoc errors
             :ids
             (reduce-kv (fn [out k v]
                          (let [id-match (map (fn [[k v]]
                                                {:ref-id k
                                                 :obj-id (:id v)})
                                              (filter (fn [[k v]]
                                                        (not= k (:id v)))
                                                      v))]
                            (if (empty? id-match)
                              out
                              (assoc out k {:id-match id-match}))))
                        {} compendium))})

(defn validate-unique-names [{:keys [errors compendium]}]
  (let [names (reduce-kv (fn [out type v]
                           (concat out (mapv (fn [[obj-name freq]]
                                               {:type type
                                                :name obj-name
                                                :freq freq})
                                             (frequencies (mapv :name v)))))
                         [] compendium)
        groups (into {}
                     (filter (fn [[obj-name group]]
                               (or (< 1 (count group))
                                   (and (= 1 (count group))
                                        (let [{:keys [freq]} (first group)]
                                          (< 1 freq)))))
                             (group-by :name names)))]
    {:compendium compendium
     :errors (assoc errors
               :unique-names
               (reduce-kv (fn [out name group]
                            (assoc out name
                                       (reduce-kv (fn [out type [{:keys [freq]}]]
                                                    (assoc out type freq)) {} (group-by :type group))))
                          {} groups))}))

(defn validate-foreign-keys [type field-ids thru]
  thru)

(defn validate-prerequisites [type skill-ids skill-set-ids thru]
  thru)

(defn validate-starting [type skill-ids skill-set-ids item-ids thru]
  thru)

(defn validate-compendium [{:keys [kin castes items skills skill-sets characters] :as compendium}]
  (let [kin-ids (keys kin)
        caste-ids (keys castes)
        item-ids (keys items)
        skill-ids (keys skills)
        skill-set-ids (keys skill-sets)
        {:keys [errors]} (->> {:compendium compendium
                               :errors {}}
                    (validate-ids)
                    (validate-unique-names)
                    (validate-foreign-keys :characters {:kin kin-ids
                                                        :caste caste-ids
                                                        :skill-sets skill-set-ids
                                                        :skills skill-ids
                                                        :items item-ids})
                    (validate-prerequisites :skill-sets skill-ids skill-set-ids)
                    (validate-prerequisites :skills skill-ids skill-set-ids)
                    (validate-starting :kin skill-ids skill-set-ids item-ids)
                    (validate-starting :castes skill-ids skill-set-ids item-ids)
                    (filter (fn [[k v]] (not-empty v)))
                    (into {}))
        is-valid (empty? errors)]
    (when-not is-valid
      (println errors))
    is-valid))
