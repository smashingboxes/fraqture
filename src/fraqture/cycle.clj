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
            [fraqture.led-array :as led]
            [fraqture.snake :as snake]
            )
  (:import  [fraqture.drawing Drawing]))

(def night-list
  [snake/drawing])

(def day-list
  [drag/drawing
   swap/drawing
   pixelate/drawing
   shifting-grid/drawing
   tweetreader/drawing])

(defn list-by-time []
  (let [hours (.getHours (new java.util.Date))]
    (if (< hours 6) night-list day-list)))

(defn current-drawing [state]
  (nth (:drawing-list state) (:drawing-i state)))

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
                        :drawing-i 0
                        :drawing-list (list-by-time)
                        :options options }]
    (bootstrap-state initial-state)))

(defn update-state [state]
  (let [update-interval (seconds (:update-interval (:options state)))
        exit-fn         (:exit? (current-drawing state))
        next?           (if exit-fn
                          (exit-fn (:drawing-state state))
                          (> (time-elapsed (:last-update state)) update-interval))]
    (if next?
      (-> state
        (assoc :drawing-list (list-by-time))
        (assoc :drawing-i (mod (inc (:drawing-i state)) (count (:drawing-list state))))
        (assoc :last-update (q/millis))
        (bootstrap-state))
      (update-in state [:drawing-state] (:update (current-drawing state))))))

(defn draw-state [state]
  ((:draw (current-drawing state)) (:drawing-state state)))

(def drawing
  (Drawing. "Cycle Drawings" setup update-state draw-state cli-options nil nil))
