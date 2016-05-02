(ns fraqture.tweetreader
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q]
            [clojure.string :as string]
            [fraqture.textify :as textify]
            [clojure.tools.cli :refer [parse-opts]]
            [fraqture.led-array :as led]
            [fraqture.stream :as stream])
  (:import  [fraqture.drawing Drawing]))

(def test-tweet "@test-tweet
  The text of the tweet goes here. For a longer tweet, I'm hoping it wraps around nicely.")

(def y-offset 50)
(def chars-per-line 60)
(def padding-time 30)
(def qwerty (apply hash-map [
  ; row column width height
  "1" [0 0 1 1]
  "2" [0 1 1 1]
  "3" [0 2 1 1]
  "4" [0 3 1 1]
  "5" [0 4 1 1]
  "6" [0 5 1 1]
  "7" [0 6 1 1]
  "8" [0 7 1 1]
  "9" [0 8 1 1]
  "0" [0 9 1 1]
  "a" [2 0 1 1]
  "b" [3 5 1 1]
  "c" [3 3 1 1]
  "d" [2 2 1 1]
  "e" [1 2 1 1]
  "f" [2 3 1 1]
  "g" [2 4 1 1]
  "h" [2 5 1 1]
  "i" [1 7 1 1]
  "j" [2 6 1 1]
  "k" [2 7 1 1]
  "l" [2 8 1 1]
  "m" [3 7 1 1]
  "n" [3 6 1 1]
  "o" [1 8 1 1]
  "p" [1 9 1 1]
  "q" [1 0 1 1]
  "r" [1 3 1 1]
  "s" [2 1 1 1]
  "t" [1 3 1 1]
  "u" [1 5 1 1]
  "v" [3 4 1 1]
  "w" [1 1 1 1]
  "x" [3 2 1 1]
  "y" [1 5 1 1]
  "z" [3 1 1 1]
  "'" [3 9 2 1]
  ";" [2 9 1 1]
  "\\" [2 10 1 1]
  "," [3 8 1 1]
  "." [4 8 1 1]
  " " [4 2 5 1]
  "\n" [3 9 2 1]]))

(def key-convert (apply hash-map [
  "!" "1"
  "@" "2"
  "#" "3"
  "$" "4"
  "%" "5"
  "^" "6"
  "&" "7"
  "*" "8"
  "(" "9"
  ")" "0"
  "\"" "'"
  ":" ";"
  "<" ","
  ">" "."
  "?" "/" ]))

(def shift-key [4 0 2 1])
(def offsets [10 5])

(defn character-to-place [character]
  (let [lower-case (string/lower-case character)
        converted-key (get key-convert lower-case)
        final-key (or converted-key lower-case)]
    (or (get qwerty final-key) [0 0 0 0])))

(defn properly-spaced [[row col width height]]
  (let [[row-offset col-offset] offsets]
    [(+ row-offset row) (+ col-offset (* 2 col)) (* 2 width) height]))

; Emulate a QWERTY keyboard on the LED board
(defn light-key [serial character [r g b]]
  (let [[row col width height] (-> character (character-to-place) (properly-spaced))]
    (led/paint-window serial row col (+ row height) (+ col width) [r g b])))

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
      (q/line x (+ y 7) (+ x 9) (+ y 10))
      (q/line (+ x 9) (+ y 10) (+ x 18) (+ y 7))]))
  (q/text-char character x y))

; Mapping function that takes in the two states and a mask and returns the proper state
(defn resolve-state [initial-state final-state is-final?]
  (if is-final? final-state initial-state))

; Take in the state arrays and mask and write it out
(defn write-characters [new-chars? serial strarray initials finals mask]
  (let [fullstr (apply str strarray)
        strlen (count fullstr)
        current-states (take strlen (map resolve-state initials finals mask))
        last-char (last fullstr)]
    (if new-chars? (do (led/clear serial) (light-key serial last-char [255 255 255])))
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
  (let [tweet-file (stream/get-tweet!)
        tweet-text (if (nil? tweet-file) test-tweet (slurp tweet-file))
        tweet-split (string/split tweet-text #"\n")
        tweet-author (first tweet-split)
        tweet-body (string/join " " (rest tweet-split))
        words (string/split tweet-body #" ")
        tweet-lines (concat [tweet-author] (reduce create-string-array [""] words))
        textify-options (:options (parse-opts "" textify/cli-options))
        textify-options (assoc textify-options :serial (:serial options))]
    (q/frame-rate 30)
    (q/text-font (q/create-font "Monoid-Regular.ttf" 30))
    (q/stroke-weight 3)
    { :textify-state (textify/setup textify-options)
      :write-index 1
      :message tweet-lines
      :initial-states (compute-initial-states tweet-lines (q/text-width " "))
      :final-states (compute-final-states tweet-lines)
      :mask-order (shuffled-indexes (count (apply str tweet-lines)))
      :serial (:serial options)
      :done? false
      :leds-left (shuffle (range 540))
      :leds '() }))

(defn update-textify [state]
  (if (:done? state) (assoc state :textify-state (textify/update-state (:textify-state state))) state))

(defn update-leds [state second-stage?]
  (if second-stage?
    (let [[current rest] (split-at 3 (:leds-left state))]
      (-> state
        (assoc :leds current)
        (assoc :leds-left rest)))
    state))

(defn set-done-time [state last-done now-done]
  (if (and (not last-done) now-done)
    (assoc state :done-at (q/millis))
    state))

(defn update-done [state now-done]
  (let [last-done (:done? state)]
    (-> state (set-done-time last-done now-done) (assoc :done? now-done))))

(defn update-state [state]
  (let [write-index (inc (:write-index state))
        str-len (count (apply str (:message state)))
        left-over (max (- write-index str-len padding-time) 0)
        new-chars? (< write-index str-len)
        mask (current-mask (:mask-order state) left-over)
        done? (> left-over (+ str-len padding-time))
        second-stage? (and (> left-over 0) (not done?))]
    (-> state
        (assoc :clear-last-key? (= write-index (+ 1 str-len)))
        (update-done done?)
        (assoc :new-chars? new-chars?)
        (assoc :mask mask)
        (assoc :write-index write-index)
        (update-textify)
        (update-leds second-stage?))))

(defn random-color []
  [(rand 255) (rand 255) (rand 255)])

(defn clear-last-character [state]
  (if (:clear-last-key? state) (led/clear (:serial state))))

(defn draw-state [state]
  (if (:done? state)
    (textify/draw-state (:textify-state state))
    (doall
      [(q/background 30)
       (write-characters
         (:new-chars? state)
         (:serial state)
         (clip-to-length (:message state) (:write-index state))
         (:initial-states state)
         (:final-states state)
         (:mask state))
       (clear-last-character state)
       (doseq [pixel (:leds state)] (led/paint-pixel (:serial state) pixel (random-color)))
       (led/refresh (:serial state))
       (q/delay-frame 100)])))

(defn exit [state]
  (let [done-at (:done-at state)]
    (if done-at
      (< (+ done-at 30000) (q/millis)))))

(def drawing (Drawing. "tweet reader" setup update-state draw-state nil exit nil))
