(defproject fraqture "0.1.0"
  :description "An interactive digital art project built with Clojure and Arduino"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.2.374"]
                 [quil "2.2.6"]
                 [rxtx22 "1.0.6"]
                 [clj-serial "2.0.3"]
                 [clj-http "2.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [net.mikera/core.matrix "0.47.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [vision "1.0.0-SNAPSHOT"]
                 [self/face-recog "0.5.0"]
                 [self/opencv "3.1.0"]
                 ]
  :jvm-opts ["-Xmx1G" "-Djna.library.path=/~/work/vision/resources/lib" "-Djava.library.path=/usr/local/Cellar/opencv3/3.1.0_3/share/OpenCV/java/"]
  :main fraqture.core)
