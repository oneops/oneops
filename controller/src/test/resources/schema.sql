CREATE TABLE ns_namespaces (
                ns_id BIGINT NOT NULL,
                ns_path VARCHAR(200) NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT ns_namespaces_pk PRIMARY KEY (ns_id)
);

CREATE TABLE cm_ci_state (
                ci_state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT cm_ci_state_pk PRIMARY KEY (ci_state_id)
);

CREATE TABLE dj_rfc_ci_actions (
                action_id INTEGER NOT NULL,
                action_name VARCHAR(200) NOT NULL,
                CONSTRAINT dj_rfc_ci_actions_pk PRIMARY KEY (action_id)
);

CREATE TABLE dj_release_states (
                release_state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_release_states_pk PRIMARY KEY (release_state_id)
);

CREATE TABLE dj_deployment_states (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_deployment_states_pk PRIMARY KEY (state_id)
);

CREATE TABLE dj_deployment_rfc_states (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_deployment_rfc_states_pk PRIMARY KEY (state_id)
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

CREATE TABLE md_relations (
                relation_id INTEGER NOT NULL,
                relation_name VARCHAR(200) NOT NULL,
                short_relation_name VARCHAR(200) NOT NULL,
                description TEXT NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_relations_pk PRIMARY KEY (relation_id)
);

CREATE TABLE md_relation_attributes (
                attribute_id INTEGER NOT NULL,
                relation_id INTEGER NOT NULL,
                attribute_name VARCHAR(200) NOT NULL,
                data_type VARCHAR(64) NOT NULL,
                is_mandatory BOOLEAN NOT NULL,
                is_encrypted BOOLEAN DEFAULT false NOT NULL,
                default_value TEXT,
                value_format TEXT,
                description TEXT NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_relation_attributes_pk PRIMARY KEY (attribute_id)
);

CREATE TABLE cm_ci (
                ci_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                class_id INTEGER NOT NULL,
                ci_name VARCHAR(200) NOT NULL,
                ci_goid VARCHAR(256) NOT NULL,
                comments VARCHAR(2000),
                ci_state_id INTEGER NOT NULL,
                last_applied_rfc_id BIGINT,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cm_ci_pk PRIMARY KEY (ci_id)
);

CREATE TABLE cm_ci_attributes (
                ci_attribute_id BIGINT NOT NULL,
                ci_id BIGINT NOT NULL,
                attribute_id INTEGER NOT NULL,
                df_attribute_value TEXT,
                dj_attribute_value TEXT,
                owner VARCHAR(32),
                comments VARCHAR(2000),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cm_ci_attributes_pk PRIMARY KEY (ci_attribute_id)
);

CREATE TABLE cm_ci_relations (
                ci_relation_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                from_ci_id BIGINT NOT NULL,
                relation_goid VARCHAR(256) NOT NULL,
                relation_id INTEGER NOT NULL,
                to_ci_id BIGINT NOT NULL,
                ci_state_id INTEGER NOT NULL,
                last_applied_rfc_id BIGINT,
                comments VARCHAR(2000),
                created_by VARCHAR(200),
                update_by VARCHAR(200),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cm_ci_relations_pk PRIMARY KEY (ci_relation_id)
);

CREATE TABLE cm_ci_relation_attributes (
                ci_rel_attribute_id BIGINT NOT NULL,
                ci_relation_id BIGINT NOT NULL,
                attribute_id INTEGER NOT NULL,
                df_attribute_value TEXT,
                dj_attribute_value TEXT,
                owner VARCHAR(32),
                comments VARCHAR(2000),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cm_ci_relation_attributes_pk PRIMARY KEY (ci_rel_attribute_id)
);

CREATE TABLE dj_releases (
                release_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                parent_release_id BIGINT,
                release_name VARCHAR(200) NOT NULL,
                created_by VARCHAR(200) NOT NULL,
                commited_by VARCHAR(200),
                release_state_id INTEGER NOT NULL,
                release_type VARCHAR(200),
                description TEXT,
                revision SMALLINT NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_releases_pk PRIMARY KEY (release_id)
);

CREATE TABLE dj_deployment (
                deployment_id BIGINT NOT NULL,
                flags INTEGER DEFAULT 0 NOT NULL,
                ns_id BIGINT NOT NULL,
                release_id BIGINT NOT NULL,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                release_revision SMALLINT NOT NULL,
                state_id INTEGER NOT NULL,
                process_id VARCHAR(60),
                description TEXT,
                comments TEXT,
                ops TEXT,
                auto_pause_exec_orders VARCHAR(200),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_deployment_pk PRIMARY KEY (deployment_id)
);



CREATE TABLE dj_rfc_ci (
                rfc_id BIGINT NOT NULL,
                release_id BIGINT NOT NULL,
                ci_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                class_id INTEGER NOT NULL,
                ci_name VARCHAR(200) NOT NULL,
                ci_goid VARCHAR(256),
                action_id INTEGER NOT NULL,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                execution_order SMALLINT,
                is_active_in_release BOOLEAN DEFAULT TRUE NOT NULL,
                last_rfc_id BIGINT,
                comments VARCHAR(2000),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                hint TEXT,
                CONSTRAINT dj_rfc_ci_pk PRIMARY KEY (rfc_id)
);

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

CREATE TABLE dj_rfc_relation (
                rfc_id BIGINT NOT NULL,
                release_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                ci_relation_id BIGINT NOT NULL,
                from_rfc_id BIGINT,
                from_ci_id BIGINT NOT NULL,
                relation_id INTEGER NOT NULL,
                relation_goid VARCHAR(256) NOT NULL,
                to_rfc_id BIGINT,
                to_ci_id BIGINT NOT NULL,
                action_id INTEGER NOT NULL,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                execution_order SMALLINT NOT NULL,
                is_active_in_release BOOLEAN DEFAULT TRUE NOT NULL,
                last_rfc_id BIGINT,
                comments VARCHAR(2000),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_rfc_relation_pk PRIMARY KEY (rfc_id)
);

CREATE TABLE dj_rfc_relation_attributes (
                rfc_attr_id BIGINT NOT NULL,
                rfc_id BIGINT NOT NULL,
                attribute_id INTEGER NOT NULL,
                old_attribute_value TEXT,
                new_attribute_value TEXT NOT NULL,
                owner VARCHAR(32),
                comments VARCHAR(2000),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_rfc_relation_attributes_pk PRIMARY KEY (rfc_attr_id)
);

CREATE TABLE dj_deployment_rfc (
                deployment_rfc_id BIGINT NOT NULL,
                deployment_id BIGINT NOT NULL,
                state_id INTEGER NOT NULL,
                rfc_id BIGINT NOT NULL,
                comments TEXT,
                ops TEXT,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_deployment_rfc_pk PRIMARY KEY (deployment_rfc_id)
);

CREATE TABLE md_class_relations (
                link_id INTEGER NOT NULL,
                from_class_id INTEGER NOT NULL,
                relation_id INTEGER NOT NULL,
                to_class_id INTEGER NOT NULL,
                is_strong BOOLEAN NOT NULL,
                link_type VARCHAR(64) NOT NULL,
                description VARCHAR(2000) NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_class_relations_pk PRIMARY KEY (link_id)
);

CREATE TABLE cms_vars (
                var_id BIGINT NOT NULL,
                var_name VARCHAR(200) NOT NULL,
                var_value TEXT NOT NULL,
                updated_by VARCHAR(200),
                created TIMESTAMP NOT NULL,
                updated TIMESTAMP NOT NULL,
                criteria VARCHAR(200),
                CONSTRAINT cms_vars_pk PRIMARY KEY (var_id)
);