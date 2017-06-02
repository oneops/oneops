#!/bin/sh

# change PSQL if needed
INSTALLED_PSQL=`which psql`
echo "INSTATTED PSQL IS : " $INSTALLED_PSQL
if test -n "$INSTALLED_PSQL"; then
    export PSQL=$INSTALLED_PSQL
else
    export PSQL=/Library/PostgreSQL/9.2/bin/psql
fi
echo "Psql set to " $PSQL
# set postgres pass
read -p "Are you using default password postgres? " -n 1 -r

if [[ $REPLY =~ ^[Nn]$ ]]
then
echo " Please enter database password "
read pgpassword
export PGPASSWORD=$pgpassword
else
export PGPASSWORD=postgres
fi
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
