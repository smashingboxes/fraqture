(ns storefront.glitch-drag
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [storefront.led-array :as led]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(defn update [collection index function]
  (assoc collection index (function (get collection index))))

(defn any? [function collection] (not (nil? (some function collection))))

(defrecord Column [index status row-index color])
(def column-count 30)
(def row-count 30)

; Setup functions
(defn color-to-rgb [color]
  [(q/red color) (q/green color) (q/blue color)])

(defn setup-new-image
  "Select a new image, display it, and setup the columns for trickle"
  [last-file serial]
  (let [image-file  (random-image-file :except #{last-file})
        sample-row-indices (repeatedly column-count #(rand-int row-count))
        sample-ys   (map #(* % (/ (q/height) row-count)) sample-row-indices)
        sample-xs   (map #(* % (/ (q/width) column-count)) (range column-count))
        samples     (map (fn [x y] (color-to-rgb (q/get-pixel x y))) sample-xs sample-ys)
        columns     (map (fn [n c] (->Column n :ready 0 c)) (range) samples)]
    (q/image (q/load-image image-file) 0 0 (q/width) (q/height))
    (led/clear serial)
    { :image-file image-file
      :columns  (vec columns) }))

; Update functions
(defn all-columns-done?
  "Return if every column has a status of :finished"
  [columns]
  (every? #(= (:status %) :finished) columns))

(defn all-columns-inactive?
  "Return if no columns are active"
  [columns]
  (not-any? #(= (:status %) :active) columns))

(defn any-column-index-x
  "Generates a function that will return if any column is at row index [x]"
  [x]
  (fn [columns]
    (any? #(= (:row-index %) x) columns)))

(defn indexes-of-col-status
  "Return the index of all columns with a given status"
  [columns status]
  (remove nil? (map-indexed (fn [idx col] (if (= (:status col) status) idx nil)) columns)))

(defn column-each
  "Takes in the `columns` and an array of indexes that should be updated and applies
  `function` to all of those indexes"
  [columns act-on-indexes function]
  (reduce (fn [cols current] (update cols current function)) columns act-on-indexes))

(defn activate-random-columns
  "Activates `count` random columns from all columns marked as :ready"
  [columns count]
  (let [ready-indexes (indexes-of-col-status columns :ready)
        ready-indexes (shuffle ready-indexes)
        ready-indexes (take count ready-indexes)]
    (column-each columns ready-indexes #(assoc % :status :active))))

(defn activate-columns-if-ready
  "Calls the activate-random-columns function if any column is 1/3 done"
  [columns]
  (if (or ((any-column-index-x 10) columns)
          (all-columns-inactive? columns))
      (activate-random-columns columns 1) columns))

(defn advance-active-column
  "Advances the row index of an active column, setting it to :finished if it is done."
  [column]
  (let [new-row-index (inc (:row-index column))
        new-status (if (= new-row-index (+ row-count 17)) :finished :active)]
    (assoc column :row-index new-row-index :status new-status)))

(defn advance-all-active
  "Applies advance-active-column to all active columns"
  [columns]
  (let [active-indexes (indexes-of-col-status columns :active)]
    (column-each columns active-indexes advance-active-column)))

(defn update-columns
  "Updates the state of all columns"
  [columns]
  (-> columns
      activate-columns-if-ready
      advance-all-active))

; Draw functions
(defn rect-at-index
  "Draws a rectangle at the given index with color `color`"
  [x-index y-index y-count color]
  (let [width             (/ (q/width) column-count)
        height            (/ (q/height) y-count)
        x                 (* width x-index)
        y                 (* height y-index)]
    (q/no-stroke)
    (apply q/fill color)
    (q/rect x y width height)))

(defn is-led?
  "Checks if the given row should be displayed on an LED or the screen."
  [row-c row-n]
  (or (< row-n 9)
      (> row-n (+ row-c 8))))

(defn to-led-index
  "Converts the overall row-index to an LED index"
  [row-c row-n]
  (assert (is-led? row-c row-n))
  (if (> row-n 9)
    (- row-n row-c)
    row-n))

(defn to-screen-index
  "Converts the overall row-index to a screen rectangle index"
  [row-c row-n]
  (assert (not (is-led? row-c row-n)))
  (- row-n 9))

(defn draw-column
  "Draw a column using the appropriate method."
  [column serial]
  (let [col-n (:index column)
        row-n (:row-index column)
        color (:color column)]
    (if (is-led? row-count row-n)
      (let [row-n (to-led-index row-count row-n)]
        (led/paint-window serial row-n col-n (inc row-n) (inc col-n) color))
      (rect-at-index col-n (to-screen-index row-count row-n) row-count color))))

; Storefront functions
(defn setup [options]
  (q/frame-rate 10)
  (-> (setup-new-image nil (:serial options))
      (assoc :options options)
      (assoc :times-run 0)))

(defn update-state [state]
  (cond
    (and (:finished-at state) (> (q/millis) (+ (:finished-at state) 5000)))
      (-> state
          (merge (setup-new-image (:image-file state) (:serial (:options state))))
          (assoc :finished-at nil)
          (update :times-run inc))
    (not (nil? (:finished-at state)))
      state
    (and (nil? (:finished-at state)) (all-columns-done? (:columns state)))
      (assoc state :finished-at (q/millis))
    :else
      (update state :columns update-columns)))

(defn draw-state [state]
  (let [active-columns (filter #(= (:status %) :active) (:columns state))
        serial (:serial (:options state))]
    (dorun (map #(draw-column % serial) active-columns))
    (led/refresh serial)))

(defn exit? [state] (= (:times-run state) 1))

(def drawing
  (Drawing. "Drag Glitch" setup update-state draw-state nil exit? nil))
