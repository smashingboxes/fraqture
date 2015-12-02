(ns storefront.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quil.helpers.drawing :refer [line-join-points]]
            [quil.helpers.seqs :refer [range-incl steps]]
            [quil.helpers.calc :refer [mul-add]]))

(defn noisify [sample]
  (* 100 (q/noise sample)))

(defn setup []
  (q/frame-rate 30)
  (q/background 255)
  (q/stroke-weight 5)
  (q/smooth)
  (let [radius    100
        cent-x    250
        cent-y    250
        rad-noise (steps (rand 10) 0.05)
        rad-noise (map noisify rad-noise)
        rads      (map q/radians (range-incl 0 1440 5))
        radii     (steps 10 0.5)
        radii     (map (fn [rad noise] (+ rad noise -100)) radii rad-noise)
        xs        (map (fn [rad radius] (mul-add (q/cos rad) radius cent-x)) rads radii)
        ys        (map (fn [rad radius] (mul-add (q/sin rad) radius cent-y)) rads radii)
        line-args (line-join-points xs ys)]
    (q/stroke 0 30)
    (q/no-fill)
    (q/ellipse cent-x cent-y (* radius 2) (* radius 2))
    (q/stroke 20 50 70)
    (dorun (map #(apply q/line %) line-args)))

  { })

(defn update-state [state]
  { })

(defn draw-state [state])

(q/defsketch storefront
  :title "Spiral Demo"
  :size [500 500]
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  :middleware [m/fun-mode])

(defn -main [& args])
