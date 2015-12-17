(ns storefront.shifting-grid
  (:require [storefront.drawing]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def x-blocks 30)
(def y-blocks 30)
(def block-width #(/ (q/width) x-blocks))
(def block-height #(/ (q/height) y-blocks))
(def total-blocks (* x-blocks y-blocks))

(defn draw-block [block x-index y-index]
  (let [x                 (* (block-width) x-index)
        y                 (* (block-height) y-index)]
    (q/rect x y (block-width) (block-height))
    (q/image block x y (block-width) (block-height))))

(defn get-block [image x y]
  (q/get-pixel image x y (block-width) (block-height)))

(defn setup []
  (def image (q/load-image "images/ross.jpg"))
  (q/resize image (q/width) (q/height))
  (let [xs     (map #(* % (block-width)) (range x-blocks))
        ys     (map #(* % (block-height))  (range y-blocks))
        blocks (map (fn [x] (map (fn [y] (get-block image x y)) ys)) xs)]
  { :blocks blocks }))

(defn update-state [state]
  (comment "Need to fill this in to rotate a random column or row")
  state)

(defn draw-state [state]
  (dorun
    (map-indexed (fn [x-index column]
      (dorun
        (map-indexed (fn [y-index block]
          (draw-block block x-index y-index))
        column)))
    (:blocks state))))

(def drawing (Drawing. "Shifting Grid" setup update-state draw-state))
