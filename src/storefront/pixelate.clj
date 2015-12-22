(ns storefront.pixelate
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.data :refer :all])
  (:import  [storefront.drawing Drawing]))

(def pixel-width 10)
(def pixel-height 10)
(def pixelation-speed 100)

(defrecord Pixel [x y w h color])

(defn average-color [image]
  (let [pixels (q/pixels image)
        reds   (map #(q/red %) pixels)
        greens (map #(q/green %) pixels)
        blues  (map #(q/blue %) pixels)]
    (q/color (average reds) (average greens) (average blues))
  ))

(defn pixelate [w h]
  (let [img  (q/get-pixel 0 0 (q/width) (q/height))
        xs   (map #(* % w) (range (/ (q/width) w)))
        ys   (map #(* % h) (range (/ (q/width) h)))]
    (for [x xs y ys] (Pixel. x y w h (average-color (q/get-pixel img x y w h))))
  ))

(defn shuffled-pixels [mult]
  (shuffle (pixelate (* pixel-width mult) (* pixel-height mult))))

(defn setup []
  (let [pixel-multiplier 1]
  (q/image (q/load-image (random-image-file)) 0 0 (q/width) (q/height))
  { :pixel-multiplier pixel-multiplier
    :hidden-pixels (shuffled-pixels pixel-multiplier)
    :showing-pixels '() }))

(defn update-state [state]
  (let [hidden     (:hidden-pixels state)
        startover? (= (count hidden) 0)
        multiplier (if startover? (* (:pixel-multiplier state) 2) (:pixel-multiplier state))
        hidden     (if startover? (shuffled-pixels multiplier) hidden)
        n          (/ pixelation-speed multiplier)
        new-pixels (take n hidden)
        hidden     (drop n hidden)
        showing    (concat (:showing-pixels state) new-pixels)]
  { :pixel-multiplier multiplier
    :hidden-pixels hidden
    :showing-pixels showing }))

(defn draw-state [state]
  (q/no-stroke)
  (doseq [pixel (:showing-pixels state)]
    (q/fill (:color pixel))
    (q/rect (:x pixel) (:y pixel) (:w pixel) (:h pixel))))

(def drawing (Drawing. "Pixelate" setup update-state draw-state :fullscreen []))
