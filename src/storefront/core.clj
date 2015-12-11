(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.spiral-glitch :as spiral]))

(def drawing spiral/drawing)

(defn load-drawing
  [drawing-info]
  (q/defsketch storefront
    :title "Twitchy Spiral"
    :size   :fullscreen
    :setup  (:setup drawing-info)
    :update (:update-state drawing-info)
    :draw   (:draw-state drawing-info)
    :features [:keep-on-top :present]
    :middleware [m/fun-mode]))

(load-drawing drawing)
(defn -main [& args])
