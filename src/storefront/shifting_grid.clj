(ns storefront.shifting-grid
  (:require [storefront.drawing]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defn setup []
  (q/image (q/load-image "ross.jpg") 0 0 (q/width) (q/height)))

(defn update-state [state])

(defn draw-state [state])

(def drawing (Drawing. "Shifting Grid" setup update-state draw-state))
