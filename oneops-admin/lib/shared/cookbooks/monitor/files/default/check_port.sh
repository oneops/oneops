#!/bin/bash
host=${1-`hostname`}
port=${2-22}

if [[ `uname` == *CYGWIN* ]]; then
  out=`powershell "Test-NetConnection $host -Port $port -InformationLevel Quiet"`
  
  if [[ "$out" == *True* ]]; then 
	ec=0
  else
    ec=1
  fi

else
  # Fix for "nc: invalid option -- 'z'" error.
  # Nmap's Netcat version is used since EL7 (RHEL/CentOS 7).
  # nc -z option is only available in EL6 openbsd-netcat.

  out=`nc -v $host $port < /dev/null >/dev/null 2>&1`
  ec=$?
fi

if [ $ec != 0 ]; then
  echo "$host $port down |up=0"
else
  echo "$host $port up |up=100"
fi
exit $? 
