#!/bin/bash
stats_dir="/sys/class/net/eth0/statistics"
rx_file="$stats_dir/rx_bytes"
tx_file="$stats_dir/tx_types"
rx_bytes=$(cat "$rx_file")
tx_bytes=$(cat "$tx_file")

echo "rx_bytes=$rx_bytes tx_bytes=$tx_bytes |rx_bytes=$rx_bytes tx_bytes=$tx_bytes"