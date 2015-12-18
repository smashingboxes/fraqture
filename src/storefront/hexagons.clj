(ns storefront.hexagons
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def padding 5)
(def COS_PI_OVER_6 (q/cos (/ q/PI 6)))

(defn hexagon-width [radius]
  (* 2 radius COS_PI_OVER_6))

(defn hexagon-height [radius]
  (* 2 radius))

(defn height-spacing [radius]
  (/ (* 3 radius) 4))

(defn center-point [q r radius]
  (let [height (height-spacing (hexagon-height radius))
        width (hexagon-width radius)
        x-offset (if (even? r) 0 (/ width 2))]
    [(+ x-offset (* q (hexagon-width radius))) (* r height)]))

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
  (q/stroke 100)
  (let [radius 200
        y-count (+ (quot (q/height) (height-spacing (hexagon-height radius))) 1)
        x-count (+ (quot (q/width) (hexagon-width radius)) 1)]
    (doseq [r (range y-count)
            q (range x-count)]
      (let [[x y] (center-point q r radius)]
        (draw-hexagon x y (- radius padding))))))

(defn update-state [state] state)

(defn draw-state [state])

(def drawing (Drawing. "Hexagons" setup update-state draw-state :fullscreen [:keep-on-top :present]))
