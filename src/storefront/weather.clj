(ns storefront.weather
  (:require [storefront.async :refer :all]))

(defn parse-weather [open-weather-data]
  (println open-weather-data)
  open-weather-data)

(defn weather [city state]
  (let [url (str "http://api.openweathermap.org/data/2.5/weather?q="
             city "," state "&appid=40dba8c9e050bf1a9ad8c684ebe5fdbd")]
    (periodic-getter 60000 url parse-weather)))
