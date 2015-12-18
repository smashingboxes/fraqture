(ns storefront.spirograph
  (:require [storefront.drawing]
            [quil.core :as q :include-macros true])
  (:import  [storefront.drawing Drawing]))

(def current-color-path-location 0)
(def color-path
  [{:r 153 :g 255 :b 204}
   {:r 255 :g 0 :b 127}
   {:r 255 :g 153 :b 51}
   {:r 153 :g 204 :b 255}
   {:r 255 :g 255 :b 51}
   {:r 204 :g 153 :b 255}])

(defn setup []
  (q/frame-rate 30)
  (let [max-r (/ (q/width) 2)
        n (int (q/map-range (q/width) 100 130 20 50))]
   {:dots (into [] (for [r (map #(* max-r %) (range 0.15 1 (/ n)))]
                        [r 0]))
    :bg-color (nth color-path 0)}))

(defn speed[]
  (+ 0.0004 (* 0.0003 (q/sin (* (q/millis) 0.00025)))))

(defn move [dot]
  (let [[r a] dot]
    [r (+ a (* r (speed)))]))

(defn next-color-index []
  (if (= (+ 1 current-color-path-location) (count color-path))
    (def current-color-path-location 0))
    (def current-color-path-location (inc current-color-path-location)))

(defn walk-color-channel [current upcoming]
  (if (= upcoming current)
    current
    (if (> upcoming current)
      (inc current)
      (dec current))))

(defn walk-color[current-bg-color]
  (apply q/background (vals current-bg-color))
  (let [upcoming-color (nth color-path current-color-path-location)]
    (if (= current-bg-color upcoming-color)
      (do
        (next-color-index)
        (walk-color current-bg-color))
      {:r (walk-color-channel (:r current-bg-color) (:r upcoming-color))
       :g (walk-color-channel (:g current-bg-color) (:g upcoming-color))
       :b (walk-color-channel (:b current-bg-color) (:b upcoming-color))})))

(defn update-state [state]
  (-> state
    (update-in [:dots] #(map move %))
    (update-in [:bg-color] #(walk-color %))))

(defn dot->coord [[r a]]
  [(+ (/ (q/width) 2) (* r (q/cos a)))
   (+ (/ (q/height) 2) (* r (q/sin a)))])

(defn pulse [low high rate]
  (let [diff (- high low)
        half (/ diff 2)
        mid (+ low half)
        s (/ (q/millis) 1000.0)
        x (q/sin (* s (/ 1.0 rate)))]
    (+ mid (* x half))))

(defn draw-state [state]
  (q/fill (pulse 20 50 2.0) 230 (pulse 150 200 1.0))
  (let [dots (:dots state)]
    (loop [curr (first dots)
           tail (rest dots)
           prev nil]
      (let [[x y] (dot->coord curr)]
        (q/ellipse x y 20 20))
      (when (seq tail)
        (recur (first tail)
               (rest tail)
               curr)))))

(def drawing (Drawing. "Spirograph" setup update-state draw-state :fullscreen [:keep-on-top :present]))
