#!/bin/bash
PROCESS=$1

if [[ `uname` == *CYGWIN* ]]; then
  out=`powershell "ps $PROCESS -ErrorAction SilentlyContinue | wc -l"`
else
  out=`ps auxwww|egrep $PROCESS| grep -v grep| grep -v check_process_count | wc -l`
fi

echo $out
echo "$PROCESS Count |count=$out"
exit $?
