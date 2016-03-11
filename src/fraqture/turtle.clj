(ns fraqture.turtle
  (:require [quil.core :as q]))

(defn build [x y]
  (atom
    { :x        x
      :y        y
      :angle    0
      :pen-down true
      :stack    nil }))

(defn move [turtle distance]
  (let [t     @turtle
        x     (:x t)
        y     (:y t)
        rads  (q/radians (:angle t))
        new-x (+ x (* distance (q/sin rads)))
        new-y (- y (* distance (q/cos rads)))]
    (if (:pen-down t) (q/line x y new-x new-y))
    (swap! turtle assoc :x new-x :y new-y)))

(defn pen-up [turtle]
  (swap! turtle assoc :pen-down false))

(defn pen-down [turtle]
  (swap! turtle assoc :pen-down true))

(defn turn [turtle amount]
  (swap! turtle update-in [:angle] #(mod (+ % amount) 360)))

(defn save [turtle]
  (swap! turtle assoc :stack @turtle))

(defn restore [turtle]
  (reset! turtle (:stack @turtle)))
