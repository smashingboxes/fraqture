(ns fraqture.cycle
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q]
            [clojure.tools.cli :refer [parse-opts]]
            [fraqture.glitch-drag :as drag]
            [fraqture.color-swap :as swap]
            [fraqture.shifting-grid :as shifting-grid]
            [fraqture.pixelate :as pixelate]
            [fraqture.tweetreader :as tweetreader]
            [fraqture.textify :as textify]
            [fraqture.led-array :as led]
            [fraqture.snake :as snake]
            [fraqture.photo-countdown :as countdown]
            [fraqture.time :refer [is-night?]]
            )
  (:import  [fraqture.drawing Drawing]))

(def night-list
  [snake/drawing])

(def day-list
  [drag/drawing
   snake/drawing
   swap/drawing
   pixelate/drawing
   shifting-grid/drawing
   textify/drawing])

(defn randomize-day-list []
  (shuffle day-list))

(defn build-list [] (if (is-night?) night-list (randomize-day-list)))

(defn cycle-list [state]
  (let [more? (> (count (:drawing-list state)) 1)
        new-list (if more? (rest (:drawing-list state)) (build-list))]
    (assoc state :drawing-list new-list)))

(defn current-drawing [state]
  (first (:drawing-list state)))

(defn default-options [drawing]
  (:options (parse-opts "" (:cli drawing))))

(defn bootstrap-state [state]
  (let [current-drawing (current-drawing state)
        serial          (:serial (:options state))
        options         (default-options current-drawing)
        options         (assoc options :serial serial)]
  (led/clear serial)
  (led/refresh serial)
  (assoc state :drawing-state ((:setup current-drawing) options))))

(def cli-options
  [
    [nil "--update-interval INT" "Number of seconds between animations"
      :default 100
      :parse-fn #(Integer/parseInt %)]
  ])

(defn setup [options]
  (let [initial-state { :last-update (q/millis)
                        :drawing-list (build-list)
                        :options options }]
    (q/no-cursor)
    (bootstrap-state initial-state)))

(defn update-state [state]
  (let [update-interval (seconds (:update-interval (:options state)))
        exit-fn         (:exit? (current-drawing state))
        next?           (if exit-fn
                          (exit-fn (:drawing-state state))
                          (> (time-elapsed (:last-update state)) update-interval))]
    (if next?
      (-> state
        (cycle-list)
        (assoc :last-update (q/millis))
        (bootstrap-state))
      (update-in state [:drawing-state] (:update (current-drawing state))))))

(defn draw-state [state]
  ((:draw (current-drawing state)) (:drawing-state state)))

(def drawing
  (Drawing. "Cycle Drawings" setup update-state draw-state cli-options nil nil))
