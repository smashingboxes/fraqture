(ns storefront.core
  (:gen-class)
  (:require [storefront.scanlines :as scanlines]
            [storefront.spiral :as spiral]
            [storefront.pulsar :as pulsar]
            [storefront.arduino :as arduino]
            [quil.core :as q]
            [quil.middleware :as m]
            [serial-port :as ser]))

(def noise-jitter 300)
(def update-interval 1000)

(defn setup
  []
  (q/frame-rate 30)
  { :arduino  (arduino/initialize "/dev/tty.usbmodem453061")
    :scan     (scanlines/initialize 2 0.2)
    :pulsar   (pulsar/initialize update-interval)
    :spiral   (spiral/initialize noise-jitter)
    :spiral-2 (spiral/initialize noise-jitter) })

(defn update-state
  [state]
  (let [updated-pulsar   (pulsar/update (:pulsar state))
        ; pulse          (pulsar/fired updated-pulsar)
        updated-arduino  (arduino/update (:arduino state))
        pulse            (:fired updated-arduino)]
    { :arduino  updated-arduino
      :scan     (scanlines/update (:scan state) pulse)
      :pulsar   updated-pulsar
      :spiral   (spiral/update (:spiral state) pulse)
      :spiral-2 (spiral/update (:spiral-2 state) pulse) }))

(defn draw-state
  [state]
  (q/background 255)
  (spiral/draw (:spiral state))
  (spiral/draw (:spiral-2 state))
  (scanlines/draw (:scan state)))

(q/defsketch storefront
  :title "Twitchy Spiral"
  :size :fullscreen
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top :present]
  :middleware [m/fun-mode])

(defn -main [& args])
