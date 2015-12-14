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
