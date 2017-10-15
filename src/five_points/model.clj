(ns five-points.model
  (:require [schema.core :as s]))

(s/defschema Score
  (s/enum 1 2 3 4 5))

(s/defschema Stat
  (s/enum :Brawn
          :Grace
          :Zeal
          :Wit
          :Sense
          :Charm))

(s/defschema Stats {Stat Score})

(s/defschema Prerequisite
  (s/either s/Str ))

(s/defschema Kin
  {:name s/Str
   :stats Stats
   :description s/Str
   (s/optional-key :starting) {(s/optional-key :skills) #{s/Str}
                               (s/optional-key :skill-sets) #{s/Str}}})

(s/defschema Caste
  {:name s/Str
   :stats Stats
   :description s/Str
   (s/optional-key :starting) {(s/optional-key :skills) #{s/Str}
                               (s/optional-key :skill-sets) #{s/Str}}})

(s/defschema Skill
  {:name s/Str
   :level s/Int
   :description s/Str
   :type [s/Str]
   (s/optional-key :prerequisites) #{Prerequisite}
   (s/optional-key :bonuses) {s/Str Score}})

(s/defschema Skill-Set
  {:name s/Str
   :level s/Int
   :description s/Str
   (s/optional-key :prerequisites) #{Prerequisite}})

(s/defschema Item
  {:name s/Str
   :description s/Str
   :type s/Str
   (s/optional-key :bonuses) {s/Str Score}
   (s/optional-key :heft) (s/enum :heavy :light)
   :cost {:value s/Int
          :unit s/Str}})
