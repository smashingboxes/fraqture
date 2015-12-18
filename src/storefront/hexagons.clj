(ns storefront.hexagons
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defn draw-hexagon
  [x y radius]
  (q/push-matrix)
  (q/translate x y)
  (q/begin-shape)
  (doseq [angle (map #(* (/ q/PI 3) %) (range 6))]
    (q/push-matrix)
    (q/vertex (* (q/sin angle) radius) (* (q/cos angle) radius))
    (q/pop-matrix))
  (q/end-shape :close)
  (q/pop-matrix))

(defn setup []
  (q/background 252 248 248)
  (q/no-fill)
  (q/stroke-weight 4)
  (q/stroke 50)
  (draw-hexagon (/ (q/width) 2) (/ (q/height) 2) 100))

(defn update-state [state])

(defn draw-state [state])

(def drawing (Drawing. "Hexagons" setup update-state draw-state :fullscreen [:keep-on-top :present]))
