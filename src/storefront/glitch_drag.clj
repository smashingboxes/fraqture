(ns storefront.glitch-drag
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def jitter-amount 10)
(def x-blocks (rand-in-range 50 150))
(def y-blocks (rand-in-range 50 150))
(def update-interval (seconds 20))

(defn index-block [i]
  [(quot i y-blocks) (mod i y-blocks)])

(defn clamp-rgb [rgb]
  (max (min rgb 255) 0))

(defn jitter [max-jitter]
  (fn [d] (+ d (- (rand-int (inc max-jitter)) (/ max-jitter 2)))))

(defn color-walk [color]
  (map clamp-rgb (map (jitter jitter-amount) color)))

(defn random-color []
  [(rand-int 255) (rand-int 255) (rand-int 255)])

(defn cycle-index [column]
  (mod (inc (:current-index column)) (:y-count column)))

(defn color-with-opac [color]
  (conj (into [] color) 120))

(defn rect-at-index [x-index y-index color y-count]
  (let [width             (/ (q/width) x-blocks)
        height            (/ (q/height) y-count)
        x                 (* width x-index)
        y                 (* height y-index)]
    (q/no-stroke)
    (apply q/fill (color-with-opac color))
    (q/rect x y width height)))

(defrecord Column [current-index color y-count])

(defn color-to-rgb [color]
  [(q/red color) (q/green color) (q/blue color)])

(defn setup
  ([]
    (q/frame-rate 10)
    (setup nil))
  ([last-file]
    (let [image-file  (random-image-file :except #{last-file})
          column-y-blocks (repeatedly x-blocks #(rand-int y-blocks))
          column-ys   (map #(* % (/ (q/height) y-blocks)) column-y-blocks)
          column-xs   (map #(* % (/ (q/width) x-blocks)) (range x-blocks))
          raw-samples (map (fn [x y] (q/get-pixel x y)) column-xs column-ys)
          samples     (map #(color-to-rgb %) raw-samples)
          columns     (map (fn [y c] (->Column y c (+ 20 (rand-int 20)))) column-y-blocks samples)]
      (q/image (q/load-image image-file) 0 0 (q/width) (q/height))
      { :image-file image-file
        :last-update (q/millis)
        :columns  columns })))

(defn update-column [column]
  (Column. (cycle-index column) (color-walk (:color column)) (:y-count column)))

(defn update-state [state]
  (if (> (time-elapsed (:last-update state)) update-interval)
    (setup (:image-file state))
    (update-in state [:columns] #(map update-column %))))

(defn draw-state [state]
  (dorun
    (map-indexed
      (fn [idx column] (rect-at-index idx (:current-index column) (:color column) (:y-count column)))
      (:columns state))))

(def drawing (Drawing. "Drag Glitch" setup update-state draw-state :fullscreen [:keep-on-top :present]))
