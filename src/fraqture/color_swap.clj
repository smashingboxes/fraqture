(ns fraqture.color-swap
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [fraqture.led-array :as led]
            [fraqture.stream :as stream]
            [quil.core :as q])
  (:import  [fraqture.drawing Drawing]))

(def pixel-size 28)
(def pixel-count 11)
(def led-pixel-size 2)

(defn exit? [state]
  (= (:times-run state) 1))

(defn rand-xy-array [x-max y-max step]
  (shuffle (for [x (range 0 x-max step)
        y (range 0 y-max step)]
    [x y])))

(defn blend-pixels [x y dx dy effect]
  (q/blend x y pixel-size pixel-size dx dy pixel-size pixel-size effect))

(defn pixel-array []
  (let [array (rand-xy-array (q/width) (q/height) pixel-size)]
    (concat array (reverse array))))

(defn led-array []
  (rand-xy-array (- 19 led-pixel-size) (- 34 led-pixel-size) led-pixel-size))

(defn load-new-image [image]
  (q/image image 0 0 (q/width) (q/height)))

(defn dups [seq]
  (for [[id freq] (frequencies seq)
        :when (> freq 1)]
   id))

(defn setup [options]
  (q/frame-rate 30)
  (->> (stream/get-raster!) (q/load-image) (load-new-image))
  {:times-run 0
   :pixel-array (pixel-array)
   :led-array (led-array)
   :pixels-to-blend []
   :dpixels-to-blend []
   :leds-to-modify []
   :reversing false
   :serial (:serial options)
   :effects [:blend]})

(defn draw-state [state]
  (let [leds (:leds-to-modify state)]
    (doseq [led leds]
      (let [x (get led 0)
            y (get led 1)
            color (if (:reversing state)
                    [0 0 0]
                    [(rand-int 255) (rand-int 255) (rand-int 255)])]
        (led/paint-window (:serial state) x y (+ x led-pixel-size) (+ y led-pixel-size) color)
        (led/refresh (:serial state)))))

  (if (and (:load-new-image state) (not (exit? state)))
    (do
      (->> (stream/get-raster!) (q/load-image) (load-new-image))
      (led/clear (:serial state))))

  (let [pixels (:pixels-to-sample state)]
    (let [dpixels (:pixels-to-modify state)]
      (doseq [pixel (map vector pixels dpixels)]
        (let [x (get-in pixel [0 0])
              y (get-in pixel [0 1])
              dx (get-in pixel [1 0])
              dy (get-in pixel [1 1])]
          (blend-pixels x y dx dy (first (:effects state))))))))

(defn update-state [state]
  (let [loading-new-image (= 0 (count (:pixel-array state)))
        reversing (not= 0 (count (dups (concat (:pixels-to-modify state) (:pixels-to-sample state)))))
        pixels-to-modify (take pixel-count (:pixel-array state))
        pixels-to-sample (take pixel-count (drop pixel-count (:pixel-array state)))]
    {:times-run (if loading-new-image (inc (:times-run state)) (:times-run state))
     :pixels-to-modify pixels-to-modify
     :pixels-to-sample pixels-to-sample
     :reversing (if (or (and reversing (not (:reversing state))) loading-new-image)
                  (not (:reversing state))
                  (:reversing state))
     :modified-pixels (concat pixels-to-sample pixels-to-modify)
     :pixel-array (if loading-new-image
                    (pixel-array)
                    (drop pixel-count (:pixel-array state)))
     :load-new-image loading-new-image
     :serial (:serial state)
     :leds-to-modify (take 1 (:led-array state))
     :led-array (if (or loading-new-image reversing)
                  (led-array)
                  (drop 1 (:led-array state)))
     :effects (if loading-new-image
                (:effects state)
                (concat (rest (:effects state)) [(first (:effects state))]))}))

(def drawing
  (Drawing. "Color Swap" setup update-state draw-state nil exit? :raster))
