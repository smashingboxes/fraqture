(ns storefront.arduino
  (:require [clojure.core.async :as async :refer :all]
            [serial-port :as ser]))

(defn on-receive
  [fire-atom]
  (fn [character]
    (reset! fire-atom true)))

(defn initialize
  [port-name]
  (let [serial    (ser/open port-name)
        fire-atom (atom false)
        fire-func (ser/on-byte serial (on-receive fire-atom))]
  { :fired-atom fire-atom
    :fired      false
    :port       serial }))

(defn update
  [state]
  (let [fired-atom (:fired-atom state)
        is-fired   @fired-atom]
    (reset! fired-atom false)
    (assoc state :fired is-fired)))
