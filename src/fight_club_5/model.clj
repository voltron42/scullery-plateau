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

(s/defschema Background
  {:name s/Str
   :proficiency #{Skill}
   :traits {Trait-Name [Trait-Text]}})

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

(s/defschema Class
  {:name s/Str
   :hd s/Int
   :proficiency #{(s/either Skill Ability)}
   (s/optional-key :spell-ability) Ability
   :autolevel Autolevel})

(s/defschema Race
  {})

(s/defschema Compendium
  {:spells [Spell]
   :backgrounds [Background]
   :feats [Feat]
   :classes [Class]
   :races [Race]
   })