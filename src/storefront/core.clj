(ns storefront.core
  (:gen-class)
  (:require [storefront.scanlines :refer [draw-scanlines]]
            [storefront.spiral :as spiral]
            [quil.core :as q]
            [quil.middleware :as m]))

(def noise-jitter 300)
(def update-interval (+ (rand 1000) 100))

(defn setup []
  (q/frame-rate 30)
  { :spiral (spiral/initialize noise-jitter update-interval)
    :spiral-2 (spiral/initialize noise-jitter update-interval)})

(defn update-state [state]
  { :spiral (spiral/update (:spiral state))
    :spiral-2 (spiral/update (:spiral-2 state)) })

(defn draw-state [state]
  (q/background 255)
  (spiral/draw (:spiral state))
  (spiral/draw (:spiral-2 state))
  (draw-scanlines 2 0.2))

(q/defsketch storefront
  :title "Twitchy Spiral"
  :size :fullscreen
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top :present]
  :middleware [m/fun-mode])

(defn -main [& args])
