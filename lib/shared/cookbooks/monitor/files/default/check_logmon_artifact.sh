#!/bin/bash
install_dir=$1
artifact_id=${install_dir//\/app\//}
out=`cat /log/logmon/oo-*$artifact_id.log  | awk '{ printf "%s ", $0 }'`
ec=$?
echo "ok | $out"
exit $ec
