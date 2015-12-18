(ns storefront.cycle
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [storefront.glitch-drag :as drag]
            [storefront.spirograph :as spirograph]
            )
  (:import  [storefront.drawing Drawing]))

(def drawing-list (seq [drag/drawing spirograph/drawing]))
(def update-interval (seconds 10))

(defn current-drawing [state]
  (nth drawing-list (:drawing-i state)))

(defn setup []
  { :last-update (q/millis)
    :drawing-i 0
    :drawing-state ((:setup-fn drag/drawing)) })

(defn update-state [state]
  (if (> (time-elapsed (:last-update state)) update-interval)
    (-> state
      (assoc :drawing-i (inc (:drawing-i state)))
      (assoc :last-update (q/millis))
      (#(assoc % :drawing-state ((:setup-fn (current-drawing %)))))
    )
    (update-in state [:drawing-state] (:update-fn (current-drawing state)))
  ))

(defn draw-state [state]
  ((:draw-fn (current-drawing state)) (:drawing-state state)))

(def drawing (Drawing. "Cycle Drawings" setup update-state draw-state :fullscreen []))
