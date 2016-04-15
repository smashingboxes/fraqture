(ns fraqture.snake
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [fraqture.led-array :as led]
            [quil.core :as q :include-macros true])
  (:import  [fraqture.drawing Drawing]))

; Grid: 2x2 on leds -> 15 columns
; Given a 16:9 screen, thats roughly 9 rows on the screen and 8 LED rows (neglect top and bottom row)

(def column-count 15)
(def screen-row-count 9)

(defn is-led-row [row]
  (or (< row 4) (> row (+ 3 screen-row-count))))

(defn game-row-to-led-row [row]
  (let [base-row (if (> row 3) (- row screen-row-count) row)]
    (-> base-row (* 2) (+ 1))))

(defn draw-led-block [serial row col color]
  (let [shifted-row (game-row-to-led-row row)
        shifted-col (* col 2)]
    (led/paint-window serial shifted-row shifted-col (+ shifted-row 2) (+ shifted-col 2) color)))

(defn draw-screen-block [row col color]
  (let [width  (/ (q/width) column-count)
        height (/ (q/height) screen-row-count)
        x      (* width col)
        y      (* height (- row 4))]
    (apply q/fill color)
    (q/rect x y width height)))

(defn draw-block [serial row col color]
  (if (is-led-row row)
    (draw-led-block serial row col color)
    (draw-screen-block row col color)))

(defn setup [options]
  (q/frame-rate 30)
  { :serial (:serial options)
    :position [0 4] })

(defn update-state [state] state)

(defn draw-state [state]
  (let [[col row] (:position state)]
    (q/background 70 100 100)
    (draw-block (:serial state) row col [255 255 255])))

(def drawing
 (Drawing. "Snake" setup update-state draw-state nil nil nil))
