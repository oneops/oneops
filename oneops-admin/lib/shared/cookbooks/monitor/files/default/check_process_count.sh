#!/bin/bash
PROCESS=$1

if [[ `uname` == *CYGWIN* ]]; then
  out=`powershell "(get-service $PROCESS).Status"`
  if [[ "$out" == *Stopped* ]]; then
	 echo "$PROCESS Count |count=0"
  else
     echo "$PROCESS Count |count=100"
  fi
  exit
fi

out=`ps auxwww|egrep $PROCESS| grep -v grep| grep -v check_process_count | wc -l`
echo $out
echo "$PROCESS Count |count=$out"
exit $?
