(ns fraqture.openstreetmap
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q]
            [fraqture.stream :as stream]
            [fraqture.led-array :as led]
            [clojure.data.xml :as xml]
            [clojure.java.io :as io])
  (:import  [fraqture.drawing Drawing]))

(def lines-per-frame 20) ; this defines the speed of the animation


(defn meters-between
  "Calculate the number of meters between two lat/lon coordinate pairs"
  [lat1 lon1 lat2 lon2]
  (let [lat1 (read-string lat1)
        lon1 (read-string lon1)
        lat2 (read-string lat2)
        lon2 (read-string lon2)
        earthRadius 6378137 ; in meters
        d2r  (/ Math/PI 180)
        dLat (* (- lat2 lat1) d2r)
        dLon (* (- lon2 lon1) d2r)
        lat1rad (* lat1 d2r)
        lat2rad (* lat2 d2r)
    		sin1 (Math/sin (/ dLat  2))
    		sin2 (Math/sin (/ dLon  2))
        a (+ (* sin1 sin1) (* sin2 sin2 (Math/cos lat1rad) (Math/cos lat2rad)))
        meters (* earthRadius 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a))))]
      meters))



(defn lat-lon-to-screen
  "Convert lat/lon coordinates to x/y coordinates"
  [lat lon map-bounds]
  (let [north (:maxlat map-bounds)
        east  (:maxlon map-bounds)
        south (:minlat map-bounds)
        west  (:minlon map-bounds)
        x-meters (meters-between north west north lon)
        y-meters (meters-between north west lat west)
        bounds-width-meters (meters-between north west north east)
        screen-width (q/width)
        meters-per-pixel (/ screen-width bounds-width-meters)
        ; x = x-meters
        x (* x-meters meters-per-pixel)
        y (* y-meters meters-per-pixel)]
    [x y]))

(defn get-tags
  "Given an xml OSM object, return the tags for the object"
  [osm-object]
  (let [tag-elements (->> osm-object (:content) (filter #(= (:tag %) :tag)))
        tag-pairs (map #(list (:k (:attrs %)) (:v (:attrs %))) tag-elements)
        tags (apply hash-map (apply concat tag-pairs))]
    tags))

(defn get-nodes
  "Given an xml OSM way and a lookup hash-map, return the nodes for the way"
  [way-xml nodes-by-id]
  (let [nds (->> way-xml (:content) (filter #(= (:tag %) :nd)))
        nodes (map #(get nodes-by-id (:ref (:attrs %))) nds)]
      nodes))


(defn create-node
  "Create a hash-map representing a node, complete with x/y coordinates"
  [raw-node map-bounds]
  (let [lat  (:lat (:attrs raw-node))
        lon  (:lon (:attrs raw-node))
        [x y] (lat-lon-to-screen lat lon map-bounds)]
  {
    :id (:id (:attrs raw-node))
    :lat lat
    :lon lon
    :x x
    :y y
    :tags (get-tags raw-node)
  }))

(defn parse-osm-data
  "Parse the given osm XML data"
  [osm]
  (let [bounds    (->> osm
                      (:content)
                      (filter #(= (:tag %) :bounds))
                      (first)
                      (:attrs))

        raw-nodes (->> osm
                      (:content)
                      (filter #(= (:tag %) :node)))
        nodes (map #(create-node % bounds) raw-nodes)
        nodes-by-id (apply hash-map (apply concat (map #(list (:id %) %) nodes)))

        raw-ways  (->> osm
                      (:content)
                      (filter #(= (:tag %) :way)))
        ways (map #(hash-map
                      :id (:id (:attrs %))
                      :tags (get-tags %)
                      :nodes (get-nodes % nodes-by-id))
                   raw-ways)
        ; ways-by-id (apply hash-map (apply concat (map #(list (:id %) %) ways)))
        ]
  {
    :nodes nodes
    :ways ways
    ; :relations nil
    :bound bounds
  }))


; OSM Helpers
(defn road?
  "Returns whether or not the given way is a road"
  [way]
  (let [tags (:tags way)
        keys (keys tags)]
    (and
      (some #{"highway"} keys )
      (not= (get tags "highway") "no"))))

(defn road-attrs [way]
  (let [highway-type (get (:tags way) "highway")]
    (case highway-type
      "motorway" {:weight 4 :color [255 116 23]}
      "primary"  {:weight 2 :color [250 205 82]}
                 {:weight 2 :color [200 200 200]}
    )))


(defn render-road
  "Renders a road as a polyline"
  [way]
  (let [nodes (:nodes way)
        points (map #(list (:x %) (:y %)) nodes)
        lines (partition 2 1 points)
        attrs (road-attrs way)
        weight (:weight attrs)
        color (:color attrs)]    
    (apply q/stroke color)
    (q/stroke-weight weight)
    (dorun
      (map #(apply q/line %) lines))))

(defn setup [options]
  (let [map-file (stream/get-map!)
        xml-input-stream (io/input-stream map-file)
        raw-data (xml/parse xml-input-stream)
        osm-data (parse-osm-data raw-data)
        ways (:ways osm-data)
        roads (filter road? ways)]
        { :undrawn-roads roads
          :drawn-roads '()}))

(defn update-state [state]
  (let [undrawn-roads (:undrawn-roads state)
        new-roads (take lines-per-frame undrawn-roads)
        undrawn-roads (drop lines-per-frame undrawn-roads)
        drawn-roads (concat (:drawn-roads state) new-roads)]
    { :undrawn-roads undrawn-roads
      :drawn-roads drawn-roads}))

(defn draw-screen [state]
  (q/background 50 50 50)
  (q/stroke-weight 2)
  (q/stroke 255)
  (let [drawn-roads (:drawn-roads state)]
    (dorun (map render-road drawn-roads))))

(defn draw-leds [state] )

(defn draw-state [state]
  (draw-screen state)
  (draw-leds state))

(defn exit? [state] )

(def drawing
  (Drawing. "Shifting Grid" setup update-state draw-state nil exit? :raster))
