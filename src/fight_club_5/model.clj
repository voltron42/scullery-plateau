(ns fight-club-5.model
  (:require [schema.core :as s]))

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
   :classes [Class-Name]
   :text [Spell-Description]
   :roll [Roll]})

(s/defschema Compendium
  {:spells [Spell]})