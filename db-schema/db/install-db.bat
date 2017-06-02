set path=%path%;C:\Program Files\PostgreSQL\9.0\bin
set PGPASSWORD=postgres
PSQL -U postgres -h localhost -f kloopzcm-dropdb.sql 2>"%~dp0drop_DB.err.log"
PSQL -U postgres -h localhost -f kloopzcm-prereq.sql 2>"%~dp0prereq_DB.err.log"

set PGPASSWORD=kloopzcm
PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-schema.sql 2>"%~dp0Create_DB.err.log"
PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-tables.ddl 2>"%~dp0Create_DB.err.log"
PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-partition.ddl 2>"%~dp0Create_DB.err.log"
PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-postprocess.sql 2>"%~dp0Create_DB.err.log"
PSQL -U kloopzcm -h localhost -d kloopzdb -f kloopzcm-functions.sql 2>"%~dp0Create_DB.err.log"

REM cmsmodel.pl -action load -init model.list
