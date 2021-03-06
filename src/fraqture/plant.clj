(ns fraqture.plant
  (:require [fraqture.drawing]
            [fraqture.lsystem :as lsys]
            [fraqture.turtle :as turtle]
            [quil.core :as q])
  (:import  [fraqture.drawing Drawing]
            [fraqture.lsystem Lindenmayer]))

(defn setup [options]
  (let [turtle (turtle/build 100 (- (q/height) 100))
        plant  (Lindenmayer.
                 [ :x ]
                 { :x '(:f :- :s :s :x :r :+ :x :r :+ :f :s :+ :f :x :r :- :x)
                   :f '(:f :f)}
                 { :f #(turtle/move turtle 3)
                   :+ #(turtle/turn turtle 25)
                   :- #(turtle/turn turtle -25)
                   :s #(turtle/save turtle)
                   :r #(turtle/restore turtle) })]
    (q/stroke 10 197 44)
    (q/stroke-weight 2)
    (turtle/turn turtle 45)
    (lsys/run-system plant 7)))

(defn update-state [state])
(defn draw-state [state])

(def drawing
  (Drawing. "Plant" setup update-state draw-state nil nil nil))
