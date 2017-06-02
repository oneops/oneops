--CREATE ROLE kloopzcm LOGIN ENCRYPTED PASSWORD 'md5bd21eadfdda4b653e92100ab7cf341d2'
--  SUPERUSER CREATEDB CREATEROLE
--   VALID UNTIL 'infinity';
--COMMENT ON ROLE kloopzcm IS 'user for config managemen system';

CREATE DATABASE kloopzdb
  WITH ENCODING='UTF8'
       OWNER=kloopzcm
       CONNECTION LIMIT=-1;


