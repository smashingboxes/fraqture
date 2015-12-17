(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.shifting-grid :as shifting-grid]))

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
  (if (= (nth args 0) "shifting-grid")
    (load-drawing shifting-grid/drawing)))
