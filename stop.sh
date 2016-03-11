#!/usr/bin/env bash

kill -9 `cat pids/social.pid`
kill -9 `cat pids/clojure.pid`
pkill -9 java
