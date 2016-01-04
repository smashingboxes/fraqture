(ns storefront.hex-spinner
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.core.matrix :as m])
  (:import  [storefront.drawing Drawing]))

(defrecord Triangle [x1 y1 x2 y2 x3 y3])

(defn draw-triangle [triangle x y color]
  (let [x1 (+ (:x1 triangle) x)
        y1 (+ (:y1 triangle) y)
        x2 (+ (:x2 triangle) x)
        y2 (+ (:y2 triangle) y)
        x3 (+ (:x3 triangle) x)
        y3 (+ (:y3 triangle) y)]
    (q/fill color)
    (q/triangle x1 y1 x2 y2 x3 y3)))

(defn hex-triangles [radius]
  (let [angles (map #(* (/ q/PI 3) %) (range 6))
        sets  (map-indexed
                  (fn [i angle]
                    (let [next-angle (nth angles (mod (+ i 1) (count angles)))]
                    [angle next-angle]))
                  angles)
        ]
        (map
          #(let [[angle next-angle color] %]
            (Triangle.
              0
              0
              (* (q/sin angle) radius)
              (* (q/cos angle) radius)
              (* (q/sin next-angle) radius)
              (* (q/cos next-angle) radius)
            ))
          (reverse sets))))

(def cli-options
  [
    ["-r" "--radius INT" "Radius of hexagon, in px"
      :default 200
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 1 % 1000) "Must be a number between 1 and 1000"]]
  ])

(defn setup [options]
  { :triangles (hex-triangles (:radius options)) })

(defn update-state [state]
  (update-in state [:triangles] #(m/rotate % 0 1)))

(defn draw-state [state]
  (q/frame-rate 10)
  (q/background 0 0 0)
  (q/no-stroke)
  (let [x (/ (q/width) 2)
        y (/ (q/height) 2)
        spinner-color (q/color 235 23 103) ;; SB pink
        colors (map #(q/lerp-color (q/color 0 0 0) spinner-color (/ % 6)) (range 6))]
    (dorun
      (map (fn [triangle color]
          (draw-triangle triangle x y color))
        (:triangles state)
        colors))))

(def drawing
  (Drawing. "Hex Spinner" setup update-state draw-state cli-options
    { :quil { :size :fullscreen :features [:keep-on-top :present] }}))
