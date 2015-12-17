(ns storefront.shifting-grid
  (:require [storefront.drawing]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def x-blocks 30)
(def y-blocks 30)
(def block-width #(/ (q/width) x-blocks))
(def block-height #(/ (q/height) y-blocks))
(def total-blocks (* x-blocks y-blocks))

(defn index-block [i]
  [(quot i y-blocks) (mod i y-blocks)])

(defn draw-block [block idx]
  (let [[x-index y-index] (index-block idx)
        x                 (* (block-width) x-index)
        y                 (* (block-height) y-index)]
    (q/image block x y (block-width) (block-height))))

(defn setup []
  (def image (q/load-image "ross.jpg"))
  (q/resize image (q/width) (q/height))
  (let [xs     (map #(* % (block-width)) (range x-blocks))
        ys     (map #(* % (block-height))  (range y-blocks))
        blocks (for [x xs y ys] (q/get-pixel image x y (block-width) (block-height)))]
  { :blocks blocks }))

(defn update-state [state]
  state)

(defn draw-state [state]
  (dorun
    (map-indexed (fn [idx block]
      (draw-block block idx))
    (:blocks state))))

(def drawing (Drawing. "Shifting Grid" setup update-state draw-state))
