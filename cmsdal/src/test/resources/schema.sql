CREATE TABLE dj_rfc_ci_attributes (
                rfc_attr_id BIGINT NOT NULL,
                rfc_id BIGINT NOT NULL,
                attribute_id INTEGER NOT NULL,
                old_attribute_value TEXT,
                new_attribute_value TEXT NOT NULL,
                owner VARCHAR(32),
                comments VARCHAR(2000),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_rfc_ci_attributes_pk PRIMARY KEY (rfc_attr_id)
);


CREATE TABLE md_classes (
                class_id INTEGER NOT NULL,
                class_name VARCHAR(200) NOT NULL,
                short_class_name VARCHAR(200) NOT NULL,
                super_class_id INTEGER,
                is_namespace BOOLEAN NOT NULL,
                flags INTEGER DEFAULT 0 NOT NULL,
                impl VARCHAR(200),
                access_level VARCHAR(200),
                description TEXT,
                format TEXT,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_classes_pk PRIMARY KEY (class_id)
);


CREATE TABLE md_class_attributes (
                attribute_id INTEGER NOT NULL,
                class_id INTEGER NOT NULL,
                attribute_name VARCHAR(200) NOT NULL,
                data_type VARCHAR(64) NOT NULL,
                is_mandatory BOOLEAN DEFAULT false NOT NULL,
                is_inheritable BOOLEAN DEFAULT true NOT NULL,
                is_encrypted BOOLEAN DEFAULT false NOT NULL,
                is_immutable BOOLEAN DEFAULT false NOT NULL,
                force_on_dependent BOOLEAN NOT NULL,
                default_value TEXT,
                value_format TEXT,
                description TEXT,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_class_attributes_pk PRIMARY KEY (attribute_id)
);

