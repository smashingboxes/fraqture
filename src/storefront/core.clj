(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [storefront.cycle :as cycle]
            [storefront.glitch-drag :as drag]
            [storefront.spirograph :as spirograph]
            [storefront.shifting-grid :as shifting-grid]
            [storefront.hexagons :as hexagons]
            [storefront.hex-spinner :as hex-spinner]
            [storefront.pixelate :as pixelate]
            [storefront.weather-drawing :as weather]
            [storefront.textify :as textify]
            [storefront.sierpinski :as sierpinski]
            [storefront.plant :as plant]))

(defn load-drawing [drawing args]
  (let [quil-options   (:quil (:options drawing))
        args-hash (parse-opts args (:cli drawing))
        errors    (:errors args-hash)]
    (if errors
      (dorun
        (println (string/join "\n" errors))
        (System/exit 1))
      (q/defsketch storefront
        :title  (:title drawing)
        :setup  (fn [] ((:setup drawing) (:options args-hash)))
        :update (:update drawing)
        :draw   (:draw drawing)
        :size   (:size quil-options)
        :features (:features quil-options)
        :middleware [m/fun-mode]))))

(def drawing-hash (hash-map
    "spiro"         spirograph/drawing
    "drag"          drag/drawing
    "spiro"         spirograph/drawing
    "shifting-grid" shifting-grid/drawing
    "hex"           hexagons/drawing
    "hex-spinner"   hex-spinner/drawing
    "cycle"         cycle/drawing
    "pixelate"      pixelate/drawing
    "weather"       weather/drawing
    "sierpinski"    sierpinski/drawing
    "plant"         plant/drawing
    "textify"       textify/drawing
  ))

(def basic-usage
  (str "lein run <drawing> [args]\n drawings: " (keys drawing-hash)))

(defn -main [& args]
  (let [[drawing-name & drawing-args] args]
    (if (not (contains? (set (keys drawing-hash)) drawing-name))
      (println basic-usage)
      (load-drawing (get drawing-hash drawing-name) drawing-args))))
