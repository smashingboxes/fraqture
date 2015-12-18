(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.cycle :as cycle]
            [storefront.glitch-drag :as drag]))

(defn load-drawing
  [drawing-info]
  (q/defsketch storefront
    :title  (:title drawing-info)
    :size   :fullscreen
    :setup  (:setup-fn drawing-info)
    :update (:update-fn drawing-info)
    :draw   (:draw-fn drawing-info)
    :features [:keep-on-top :present]
    :middleware [m/fun-mode]))

(defn -main [& args]
  (def command (nth args 0))
  (if (= command "ross")
    (load-drawing drag/drawing))
  (if (= command "cycle")
    (load-drawing cycle/drawing)))
