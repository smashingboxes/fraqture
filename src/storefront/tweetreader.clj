(ns storefront.tweetreader
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.string :as str])
  (:import  [storefront.drawing Drawing]))

(def tweeter "@smashingboxes")
(def tweet "Designer @schombombadil talks to @designationio about making stuff at SB and smashing established UI patterns. http://sbox.es/1Qxe9OH")
(def words (str/split tweet #" "))
(def y-offset 34)
(def chars-per-line 80)

(defn format-string
  [current appended])

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

(def tweet-lines
  (concat [tweeter] (reduce create-string-array [""] words)))

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

(defn setup [options]
  (let [font  (q/create-font "Monoid-Regular.ttf" 20)
        _     (q/text-font font)
        width (q/text-width " ")]
    { :write-index 0
      :monoid font
      :char-width width
      :x-offset (/ (- (q/width) (* chars-per-line width)) 2)
      :current-str [] }))

(defn update-state [state]
  (-> state
      (update-in [:write-index] inc)
      (assoc :current-str (clip-to-length tweet-lines (:write-index state)))))

(defn style-line [line]
  (if (= line 0) (q/fill 235 23 103) (q/fill 255)))

(defn write-letter [character char-width x-offset line-no index]
  (style-line line-no)
  (q/text-char character (+ x-offset (* index char-width)) (+ 320 (* y-offset line-no))))

(defn draw-state [state]
  (q/background 30)
  (doall
    (map-indexed
      (fn [idx line]
        (doall
          (map-indexed
            #(write-letter %2 (:char-width state) (:x-offset state) idx %1)
            line)))
      (:current-str state)))
  (q/delay-frame 100))

(def drawing (Drawing. "tweet reader" setup update-state draw-state nil nil))
