#!/usr/bin/env bash

(./bin/social >> logs/social.log 2>&1) &
echo $! > pids/social.pid

(lein run cycle --update-interval 20 >> logs/clojure.log 2>&1) &
echo $! > pids/clojure.pid
