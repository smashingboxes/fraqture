(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quil.helpers.drawing :refer [line-join-points]]
            [quil.helpers.seqs :refer [range-incl steps]]
            [quil.helpers.calc :refer [mul-add]]))

(def noise-jitter 300)
(def update-interval 2000)

(defn decay-noise [noise-array]
  (map #(* 0.9 %) noise-array))

(defn generate-noise []
  (let [base-noise (steps (rand 10) 0.05)]
    (map #(- (* noise-jitter (q/noise %)) (/ noise-jitter 2)) base-noise)))

(defn update-noise [last-time noise-array]
  (let [current-time (q/millis)]
    (if (< (+ last-time update-interval) current-time)
      [current-time (generate-noise)]
      [last-time (decay-noise noise-array)])))

(defn setup []
  (q/frame-rate 30)
  { :last-noise  (q/millis)
    :noise-array (generate-noise) })

(defn update-state [state]
  (let [[last-noise noise-array] (update-noise (:last-noise state) (:noise-array state))]
  { :last-noise  last-noise
    :noise-array noise-array }))

(defn draw-state [state]
  (q/background 255)
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
    (q/stroke 0 30)
    (q/no-fill)
    (q/stroke 20 50 70)
    (dorun (map #(apply q/line %) line-args))))

(q/defsketch storefront
  :title "Twitchy Spiral"
  :size :fullscreen
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top :present]
  :middleware [m/fun-mode])

(defn -main [& args])
