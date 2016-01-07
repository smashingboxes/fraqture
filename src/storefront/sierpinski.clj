(ns storefront.sierpinski
  (:require [storefront.drawing]
            [storefront.lsystem :as lsys]
            [storefront.turtle :as turtle]
            [quil.core :as q])
  (:import  [storefront.drawing Drawing]
            [storefront.lsystem Lindenmayer]))

(defn setup [options]
  (let [turtle     (turtle/build 100 (- (q/height) 100))
        sierpinski (Lindenmayer.
                      [ :a ]
                      { :a '(:+ :b :- :a :- :b :+)
                        :b '(:- :a :+ :b :+ :a :-)}
                      { :a #(turtle/move turtle 5)
                        :b #(turtle/move turtle 5)
                        :+ #(turtle/turn turtle 60)
                        :- #(turtle/turn turtle -60) })]
    (lsys/run-system sierpinski 7)))

(defn update-state [state])
(defn draw-state [state])

(def drawing
  (Drawing. "Sierpinski" setup update-state draw-state nil nil))
