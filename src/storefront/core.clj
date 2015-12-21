(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.cycle :as cycle]
            [storefront.glitch-drag :as drag]
            [storefront.spirograph :as spirograph]
            [storefront.shifting-grid :as shifting-grid]
            [storefront.hexagons :as hexagons]
            [storefront.spinner-hex :as spinner-hex]
            ))

(defn load-drawing
  [drawing-info]
  (q/defsketch storefront
    :title  (:title drawing-info)
    :size   (:size drawing-info)
    :setup  (:setup-fn drawing-info)
    :update (:update-fn drawing-info)
    :draw   (:draw-fn drawing-info)
    :features (:features drawing-info)
    :middleware [m/fun-mode]))

(defn -main [& args]
  (def command (nth args 0))
  (if (= command "spiro")
    (load-drawing spirograph/drawing))
  (if (= command "drag")
    (load-drawing drag/drawing))
  (if (= command "shifting-grid")
    (load-drawing shifting-grid/drawing))
  (if (= command "hex")
    (load-drawing hexagons/drawing))
  (if (= command "spinner-hex")
    (load-drawing spinner-hex/drawing))
  (if (= command "cycle")
    (load-drawing cycle/drawing)))
