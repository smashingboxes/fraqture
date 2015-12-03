(ns storefront.pulsar
  (:require [quil.core :as q]))

(defn initialize
  [interval]
  { :last-fire (q/millis)
    :interval  interval
    :fired     false })

(defn fired
  [state]
  (:fired state))

(defn check-fire
  [state]
  (let [last-fire    (:last-fire state)
        interval     (:interval  state)
        current-time (q/millis)]
    (if (< (+ last-fire interval) current-time)
      [current-time true]
      [last-fire    false])))

(defn update
  [state]
  (let [[fire-time fire-status] (check-fire state)]
    (assoc state :last-fire fire-time :fired fire-status)))
