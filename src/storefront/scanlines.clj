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
    (initialize thickness opacity [0 0 0]))
  ([thickness opacity color]
    { :thickness thickness
      :target-opacity opacity
      :current-opacity 1.0
      :color color }))

(defn update-opacity
  [state pulse]
  (let [current-opacity (:current-opacity state)
        target-opacity  (:target-opacity state)
        opacity-diff    (- target-opacity current-opacity)]
  (if pulse
    1.0
    (+ (* opacity-diff 0.5) current-opacity))))

(defn update
  [state pulse]
  (assoc state :current-opacity (update-opacity state pulse)))

(defn draw
  [state]
  (let [thickness       (:thickness state)
        opacity         (:opacity state)
        color           (:color state)
        current-opacity (:current-opacity state)]
    (q/no-stroke)
    (q/fill 0 (* current-opacity 255))
    (rects thickness)))
