(ns storefront.spinner-hex
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defrecord Triangle [x1 y1 x2 y2 x3 y3])

(defn draw-triangle [triangle x y]
  (let [coords (map-indexed (fn [i val] (if (even? i) (+ val x) (+ val y))) (vals triangle))]
    (apply q/triangle coords)))

(defn hex-triangles [radius]
  (let [angles (map #(* (/ q/PI 3) %) (range 6))
        pairs  (map-indexed
                  (fn [i angle]
                    (let [next-angle (nth angles (mod (+ i 1) (count angles)))]
                    [angle next-angle]))
                  angles)]
        (map
          #(let [[angle next-angle] %]
            (Triangle.
              0
              0
              (* (q/sin angle) radius)
              (* (q/cos angle) radius)
              (* (q/sin next-angle) radius)
              (* (q/cos next-angle) radius)
            ))
          pairs)))

(defn setup []
  { :triangles (hex-triangles 200)})

(defn update-state [state]
  state)

(defn draw-state [state]
  (q/background 0 0 0)
  (q/stroke-weight 1)
  (q/stroke 255 255 255)
  (let [x (/ (q/width) 2)
        y (/ (q/height) 2)]
    (doseq [triangle (:triangles state)]
      (draw-triangle triangle x y))))

(def drawing (Drawing. "Spinner Hex" setup update-state draw-state :fullscreen [:keep-on-top :present]))
