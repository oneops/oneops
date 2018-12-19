

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2012 (
		CHECK ( log_time >= DATE '2012-01-01' AND log_time < DATE '2013-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2013 (
		CHECK ( log_time >= DATE '2013-01-01' AND log_time < DATE '2014-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2014 (
		CHECK ( log_time >= DATE '2014-01-01' AND log_time < DATE '2015-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2015 (
		CHECK ( log_time >= DATE '2015-01-01' AND log_time < DATE '2016-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2016 (
		CHECK ( log_time >= DATE '2016-01-01' AND log_time < DATE '2017-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2017 (
		CHECK ( log_time >= DATE '2017-01-01' AND log_time < DATE '2018-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2018 (
		CHECK ( log_time >= DATE '2018-01-01' AND log_time < DATE '2019-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_attr_log_2019 (
		CHECK ( log_time >= DATE '2019-01-01' AND log_time < DATE '2020-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_attr_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2012 (
		CHECK ( log_time >= DATE '2012-01-01' AND log_time < DATE '2013-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2013 (
		CHECK ( log_time >= DATE '2013-01-01' AND log_time < DATE '2014-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2014 (
		CHECK ( log_time >= DATE '2014-01-01' AND log_time < DATE '2015-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2015 (
		CHECK ( log_time >= DATE '2015-01-01' AND log_time < DATE '2016-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2016 (
		CHECK ( log_time >= DATE '2016-01-01' AND log_time < DATE '2017-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2017 (
		CHECK ( log_time >= DATE '2017-01-01' AND log_time < DATE '2018-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2018 (
		CHECK ( log_time >= DATE '2018-01-01' AND log_time < DATE '2019-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);


CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_relation_log_2019 (
		CHECK ( log_time >= DATE '2019-01-01' AND log_time < DATE '2020-01-01' )
) INHERITS (kloopzcm.cm_ci_relation_log);



CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2012 (
		CHECK ( log_time >= DATE '2012-01-01' AND log_time < DATE '2013-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2013 (
		CHECK ( log_time >= DATE '2013-01-01' AND log_time < DATE '2014-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2014 (
		CHECK ( log_time >= DATE '2014-01-01' AND log_time < DATE '2015-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2015 (
		CHECK ( log_time >= DATE '2015-01-01' AND log_time < DATE '2016-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2016 (
		CHECK ( log_time >= DATE '2016-01-01' AND log_time < DATE '2017-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2017 (
		CHECK ( log_time >= DATE '2017-01-01' AND log_time < DATE '2018-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2018 (
		CHECK ( log_time >= DATE '2018-01-01' AND log_time < DATE '2019-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_attribute_log_2019 (
		CHECK ( log_time >= DATE '2019-01-01' AND log_time < DATE '2020-01-01' )
) INHERITS (kloopzcm.cm_ci_attribute_log);


CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2012 (
		CHECK ( log_time >= DATE '2012-01-01' AND log_time < DATE '2013-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2013 (
		CHECK ( log_time >= DATE '2013-01-01' AND log_time < DATE '2014-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2014 (
		CHECK ( log_time >= DATE '2014-01-01' AND log_time < DATE '2015-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2015 (
		CHECK ( log_time >= DATE '2015-01-01' AND log_time < DATE '2016-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2016 (
		CHECK ( log_time >= DATE '2016-01-01' AND log_time < DATE '2017-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2017 (
		CHECK ( log_time >= DATE '2017-01-01' AND log_time < DATE '2018-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2018 (
		CHECK ( log_time >= DATE '2018-01-01' AND log_time < DATE '2019-01-01' )
) INHERITS (kloopzcm.cm_ci_log);

CREATE TABLE IF NOT EXISTS kloopzcm.cm_ci_log_2019 (
		CHECK ( log_time >= DATE '2019-01-01' AND log_time < DATE '2020-01-01' )
) INHERITS (kloopzcm.cm_ci_log);


CREATE OR REPLACE FUNCTION cm_ci_relation_attr_log_insert()
RETURNS TRIGGER AS
$BODY$
BEGIN
    IF ( NEW.log_time >= DATE '2012-01-01' AND
         NEW.log_time < DATE '2013-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2012 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2013-01-01' AND
            NEW.log_time < DATE '2014-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2013 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2014-01-01' AND
            NEW.log_time < DATE '2015-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2014 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2015-01-01' AND
            NEW.log_time < DATE '2016-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2015 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2016-01-01' AND
            NEW.log_time < DATE '2017-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2016 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2017-01-01' AND
            NEW.log_time < DATE '2018-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2017 VALUES (NEW.*);
	    ELSIF ( NEW.log_time >= DATE '2018-01-01' AND
            NEW.log_time < DATE '2019-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2018 VALUES (NEW.*);
	    ELSIF ( NEW.log_time >= DATE '2019-01-01' AND
            NEW.log_time < DATE '2020-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_attr_log_2019 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the cm_ci_relation_attr_log_insert() function!';
    END IF;
    RETURN NULL;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


CREATE OR REPLACE FUNCTION cm_ci_relation_log_insert()
RETURNS TRIGGER AS
$BODY$
BEGIN
    IF ( NEW.log_time >= DATE '2012-01-01' AND
         NEW.log_time < DATE '2013-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2012 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2013-01-01' AND
            NEW.log_time < DATE '2014-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2013 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2014-01-01' AND
            NEW.log_time < DATE '2015-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2014 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2015-01-01' AND
            NEW.log_time < DATE '2016-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2015 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2016-01-01' AND
            NEW.log_time < DATE '2017-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2016 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2017-01-01' AND
            NEW.log_time < DATE '2018-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2017 VALUES (NEW.*);
	  ELSIF ( NEW.log_time >= DATE '2018-01-01' AND
            NEW.log_time < DATE '2019-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2018 VALUES (NEW.*);
	  ELSIF ( NEW.log_time >= DATE '2019-01-01' AND
            NEW.log_time < DATE '2020-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_relation_log_2019 VALUES (NEW.*);

    ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the cm_ci_relation_log_insert() function!';
    END IF;
    RETURN NULL;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION cm_ci_attribute_log_insert()
RETURNS TRIGGER AS
$BODY$
BEGIN
    IF ( NEW.log_time >= DATE '2012-01-01' AND
         NEW.log_time < DATE '2013-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2012 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2013-01-01' AND
            NEW.log_time < DATE '2014-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2013 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2014-01-01' AND
            NEW.log_time < DATE '2015-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2014 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2015-01-01' AND
            NEW.log_time < DATE '2016-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2015 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2016-01-01' AND
            NEW.log_time < DATE '2017-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2016 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2017-01-01' AND
            NEW.log_time < DATE '2018-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2017 VALUES (NEW.*);
	    ELSIF ( NEW.log_time >= DATE '2018-01-01' AND
            NEW.log_time < DATE '2019-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2018 VALUES (NEW.*);
 	    ELSIF ( NEW.log_time >= DATE '2019-01-01' AND
            NEW.log_time < DATE '2020-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_attribute_log_2019 VALUES (NEW.*);   ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the cm_ci_attribute_log_insert() function!';
    END IF;
    RETURN NULL;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION cm_ci_log_insert()
RETURNS TRIGGER AS
$BODY$
BEGIN
    IF ( NEW.log_time >= DATE '2012-01-01' AND
         NEW.log_time < DATE '2013-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2012 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2013-01-01' AND
            NEW.log_time < DATE '2014-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2013 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2014-01-01' AND
            NEW.log_time < DATE '2015-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2014 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2015-01-01' AND
            NEW.log_time < DATE '2016-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2015 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2016-01-01' AND
            NEW.log_time < DATE '2017-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2016 VALUES (NEW.*);
    ELSIF ( NEW.log_time >= DATE '2017-01-01' AND
            NEW.log_time < DATE '2018-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2017 VALUES (NEW.*);
	ELSIF ( NEW.log_time >= DATE '2018-01-01' AND
            NEW.log_time < DATE '2019-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2018 VALUES (NEW.*);
  	ELSIF ( NEW.log_time >= DATE '2019-01-01' AND
            NEW.log_time < DATE '2020-01-01' ) THEN
	INSERT INTO kloopzcm.cm_ci_log_2019 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the cm_ci_log_insert() function!';
    END IF;
    RETURN NULL;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


CREATE TRIGGER insert_cm_ci_relation_attr_log_trigger
    BEFORE INSERT ON cm_ci_relation_attr_log
    FOR EACH ROW EXECUTE PROCEDURE cm_ci_relation_attr_log_insert();

CREATE TRIGGER insert_cm_ci_relation_log_trigger
    BEFORE INSERT ON cm_ci_relation_log
    FOR EACH ROW EXECUTE PROCEDURE cm_ci_relation_log_insert();

CREATE TRIGGER insert_cm_ci_attribute_log_trigger
    BEFORE INSERT ON cm_ci_attribute_log
    FOR EACH ROW EXECUTE PROCEDURE cm_ci_attribute_log_insert();

CREATE TRIGGER insert_cm_ci_log_trigger
    BEFORE INSERT ON cm_ci_log
    FOR EACH ROW EXECUTE PROCEDURE cm_ci_log_insert();

