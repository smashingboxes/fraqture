(ns storefront.turtle
  (:require [storefront.drawing]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defn build [x y]
  (atom
    { :x        x
      :y        y
      :angle    0
      :pen-down true }))

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

(defn setup []
  (let [turtle (build (/ (q/width) 2) (/ (q/height) 2))]
    (doseq [i (range 200)] (do (move turtle (/ i 5)) (turn turtle 5)))))

(defn update-state [state])
(defn draw-state [state])

(def drawing (Drawing. "Turtle" setup update-state draw-state :fullscreen [:present]))
