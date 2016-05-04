(ns fraqture.time)

(defn is-night? []
  (let [hours (.getHours (new java.util.Date))]
    (< hours 6)))
