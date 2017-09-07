(ns scullery-plateau.draw)

(defn draw-pixels [size width height palette grid]
  {:tag :svg
   :attrs {:xmlns ""
           :width (str (* size width))
           :height (str (* size height))}
   :content []})