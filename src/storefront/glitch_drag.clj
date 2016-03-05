(ns storefront.glitch-drag
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]))

(def column-count 30)

(defn clamp-rgb [rgb]
  (max (min rgb 255) 0))

(defn jitter [max-jitter]
  (fn [d] (+ d (- (rand-int (inc max-jitter)) (/ max-jitter 2)))))

(defn color-walk [color jitter-amount]
  (map clamp-rgb (map (jitter jitter-amount) color)))

(defn random-color []
  [(rand-int 255) (rand-int 255) (rand-int 255)])

(defn cycle-index [column]
  (mod (inc (:current-index column)) (:y-count column)))

(defn color-with-opac [color]
  (conj (into [] color) 120))

(defn rect-at-index [x-index y-index y-count color]
  (let [width             (/ (q/width) column-count)
        height            (/ (q/height) y-count)
        x                 (* width x-index)
        y                 (* height y-index)]
    (q/no-stroke)
    (apply q/fill (color-with-opac color))
    (q/rect x y width height)))

(defrecord Column [current-index color y-count])

(defn color-to-rgb [color]
  [(q/red color) (q/green color) (q/blue color)])

(defn setup-new-image [last-file y-blocks]
  (let [image-file  (random-image-file :except #{last-file})
        column-y-blocks (repeatedly column-count #(rand-int y-blocks))
        column-ys   (map #(* % (/ (q/height) y-blocks)) column-y-blocks)
        column-xs   (map #(* % (/ (q/width) column-count)) (range column-count))
        raw-samples (map (fn [x y] (q/get-pixel x y)) column-xs column-ys)
        samples     (map #(color-to-rgb %) raw-samples)
        columns     (map (fn [y c] (->Column y c (+ 20 (rand-int 20)))) column-y-blocks samples)]
    (q/image (q/load-image image-file) 0 0 (q/width) (q/height))
    { :image-file image-file
      :last-update (q/millis)
      :columns  columns }))

(def cli-options
  [
    ["-y" "--y-blocks INT" "Number of blocks in the vertical direction"
      :default 30
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 2 % 200) "Must be a number between 0 and 200"]]
    ["-u" "--update-interval INT" "Number of seconds between switching images"
      :default 20
      :parse-fn #(Integer/parseInt %)]
    ["-j" "--jitter-amount INT" "How much to vary the color as it slides down"
      :default 10
      :parse-fn #(Integer/parseInt %)]
  ])

(defn setup [options]
    (q/frame-rate 10)
    (-> (setup-new-image nil (:y-blocks options))
        (assoc state :options options)
        (assoc state :times-run 0)))

(defn update-column-generator [jitter-amount]
  (fn [column]
    (Column. (cycle-index column) (color-walk (:color column) jitter-amount) (:y-count column))))

(defn update-state [state]
  (let [options         (:options state)
        update-interval (:update-interval options)
        times-run       (inc (:times-run state))
        jitter-amount   (:jitter-amount options)
        y-blocks        (:y-blocks options)]
    (if (> (time-elapsed (:last-update state)) (seconds update-interval))
      (assoc (setup-new-image (:image-file state) y-blocks) :options options :times-run times-run)
      (update-in state [:columns] #(map (update-column-generator jitter-amount) %)))))

(defn draw-state [state]
  (dorun
    (map-indexed
      (fn [idx column]
        (rect-at-index idx (:current-index column) (:y-count column) (:color column)))
      (:columns state))))

(defn exit? [state] (>= (:times-run state) 2))

(def drawing
  (Drawing. "Drag Glitch" setup update-state draw-state cli-options exit? nil))
