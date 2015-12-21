(ns storefront.spinner-hex
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defn draw-tri-hex [x y radius spacing]
  (q/push-matrix)
  (q/translate x y)
  ;; (doseq [angle (map #(* (/ q/PI 3) %) (range 6))]
    ;; (q/push-matrix)
    ;; (q/vertex (* (q/sin angle) radius) (* (q/cos angle) radius))
    ;; (q/pop-matrix))
  (let [angles (map #(* (/ q/PI 3) %) (range 6))
        pairs  (map-indexed
                  (fn [i angle]
                    (let [next-angle (nth angles (mod (+ i 1) (count angles)))]
                    [angle next-angle]))
                  angles)]
        (doseq [pair pairs]
          (let [angle      (get pair 0)
                next-angle (get pair 1)]
            (q/begin-shape)
            (q/push-matrix)
            (q/vertex 0 0)
            (q/vertex (* (q/sin angle) radius) (* (q/cos angle) radius))
            (q/vertex (* (q/sin next-angle) radius) (* (q/cos next-angle) radius))
            (q/pop-matrix)
            (q/end-shape :close)
          )
        )
  )

  (q/pop-matrix))


(defn setup [] )

(defn update-state [state] )

(defn draw-state [state]
  (q/background 0 0 0)
  (q/stroke-weight 1)
  (q/stroke 255 255 255)
  (draw-tri-hex (/ (q/width) 2) (/ (q/height) 2) 200 0))

(def drawing (Drawing. "Spinner Hex" setup update-state draw-state :fullscreen [:keep-on-top :present]))
