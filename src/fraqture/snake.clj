(ns fraqture.snake
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [fraqture.led-array :as led]
            [quil.core :as q :include-macros true])
  (:import  [fraqture.drawing Drawing]))

(def column-count 15)
(def screen-row-count 9)
(def led-row-count 4)
(def row-count (+ (* 2 led-row-count) screen-row-count))
(def directions { :south [0 1] :east [1 0] :north [0 -1] :west [-1 0] })

(defn setup
  "The main setup function."
  [options]
  (q/frame-rate 30)
  { :options options
    :positions '([0 4] [0 3])
    :direction :south
    :last-render (q/millis)
    :render-delay 700
    :food-position [0 6]})


; Setup Functions
(defn add-positions
  "Add two positions together"
  [[v1x v1y] [v2x v2y]]
  [(+ v1x v2x) (+ v1y v2y)])

(defn wrap-position
  "When travelling off the screen, wrap-around"
  [[x y]]
  [(cond (= x column-count) 0 (= x -1) (- column-count 1) :else x)
   (cond (= y (+ 8 screen-row-count)) 0 (= y -1) (+ 7 screen-row-count) :else y)])

(defn apply-movement
  "Get a new position given a position and direction"
  [position direction]
  (->> direction (get directions) (add-positions position) (wrap-position)))

(defn remove-tail-unless-growing
  "Removes the tail if growing is set"
  [positions growing?]
  (if growing? positions (butlast positions)))

(defn move-head
  "Move the head of the snake and delete the tail (unless the snake is feeding)"
  [direction growing?]
  (fn [positions]
    (-> positions
        (remove-tail-unless-growing growing?)
        (conj (apply-movement (first positions) direction)))))

(defn update-positions
  "Update the locations of the snake body pieces"
  [state]
  (update-in state [:positions] (move-head (get state :direction) (get state :is-eating?))))

(defn update-timing
  "Update the time between frames based on how big the snake is"
  [state]
  (assoc state :render-delay (+ 200 (/ 800 (count (:positions state))))))

(defn update-eating
  "Check if the snake is currently eating. (i.e. the head is on top of food)"
  [state]
  (assoc state :is-eating?
    (-> state (get :positions) (first) (apply-movement (get state :direction)) (= (:food-position state)))))

(defn not-snake-occupied
  "Return all positions that are not occupied by the snake"
  [state]
  (filter
    (fn [pos] (not-any? #(= pos %) (:positions state)))
    (for [row (range row-count) col (range column-count)] [col row])))

(defn update-food
  "When the snake has eaten his food, create a new food piece at a random place
   not occupied by the snake."
  [state]
  (if (:is-eating? state)
    (assoc state :food-position (rand-nth (not-snake-occupied state)))
    state))

(defn possible-next-positions
  "Find which moves are even possible. Having no moves left is a loss condition"
  [positions]
  (->> [:north :south :east :west]
       (map (fn [dir] [dir (apply-movement (first positions) dir)]))
       (filter (fn [[dir pos]] (not-any? #(= pos %) positions)))))

(defn manhattan-distance
  "Find the manhattan distance from point A to point B."
  [[x1 y1] [x2 y2]]
  (+ (Math/abs (- x1 x2)) (Math/abs (- y1 y2))))

(defn manhattan-with-wraparound
  "Project the manhattan distances using wraparound"
  [pos-1 pos-2]
  (let [potential-x-offsets [(- column-count) 0 column-count]
        potential-y-offsets [(- row-count) 0 row-count]
        potential-offsets (for [x potential-x-offsets y potential-y-offsets] [x y])
        realized-points (map #(add-positions % pos-1) potential-offsets)
        realized-distances (map #(manhattan-distance % pos-2) realized-points)]
    (apply min realized-distances)))

(defn position-distances
  "Create a map of distances to the food piece."
  [positions food]
  (map (fn [[dir pos]] [dir pos (manhattan-with-wraparound food pos)]) positions))

(defn find-nearest-obstacle
  "Recurse through looking for how many blocks it will take to hit a snake piece
   in a given direction."
  ([dir pos state] (find-nearest-obstacle dir pos state 0))
  ([dir pos state count]
    (let [new-pos (apply-movement pos dir)]
      (if (some (partial = new-pos) (:positions state))
        count
        (find-nearest-obstacle dir new-pos state (inc count))))))

(defn best-dir
  "This is essentially the AI. It does the following:
   - Find all moves that share the shortest distance to the food block. If we
     can keep moving in the same direction, prefer that to anything else.
   - If we can't move in the same direction, see if it is because there is a
     snake piece in front of the head. If so, prefer the direction that is
     farthest from other snake pieces. Otherwise, prefer the shortest distance
     to the food block"
  [positions state]
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

(defn update-direction
  "Find the best possible direction to move and update the state accordingly."
  [state]
  (let [best-direction (-> (possible-next-positions (:positions state))
                           (position-distances (:food-position state))
                           (best-dir state))]
    (if (nil? best-direction)
      (setup (:options state))
      (assoc state :direction best-direction))))

(defn update-frame-if-rendering
  "Only update the state on render frames"
  [state]
  (if (:render? state)
    (-> state (update-timing) (update-direction) (update-eating) (update-positions) (update-food))
    state))

(defn update-render
  "Check if it is time to render a new frame."
  [state]
  (if (> (q/millis) (+ (:last-render state) (:render-delay state)))
    (assoc state :last-render (q/millis) :render? true)
    (assoc state :render? false)))

(defn update-state
  "The main update function"
  [state]
  (-> state (update-render) (update-frame-if-rendering)))

; Draw State
(defn is-led-row
  "Returns whether the given row is an LED row or on screen"
  [row]
  (or (< row 4) (> row (+ 3 screen-row-count))))

(defn game-row-to-led-row
  "Converts a snake piece row to an LED row"
  [row]
  (let [base-row (if (> row 3) (- row screen-row-count) row)]
    (-> base-row (* 2) (+ 1))))

(defn draw-led-block
  "Draw a single LED snake block"
  [serial row col color]
  (let [shifted-row (game-row-to-led-row row)
        shifted-col (* col 2)]
    (led/paint-window serial shifted-row shifted-col (+ shifted-row 2) (+ shifted-col 2) color)))

(defn draw-screen-block
  "Draw a single screen snake block"
  [row col color]
  (let [offset 90
        width  (/ (- (q/width) (* 2 offset)) column-count)
        height (/ (q/height) screen-row-count)
        x      (+ (* width col) offset)
        y      (* height (- row 4))]
    (apply q/fill color)
    (q/rect x y width height)))

(defn draw-block
  "Draws a block by delegating to the screen or LED block draw function"
  [serial row col color]
  (if (is-led-row row)
    (draw-led-block serial row col color)
    (draw-screen-block row col color)))

(defn draw-food
  "Draws the food block"
  [serial [row col]]
  (draw-block serial col row [200 70 100]))

(defn render-frame
  "Render a given frame"
  [state]
  (let [serial (:serial (:options state))]
    (q/background 0 0 0)
    (led/clear serial)
    (draw-food serial (:food-position state))
    (doseq [[col row] (:positions state)] (draw-block serial row col [90 130 200]))
    (led/refresh serial)))

(defn draw-state
  "The main draw function"
  [state]
  (if (:render? state) (render-frame state)))

(def drawing
 (Drawing. "Snake" setup update-state draw-state nil nil nil))
