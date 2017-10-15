(ns fight-club-5.model
  (:require [schema.core :as s])
  (:import (javafx.scene.layout Background)
           (org.apache.batik.svggen.font.table Feature)
           (java.lang.reflect Modifier)))

(s/defschema Ability (s/enum :Strength :Dexterity :Constitution :Wisdom :Intelligence :Charisma))

(s/defschema Skill (s/enum :Insight :Religion :Deception :Sleight-of-Hand ))

(s/defschema Casting-Time s/Str)

(s/defschema Game-Range s/Str)

(s/defschema Spell-Component s/Str)

(s/defschema Effect-Duration s/Str)

(s/defschema Class-Name s/Str)

(s/defschema Spell-Description s/Str)

(s/defschema Roll s/Str)

(s/defschema Magic-School (s/enum :C))

(s/defschema Spell
  {:name s/Str
   :level s/Int
   :school Magic-School
   :time Casting-Time
   :range Game-Range
   :components #{Spell-Component}
   :duration Effect-Duration
   :classes #{Class-Name}
   :text [Spell-Description]
   :roll [Roll]})

(s/defschema Trait-Name s/Str)

(s/defschema Trait-Text s/Str)

(s/defschema Traits {Trait-Name [Trait-Text]})

(s/defschema Background
  {:name s/Str
   :proficiency #{Skill}
   :traits Traits})

(s/defschema Modifier
  {:category s/Str
   :description s/Str})

(s/defschema Feat
  {:name        s/Str
   :description [s/Str]
   :modifier    Modifier
   :prerequisite s/Str})

(s/defschema Feature
  {:name s/Str
   :description [s/Str]
   :optional s/Bool})

(s/defschema Autolevel
  {:level s/Int
   :features [Feature]
   :slots [s/Int]})

(s/defschema Proficiency #{(s/either Skill Ability)})

(s/defschema Class
  {:name s/Str
   :hd s/Int
   :proficiency Proficiency
   (s/optional-key :spell-ability) Ability
   :autolevel Autolevel})

(s/defschema Size (s/enum :M :L))

(s/defschema Ability-Bonus
  {:ability Ability
   :bonus s/Int})

(s/defschema Speed s/Str)

(s/defschema Race
  {:name s/Str
   :size Size
   :speed Speed
   :ability-bonuses #{Ability-Bonus}
   (s/optional-key :proficiency) Proficiency
   :traits Traits})

(s/defschema NPC-Type s/Str)

(s/defschema Alignment
  (s/enum "neutral"
          "neutral good"
          "neutral evil"
          "chaotic neutral"
          "lawful neutral"
          "chaotic good"
          "chaotic evil"
          "lawful good"
          "lawful evil"))

(s/defschema Roll
  {:number-of s/Int
   :side-count s/Int
   :bonus s/Int})

(s/defschema HP
  {:default s/Int
   :custom Roll})

(s/defschema Language
  (s/enum "Auran"
          "Aarakocra"

          ))

(s/defschema CR s/Num)

(s/defschema NPC-Action
  {:name   s/Str
   :text   [s/Str]
   :attack {:name   s/Str
            :bonus  s/Int
            :damage Roll}})

(s/defschema NPC
  {:name s/Str
   :size Size
   :type NPC-Type
   :alignment Alignment
   :ac s/Int
   :hp HP
   :speed Speed
   :stats {Ability s/Int}
   :saves {Ability s/Int}
   :skills {Skill s/Int}
   :senses s/Str
   :passive s/Int
   :languages #{Language}
   :cr CR
   :traits #{NPC-Action}
   :action #{NPC-Action}
   :legendary #{NPC-Action}
   })

(s/defschema Compendium
  {:spells [Spell]
   :backgrounds [Background]
   :feats [Feat]
   :classes [Class]
   :races [Race]
   :npcs [NPC]})