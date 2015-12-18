(ns storefront.helpers
  (:require [quil.core :as q]))

; Milliseconds to seconds
(defn seconds [x] (* 1000 x))

; Milliseconds elapsed since
(defn time-elapsed [last-time]
  (- (q/millis) last-time))

; Random in range
(defn rand-in-range [low high]
  (+ (rand-int (- high low)) low))

; Accepted file extensions
(def image-extensions [".png" ".jpg" ".jpeg"])
(defn valid-image [file]
  (some true? (map #(.endsWith (.getName file) %) image-extensions)))

; Simple linear interpolation
(defn interpolate [x1 y1 x2 y2 x]
  (+ y1 (* (- y2 y1) (/ (- x x1) (- x2 x1)))))
