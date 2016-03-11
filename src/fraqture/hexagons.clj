(ns fraqture.hexagons
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q])
  (:import  [fraqture.drawing Drawing]))

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

(defn setup [options]
  (q/frame-rate 6)
  (let [radius 100
        y-count (+ (quot (q/height) (height-spacing (hexagon-height radius))) 1)
        x-count (+ (quot (q/width) (hexagon-width radius)) 1)]
    { :size radius :r y-count :q x-count :c-r (rand-int y-count) :c-q (rand-int x-count) }))

(defn update-state [state]
  (-> state
    (assoc :c-r (rand-int (:r state)))
    (assoc :c-q (rand-int (:q state)))))

(defn draw-state [state]
  (q/background 252 248 248)
  (q/stroke-weight 4)
  (q/stroke 100)
  (doseq [r (range (:r state))
          q (range (:q state))]
    (let [[x y] (center-point q r (:size state))]
      (q/no-fill)
      (draw-hexagon x y (- (:size state) padding))))
  (let [[x y] (center-point (:c-q state) (:c-r state) (:size state))]
    (q/fill 235 23 103)
    (draw-hexagon x y (- (:size state) padding))))

(def drawing
  (Drawing. "Hexagons" setup update-state draw-state nil nil nil))
