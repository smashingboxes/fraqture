#!/usr/bin/env bash

./bin/social >> logs/social.log 2>&1 &
echo $! > pids/social.pid

lein run cycle >> logs/clojure.log 2>&1 &
echo $! > pids/clojure.pid
