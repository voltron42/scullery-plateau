(ns scullery-plateau.draw)

(defn draw-pixels [size width height palette grid]
  {:tag :svg
   :attrs {:xmlns "http://www.w3.org/2000/svg"
           :width (str (* size width))
           :height (str (* size height))}
   :content (into
              (if (= (first palette) "none")
                []
                [{:tag :rect
                  :attrs {:width (* size width)
                          :height (* size height)
                          :fill (first palette)}}])
              (mapv (fn [{:keys [x y c]}]
                      {:tag :rect
                       :attrs {:x (* size x)
                               :y (* size y)
                               :width size
                               :height size
                               :fill (palette c)}})
                    grid))})