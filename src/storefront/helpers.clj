(ns storefront.helpers
  (:require [quil.core :as q]))
            ;; [clojure.core.matrix :as m]))

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

; sine wave pulse between a hight and low at x rate
; returns an Integer
(defn pulse [low high rate]
  (let [diff (- high low)
        half (/ diff 2)
        mid (+ low half)
        s (/ (q/millis) 1000.0)
        x (q/sin (* s (/ 1.0 rate)))]
    (+ mid (* x half))))

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
        reds   (map q/red pixels)
        greens (map q/green pixels)
        blues  (map q/blue pixels)]
    (q/color (average reds) (average greens) (average blues))))

; From: https://github.com/mikera/clojure-utils/blob/master/src/main/clojure/mikera/cljutils/loops.clj
(defmacro doseq-indexed
  "loops over a set of values, binding index-sym to the 0-based index of each value"
  ([[val-sym values index-sym] & code]
  `(loop [vals# (seq ~values)
          ~index-sym (long 0)]
     (if vals#
       (let [~val-sym (first vals#)]
             ~@code
             (recur (next vals#) (inc ~index-sym)))
       nil))))
