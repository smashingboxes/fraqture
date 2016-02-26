(ns storefront.led-array
  (:require [serial.core :as ser]
            [quil.core :as q]
            [storefront.helpers :refer :all]))

(def row-count 18)
(def col-count 30)

(defn validate-serial-port [port]
  (let [ports (ser/port-identifiers)
        port-names (map #(.getName %) ports)]
    (some #(= port %) port-names)))

(defn- create-mock-leds
  ([_] (create-mock-leds))
  ([]
  (vec (repeatedly row-count
    (fn [] (vec (repeatedly col-count
      (fn [] [0 0 0]))))))))

(defn- write [port iter]
  (if port (ser/write port (vec (map int iter)))))

(defn connect [port]
  (if port
    [:con (ser/open port)]
    [:mock (atom (create-mock-leds))]))

(defn- mock-window [mock-atom row-s col-s row-e col-e color]
  (let [pairs (for [row (range row-s (+ 1 row-e))
                    col (range col-s (+ 1 col-e))]
                      [row col])]
    (reduce (fn [acc [row col]] (assoc-in acc [row col] color)) mock-atom pairs)))

(defn paint-window [port row-start col-start row-end col-end [r g b]]
  (let [type (first port) port (second port)]
    (if (= type :con)
      (write port [\W row-start col-start row-end col-end r g b])
      (swap! port mock-window row-start col-start row-end col-end [r g b]))))

(defn- mock-pixel [mock-atom index color]
  (let [row (quot index col-count)
        col (rem index col-count)]
    (assoc-in mock-atom [row col] color)))

(defn paint-pixel [port index [r g b]]
  (let [type (first port) port (second port)]
    (if (= type :con)
      (write port [\S (rem index 256) (quot index 256) r g b])
      (swap! port mock-pixel index [r g b]))))

(defn clear [port]
  (let [type (first port) port (second port)]
    (if (= type :con)
      (write port [\C])
      (swap! port create-mock-leds))))

(defn refresh [port]
  (let [type (first port) port (second port)]
    (if (= type :con) (write port [\R]))))

(defn- draw-led [row col color]
  (let [x-width (/ (q/width) col-count)
        y-height 8
        y-adder (if (> row 9) (- (q/height) (* 18 y-height)) 0)
        x (* col x-width)
        y (+ y-adder (* row y-height))]
    (apply q/fill color)
    (q/rect x y x-width y-height)))

(defn draw-mock [port]
  (let [type (first port) port (second port)]
    (if (= type :mock)
      (let [mocked-leds @port]
        (q/no-stroke)
        (doseq-indexed [row mocked-leds row-c]
          (doseq-indexed [color row col-c]
            (draw-led row-c col-c color)))))))

(defn god-bless-america [port]
  (clear port)
  (doseq [y (range 1 10 2)] (paint-window port y 0 (+ y 1) 30 [0 0 130]))
  (doseq [y (range 0 9 2)] (paint-window port y 0 (+ y 1) 30 [100 100 100]))
  (paint-window port 0 0 5 9 [20 20 120])
  (refresh port))
