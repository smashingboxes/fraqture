(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [storefront.led-array :as led]
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

(def defaults [
  ["-s" "--serial SERIAL" "Serial port for LEDs"
   :parse-fn #(str %)
   :validate [#(led/validate-serial-port %) "Serial port not connected"]]
  ["-m" "--mock"]
  ["-h" "--help"]])

(defn reload-drawing! [drawing]
  (reset! drawing-atom drawing))

(defn curried-draw [drawing-atom with-mock? serial]
  (if with-mock?
    (fn [state] (do ((:draw @drawing-atom) state) (led/draw-mock serial)))
    (fn [state] ((:draw @drawing-atom) state))))

(defn load-drawing [drawing options]
  (let [quil-options   (:quil (:options drawing))
        with-mock? (:mock options)
        serial (:serial options)]
    (reload-drawing! drawing)
    (q/defsketch storefront
      :title  (:title drawing)
      :setup  #((:setup @drawing-atom) options)
      :update #((:update @drawing-atom) %)
      :draw   (curried-draw drawing-atom with-mock? serial)
      :size   (or (:size quil-options) :fullscreen)
      :features (or (:features quil-options) [:present])
      :middleware [m/fun-mode])))

(defn parse-cli [drawing-name args]
  (let [drawing        (get drawing-hash drawing-name)
        cli-options    (into [] (concat (or (:cli drawing) []) defaults))
        {:keys [options arguments errors summary]} (parse-opts args cli-options)
        options        (update-in options [:serial] #(led/connect %))
        help?          (:help options)]
    (led/clear (:serial options))
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
