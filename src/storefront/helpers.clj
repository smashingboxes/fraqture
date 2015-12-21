(ns storefront.helpers
  (:require [quil.core :as q]))

; Milliseconds to seconds
(defn seconds [x] (* 1000 x))

; Milliseconds elapsed since
(defn time-elapsed [last-time]
  (- (q/millis) last-time))

; Random in range
(defn rand-in-range [low high]
  (+ (rand-int (- high low)) low))

; Accepted file extensions
(def image-extensions [".png" ".jpg" ".jpeg"])
(defn valid-image [file]
  (some true? (map #(.endsWith (.getName file) %) image-extensions)))

; Get a random file, except the given file
(defn random-image-file
  [& {:keys [except]
      :or {except #{}}}]
  (let [directory      (clojure.java.io/file "./images")
        files          (file-seq directory)
        image-files    (filter valid-image files)
        included-files (apply list (apply disj (set image-files) except))]
    (rand-nth included-files)))

; Average: sum / count
(defn average [numbers]
  (/ (reduce + numbers) (count numbers)))
