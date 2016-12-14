#!/bin/sh


# xargs to trim
is_standby=`sudo -u postgres psql -t -c "SELECT pg_is_in_recovery();"|xargs`

echo "is_standby=$is_standby"
if [ $is_standby = "t" ]; then
  echo "skipping because is standby"
  exit
fi


export PSQL=psql


# set PGUSER
export PGUSER=${1-kloopzcm}

# set kloopzcm pass
export PGPASSWORD=${2-kloopzcm}

# create schema in the database
$PSQL  -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-schema.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create schema failed && exit 1

# set search path to kloopzcm,public
alter_role=`sudo -u postgres psql -t -c "ALTER ROLE $PGUSER  SET search_path TO kloopzcm,public;"|xargs`
echo "alter_role result =$alter_role"
if [ "$alter_role" != "ALTER ROLE" ]; then
  echo "Could not set search_path for $PGUSER"
  exit 1
fi


# create tables in the schema
$PSQL  -h localhost -d kloopzdb -f kloopzcm-tables.ddl
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create tables failed && exit 1

# create partition tables in the schema
$PSQL  -h localhost -d kloopzdb -f kloopzcm-partition.ddl
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create partition tables failed && exit 1

$PSQL  -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-postprocess.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo post process failed && exit 1

$PSQL  -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-functions.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo functions failed && exit 1

exit 0
