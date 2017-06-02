#!/bin/bash
#
# simple wrapper around sar for debian or rpm based systems to output in nagios plugin format to get perf data
# using typeperf command in windows

if [[ `uname` == *CYGWIN* ]]; then
  cpu_load_s=`typeperf -sc 1 "processor(_total)\\% processor time"|tail -4|head -1|awk -F "," '{print $2}'`
  cpu_load=${cpu_load_s//"\""/""}
  
  echo $cpu_load | awk '{print "CpuIdle = "100-$1"% | " "CpuUser="$1" CpuNice="0" CpuSystem="0"; CpuIowait="0"; CpuSteal="0"; CpuIdle="100-$1""}'
  
else
  /usr/bin/sar 5 1|tail -2|head -1|awk '{print "CpuIdle = "$9"% | " "CpuUser="$4" CpuNice="$5" CpuSystem="$6"; CpuIowait="$7"; CpuSteal="$8"; CpuIdle="$9""}'
fi
