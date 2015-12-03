(ns storefront.scanlines
  (:require [quil.core :as q]))

(defn horizontals
  [line-height]
  (range 0 (q/height) (* line-height 2)))

(defn rects
  [line-height]
  (doseq [y (horizontals line-height)]
    (q/rect 0 y (q/width) line-height)))

(defn draw-scanlines
  [thickness opacity-float]
  (q/no-stroke)
  (q/fill 0 (* opacity-float 255))
  (rects thickness))