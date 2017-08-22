(ns scullery-plateau.rasterize-test
  (:require [clojure.test :refer :all]
            [scullery-plateau.raster :refer :all])
  (:import (java.io FileOutputStream)))

(deftest test-rasterize
  (rasterize :png {} {:tag :svg
                   :attrs {:width "200" :height "300" :xmlns "http://www.w3.org/2000/svg"}
                   :content [{
                              :tag :rect
                              :attrs {:x "50" :y "50" :width "100" :height "200" :stroke "black" :fill "red" :stroke-width "1"}
                              },{
                                 :tag :circle
                                 :attrs {:cx "100" :cy "150" :r "50" :stroke "blue" :fill "yellow" :stroke-width "1"}
                                 }]}
             (FileOutputStream. "resources/test.png")))