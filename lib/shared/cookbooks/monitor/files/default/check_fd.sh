#!/bin/bash
############################################################

# Check open file descriptors and compare with percent of
# maximum allowance by kernel
# There is default values. So script do not need any args
# but you can customize:
# -w - warning level in number of FDs
# -W - warning level in % to kernel limit (default = 75%)
# -c - critical level in number of FDs
# -C - critical level in % to kernel limit (default = 90%)
# w\W < c\C
#
# Script uses logic from this page:
# http://www.netadmintools.com/art295.html
#
# # Output of cat /proc/sys/fs/file-nr on differen kernels
# 3391   969     52427   # For kernels <= 2.4.X
# 2323   0       141241  # For kernels >= 2.6.X
# |      |       |
# |      |       maximum open file descriptors (LIMIT)
# |      total free allocated file descriptors
# total allocated file descriptors
# (the number of file descriptors allocated since boot)
#
# The number of open file descriptors is column 1 - column 2
# In new kernel column 1 - is what we need.
# use 1 - 2 for backcompatibility.
#
# If problems and too much opened files you can increase allowance
# echo "104854" > /proc/sys/fs/file-max
#
###########################################################

USAGE="`basename $0` ([-w]<warn abs>|[-W]<warn %>)([-c]<crit abs>|[-C]<crit %)"
THRESHOLD_USAGE="CRITICAL threshold must be greater than WARNING: `basename $0` $*"

file_nr=`cat /proc/sys/fs/file-nr`
open_fd=`echo $file_nr | awk '{print $1-$2}'`
file_max=`echo $file_nr | awk '{print $3}'`

warn=$[${file_max}/100*75]      # Default warning  is last number
crit=$[${file_max}/100*90]      # Default critical is last number

while getopts "hw:c:W:C:" OPTION; do
    case $OPTION in
        h)
        echo $USAGE
        exit 0
        ;;
        w)
        warn=$OPTARG
        ;;
        c)
        crit=$OPTARG
        ;;
        W)
        warn=$[${file_max}/100*${OPTARG}]
        ;;
        C)
        crit=$[${file_max}/100*${OPTARG}]
        ;;
    esac
done

open_perc=$[$open_fd*100/${file_max}] # Calculate levels in % just for "Performance Data"
warn_perc=$[$warn*100/${file_max}] # Calculate levels in % just for "Performance Data"
crit_perc=$[$crit*100/${file_max}] # in "Service State Information". Plugin really don't need it
# verify input
[[ $warn -ge $crit ]] && echo -e "\n$THRESHOLD_USAGE\n\nUsage: $USAGE\n" && exit 0

text="$open_fd ($open_perc%) of $file_max allowed file descriptors open, WARNING = $warn ($warn_perc%), CRITICAL = $crit ($crit_perc%)|fd_Used=$open_perc%"
[[ "$open_fd" -le $warn ]] && echo       "OK - $text" && exit 0
[[ "$open_fd" -gt $crit ]] && echo "CRITICAL - $text" && exit 2
[[ "$open_fd" -gt $warn ]] && echo "WARNING - $text" && exit 1
