#!/usr/bin/env bash

git fetch --all && git log origin/master --pretty="%H" --no-merges --until="2018-12-31"
