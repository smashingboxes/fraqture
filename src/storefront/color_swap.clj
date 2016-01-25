(ns storefront.color-swap
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def pixel-size 20)
(def pixel-count 5)

(defn blend-pixels [x y dx dy effect]
  (q/blend x y pixel-size pixel-size dx dy pixel-size pixel-size effect))

(defn pixel-array []
  (let [x-arry (range 0 (q/width) pixel-size)]
    (shuffle
      (partition 2
        (flatten
          (map
            (fn [arg1]
              (map #(list arg1 %) (range 0 (q/height) pixel-size)))
            x-arry))))))

(defn end-of-pixel-array? [state]
  (= 0 (count (:pixel-array state))))

(defn load-new-image []
  (q/image (q/load-image (random-image-file)) 0 0 (q/width) (q/height)))

(defn setup [options]
  (load-new-image)
  {:pixel-array (pixel-array)
   :pixels-to-blend []
   :dpixels-to-blend []
   :effects [:blend]})

(defn draw-state [state]
  (if (end-of-pixel-array? state)
    (load-new-image))
  (let [pixels (:pixels-to-sample state)]
    (let [dpixels (:pixels-to-modify state)]
      (doseq [pixel (map vector pixels dpixels)]
        (let [x (nth (nth pixel 0) 0)
              y (nth (nth pixel 0) 1)
              dx (nth (nth pixel 1) 0)
              dy (nth (nth pixel 1) 1)]
          (blend-pixels x y dx dy (first (:effects state))))))))

(defn update-state [state]
  {:pixels-to-modify (take pixel-count (:pixel-array state))
   :pixels-to-sample (take pixel-count (drop pixel-count (:pixel-array state)))
   :pixel-array (if (end-of-pixel-array? state)
                  (pixel-array)
                  (drop pixel-count (:pixel-array state)))
   :load-new-image (end-of-pixel-array? state)
   :effects (if (end-of-pixel-array? state)
              (:effects state)
              (concat (rest (:effects state)) [(first (:effects state))]))})

(def drawing
  (Drawing. "Color Swap" setup update-state draw-state nil { :quil { :size [700 450] :features [] }}))
