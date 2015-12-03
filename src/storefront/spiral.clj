(ns storefront.spiral
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quil.helpers.drawing :refer [line-join-points]]
            [quil.helpers.seqs :refer [range-incl steps]]
            [quil.helpers.calc :refer [mul-add]]))

(defn decay-noise [noise-array]
  (map #(* 0.9 %) noise-array))

(defn generate-noise
  [jitter]
  (let [base-noise (steps (rand 10) 0.05)]
    (map #(- (* jitter (q/noise %)) (/ jitter 2)) base-noise)))

(defn update-noise
  [state]
  (let [current-time    (q/millis)
        last-time       (:last-noise state)
        noise-array     (:noise-array state)
        jitter          (:jitter state)
        update-interval (:update-interval state)]
    (if (< (+ last-time update-interval) current-time)
      [current-time (generate-noise jitter)]
      [last-time (decay-noise noise-array)])))

(defn initialize
  [jitter update-interval]
  { :last-noise      (q/millis)
    :noise-array     (generate-noise jitter)
    :jitter          jitter
    :update-interval update-interval })

(defn update
  [state]
  (let [[last-noise noise-array] (update-noise state)]
  { :last-noise      last-noise
    :noise-array     noise-array
    :jitter          (:jitter state)
    :update-interval (:update-interval state) }))

(defn draw
  [state]
  (q/stroke-weight 5)
  (q/smooth)
  (let [cent-x    (/ (q/width) 2)
        cent-y    (/ (q/height) 2)
        rad-noise (:noise-array state)
        rads      (map q/radians (range-incl 0 1440 5))
        radii     (steps 10 1)
        radii     (map (fn [rad noise] (+ rad noise)) radii rad-noise)
        xs        (map (fn [rad radius] (mul-add (q/cos rad) radius cent-x)) rads radii)
        ys        (map (fn [rad radius] (mul-add (q/sin rad) radius cent-y)) rads radii)
        line-args (line-join-points xs ys)]
    (q/stroke 20 50 70)
    (dorun (map #(apply q/line %) line-args))))