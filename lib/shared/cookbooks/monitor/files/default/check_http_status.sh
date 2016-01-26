#!/bin/bash
HOST=$1
PORT=$2
URL=$3
WAITTIME=$4
EXPECT=$5
REGEX=$6

ec=1

out=`/opt/nagios/libexec/check_http -H $HOST -p $PORT -u "$URL" -t $WAITTIME -e "$EXPECT" -r "$REGEX"`

ec=$?
up=0

OK="HTTP OK"
if [[ "${out}" =~ OK ]]; then
   up=100
fi

IFS='|' out_split=($out)

for i in ${out_split[@]}
do
  out=$i
done


if [ $ec != 0 ]; then
 echo "time=$WAITTIME; size=0.00; up=$up|time=$WAITTIME; size=0.00; up=$up"
else
 echo "$out; up=$up|$out; up=$up"
fi
exit $?