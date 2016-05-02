(ns fraqture.photo-countdown
  (:require [fraqture.drawing]
            [quil.core :as q]
            [fraqture.led-array :as led]
            [clojure.java.shell :as shell])
  (:import  [fraqture.drawing Drawing]))

(defn setup [options]
  {:background-color [0 0 0]
   :time-left 3
   :options options})

(defn update-state [state]
  (update-in state [:time-left] dec))

(def vertical-line-offset 176)

(defn take-picture [holdoff]
  (let [now (System/currentTimeMillis)]
    (shell/sh "imagesnap" "-w" (str holdoff) (str "rasters/once_" now ".jpg"))))

(defn draw-state [state]
  (let [options (:options state)
        serial  (:serial options)
        center-width (/ (q/width) 2)
        center-height (/ (q/height) 2)]
    (apply q/background (:background-color state))
    (if (>= (:time-left state) 0) (q/delay-frame 1000))
    (cond
      (> (:time-left state) 0)
        (do (q/fill 255 255 255)
            (q/text-size 512)
            (q/text-align :center :center)
            (q/text (str (:time-left state)) center-width center-height))
      (= (:time-left state) 0)
        (do (led/paint-window serial 0 0 led/row-count led/col-count [255 255 255])
            (led/refresh serial))
      (= (:time-left state) -1)
        (do (take-picture 1)
            (led/clear serial)
            (led/refresh serial)))))

(defn exit?
  [state]
  (< (:time-left state) -2))

(def drawing
  (Drawing. "Photo Countdown" setup update-state draw-state nil exit? nil))
