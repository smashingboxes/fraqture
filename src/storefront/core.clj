(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [storefront.color-swap :as color-swap]
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
            [storefront.plant :as plant]
            [storefront.throw :as thro]
            [storefront.tweetreader :as tweetreader]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn usage [drawing-name summary]
  (string/join \newline
    [(str "Usage: lein run " drawing-name " [args]")
      ""
      "args:"
      summary]))

(def drawing-hash (hash-map
    "swap"          color-swap/drawing
    "spiro"         spirograph/drawing
    "drag"          drag/drawing
    "shifting-grid" shifting-grid/drawing
    "hex"           hexagons/drawing
    "hex-spinner"   hex-spinner/drawing
    "cycle"         cycle/drawing
    "pixelate"      pixelate/drawing
    "weather"       weather/drawing
    "sierpinski"    sierpinski/drawing
    "plant"         plant/drawing
    "textify"       textify/drawing
    "throw"         thro/drawing
    "tweetreader"   tweetreader/drawing
  ))

(defonce drawing-atom (atom nil))

(defn reload-drawing! [drawing]
  (reset! drawing-atom drawing))

(defn load-drawing [drawing options]
  (let [quil-options   (:quil (:options drawing))]
    (reload-drawing! drawing)
    (q/defsketch storefront
      :title  (:title drawing)
      :setup  #((:setup @drawing-atom) options)
      :update #((:update @drawing-atom) %)
      :draw   #((:draw @drawing-atom) %)
      :size   (or (:size quil-options) :fullscreen)
      :features (or (:features quil-options) [:present])
      :middleware [m/fun-mode])))

(defn parse-cli [drawing-name args]
  (let [drawing        (get drawing-hash drawing-name)
        cli-options    (merge (:cli drawing) ["-h" "--help"])
        {:keys [options arguments errors summary]} (parse-opts args cli-options)
        help?          (:help options)]
    (cond
      help? (exit 1 (usage drawing-name summary))
      errors (exit 1 (string/join \newline errors))
      :else (load-drawing drawing options))))

(def basic-usage
  (str "Usage: lein run <drawing> [args]\n drawings: " (keys drawing-hash)))

(defn -main [& args]
  (let [[drawing-name & drawing-args] args]
    (if (not (contains? (set (keys drawing-hash)) drawing-name))
      (println basic-usage)
      (parse-cli drawing-name drawing-args))))
