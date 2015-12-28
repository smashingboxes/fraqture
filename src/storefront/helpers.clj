(ns storefront.helpers
  (:require [quil.core :as q]
            [clojure.core.matrix :as m]))

; Milliseconds to seconds
(defn seconds [x] (* 1000 x))

; Milliseconds elapsed since
(defn time-elapsed [last-time]
  (- (q/millis) last-time))

; Random in range
(defn rand-in-range [low high]
  (+ (rand-int (- high low)) low))

; Accepted file extensions
(def image-extensions [".png" ".jpg" ".jpeg"])
(defn valid-image [file]
  (some true? (map #(.endsWith (.getName file) %) image-extensions)))

; Get a random file, except the given file
(defn random-image-file
  [& {:keys [except]
      :or {except #{}}}]
  (let [directory      (clojure.java.io/file "./images")
        files          (file-seq directory)
        image-files    (filter valid-image files)
        included-files (apply list (apply disj (set image-files) except))]
    (rand-nth included-files)))

; Average: sum / count
(defn average [numbers]
  (if (= (count numbers) 0)
    0
    (/ (reduce + numbers) (count numbers))))

; Exponent
(defn exp [x n]
  (reduce * (repeat n x)))

; Simple linear interpolation
(defn interpolate [x1 y1 x2 y2 x]
  (+ y1 (* (- y2 y1) (/ (- x x1) (- x2 x1)))))

(defn clamp [value min-val max-val]
  (max (min max-val value) min-val))

(defn gridize
  ([rows columns] (gridize rows columns (q/width) (q/height)))
  ([rows columns width height]
    (let [row-height (/ height rows)
          col-width  (/ width columns)
          ys         (map (partial * row-height) (range rows))
          xs         (map (partial * col-width) (range columns))
          x-ends     (map #(+ (- col-width 1) %) xs)
          y-ends     (map #(+ (- row-height 1) %) ys)]
      { :row-height row-height
        :col-width  col-width
        :xs         xs
        :ys         ys
        :x-ends     x-ends
        :y-ends     y-ends })))

(defn average-color [image]
  (let [pixels (q/pixels image)
        reds   (map q/red  pixels)
        greens (map q/green pixels)
        blues  (map q/blue pixels)]
    (q/color (average reds) (average greens) (average blues))))
