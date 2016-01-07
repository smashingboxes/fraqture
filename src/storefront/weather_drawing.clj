(ns storefront.weather-drawing
  (:require [storefront.drawing]
            [storefront.helpers :refer [interpolate]]
            [storefront.weather :refer [weather]]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defn k-to-h [kelvin]
  (- (* kelvin (/ 9 5)) 459.67))

(defn temp-to-color [temperature temp-min temp-max]
  (let [cold-color  (q/color 0 0 255)
        hot-color   (q/color 255 0 0)
        amount      (interpolate temp-min 0 temp-max 1 temperature)
        amt-clamped (max 0 (min amount 1))]
    (q/lerp-color cold-color hot-color amt-clamped)))

(defn wind-to-rotation [wind-direction]
  (if (= wind-direction nil)
    0
    (q/radians wind-direction)))

(defn wind-to-size [wind-speed]
  (+ 100 (* 30 wind-speed)))

(defn setup [options]
  (q/frame-rate 1)
  (weather "Durham" "NC"))

(defn update-state [state] state)

(defn draw-state [state]
  (let [temperature (:temp (:main @state))
        temp-min    (:temp_min (:main @state))
        temp-max    (:temp_max (:main @state))
        wind-speed  (:speed (:wind @state))
        wind-dir    (:deg (:wind @state))]
    (q/background (temp-to-color temperature temp-min temp-max))
    (q/no-stroke)
    (q/push-matrix)
    (q/translate (/ (q/width) 2) (/ (q/height) 2))
    (q/scale (wind-to-size wind-speed))
    (q/rotate (wind-to-rotation wind-dir))
    (q/fill 255)
    (q/triangle -0.5 1 0.5 1 0 -1)
    (q/pop-matrix)))

(def drawing
  (Drawing. "The Weather" setup update-state draw-state nil nil))
