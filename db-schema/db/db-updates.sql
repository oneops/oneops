ALTER TABLE md_classes
   ADD COLUMN impl character varying(200);


ALTER TABLE dj_deployment
   ADD COLUMN process_id character varying(60);

CREATE TABLE kloopzcm.cms_lock (
                lock_id BIGINT NOT NULL,
                lock_name VARCHAR(64) NOT NULL,
                locked_by VARCHAR(200) NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cms_lock_pk PRIMARY KEY (lock_id)
);


CREATE UNIQUE INDEX cms_lock_uln
 ON kloopzcm.cms_lock
 ( lock_name );
