(ns storefront.mosaic
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def x-blocks 50)
(def y-blocks 50)

(def block-width #(/ (q/width) x-blocks))
(def block-height #(/ (q/height) y-blocks))

(defn rect-at-index [x-index y-index color]
  (let [x (* (block-width) x-index)
        y (* (block-height) y-index)]
    (q/stroke-weight 1)
    (q/stroke 100 100 100)
    (q/fill color)
    (q/rect x y (block-width) (block-height))))

(defn get-block [image x y]
  (q/get-pixel image x y (block-width) (block-height)))

(defn average-color [image]
  (let [pixels (q/pixels image)
        reds   (map #(q/red %) pixels)
        greens (map #(q/green %) pixels)
        blues  (map #(q/blue %) pixels)]
    (q/color (average reds) (average greens) (average blues))
  ))

(defn setup []
  (let [image-file (random-image-file)
        image  (q/load-image image-file)
        _resized (q/resize image (q/width) (q/height))
        xs     (map #(* % (block-width)) (range x-blocks))
        ys     (map #(* % (block-height)) (range y-blocks))
        grid (map (fn [x] (map (fn [y] (average-color (get-block image x y))) ys)) xs)]
  { :grid grid }))

(defn update-state [state] state)

(defn draw-state [state]
  (dorun
    (map-indexed (fn [x-index column]
      (dorun
        (map-indexed (fn [y-index color]
          (rect-at-index x-index y-index color))
        column)))
      (:grid state))))

(def drawing (Drawing. "Mosaic" setup update-state draw-state :fullscreen []))
