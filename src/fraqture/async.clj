(ns fraqture.async
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn periodic-function [millis func]
  (func)
  (future (Thread/sleep millis) (periodic-function millis func)))

(defn periodic-getter
  [interval url response-parser]
  (let [result-atom     (atom nil)
        curried         #(response-parser (json/read-str (:body (client/get url)) :key-fn keyword))
        updater         #(reset! result-atom (curried))]
    (periodic-function interval updater)
    result-atom))
