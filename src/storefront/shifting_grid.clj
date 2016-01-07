(ns storefront.shifting-grid
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.core.matrix :as m])
  (:import  [storefront.drawing Drawing]))

(defn draw-block [block x-index y-index w h]
  (let [x                 (* w x-index)
        y                 (* h y-index)]
    (q/rect x y w h)
    (q/image block x y w h)))

(defn rand-speed [min-max]
  ((rand-nth [- +]) 0 (+ (rand-int min-max) 1)))

(defn rotate-nth-generator [max-rotation]
  (fn [matrix n]
    (let [shift (rand-speed max-rotation)]
      (update-in matrix [n] #(m/rotate % 0 shift)))))

; Returns n indices, from start, and wrapping after max
(defn n-indices-wrapped [n start y]
  (map #(mod % y) (map #(+ start %) (range n))))

(defn random-rotation [matrix max-rotation chunk-size]
  (let [column? (rand-nth '(true false))
        x-blocks (count matrix)
        y-blocks (count (first matrix))
        max     (if column? (- x-blocks 1) (- y-blocks 1))
        start   (rand-int max)
        size    chunk-size
        columns (n-indices-wrapped size start max)
        rotated (if column? matrix (m/transpose matrix))
        rotate-nth (rotate-nth-generator max-rotation)
        shifted (reduce rotate-nth rotated columns)]
    (if column? shifted (m/transpose shifted))))

(def cli-options
  [
    ["-x" "--x-blocks INT" "Number of blocks in the horizontal"
      :default 50
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 2 % 200) "Must be a number between 2 and 200"]]
    ["-y" "--y-blocks INT" "Number of blocks in the vertical direction"
      :default 30
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 2 % 200) "Must be a number between 0 and 200"]]
    ["-c" "--chunk-size INT" "Number of columns/rows to slide together"
      :default 5
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 1 % 10) "Must be a number between 1 and 10"]]
    ["-r" "--max-rotation INT" "Max number of positions to rotate columns/rows"
      :default 3
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 1 % 10) "Must be a number between 1 and 10"]]
  ])

(defn setup [options]
  (q/frame-rate 5)
  (let [image-file (random-image-file)
        image  (q/load-image image-file)
        _resized (q/resize image (q/width) (q/height))
        x-blocks (:x-blocks options)
        y-blocks (:y-blocks options)
        block-w (/ (q/width) x-blocks)
        block-h (/ (q/height) y-blocks)
        xs     (map #(* % block-w) (range x-blocks))
        ys     (map #(* % block-h) (range y-blocks))
        blocks (map (fn [x] (map (fn [y] (q/get-pixel image x y block-w block-h)) ys)) xs)]
        { :blocks blocks
          :options options
          :block-w block-w
          :block-h block-h }))

(defn update-state [state]
  (let [options (:options state)
        max-rotation (:max-rotation options)
        chunk-size (:chunk-size options)]
    (update-in state [:blocks] #(random-rotation % max-rotation chunk-size))))

(defn draw-state [state]
  (dorun
    (map-indexed (fn [x-index column]
      (dorun
        (map-indexed (fn [y-index block]
          (draw-block block x-index y-index (:block-w state) (:block-h state)))
        column)))
    (:blocks state))))

(def drawing
  (Drawing. "Shifting Grid" setup update-state draw-state cli-options nil nil))
