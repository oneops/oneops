#!/bin/sh

export PSQL=psql

# set kloopzcm pass
export PGPASSWORD=kloopzcm

$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-schema.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create schema failed && exit 1

# create tables in the schema
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-tables.ddl
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create tables failed && exit 1

# create partition tables in the schema
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-partition.ddl
RETVAL=$?
[ $RETVAL -ne 0 ] && echo create partition tables failed && exit 1

$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-postprocess.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo post process failed && exit 1

$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-functions.sql
RETVAL=$?
[ $RETVAL -ne 0 ] && echo functions failed && exit 1

exit 0
