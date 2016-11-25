#!/bin/sh

# change PSQL if needed
INSTALLED_PSQL=`which psql`
echo "INSTATTED PSQL IS : " $INSTALLED_PSQL
if test -n "$INSTALLED_PSQL"; then
    export PSQL=$INSTALLED_PSQL
else
export PSQL=/Library/PostgreSQL/9.0/bin/psql
fi

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
$PSQL -U postgres -h localhost -f activiti-dropdb.sql

# create a role/user and database
$PSQL -U postgres -h localhost -f activiti-prereq.sql
