(ns five-points.validate-test
  (:require [clojure.test :refer :all]
            [five-points.validate :as v]))

(deftest test-validate-ids
  (is (= {:compendium {} :errors {:ids {}}} (v/validate-ids {:compendium {} :errors {}}))))

(deftest test-validate-unique-names
  (is (= {:compendium {} :errors {:unique-names {}}} (v/validate-unique-names {:compendium {} :errors {}}))))

