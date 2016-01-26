#!/bin/bash

STATE=$1
COUNT=`netstat -an |grep "$STATE" | wc -l`

echo "count=$COUNT|count=$COUNT"