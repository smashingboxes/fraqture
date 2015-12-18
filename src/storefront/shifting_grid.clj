(ns storefront.shifting-grid
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.core.matrix :as m])
  (:import  [storefront.drawing Drawing]))

(def x-blocks 50)
(def y-blocks 30)
(def max-rotation-length 5)

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

(defn setup []
  (def image (q/load-image "images/ross.jpg"))
  (q/resize image (q/width) (q/height))
  (let [xs     (map #(* % (block-width)) (range x-blocks))
        ys     (map #(* % (block-height))  (range y-blocks))
        blocks (map (fn [x] (map (fn [y] (get-block image x y)) ys)) xs)]
        { :blocks blocks }))

(defn rand-speed [min-max]
  ((rand-nth [- +]) 0 (+ (rand-int min-max) 1)))

(defn rotate-nth [matrix n]
  (let [shift (rand-speed max-rotation-length)]
    (update-in matrix [n] #(m/rotate % 0 shift))))

(defn random-rotation [matrix]
  (let [column? (rand-nth '(true false))
        n-max   (if column? x-blocks y-blocks)
        n       (rand-int n-max)]
        (if column?
          (rotate-nth matrix n)
          (m/transpose (rotate-nth (m/transpose matrix) n)))))

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
