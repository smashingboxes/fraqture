(ns fraqture.stream
  (:require [clojure.java.io :as io]
            [clojure.set :refer :all]
            [clojure.string :refer [split]]
            [fraqture.helpers :refer [image-extensions]]))

(def tweet-extensions [".txt"])
(def map-extensions [".osm"])

; Store the state in the form:
; "foldername" -> FolderState
(defonce file-states (atom {}))

; FolderState
; current -> the last selected object
; all -> a hash of { "filename" -> display-count }
(defrecord FolderState [current all])

(defn valid? [extensions]
  (fn [filename]
    (let [extension (->> (split filename #"\.") (last) (str "."))]
      (.contains extensions extension))))

(defn update-folder-state [folder-name extensions]
  (let [folder-state (or (get @file-states folder-name) {})
        known-file-hash (or (:all folder-state) {})
        known-file-set (->> known-file-hash (keys) (set))
        live-file-set (->> folder-name (io/file) (.list) (map str) (filter (valid? extensions)) (set))
        removed-files (difference known-file-set live-file-set)
        known-file-hash (reduce (fn [hash file] (dissoc hash file)) known-file-hash removed-files)
        new-files (difference live-file-set known-file-set)
        new-hash (->> new-files (map #(vector % 0)) (into {}))]
    [(:current folder-state) (merge new-hash known-file-hash)]))

; Todo: Use a weighted algorithm to display lesser shown image
(defn rand-weighted [folder-contents]
  (rand-nth (keys folder-contents)))

(defn archive? [filename]
  (re-seq #"once_" filename))

(defn archive-file [file-path]
  (let [filename (-> file-path (split #"/") (last))
        new-filename (str "archived/" filename)]
    (io/copy (io/file file-path) (io/file new-filename))
    (io/delete-file file-path)
    new-filename))

(defn get-file!
  "Retrieve a file from the given folder, highly prioritizing new images"
  [folder-name extensions]
  (let [[current-file folder-contents] (update-folder-state folder-name extensions)
        new-files (->> folder-contents (filter #(= (second %) 0)) (map first))
        except-last (->> folder-contents (filter #(not= current-file %)) (into {}))
        filename (if (empty? new-files) (rand-weighted folder-contents) (rand-nth new-files))
        folder-contents (if (empty? folder-contents) {} (update-in folder-contents [filename] inc))
        file-path (str folder-name "/" filename)
        archived (if (archive? filename) (archive-file file-path))]
    (if (nil? archived)
      (swap! file-states assoc folder-name (->FolderState filename folder-contents)))
    (or archived file-path)))

(defn get-tweet! [] (get-file! "tweets" tweet-extensions))
(defn get-raster! [] (get-file! "rasters" image-extensions))
(defn get-logo! [] (get-file! "logos"  image-extensions))
(defn get-map! [] (get-file! "maps" map-extensions))
