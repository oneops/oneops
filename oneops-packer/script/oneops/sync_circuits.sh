#!/bin/bash

set -e

export CIRCUIT_LOCAL_ASSET_STORE_ROOT=/opt/oneops/app/public/_circuit

sync_folder_root="/home/oneops/build"
declare -a circuits=(circuit-oneops-1 circuit-main-1 circuit-walmartlabs circuit-main-2 circuit-walmartlabs-2)

echo "Start syncing all circuits..."
sleep 2

echo "Syncing all models..."
sleep 1

for circuit in "${circuits[@]}"
do
  echo "Syncing models for $circuit..."
  cd "$sync_folder_root/$circuit"
  /usr/local/bin/knife model sync -a 
done

sleep 1

echo "Syncing all clouds..."
sleep 1

for circuit in "${circuits[@]}"
do
  echo "Syncing clouds for $circuit..."
  cd "$sync_folder_root/$circuit"
  /usr/local/bin/knife cloud sync -a
done

sleep 1

echo "Syncing all packs..."
sleep 1

for circuit in "${circuits[@]}"
do
  echo "Syncing packs for $circuit..."
  cd "$sync_folder_root/$circuit"
  /usr/local/bin/knife register
  /usr/local/bin/knife pack sync -a
done

echo "All circuits synced successfully!"
