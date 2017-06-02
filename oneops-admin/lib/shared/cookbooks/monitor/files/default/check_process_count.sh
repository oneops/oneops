#!/bin/bash
PROCESS=$1
out=`ps auxwww|egrep $PROCESS| grep -v grep| grep -v check_process_count | wc -l`
echo $out
echo "$PROCESS Count |count=$out"
exit $?