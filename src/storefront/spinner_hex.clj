(ns storefront.spinner-hex
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defrecord Triangle [x1 y1 x2 y2 x3 y3 color])

(defn draw-triangle [triangle x y]
  (let [x1 (+ (:x1 triangle) x)
        y1 (+ (:y1 triangle) y)
        x2 (+ (:x2 triangle) x)
        y2 (+ (:y2 triangle) y)
        x3 (+ (:x3 triangle) x)
        y3 (+ (:y3 triangle) y)]
    (q/fill (:color triangle))
    (q/triangle x1 y1 x2 y2 x3 y3)))

(defn hex-triangles [radius]
  (let [angles (map #(* (/ q/PI 3) %) (range 6))
        spinner-color (q/color 235 23 103) ;; SB pink
        colors  (map #(q/lerp-color (q/color 0 0 0) spinner-color (/ % 6)) (range 6))
        sets  (map-indexed
                  (fn [i angle]
                    (let [next-angle (nth angles (mod (+ i 1) (count angles)))]
                    [angle next-angle (nth colors i)]))
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
              color
            ))
          sets)))

(defn setup []
  { :triangles (hex-triangles 200)})

(defn update-state [state]
  state)

(defn draw-state [state]
  (q/background 0 0 0)
  (q/no-stroke)
  (let [x (/ (q/width) 2)
        y (/ (q/height) 2)]
    (doseq [triangle (:triangles state)]
      (draw-triangle triangle x y))))

(def drawing (Drawing. "Spinner Hex" setup update-state draw-state :fullscreen [:keep-on-top :present]))
