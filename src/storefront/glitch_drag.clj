(ns storefront.glitch-drag
  (:require [storefront.drawing]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def jitter-amount 10)
(def x-blocks 30)
(def y-blocks 30)
(def total-blocks (* x-blocks y-blocks))

(defn index-block [i]
  [(quot i y-blocks) (mod i y-blocks)])

(defn clamp-rgb [rgb]
  (max (min rgb 255) 0))

(defn jitter [max-jitter]
  (fn [d] (+ d (- (rand-int (inc max-jitter)) (/ max-jitter 2)))))

(defn color-walk [color]
  (map clamp-rgb (map (jitter jitter-amount) color)))

(defn random-color []
  [(rand-int 255) (rand-int 255) (rand-int 255)])

(defn cycle-index [idx]
  (mod (inc idx) y-blocks))

(defn rect-at-index [x-index y-index color]
  (let [width             (/ (q/width) x-blocks)
        height            (/ (q/height) y-blocks)
        x                 (* width x-index)
        y                 (* height y-index)]
    (q/no-stroke)
    (apply q/fill color)
    (q/rect x y width height)))

(defrecord Column [current-index color])

(defn setup []
  (q/frame-rate 10)
  (repeatedly
     x-blocks
     (fn [] (->Column (rand-int y-blocks) (random-color)))))

(defn update-column [column]
  (Column. (cycle-index (:current-index column)) (color-walk (:color column))))

(defn update-state [state]
  (map update-column state))

(defn draw-state [state]
  (dorun
    (map-indexed
      (fn [idx column] (rect-at-index idx (:current-index column) (:color column)))
      state)))

(def drawing (Drawing. "Drag Glitch" setup update-state draw-state))
