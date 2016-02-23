(ns storefront.tweetreader
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.string :as str]
            [storefront.textify :as textify]
            [storefront.led-array :as led])
  (:import  [storefront.drawing Drawing]))

(def tweeter "@smashingboxes")
(def tweet "Designer @schombombadil talks to @designationio about making stuff at SB and smashing established UI patterns. http://sbox.es/1Qxe9OH")
(def words (str/split tweet #" "))
(def y-offset 34)
(def chars-per-line 80)
(def padding-time 30)

; This will split a string into an array of < 80 character strings
(defn create-string-array [str_array word]
  (let [len (count word)
        current (last str_array)
        line_length (count current)
        other_strings (pop str_array)]
    (cond
      (> (+ line_length len) chars-per-line) (conj str_array word)
      (= 0 (count current)) (conj other_strings word)
      :else (conj other_strings (str current " " word)))))


; The reduction function that will return N characters from an array of strings
(defn reduce-string-array [[array-out chars-left] string]
  (let [strlen (count string)]
    (cond
      (= chars-left 0) [array-out 0]
      (> strlen chars-left) [(conj array-out (apply str (take chars-left string))) 0]
      :else [(conj array-out string) (- chars-left strlen)])))

; Take an array of strings and return an array of strings with n
; characters.
(defn clip-to-length [str-array length]
  (let [[new-array _chars] (reduce reduce-string-array [[] length] str-array)]
    new-array))

; Create the initial state for a given letter. Returns [[r g b] x y]
(defn initial-letter-state [char-width line-no index]
  (let [color (if (= line-no 0) [235 23 103] [255 255 255])
        y-start (- (/ (q/height) 2) 100)
        x-offset (/ (- (q/width) (* chars-per-line char-width)) 2)]
    [color (= line-no 0) (+ x-offset (* index char-width)) (+ y-start (* y-offset line-no))]))

; Curry a state generator with the width
(defn line-to-state [width]
  (fn [line-no text]
    (map-indexed (fn [char-no char] (initial-letter-state width line-no char-no)) text)))

; Creates an array of proper letter positions and colors
(defn compute-initial-states [strarray width]
  (apply concat (map-indexed (line-to-state width) strarray)))

; Returns random colors and placements
(defn final-letter-state []
  [(vec (repeatedly 3 #(rand 255))) false (rand (q/width)) (rand (q/height))])

; Creates an array of random positions and colors
(defn compute-final-states [strarray]
  (let [strlen (count (apply str strarray))]
    (repeatedly strlen final-letter-state)))

; Write a letter from its state
(defn write-letter [[color underline? x y] character]
  (apply q/fill color)
  (if underline?
    (doall
      [(apply q/stroke color)
      (q/line x (+ y 7) (+ x 6) (+ y 10))
      (q/line (+ x 6) (+ y 10) (+ x 13) (+ y 7))]))
  (q/text-char character x y))

; Mapping function that takes in the two states and a mask and returns the proper state
(defn resolve-state [initial-state final-state is-final?]
  (if is-final? final-state initial-state))

; Take in the state arrays and mask and write it out
(defn write-characters [strarray initials finals mask]
  (let [fullstr (apply str strarray)
        strlen (count fullstr)
        current-states (take strlen (map resolve-state initials finals mask))]
    (doall (map (fn [state char] (write-letter state char)) current-states fullstr))))

; Given a length, return a list of shuffled indexes
(defn shuffled-indexes [list-size]
  (shuffle (range list-size)))

; Set the given element in an array to true
(defn mask-reducer [acc cur] (assoc acc cur true))

; Given a static list of the order of characters and an index, return the current mask
(defn current-mask [shuffled index]
  (let [arrlen (count shuffled)
        flipped (take index shuffled)]
    (reduce mask-reducer (vec (repeat arrlen false)) flipped)))

(defn setup [options]
  (let [tweet-lines (concat [tweeter] (reduce create-string-array [""] words))
        image (textify/loader "images/logo.png" false)]
    (q/frame-rate 30)
    (q/text-font (q/create-font "Monoid-Regular.ttf" 20))
    (q/stroke-weight 3)
    { :write-index 1
      :message tweet-lines
      :initial-states (compute-initial-states tweet-lines (q/text-width " "))
      :final-states (compute-final-states tweet-lines)
      :mask-order (shuffled-indexes (count (apply str tweet-lines)))
      :image image
      :serial (:serial options)
      :options { :letters-per-frame 12 :min-letter-size 12 :max-letter-size 36 } }))

(defn update-state [state]
  (led/paint-window (:serial state) 0 (:write-index state) 17 (:write-index state) [255 255 255])
  (-> state
      (update-in [:write-index] inc)))

(defn draw-state [state]
  (let [str-len (count (apply str (:message state)))
        left-over (max (- (:write-index state) str-len padding-time) 0)
        mask (current-mask (:mask-order state) left-over)
        done? (> left-over (+ str-len padding-time))
        serial (:serial state)]
    (if done?
      (textify/draw-state state)
      (doall
        [(q/background 30)
         (write-characters
           (clip-to-length (:message state) (:write-index state))
           (:initial-states state)
           (:final-states state)
           mask)
         (q/delay-frame 10)]))
    (led/draw-mock serial)))

(def drawing (Drawing. "tweet reader" setup update-state draw-state nil nil nil))
