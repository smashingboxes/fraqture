(ns fraqture.template
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q :include-macros true])
  (:import  [fraqture.drawing Drawing]))

(defn setup [options]
  (q/frame-rate 30)
  {})

(defn update-state [state] state)

(defn draw-state [state]
  (q/background 40 100 100))

(def drawing
 (Drawing. "Spirograph" setup update-state draw-state nil nil nil))
