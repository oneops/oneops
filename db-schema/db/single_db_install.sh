#!/bin/sh

export PSQL=psql

# set PGUSER
export PGUSER=${1-kloopzcm}

# set kloopzcm pass
export PGPASSWORD=${2-kloopzcm}


$PSQL  -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-schema.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create schema failed && exit 1

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
