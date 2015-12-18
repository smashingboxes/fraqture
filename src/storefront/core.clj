(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.spirograph :as spirograph]
            [storefront.glitch-drag :as drag]
            [storefront.weather-drawing :as weather]))

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
  (let [drawing-arg (nth args 0)]
    (load-drawing (cond
      (= drawing-arg "spiro")   spirograph/drawing
      (= drawing-arg "ross")    drag/drawing
      (= drawing-arg "weather") weather/drawing
      :else                     drag/drawing))))
