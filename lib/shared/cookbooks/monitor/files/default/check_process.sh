#!/bin/bash
service_name=$1
use_script_status=$2
pattern=$3

ec=1
if [ $use_script_status == 'true' ]; then
  out=`service $service_name status`
  ec=$?
else
  out=`ps auxwww| grep -v grep | grep -v check_process | egrep $pattern`
  ec=$?
fi

if [ $ec != 0 ]; then
  echo "$service_name down |up=0"
else
  echo "$service_name up |up=100"
fi
exit $? 
