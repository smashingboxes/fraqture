(ns fraqture.pixelate
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [fraqture.led-array :as led]
            [quil.core :as q]
            [fraqture.stream :as stream]
            [clojure.data :refer :all])
  (:import  [fraqture.drawing Drawing]))

(def pixel-width 10)
(def pixel-height 10)
(def pixelation-speed 50)

(defrecord Pixel [x y w h color])

(defn pixelate [w h]
  (let [img  (q/get-pixel 0 0 (q/width) (q/height))
        xs   (map #(* % w) (range (/ (q/width) w)))
        ys   (map #(* % h) (range (/ (q/height) h)))
        x2s  (map #(clamp (+ w %) 0 (q/width)) xs)
        y2s  (map #(clamp (+ h %) 0 (q/height)) ys)
        widths (map - x2s xs)
        heights (map - y2s ys)
        x-pairs (map vector xs widths)
        y-pairs (map vector ys heights)]
    (for [[x w] x-pairs [y h] y-pairs]
      (Pixel. x y w h (average-color (q/get-pixel img x y w h))))
  ))

(defn shuffled-pixels [mult]
  (shuffle (pixelate (* pixel-width mult) (* pixel-height mult))))

(defn setup [options]
  (q/frame-rate 30)
  (let [pixel-multiplier 3]
    (-> (stream/get-image!) (q/load-image) (q/image 0 0 (q/width) (q/height)))
    { :pixel-multiplier pixel-multiplier
      :options options
      :hidden-pixels (shuffled-pixels pixel-multiplier)
      :new-pixels '()
      :showing-pixels '() }))

(defn update-state [state]
  (let [hidden     (:hidden-pixels state)
        startover? (= (count hidden) 0)
        multiplier (if startover? (* (:pixel-multiplier state) 2) (:pixel-multiplier state))
        hidden     (if startover? (shuffled-pixels multiplier) hidden)
        n          (/ pixelation-speed (exp multiplier 2))
        new-pixels (take n hidden)
        hidden     (drop n hidden)
        showing    (concat (:showing-pixels state) new-pixels)
        options    (:options state)]
  { :pixel-multiplier multiplier
    :hidden-pixels hidden
    :new-pixels new-pixels
    :showing-pixels showing
    :options options}))

(defn convert-window [pixel]
  ; convert a rectangular pixel region on the main screen to
  ; a paint-window region on the led screen
  (let [x (/ (:x pixel) (q/width))
        y (/ (:y pixel) (q/height))
        width (/ (:w pixel) (q/width))
        height (/ (:h pixel) (q/height))
        window-x (int (* x led/col-count))
        window-y (int (* y led/row-count))
        window-width (int (* width led/col-count))
        window-height (int (* height led/row-count))]
    { :x-start window-x
      :y-start window-y
      :x-end (min (+ 1 (+ window-x window-width)) led/col-count)
      :y-end (min (+ 1 (+ window-y window-height)) led/row-count) }))

(defn decompose-color [color]
  [(q/red color) (q/green color) (q/blue color)])

(defn draw-screen [state]
  (q/no-stroke)
  (doseq [pixel (:showing-pixels state)]
    (q/fill (:color pixel))
    (q/rect (:x pixel) (:y pixel) (:w pixel) (:h pixel))))

(defn draw-leds [state]
  (let [options (:options state)
        serial  (:serial options)]
    (doseq [pixel (:new-pixels state)
      :let [window (convert-window pixel)]]
      (led/paint-window serial (:y-start window) (:x-start window) (:y-end window) (:x-end window) (decompose-color (:color pixel))))
    (led/refresh serial)))

(defn draw-state [state]
  (draw-screen state)
  (draw-leds state))

(defn exit? [state]
  (let [mult (:pixel-multiplier state)
        px-w (* pixel-width mult)
        px-h (* pixel-height mult)
        x (/ (q/width) px-w)
        y (/ (q/height) px-h)
        total-pixels (* x y)]
  (< total-pixels 50)))

(def drawing
  (Drawing. "Pixelate" setup update-state draw-state nil exit? nil))
