(ns storefront.hexagons
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def padding 5)
(def COS_PI_OVER_6 (q/cos (/ q/PI 6)))

(defn distance-between
  [radius]
  (* 2 (+ radius padding) COS_PI_OVER_6))

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
  (let [x-separation (distance-between 100)
        y-separation (* COS_PI_OVER_6 x-separation)
        y-count    (+ (quot (q/height) y-separation) 2)
        x-count    (+ (quot (q/width) x-separation) 2)]
    (doseq [y (range y-count)
            x (range x-count)]
      (let [offset (if (even? y) (/ x-separation 2) 0)]
        (draw-hexagon (+ offset (* x x-separation)) (* y y-separation) 100)))))

(defn update-state [state])

(defn draw-state [state])

(def drawing (Drawing. "Hexagons" setup update-state draw-state :fullscreen [:keep-on-top :present]))
