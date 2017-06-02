set path=%path%;C:\Program Files\PostgreSQL\9.0\bin
SET PGPASSWORD=postgres
psql -U postgres -h localhost -f activiti-dropdb.sql
psql -U postgres -h localhost -f activiti-prereq.sql
