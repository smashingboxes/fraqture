(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.cycle :as cycle]
            [storefront.glitch-drag :as drag]
            [storefront.spirograph :as spirograph]
            [storefront.shifting-grid :as shifting-grid]
            [storefront.hexagons :as hexagons]
            [storefront.hex-spinner :as hex-spinner]
            [storefront.pixelate :as pixelate]
            [storefront.weather-drawing :as weather]
            [storefront.textify :as textify]
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
  (let [name (nth args 0)
        drawings (hash-map
          "spiro"         spirograph/drawing
          "drag"          drag/drawing
          "spiro"         spirograph/drawing
          "shifting-grid" shifting-grid/drawing
          "hex"           hexagons/drawing
          "hex-spinner"   hex-spinner/drawing
          "cycle"         cycle/drawing
          "pixelate"      pixelate/drawing
          "textify"       textify/drawing
          "weather"       weather/drawing
        )]
    (load-drawing (get drawings name))
  ))
