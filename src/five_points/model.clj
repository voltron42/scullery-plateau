(ns five-points.model
  (:require [schema.core :as s]
            [five-points.validate :as v]))

(s/defschema Kin-Id (s/constrained s/Str #(re-matches #"kin[A-Za-z0-9]{10}" %) 'Kin-Id))

(s/defschema Caste-Id (s/constrained s/Str #(re-matches #"caste[A-Za-z0-9]{10}" %) 'Caste-Id))

(s/defschema Skill-Id (s/constrained s/Str #(re-matches #"skill[A-Za-z0-9]{10}" %) 'Skill-Id))

(s/defschema Skill-Set-Id (s/constrained s/Str #(re-matches #"skst[A-Za-z0-9]{10}" %) 'Skill-Set-Id))

(s/defschema Item-Id (s/constrained s/Str #(re-matches #"item[A-Za-z0-9]{10}" %) 'Item-Id))

(s/defschema Char-Id (s/constrained s/Str #(re-matches #"char[A-Za-z0-9]{10}" %) 'Char-Id))

(s/defschema Score
  (s/enum 1 2 3 4 5))

(s/defschema Stat
  (s/enum :Brawn
          :Grace
          :Zeal
          :Wit
          :Sense
          :Charm))

(s/defschema Check (s/enum
                     :Push :Pull :Jump :Catch :Throw :Run :Climb :Grab :Hold :Block :Melee-Attack
                     :Balance :Tumble :Dodge :Parry :Range-Attack :Hide
                     :Resist-Attack :Resist-Poison :Resist-Disease :Heal-Self
                     :Find :Recall :Learn :Resist-Psychic :Detect-Poison :Detect-Disease :Triage :Cure
                     :Look :Smell :Taste :Touch :Hear :Detect-Psychic
                     :Inspire :Sway :Mock :Tame :Lie))

(s/defschema Stats {Stat Score})

(s/defschema Prerequisite
  (s/constrained
    {(s/optional-key :min-lvl) s/Int
     (s/optional-key :skill-sets) #{Skill-Set-Id}
     (s/optional-key :skills) #{Skill-Id}}
    not-empty))

(s/defschema Bonus
  {:check (s/either Check Stat)
   :value (s/either Score :talent {:talent s/Int} {:level Score} {:talent :level})
   (s/optional-key :min-lvl) s/Int})

(s/defschema Starting {(s/optional-key :skills) #{Skill-Id}
                       (s/optional-key :skill-sets) #{Skill-Set-Id}
                       (s/optional-key :items) #{Item-Id}})

(s/defschema Kin
  {:id Kin-Id
   :name s/Str
   :stats Stats
   :description s/Str
   (s/optional-key :starting) Starting})

(s/defschema Caste
  {:id Caste-Id
   :name s/Str
   :stats Stats
   :description s/Str
   (s/optional-key :starting) Starting})

(s/defschema Skill
  {:id Skill-Id
   :name s/Str
   :description s/Str
   :type [s/Str]
   (s/optional-key :min-lvl) s/Int
   (s/optional-key :prerequisites) #{Prerequisite}
   (s/optional-key :bonuses) #{Bonus}})

(s/defschema Skill-Set
  {:id Skill-Set-Id
   :name s/Str
   :description s/Str
   (s/optional-key :min-lvl) s/Int
   (s/optional-key :prerequisites) #{Prerequisite}})

(s/defschema Item
  {:id Item-Id
   :name s/Str
   :description s/Str
   :type s/Str
   (s/optional-key :min-lvl) s/Int
   (s/optional-key :prerequisites) #{Prerequisite}
   (s/optional-key :bonuses) #{Bonus}
   (s/optional-key :heft) (s/enum :heavy :light)
   :cost {:value s/Int :unit s/Str}})

(s/defschema Character
  {:id Char-Id
   :name s/Str
   :level s/Int
   :kin Kin-Id
   :caste Caste-Id
   :skill-sets #{Skill-Set-Id}
   :skills #{Skill-Id}
   :items #{Item-Id}})

(s/defschema Context
  (s/constrained
    {:kin {Kin-Id Kin}
     :castes {Caste-Id Caste-Id}
     :items {Item-Id Item}
     :skills {Skill-Id Skill}
     :skill-sets {Skill-Set-Id Skill-Set}
     :characters {Char-Id Character}}
    v/validate-compendium
    'Compendium))
