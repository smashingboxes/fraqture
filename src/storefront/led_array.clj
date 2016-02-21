(ns storefront.led-array
  (:require [serial.core :as ser]))

(defn validate-serial-port [port]
  (let [ports (ser/port-identifiers)
        port-names (map #(.getName %) ports)]
    (some #(= port %) port-names)))

(defn- write [port iter]
  (if port
    (ser/write port (vec (map byte iter)))
    nil))

(defn reset-coms [port]
  (write port (repeat 6 \*)))

(defn connect [port]
  (let [open-port (if port (ser/open port))]
    (reset-coms open-port)
    open-port))

(defn paint-window [port row-start col-start row-end col-end [r g b]]
  (write port [\W row-start col-start row-end col-end r g b]))

(defn paint-pixel [port index [r g b]]
  (write port [\S (rem index 256) (quot index 256) r g b]))

(defn clear [port]
  (write port [\C]))
