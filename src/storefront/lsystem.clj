(ns storefront.lsystem)

(defrecord Lindenmayer [axiom rule-set function-map])

(defn rule-iterator [rules]
  (fn [collection] (flatten (map #(or (% rules) %) collection))))

(defn run-system [l-system iterations]
  (let [start (:axiom l-system)
        rules (:rule-set l-system)
        funcs (:function-map l-system)
        final (nth (iterate (rule-iterator rules) start) iterations)]
    (doseq [transform final]
      (if (transform funcs) ((transform funcs))))))
