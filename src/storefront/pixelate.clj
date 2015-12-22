(ns storefront.pixelate
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.data :refer :all])
  (:import  [storefront.drawing Drawing]))

(def pixel-width 10)
(def pixel-height 10)

(defrecord Pixel [x y w h color])

(defn average-color [image]
  (let [pixels (q/pixels image)
        reds   (map #(q/red %) pixels)
        greens (map #(q/green %) pixels)
        blues  (map #(q/blue %) pixels)]
    (q/color (average reds) (average greens) (average blues))
  ))

(defn pixelate [img w h]
  (let [xs   (map #(* % w) (range (/ (q/width) w)))
        ys   (map #(* % h) (range (/ (q/width) h)))]
    (for [x xs y ys] (Pixel. x y w h (average-color (q/get-pixel img x y w h))))
  ))

(defn setup []
  (let [image-file (random-image-file)
        image      (q/load-image image-file)
        _resized   (q/resize image (q/width) (q/height))]
  (q/image image 0 0 (q/width) (q/height))
  { :hidden-pixels (shuffle (pixelate image pixel-width pixel-height))
    :showing-pixels '() }))

(defn update-state [state]
  (let [hidden     (:hidden-pixels state)
        n          100
        new-pixels (take n hidden)
        hidden     (drop n hidden)
        showing    (concat (:showing-pixels state) new-pixels)]
  { :hidden-pixels hidden
    :showing-pixels showing }))

(defn draw-state [state]
  (q/no-stroke)
  (doseq [pixel (:showing-pixels state)]
    (q/fill (:color pixel))
    (q/rect (:x pixel) (:y pixel) (:w pixel) (:h pixel))))

(def drawing (Drawing. "Pixelate" setup update-state draw-state :fullscreen []))
