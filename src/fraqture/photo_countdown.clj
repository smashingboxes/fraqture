(ns fraqture.photo-countdown
  (:require [fraqture.drawing]
            [quil.core :as q]
            [fraqture.led-array :as led])
  (:import  [fraqture.drawing Drawing]))

(defn setup [options]
  {:background-color [0 0 0]
   :time-left 3
   :options options})

(defn update-state [state]
  (update-in state [:time-left] dec))

(def vertical-line-offset 176)

(defn draw-state [state]
  (apply q/background (:background-color state))
  (q/fill 255 255 255)
  (q/text-size 512)
  (q/text-align :center :center)
  (let [options (:options state)
        serial  (:serial options)
        center-width (/ (q/width) 2)
        center-height (/ (q/height) 2)]
    (if (> (:time-left state) 0)
      (q/text (str (:time-left state)) center-width center-height)
      (led/paint-window serial 0 0 led/row-count led/col-count [255 255 255]))
    (q/delay-frame 1000)))

(defn exit?
  [state]
  (< (:time-left state) -1))

(def drawing
  (Drawing. "Photo Countdown" setup update-state draw-state nil exit? nil))
