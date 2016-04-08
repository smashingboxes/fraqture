(ns fraqture.photo-countdown
  (:require [fraqture.drawing]
            [quil.core :as q])
  (:import  [fraqture.drawing Drawing]))

(defn setup [options]
  {:background-color [100 100 255]})

(defn update-state [state]
  state)

(defn draw-state [state]
  (apply q/background (:background-color state)))

(def drawing
  (Drawing. "Photo Countdown" setup update-state draw-state nil nil nil))
