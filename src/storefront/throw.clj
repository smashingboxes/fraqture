(ns storefront.throw
  (:require [clojure.core.async :as async :refer :all]
            [quil.core :as q]
            [storefront.drawing]
            [serial-port :as ser])
  (:import  [storefront.drawing Drawing]))

(def cli-options [["-p" "--port PATH" "Serial port, /dev/cu.*"]])

(defn setup [options]
  (let [serial    (ser/open (:port options))]
    (q/background 0)
    { :port serial
      :next-fire (q/millis)
      :fired false }))

(defn update-state [state]
  (if (> (q/millis) (:next-fire state))
    (assoc state :next-fire (+ (q/millis) 15000) :fired true)
    (assoc state :fired false)))

(defn draw-state [state]
  (if (:fired state) (ser/write (:port state) (byte-array 1))))

(def drawing (Drawing. "throw" setup update-state draw-state cli-options nil))
