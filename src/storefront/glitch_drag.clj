(ns storefront.glitch-drag
  (:require [storefront.drawing]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def jitter-amount 10)
(def x-blocks 20)
(def y-blocks 10)
(def total-blocks (* x-blocks y-blocks))

(defn index-block [i]
  [(mod i x-blocks) (quot i x-blocks)])

(defn clamp-rgb [rgb]
  (max (min rgb 255) 0))

(defn jitter [max-jitter]
  (fn [d] (+ d (- (rand-int (inc max-jitter)) (/ max-jitter 2)))))

(defn color-walk [color]
  (map clamp-rgb (map (jitter jitter-amount) color)))

(defn random-color []
  [(rand-int 255) (rand-int 255) (rand-int 255)])

(defn rect-at-index [idx color]
  (let [[x-index y-index] (index-block idx)
        width             (/ (q/width) x-blocks)
        height            (/ (q/height) y-blocks)
        x                 (* width x-index)
        y                 (* height y-index)]
    (apply q/fill color)
    (q/rect x y width height)))

(defn setup []
  (q/frame-rate 10)
  { :colors (repeatedly total-blocks random-color) })

(defn update-state [state]
  { :colors (map color-walk (:colors state)) })

(defn draw-state [state]
  (dorun (map-indexed rect-at-index (:colors state))))

(def drawing (Drawing. "Drag Glitch" setup update-state draw-state))
