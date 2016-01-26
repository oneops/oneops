#!/bin/bash
#
# simple wrapper around sar for debian or rpm based systems to output in nagios plugin format to get perf data
#
/usr/bin/sar 5 1|tail -2|head -1|awk '{print "CpuIdle = "$9"% | " "CpuUser="$4" CpuNice="$5" CpuSystem="$6"; CpuIowait="$7"; CpuSteal="$8"; CpuIdle="$9""}'
