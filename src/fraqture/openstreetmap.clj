(ns fraqture.openstreetmap
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q]
            [fraqture.stream :as stream]
            [fraqture.led-array :as led]
            [clojure.data.xml :as xml]
            [clojure.java.io :as io])
  (:import  [fraqture.drawing Drawing]))

(def cli-options )

(defn parse-osm-data [osm]
  ;(-> osm (:content) (:bounds)
  (let [bounds    (->> osm 
                      (:content)
                      (filter #(= (:tag %) :bounds))
                      (first)
                      (:attrs))
        nodes     []
        ways      []
        relations []]
  {
    :nodes nodes
    :ways ways
    :relations relations
    :bound bounds
  }))

(defn setup [options] 
  (let [map-file (stream/get-map!)
        xml-input-stream (io/input-stream map-file)
        raw-data (xml/parse xml-input-stream)
        osm-data (parse-osm-data raw-data)]
        { :file map-file
          :osm-data osm-data }))

(defn update-state [state] state)

(defn draw-screen [state] )

(defn draw-leds [state] )

(defn draw-state [state] 
  (draw-screen state)
  (draw-leds state))

(defn exit? [state] )

(def drawing
  (Drawing. "Shifting Grid" setup update-state draw-state nil exit? :raster))
