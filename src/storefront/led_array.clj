(ns storefront.led-array
  (:require [serial.core :as ser]))

(def rows 18)
(def cols 30)

(defn validate-serial-port [port]
  (let [ports (ser/port-identifiers)
        port-names (map #(.getName %) ports)]
    (some #(= port %) port-names)))

(defn- create-mock-leds []
  (vec (repeatedly rows #(repeatedly cols #(repeat 3 0)))))

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
    (if (= (first port) :con)
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
    (if (= (first port) :con)
      (write port [\C])
      (reset! port (create-mock-leds)))))

(defn- draw-led [row col color]
  "TODO add me"
  [])

(defn draw-mock [port]
  (let [type (first port) port (second port)]
    (if (= (first port) :mock)
      (let [mocked-leds @port]
        (doseq [row mocked-leds col row color col] (draw-led row col color))))))
