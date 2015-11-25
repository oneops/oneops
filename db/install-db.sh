#!/bin/sh

# change PSQL if needed
export PSQL=/Library/PostgreSQL/9.2/bin/psql


# set postgres pass
export PGPASSWORD=postgres

# drop database first
$PSQL -U postgres -h localhost -f kloopzcm-dropdb.sql

# create a role/user and database
$PSQL -U postgres -h localhost -f kloopzcm-prereq.sql


# set kloopzcm pass
export PGPASSWORD=kloopzcm

# create schema in the database
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-schema.sql

# create tables in the schema
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-tables.ddl

# create partition tables in the schema
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-partition.ddl

# postprocess
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-postprocess.sql

# postprocess
$PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-functions.sql
