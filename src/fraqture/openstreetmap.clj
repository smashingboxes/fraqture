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

(defn get-tags [osm-object]
  (let [tag-elements (->> osm-object (:content) (filter #(= (:tag %) :tag)))
        tag-pairs (map #(list (:k (:attrs %)) (:v (:attrs %))) tag-elements)
        tags (apply hash-map (apply concat tag-pairs))]
    tags))

(defn get-nodes [way-xml nodes-by-id]
  (let [nds (->> way-xml (:content) (filter #(= (:tag %) :nd)))
        nodes (map #(get nodes-by-id (:ref (:attrs %))) nds)]
      nodes))

(defn parse-osm-data [osm]
  ;(-> osm (:content) (:bounds)
  (let [bounds    (->> osm 
                      (:content)
                      (filter #(= (:tag %) :bounds))
                      (first)
                      (:attrs))
                      
        raw-nodes (->> osm
                      (:content)
                      (filter #(= (:tag %) :node)))
        nodes (map #(hash-map 
                      :id (:id (:attrs %))
                      :lat (:lat (:attrs %))
                      :lon (:lon (:attrs %))
                      :tags (get-tags %)) 
                   raw-nodes)
        nodes-by-id (apply hash-map (apply concat (map #(list (:id %) %) nodes)))
        
        raw-ways  (->> osm
                      (:content)
                      (filter #(= (:tag %) :way)))     
        ways (map #(hash-map 
                      :id (:id (:attrs %))
                      :tags (get-tags %)
                      :nodes (get-nodes % nodes-by-id)) 
                   raw-ways)
        ways-by-id (apply hash-map (apply concat (map #(list (:id %) %) ways)))      
        ]
  {
    :nodes nodes
    :ways ways
    ; :relations nil
    :bound bounds
  }))

(defn setup [options] 
  (let [map-file (stream/get-map!)
        xml-input-stream (io/input-stream map-file)
        raw-data (xml/parse xml-input-stream)
        osm-data (parse-osm-data raw-data)
        _ (println (:ways osm-data))]
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
