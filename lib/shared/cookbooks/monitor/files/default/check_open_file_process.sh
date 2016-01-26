#!/bin/bash

process=$1

out=`ps auxwww| grep -v grep | grep -v check_open_file_process | egrep $process| awk '{print $2}'`
open_files=0

for pid in $out; do

 if [[ "$pid" -gt 0 ]]; then
   open_files=$(($open_files + `lsof -p $pid | wc -l`))
 else
   open_files = $open_files
 fi
done
echo "open_files=$open_files|open_files=$open_files" 