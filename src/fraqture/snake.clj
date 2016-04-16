(ns fraqture.snake
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [fraqture.led-array :as led]
            [quil.core :as q :include-macros true])
  (:import  [fraqture.drawing Drawing]))

; Grid: 2x2 on leds -> 15 columns
; Given a 16:9 screen, thats roughly 9 rows on the screen and 8 LED rows (neglect top and bottom row)

(def column-count 15)
(def screen-row-count 9)
(def led-row-count 4)
(def row-count (+ (* 2 led-row-count) screen-row-count))

(defn is-led-row [row]
  (or (< row 4) (> row (+ 3 screen-row-count))))

(defn game-row-to-led-row [row]
  (let [base-row (if (> row 3) (- row screen-row-count) row)]
    (-> base-row (* 2) (+ 1))))

(defn draw-led-block [serial row col color]
  (let [shifted-row (game-row-to-led-row row)
        shifted-col (* col 2)]
    (led/paint-window serial shifted-row shifted-col (+ shifted-row 2) (+ shifted-col 2) color)))

(defn draw-screen-block [row col color]
  (let [width  (/ (q/width) column-count)
        height (/ (q/height) screen-row-count)
        x      (* width col)
        y      (* height (- row 4))]
    (apply q/fill color)
    (q/rect x y width height)))

(defn draw-block [serial row col color]
  (if (is-led-row row)
    (draw-led-block serial row col color)
    (draw-screen-block row col color)))

(defn setup [options]
  (q/frame-rate 30)
  { :options options
    :serial (:serial options)
    :positions '([0 4] [0 3])
    :direction :south
    :last-render (q/millis)
    :render-delay 700
    :food-position [0 6]})

(def directions { :south [0 1] :east [1 0] :north [0 -1] :west [-1 0] })

(defn add-vector [[v1x v1y] [v2x v2y]]
  [(+ v1x v2x) (+ v1y v2y)])

(defn clamp-position [[x y]]
  [(cond (= x column-count) 0 (= x -1) (- column-count 1) :else x)
   (cond (= y (+ 8 screen-row-count)) 0 (= y -1) (+ 7 screen-row-count) :else y)])

(defn apply-movement [position direction]
  (clamp-position (add-vector position (get directions direction))))

(defn remove-tail-unless-growing [positions growing?]
  (if growing? positions (butlast positions)))

(defn move-head [direction growing?]
  (fn [positions]
    (-> positions
        (remove-tail-unless-growing growing?)
        (conj (apply-movement (first positions) direction)))))

(defn draw-food [serial [row col]]
  (draw-block serial col row [200 70 100]))

(defn update-positions [state]
  (update-in state [:positions] (move-head (get state :direction) (get state :is-eating?))))

(defn update-timing [state]
  (assoc state :render-delay (+ 200 (/ 800 (count (:positions state))))))

(defn update-eating [state]
  (assoc state :is-eating?
    (-> state (get :positions) (first) (apply-movement (get state :direction)) (= (:food-position state)))))

(defn update-render [state]
  (if (> (q/millis) (+ (:last-render state) (:render-delay state)))
    (assoc state :last-render (q/millis) :render? true)
    (assoc state :render? false)))

(defn not-snake-occupied [state]
  (filter
    (fn [pos] (not-any? #(= pos %) (:positions state)))
    (for [row (range row-count) col (range column-count)] [col row])))

(defn update-food [state]
  (if (:is-eating? state)
    (assoc state :food-position (rand-nth (not-snake-occupied state)))
    state))

(defn possible-next-positions [positions]
  (->> [:north :south :east :west]
       (map (fn [dir] [dir (apply-movement (first positions) dir)]))
       (filter (fn [[dir pos]] (not-any? #(= pos %) positions)))))

(defn manhattan-distance [[x1 y1] [x2 y2]]
  (+ (Math/abs (- x1 x2)) (Math/abs (- y1 y2))))

(defn position-distances [positions food]
  (map (fn [[dir pos]] [dir pos (manhattan-distance food pos)]) positions))

(defn find-nearest-obstacle
  ([dir pos state] (find-nearest-obstacle dir pos state 0))
  ([dir pos state count]
    (let [new-pos (apply-movement pos dir)]
      (if (some (partial = new-pos) (:positions state))
        count
        (find-nearest-obstacle dir new-pos state (inc count))))))

(defn best-dir [positions state]
  (let [current-trajectory (apply-movement (first (:positions state)) (:direction state))
        ordered-by-distance (sort-by #(nth % 2) positions)
        lowest-distance (nth (first ordered-by-distance) 2)
        only-fastest (filter #(= (nth % 2) lowest-distance) ordered-by-distance)
        same-direction (first (filter #(= (first %) (:direction state)) only-fastest))
        turning-because-snake? (some (partial = current-trajectory) (:positions state))
        obstacle-set (if turning-because-snake? positions only-fastest)
        nearest-obstacles (map (fn [[dir pos dis]] [dir (find-nearest-obstacle dir pos state)]) obstacle-set)
        least-obstacles (first (sort-by second > nearest-obstacles))]
    (first (or same-direction least-obstacles))))

(defn update-direction [state]
  (let [best-direction (-> (possible-next-positions (:positions state))
                           (position-distances (:food-position state))
                           (best-dir state))]
    (if (nil? best-direction)
      (setup (:options state))
      (assoc state :direction best-direction))))

(defn update-frame-if-rendering [state]
  (if (:render? state)
    (-> state (update-timing) (update-direction) (update-eating) (update-positions) (update-food))
    state))

(defn update-state [state]
  (-> state (update-render) (update-frame-if-rendering)))

(defn render-frame [state]
  (q/background 70 100 100)
  (led/clear (:serial state))
  (draw-food (:serial state) (:food-position state))
  (doseq [[col row] (:positions state)] (draw-block (:serial state) row col [255 255 255])))

(defn draw-state [state]
  (if (:render? state) (render-frame state)))

(def drawing
 (Drawing. "Snake" setup update-state draw-state nil nil nil))
