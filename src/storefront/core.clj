(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
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

(defn get-rossy[]
  (future
    (println "[Future] started computation")
    (load-drawing drag/drawing)
    (Thread/sleep 20000)
    (get-rossy)))

(get-rossy)
(defn -main [& args])
