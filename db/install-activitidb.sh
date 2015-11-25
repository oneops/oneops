#!/bin/sh

# change PSQL if needed
export PSQL=/Library/PostgreSQL/9.0/bin/psql


# set postgres pass
export PGPASSWORD=postgres

# drop database first
$PSQL -U postgres -h localhost -f activiti-dropdb.sql

# create a role/user and database
$PSQL -U postgres -h localhost -f activiti-prereq.sql
