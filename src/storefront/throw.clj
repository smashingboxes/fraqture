(ns storefront.throw
  (:require [quil.core :as q]
            [storefront.drawing]
            [serial.core :as ser])
  (:import  [storefront.drawing Drawing]))

(def cli-options [["-p" "--port PATH" "Serial port, /dev/cu.*"]])
(def csize 80)

(defn fire-leds [state]
  (ser/write (:port state) (byte-array 1)))

(defn setup [options]
  (let [serial    (ser/open (:port options))]
    { :loc (+ csize (q/width))
      :fresh true
      :port serial
      :next-fire (q/millis) }))

(defn update-state [state]
  (if (> (q/millis) (:next-fire state))
    (assoc state :loc (+ csize (q/width)) :next-fire (+ (q/millis) 6000) :fresh true)
    (let [new-loc (- (:loc state) 10)]
      (-> (if (and (:fresh state) (< new-loc 0))
            (do (fire-leds state)
                (assoc state :fresh false))
            state)
          (assoc :loc new-loc)))))

(defn draw-state [state]
  (q/background 0)
  (q/fill 255)
  (q/ellipse (:loc state) (/ (q/height) 2) csize csize))

(def drawing (Drawing. "throw" setup update-state draw-state cli-options nil nil))
