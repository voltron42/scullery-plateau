(ns five-points.model
  (:require [schema.core :as s]
            [five-points.validate :as v])
  (:import (javafx.scene.effect Effect)
           (cljpdf.text Chapter)))

(s/defschema Kin-Id (s/constrained s/Str #(re-matches #"kin_[A-Za-z0-9]{10}" %) 'Kin-Id))

(s/defschema Caste-Id (s/constrained s/Str #(re-matches #"caste_[A-Za-z0-9]{10}" %) 'Caste-Id))

(s/defschema Skill-Id (s/constrained s/Str #(re-matches #"skill_[A-Za-z0-9]{10}" %) 'Skill-Id))

(s/defschema Skill-Set-Id (s/constrained s/Str #(re-matches #"skst_[A-Za-z0-9]{10}" %) 'Skill-Set-Id))

(s/defschema Item-Id (s/constrained s/Str #(re-matches #"item_[A-Za-z0-9]{10}" %) 'Item-Id))

(s/defschema Char-Id (s/constrained s/Str #(re-matches #"char_[A-Za-z0-9]{10}" %) 'Char-Id))

(s/defschema Check-Id (s/constrained s/Str #(re-matches #"chk_[A-Za-z0-9]{10}" %) 'Check-Id))

(s/defschema Damage-Id (s/constrained s/Str #(re-matches #"dmg_[A-Za-z0-9]{10}" %) 'Damage-Id))

(s/defschema Condition-Id (s/constrained s/Str #(re-matches #"cond_[A-Za-z0-9]{10}" %) 'Condition-Id))

(s/defschema Obstacle-Id (s/constrained s/Str #(re-matches #"obst_[A-Za-z0-9]{10}" %) 'Obstacle-Id))

(s/defschema Maze-Id (s/constrained s/Str #(re-matches #"maze_[A-Za-z0-9]{10}" %) 'Maze-Id))

(s/defschema Room-Id (s/constrained s/Str #(re-matches #"room_[A-Za-z0-9]{10}" %) 'Room-Id))

(s/defschema NPC-Id (s/constrained s/Str #(re-matches #"npc_[A-Za-z0-9]{10}" %) 'NPC-Id))

(s/defschema Question-Id (s/constrained s/Str #(re-matches #"q_[A-Za-z0-9]{10}" %) 'Question-Id))

(s/defschema GameTime {:value s/Int
                       :unit (s/enum :Minor-Action :Major-Action :Minute :Hour :Day :Month :Year)})

(s/defschema Score
  (s/enum 1 2 3 4 5))

(s/defschema Stat
  (s/enum :Brawn
          :Grace
          :Zeal
          :Wit
          :Sense
          :Charm))

(s/defschema Custom-Check
  {:id Check-Id
   :name s/Str
   :description s/Str
   :stat Stat})

(s/defschema Check (s/enum
                     :Push :Pull :Jump :Catch :Throw :Run :Climb :Grab :Hold :Block :Melee-Attack
                     :Balance :Tumble :Dodge :Parry :Range-Attack :Hide
                     :Resist :Heal-Self
                     :Find :Recall :Learn :Triage :Cure :Aim
                     :Look :Smell :Taste :Touch :Hear :Detect-Psychic
                     :Inspire :Sway :Mock :Tame :Lie))

(s/defschema Stats {Stat Score})

(s/defschema Impact (s/enum :immune :guarded :exposed))

(s/defschema Size (s/enum :tiny :small :medium :large :giant :huge :gargantuan))

(s/defschema Susceptability
  {:types #{Damage-Id}
   :impact Impact})

(s/defschema Limitation
  (s/cond-pre
    Condition-Id
    Stats
    {(s/enum :edge :burden) (s/cond-pre Check Stat Check-Id)}
    Susceptability))

(s/defschema Condition
  {:id Condition-Id
   :name s/Str
   :description s/Str
   :limitations #{Limitation}})

(s/defschema Prerequisite
  (s/constrained
    {(s/optional-key :kin) #{Kin-Id}
     (s/optional-key :caste) #{Caste-Id}
     (s/optional-key :skill-sets) #{Skill-Set-Id}
     (s/optional-key :skills) #{Skill-Id}}
    not-empty))

(s/defschema All-Checks (s/cond-pre Check Stat Check-Id))

(s/defschema Effect-Check-Value
  {:score (s/cond-pre Score (s/enum :talent :mastery))
   (s/optional-key :multiplier) (s/cond-pre s/Int :level)})

(comment "
Effects:
Apply Damage
Remove Damage
Apply Edge / Burden / Bonus for check(s)
Apply guard / exposure / immunity for certain damage type(s)
Alter movement *2 / *1/2 / +/-X / 0
Apply Condition
Extra Action
Extra Attack in action
Off-hand Attack as bonus-action

")

(s/defschema Effect
  {:description s/Str
   (s/optional-key :lvl) s/Int
   (s/optional-key :invocation) GameTime
   (s/optional-key :duration) GameTime
   (s/optional-key :frequency) GameTime
   :area {:size s/Int
          :shape (s/enum :Line :Cone :Cube :Column)
          :targets }
   :range (s/cond-pre :touch s/Int)
   :target {(s/enum :count :lvl-) s/Int
            :values #{(s/enum :self :ally :opponent)}}
   :effect (s/cond-pre
             {Condition-Id {:check All-Checks
                            :mark s/Int}}
             {(s/enum :edge :burden) All-Checks}
             {(s/cond-pre All-Checks Damage-Id) Effect-Check-Value})})

(s/defschema Damage-Type
  {:id Damage-Id
   :name s/Str
   :critical-effect Effect
   :description s/Str})

(s/defschema Starting
  (s/constrained
    {(s/optional-key :skills) #{Skill-Id}
     (s/optional-key :skill-sets) #{Skill-Set-Id}
     (s/optional-key :items) #{Item-Id}}
    not-empty))

(s/defschema Kin
  {:id Kin-Id
   :name s/Str
   :stats Stats
   :description s/Str
   (s/optional-key :size) Size
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
   (s/optional-key :lvl) s/Int
   (s/optional-key :prerequisites) Prerequisite
   (s/optional-key :effects) #{Effect}})

(s/defschema Skill-Set
  {:id Skill-Set-Id
   :name s/Str
   :description s/Str
   (s/optional-key :lvl) s/Int
   (s/optional-key :prerequisites) Prerequisite})

(s/defschema Item
  {:id Item-Id
   :name s/Str
   :description s/Str
   :type s/Str
   (s/optional-key :lvl) s/Int
   (s/optional-key :bonded) (s/eq true)
   (s/optional-key :prerequisites) Prerequisite
   (s/optional-key :effects) #{Effect}
   (s/optional-key :heft) (s/enum :heavy :light)
   (s/optional-key :time-to-equip) GameTime
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

(s/defschema Solution
  {:check All-Checks
   :mark s/Int
   (s/enum :succeed :fail) Effect})

(s/defschema Obstacle
  {:id Obstacle-Id
   :name s/Str
   :description s/Str
   :effects #{Effect}
   :solutions #{Solution}})

(s/defschema Context
  (s/constrained
    {:checks {Check-Id Custom-Check}
     :damage-types {Damage-Id Damage-Type}
     :conditions {Condition-Id Condition}
     :kin {Kin-Id Kin}
     :castes {Caste-Id Caste}
     :items {Item-Id Item}
     :skills {Skill-Id Skill}
     :skill-sets {Skill-Set-Id Skill-Set}
     :characters {Char-Id Character}
     :obstacles {Obstacle-Id Obstacle}}
    v/validate-compendium
    'Compendium))

(s/defschema Answer
  {:text s/Str
   :next #{Question-Id}})

(s/defschema Question-Node
{:id Question-Id
 :question s/Str
 :answer (s/cond-pre
           Answer
           {:checks #{{:check All-Checks
                       :mark s/Int}}
            :fail Answer
            :succeed Answer})})

(s/defschema NPC
  {:character Char-Id
   :questions {Question-Id Question-Node}
   :base-line #{Question-Id}})

(s/defschema Position
  (s/cond-pre
    {:x s/Int :y s/Int}
    {:angle s/Int}))

(s/defschema Door
  {:label s/Str
   :description s/Str
   :width (s/enum 1 2)
   :to Room-Id
   :out Position
   (s/optional-key :obstacles) #{Obstacle-Id}})

(s/defschema Room
  {:id Room-Id
   :name s/Str
   :description s/Str
   :shape (s/cond-pre
            {:width s/Int :height s/Int}
            {:points [Position]}
            {:radius s/Int}
            {:radius-x s/Int :radius-y s/Int :angle s/Int})
   (s/optional-key :doors) #{Door}
   (s/optional-key :contents){Position (s/cond-pre Char-Id NPC-Id Obstacle-Id Item-Id)}})

(s/defschema Maze
  {:rooms {Room-Id Room}
   :entrance Room-Id
   :exit Room-Id})

(s/defschema Chapter
  {:intro s/Str
   :outro s/Str
   :maze Maze-Id})

(s/defschema Story
  {:title s/Str
   :prologue s/Str
   :epilogue s/Str
   :chapters [Chapter]})

(s/defschema Adventure
  {:context Context
   :story Story
   :mazes {Maze-Id Maze}
   :NPCs {NPC-Id NPC}})