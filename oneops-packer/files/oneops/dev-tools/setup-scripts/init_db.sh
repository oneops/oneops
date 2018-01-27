#!/bin/sh

echo "Initializing DB.."

cd $OO_HOME/dist



tar -xzvf db.tar.gz

cp $OO_HOME/dist/oneops/dist/single_db_schemas.sql /var/lib/pgsql/single_db_schemas.sql

cd /var/lib/pgsql
su postgres -c 'psql -f /var/lib/pgsql/single_db_schemas.sql'
cd $OO_HOME/dist/oneops/dist
./single_db_install.sh


now=$(date +"%T")
echo "Completed DB init : $now"


