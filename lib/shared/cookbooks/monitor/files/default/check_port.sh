#!/bin/bash
host=${1-`hostname`}
port=${2-22}

# Fix for "nc: invalid option -- 'z'" error.
# Nmap's Netcat version is used since EL7 (RHEL/CentOS 7).
# nc -z option is only available in EL6 openbsd-netcat.

out=`nc -v $host $port < /dev/null >/dev/null 2>&1`
ec=$?

if [ $ec != 0 ]; then
  echo "$host $port down |up=0"
else
  echo "$host $port up |up=100"
fi
exit $? 
