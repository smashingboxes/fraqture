(ns storefront.cycle
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [storefront.glitch-drag :as drag])
  (:import  [storefront.drawing Drawing]))

(def drawing-list '(drag/drawing))
(def current-drawing-index 0)
(def update-interval (seconds 10))

(defn get-current-drawing [state] (nth drawing-list (:drawing-i state)))

(defn setup []
  { :last-update (q/millis)
    :drawing-i 0
    :drawing-state ((:setup-fn drag/drawing)) })

(defn update-state [state]
  (if (> (time-elapsed (:last-update state)) update-interval)
    (-> state
      (assoc :drawing-state ((:setup-fn drag/drawing)))
      (assoc :last-update (q/millis)))
    (update-in state [:drawing-state] (:update-fn drag/drawing))
  ))

(defn draw-state [state]
  ((:draw-fn drag/drawing) (:drawing-state state)))

(def drawing (Drawing. "Cycle Drawings" setup update-state draw-state :fullscreen []))
