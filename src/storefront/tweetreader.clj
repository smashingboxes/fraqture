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

(defn reducer [str_array word]
  (let [len (count word)
        current (last str_array)
        line_length (count current)
        other_strings (pop str_array)]
    (cond
      (> (+ line_length len) chars-per-line) (conj str_array word)
      (= 0 (count current)) (conj other_strings word)
      :else (conj other_strings (str current " " word)))))

(def words_concat_tuple
  (reduce reducer [""] words))

(def words_concat (first words_concat_tuple))

(defn setup [options]
  (let [font (q/create-font "Monoid-Regular.ttf" 20)]
    (q/text-font font)
    { :write-index 0
      :monoid font
      :char-width (q/text-width " ") }))

(defn update-state [state] state)

(defn style-line [line]
  (if (= line 0) (q/fill 235 23 103) (q/fill 255)))

(defn draw-state [state]
  (let [x-offset (/ (- (q/width) (* chars-per-line (:char-width state))) 2)
        lines (concat [tweeter] words_concat_tuple)]
    (q/background 30)
    (q/text-size 20)
    (q/stroke 235 23 103)
    (q/stroke-weight 2)
    (q/line x-offset 326 (+ x-offset (* (count tweeter) (:char-width state))) 326)

    (doall (map-indexed
      (fn [idx line]
        (doall [(style-line idx) (q/text line x-offset (+ 320 (* y-offset idx)))]))
      lines))))

(def drawing (Drawing. "tweet reader" setup update-state draw-state nil nil))
