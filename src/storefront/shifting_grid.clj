(ns storefront.shifting-grid
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.core.matrix :as m])
  (:import  [storefront.drawing Drawing]))

(def x-blocks 50)
(def y-blocks 30)
(def max-chunk-size 10)
(def max-rotation-length 3)

(def block-width #(/ (q/width) x-blocks))
(def block-height #(/ (q/height) y-blocks))
(def total-blocks (* x-blocks y-blocks))



(defn draw-block [block x-index y-index]
  (let [x                 (* (block-width) x-index)
        y                 (* (block-height) y-index)]
    (q/rect x y (block-width) (block-height))
    (q/image block x y (block-width) (block-height))))

(defn get-block [image x y]
  (q/get-pixel image x y (block-width) (block-height)))

(defn rand-speed [min-max]
  ((rand-nth [- +]) 0 (+ (rand-int min-max) 1)))

(defn rotate-nth [matrix n]
  (let [shift (rand-speed max-rotation-length)]
    (update-in matrix [n] #(m/rotate % 0 shift))))

; Returns n indices, from start, and wrapping after max
(defn n-indices-wrapped [n start y]
  (map #(mod % y) (map #(+ start %) (range n))))

(defn matrix-rotate-generator [n column?]
  (fn [matrix]
    (if column?
      (rotate-nth matrix n)
      (m/transpose (rotate-nth (m/transpose matrix) n)))))


(defn random-rotation [matrix]
  (let [column? (rand-nth '(true false))
        max     (if column? (- x-blocks 1) (- y-blocks 1))
        start   (rand-int max)
        size    (+ (rand-int max-chunk-size) 1)
        columns (n-indices-wrapped size start max)
        rotated (if column? matrix (m/transpose matrix))
        shifted (reduce rotate-nth rotated columns)]
    (if column? shifted (m/transpose shifted))))

(defn setup []
  (q/frame-rate 5)
  (let [image-file (random-image-file)
        image  (q/load-image image-file)
        _resized (q/resize image (q/width) (q/height))
        xs     (map #(* % (block-width)) (range x-blocks))
        ys     (map #(* % (block-height))  (range y-blocks))
        blocks (map (fn [x] (map (fn [y] (get-block image x y)) ys)) xs)]
        { :blocks blocks }))

(defn update-state [state]
  (update-in state [:blocks] random-rotation))

(defn draw-state [state]
  (dorun
    (map-indexed (fn [x-index column]
      (dorun
        (map-indexed (fn [y-index block]
          (draw-block block x-index y-index))
        column)))
    (:blocks state))))

(def drawing (Drawing. "Shifting Grid" setup update-state draw-state :fullscreen []))
