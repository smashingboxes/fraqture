(ns storefront.scanlines
  (:require [quil.core :as q]))

(defn horizontals
  [line-height]
  (range 0 (q/height) (* line-height 2)))

(defn rects
  [line-height]
  (doseq [y (horizontals line-height)]
    (q/rect 0 y (q/width) line-height)))

(defn initialize
  ([thickness opacity]
    (initialize thickness opacity 0 0 0))
  ([thickness opacity red green blue]
    { :thickness       thickness
      :target-opacity  opacity
      :current-opacity 0.8
      :current-color   [red green blue]
      :target-color    [red green blue] }))

(defn update-opacity
  [state pulse]
  (let [current-opacity (:current-opacity state)
        target-opacity  (:target-opacity state)
        opacity-diff    (- target-opacity current-opacity)]
  (if pulse
    0.8
    (+ (* opacity-diff 0.1) current-opacity))))

(defn update-color
  [state pulse]
  (let [current-color (:current-color state)
        target-color  (:target-color state)
        color-diff    (map - target-color current-color)
        diff-weighted (map #(* 0.1 %) color-diff)]
    (if pulse
      [(rand 255) (rand 255) (rand 255)]
      (map + current-color diff-weighted))))

(defn update
  [state pulse]
  (assoc state
    :current-opacity (update-opacity state pulse)
    :current-color   (update-color state pulse)))

(defn draw
  [state]
  (let [thickness       (:thickness state)
        opacity         (:opacity state)
        red             (nth (:current-color state) 0)
        green           (nth (:current-color state) 1)
        blue            (nth (:current-color state) 2)
        current-opacity (:current-opacity state)]
    (q/no-stroke)
    (q/fill red green blue (* current-opacity 255))
    (rects thickness)))
