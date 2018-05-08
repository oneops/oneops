#!/bin/bash
interface=$(ip -o -4 route show to default | awk '{print $5}')
stats_dir="/sys/class/net/$interface/statistics"
if [ -e $stats_dir ]
 then
  rx_file="$stats_dir/rx_bytes"
  tx_file="$stats_dir/tx_bytes"
  rx_bytes=$(cat "$rx_file")
  tx_bytes=$(cat "$tx_file")
else
  rx_bytes=`netstat -e | grep Bytes | awk '{print $2}'`
  tx_bytes=`netstat -e | grep Bytes | awk '{print $3}'`
fi
echo "rx_bytes=$rx_bytes tx_bytes=$tx_bytes |rx_bytes=$rx_bytes tx_bytes=$tx_bytes"
  