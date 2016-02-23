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

(defn- create-mock-leds []
  (repeatedly row-count (fn [] (repeatedly col-count (fn [] (repeat 3 0))))))

(defn- write [port iter]
  (if port (ser/write port (vec (map byte iter)))))

(defn connect [port]
  (if port
    [:con (ser/open port)]
    [:mock (atom (create-mock-leds))]))

(defn- mock-window
  "TODO: add me"
  [mock-atom])

(defn paint-window [port row-start col-start row-end col-end [r g b]]
  (let [type (first port) port (second port)]
    (if (= type :con)
      (write port [\W row-start col-start row-end col-end r g b])
      (reset! port (mock-window @port)))))

(defn- mock-pixel
  "TODO: add me"
  [mock-atom])

(defn paint-pixel [port index [r g b]]
  (let [type (first port) port (second port)]
    (if (= type :con)
      (write port [\S (rem index 256) (quot index 256) r g b])
      (reset! port (mock-pixel @port)))))

(defn clear [port]
  (let [type (first port) port (second port)]
    (if (= type :con)
      (write port [\C])
      (reset! port (create-mock-leds)))))

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
