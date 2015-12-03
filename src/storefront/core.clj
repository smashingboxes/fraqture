(ns storefront.core
  (:gen-class)
  (:require [storefront.scanlines :refer [draw-scanlines]]
            [storefront.spiral :as spiral]
            [storefront.pulsar :as pulsar]
            [quil.core :as q]
            [quil.middleware :as m]))

(def noise-jitter 300)
(def update-interval 2000)

(defn setup
  []
  (q/frame-rate 30)
  { :pulsar   (pulsar/initialize update-interval)
    :spiral   (spiral/initialize noise-jitter)
    :spiral-2 (spiral/initialize noise-jitter) })

(defn update-state
  [state]
  (let [updated-pulsar (pulsar/update (:pulsar state))]
    { :pulsar   updated-pulsar
      :spiral   (spiral/update (:spiral state) (pulsar/fired updated-pulsar))
      :spiral-2 (spiral/update (:spiral-2 state) (pulsar/fired updated-pulsar)) }))

(defn draw-state
  [state]
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
