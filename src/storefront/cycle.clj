(ns storefront.cycle
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.tools.cli :refer [parse-opts]]
            [storefront.glitch-drag :as drag]
            [storefront.spirograph :as spirograph]
            [storefront.shifting-grid :as shifting-grid]
            [storefront.hexagons :as hexagons]
            [storefront.hex-spinner :as hex-spinner]
            [storefront.pixelate :as pixelate]
            [storefront.textify :as textify]
            )
  (:import  [storefront.drawing Drawing]))

(def drawing-list
  (shuffle
   [drag/drawing
    spirograph/drawing
    shifting-grid/drawing
    hexagons/drawing
    hex-spinner/drawing
    pixelate/drawing
    textify/drawing]))

(defn current-drawing [state]
  (nth drawing-list (:drawing-i state)))

(defn default-options [drawing]
  (:options (parse-opts "" (:cli drawing))))

(defn bootstrap-state [state]
  (let [current-drawing (current-drawing state)
        options         (default-options current-drawing)]
  (assoc state :drawing-state ((:setup current-drawing) options))))

(def cli-options
  [
    [nil "--update-interval INT" "Number of seconds between animations"
      :default 120
      :parse-fn #(Integer/parseInt %)]
  ])

(defn setup [options]
  (let [initial-state { :last-update (q/millis) :drawing-i 0 :options options }]
    (bootstrap-state initial-state)))

(defn update-state [state]
  (let [update-interval (seconds (:update-interval (:options state)))
        exit-fn         (:exit? (current-drawing state))
        next?           (if exit-fn
                          (exit-fn (:drawing-state state))
                          (> (time-elapsed (:last-update state)) update-interval))]
    (if next?
      (-> state
        (assoc :drawing-i (mod (inc (:drawing-i state)) (count drawing-list)))
        (assoc :last-update (q/millis))
        (bootstrap-state))
      (update-in state [:drawing-state] (:update (current-drawing state))))))

(defn draw-state [state]
  ((:draw (current-drawing state)) (:drawing-state state)))

(def drawing
  (Drawing. "Cycle Drawings" setup update-state draw-state cli-options nil nil))
