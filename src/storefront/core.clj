(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.shifting-grid :as shifting-grid]
            [storefront.spirograph :as spirograph]
            [storefront.glitch-drag :as drag]))

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
  (if (= (nth args 0) "shifting-grid")
    (load-drawing shifting-grid/drawing))
  (if (= (nth args 0) "spiro")
    (load-drawing spirograph/drawing))
  (if (= (nth args 0) "drag")
    (load-drawing drag/drawing)))
