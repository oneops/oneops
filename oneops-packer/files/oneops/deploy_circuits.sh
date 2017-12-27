#!/bin/bash

set -e

declare -A CIRCUITS

CWD=$(pwd)

for d in /tmp/oneops_circuits/* ; do
  if [ -d ${d} ]; then
    cd /opt/oneops/inductor
    CIRCUIT="$(/bin/basename ${d})"
    CIRCUITS[${CIRCUIT}]=$CIRCUIT
    rm -rf /home/oneops/build/${CIRCUIT}
    mv ${d} /home/oneops/build/
    if [ ! -L ${CIRCUIT} ]; then
      ln -s /home/oneops/build/${CIRCUIT} ${CIRCUIT}
    fi
    chown -R ooadmin:ooadmin /opt/oneops/inductor/${CIRCUIT}
    cd /opt/oneops/inductor/${CIRCUIT}
    knife model sync -a
  fi
done

# install circuit
for d in "${!CIRCUITS[@]}"; do
  cd /opt/oneops/inductor/${d}
  echo "Installing circuit ${d}"
  circuit install
done

cd ${CWD}
