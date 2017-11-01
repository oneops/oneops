#!/bin/bash

VER=$1

if [[ "$VER" == "" ]]; then
  echo "Usage: ./chver.sh <new-version>"
  exit 1
fi

mvn release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$VER
