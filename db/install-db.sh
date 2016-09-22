#!/bin/sh

# change PSQL if needed
export PSQL=/Library/PostgreSQL/9.2/bin/psql


# set postgres pass
export PGPASSWORD=postgres

# drop database first
$PSQL -U postgres -h localhost -f kloopzcm-dropdb.sql

# create a role/user and database
$PSQL -U postgres -h localhost -f kloopzcm-prereq.sql



# set PGUSER
export PGUSER=${1-kloopzcm}

# set kloopzcm pass
export PGPASSWORD=${2-kloopzcm}


# create schema in the database
$PSQL -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-schema.sql

# create tables in the schema
$PSQL  -h localhost -d kloopzdb -f kloopzcm-tables.ddl

# create partition tables in the schema
$PSQL  -h localhost -d kloopzdb -f kloopzcm-partition.ddl

# postprocess
$PSQL  -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-postprocess.sql

# postprocess
$PSQL  -h localhost -d kloopzdb -v user=${PGUSER} -f kloopzcm-functions.sql
