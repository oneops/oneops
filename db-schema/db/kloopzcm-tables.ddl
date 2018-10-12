
CREATE TABLE kloopzcm.ns_opt_tag (
                tag_id BIGINT NOT NULL,
                tag VARCHAR(64) NOT NULL,
                CONSTRAINT tag_id PRIMARY KEY (tag_id)
);


CREATE UNIQUE INDEX ns_opt_tag_idx
 ON kloopzcm.ns_opt_tag
 ( tag );

CREATE TABLE kloopzcm.dj_approval_states (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_approval_states_pk PRIMARY KEY (state_id)
);


CREATE TABLE kloopzcm.cms_vars (
                var_id BIGINT NOT NULL,
                var_name VARCHAR(200) NOT NULL,
                var_value TEXT NOT NULL,
                updated_by VARCHAR(200),
                created TIMESTAMP NOT NULL,
                updated TIMESTAMP NOT NULL,
                criteria VARCHAR(200),
                CONSTRAINT cms_vars_pk PRIMARY KEY (var_id)
);


CREATE UNIQUE INDEX cms_vars_idx
 ON kloopzcm.cms_vars
 ( var_name );

CREATE TABLE kloopzcm.cms_lock (
                lock_id BIGINT NOT NULL,
                lock_name VARCHAR(64) NOT NULL,
                locked_by VARCHAR(200) NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cms_lock_pk PRIMARY KEY (lock_id)
);


CREATE UNIQUE INDEX cms_lock_uln
 ON kloopzcm.cms_lock
 ( lock_name );

CREATE TABLE kloopzcm.cm_ops_proc_state (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT cm_ops_proc_state_pk PRIMARY KEY (state_id)
);


CREATE TABLE kloopzcm.cm_ops_action_state (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT cm_ops_action_state_pk PRIMARY KEY (state_id)
);


CREATE TABLE kloopzcm.cms_event_type (
                event_type_id INTEGER NOT NULL,
                event_type VARCHAR(64) NOT NULL,
                CONSTRAINT cms_event_type_pk PRIMARY KEY (event_type_id)
);


CREATE TABLE kloopzcm.cms_ci_event_queue (
                event_id BIGINT NOT NULL,
                source_pk BIGINT NOT NULL,
                source_name VARCHAR(200) NOT NULL,
                event_type_id INTEGER NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cms_ci_event_queue_pk PRIMARY KEY (event_id)
);


CREATE TABLE kloopzcm.dj_deployment_states (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_deployment_states_pk PRIMARY KEY (state_id)
);


CREATE TABLE kloopzcm.dj_release_rev_label (
                release_id BIGINT NOT NULL,
                revision SMALLINT NOT NULL,
                rfc_id BIGINT NOT NULL,
                target_id SMALLINT NOT NULL,
                CONSTRAINT dj_release_rev_label_pk PRIMARY KEY (release_id, revision, rfc_id)
);


CREATE TABLE kloopzcm.ns_namespaces (
                ns_id BIGINT NOT NULL,
                ns_path VARCHAR(200) NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT ns_namespaces_pk PRIMARY KEY (ns_id)
);


CREATE UNIQUE INDEX ns_namespaces_ak
 ON kloopzcm.ns_namespaces
 ( ns_path );

CREATE INDEX ns_namespaces_vpo
 ON kloopzcm.ns_namespaces
 (ns_path varchar_pattern_ops);

CREATE TABLE kloopzcm.dj_deployment_rfc_states (
                state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_deployment_rfc_states_pk PRIMARY KEY (state_id)
);


CREATE TABLE kloopzcm.cm_ci_state (
                ci_state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT cm_ci_state_pk PRIMARY KEY (ci_state_id)
);


CREATE TABLE kloopzcm.dj_rfc_ci_actions (
                action_id INTEGER NOT NULL,
                action_name VARCHAR(200) NOT NULL,
                CONSTRAINT dj_rfc_ci_actions_pk PRIMARY KEY (action_id)
);


CREATE TABLE kloopzcm.dj_release_states (
                release_state_id INTEGER NOT NULL,
                state_name VARCHAR(64) NOT NULL,
                CONSTRAINT dj_release_states_pk PRIMARY KEY (release_state_id)
);


CREATE TABLE kloopzcm.dj_releases (
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


CREATE INDEX dj_releases_ns_idx
 ON kloopzcm.dj_releases
 ( ns_id );

CREATE TABLE kloopzcm.dj_deployment (
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
                current_step SMALLINT,
                exec_model VARCHAR(100),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_deployment_pk PRIMARY KEY (deployment_id)
);


CREATE INDEX dj_deployment_ns_idx
 ON kloopzcm.dj_deployment
 ( ns_id );

CREATE INDEX dj_deployment_rl_idx
 ON kloopzcm.dj_deployment
 ( release_id );

CREATE TABLE kloopzcm.dj_dpmt_approvals (
                approval_id BIGINT NOT NULL,
                deployment_id BIGINT NOT NULL,
                govern_ci_id BIGINT NOT NULL,
                govern_ci TEXT,
                state_id INTEGER NOT NULL,
                updated_by VARCHAR(200),
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                expires_in INTEGER,
                comments TEXT,
                CONSTRAINT dj_dpmt_approvals_pk PRIMARY KEY (approval_id)
);


CREATE INDEX dj_dpmt_approvals_dpmt_idx
 ON kloopzcm.dj_dpmt_approvals
 ( deployment_id );

CREATE INDEX dj_dpmt_approvals_cid_idx
 ON kloopzcm.dj_dpmt_approvals
 ( govern_ci_id );

CREATE TABLE kloopzcm.dj_deployment_state_hist (
                hist_id BIGINT NOT NULL,
                deployment_id BIGINT NOT NULL,
                old_state_id INTEGER,
                new_state_id INTEGER NOT NULL,
                description TEXT,
                comments TEXT,
                ops TEXT,
                updated_by VARCHAR(200),
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT dj_deployment_state_hist_pk PRIMARY KEY (hist_id)
);


CREATE INDEX dj_deployment_state_hist_idx
 ON kloopzcm.dj_deployment_state_hist
 ( deployment_id );

CREATE TABLE kloopzcm.dj_deployment_rfc (
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


CREATE INDEX dj_deployment_rfc_idx
 ON kloopzcm.dj_deployment_rfc
 ( rfc_id );

CREATE INDEX dj_deployment_rfc_d_idx1
 ON kloopzcm.dj_deployment_rfc
 ( deployment_id );

CREATE TABLE kloopzcm.cms_event_queue (
                event_id BIGINT NOT NULL,
                source_pk BIGINT NOT NULL,
                source_name VARCHAR(200) NOT NULL,
                event_type_id INTEGER NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cms_event_queue_pk PRIMARY KEY (event_id)
);


CREATE TABLE kloopzcm.cm_ci_relation_attr_log (
                log_id BIGINT NOT NULL,
                log_time TIMESTAMP NOT NULL,
                log_event INTEGER NOT NULL,
                ci_relation_id BIGINT NOT NULL,
                ci_rel_attribute_id BIGINT NOT NULL,
                attribute_id INTEGER NOT NULL,
                attribute_name VARCHAR(200) NOT NULL,
                comments VARCHAR(2000),
                owner VARCHAR(32),
                dj_attribute_value TEXT,
                dj_attribute_value_old TEXT,
                df_attribute_value TEXT,
                df_attribute_value_old TEXT,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                CONSTRAINT cm_ci_relation_attr_log_pk PRIMARY KEY (log_id)
);


CREATE INDEX cm_ci_rel_attr_log_crid
 ON kloopzcm.cm_ci_relation_attr_log
 ( ci_relation_id, ci_rel_attribute_id );

CREATE TABLE kloopzcm.cm_ci_relation_log (
                log_id BIGINT NOT NULL,
                log_time TIMESTAMP NOT NULL,
                log_event INTEGER NOT NULL,
                ci_relation_id BIGINT NOT NULL,
                from_ci_id BIGINT NOT NULL,
                to_ci_id BIGINT NOT NULL,
                ci_state_id INTEGER NOT NULL,
                ci_state_id_old INTEGER,
                comments VARCHAR(2000),
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                CONSTRAINT cm_ci_relation_log_pk PRIMARY KEY (log_id)
);


CREATE INDEX cm_ci_relation_log_crid
 ON kloopzcm.cm_ci_relation_log
 ( ci_relation_id );

CREATE TABLE kloopzcm.cm_ci_attribute_log (
                log_id BIGINT NOT NULL,
                log_time TIMESTAMP NOT NULL,
                log_event INTEGER NOT NULL,
                ci_id BIGINT NOT NULL,
                ci_attribute_id BIGINT NOT NULL,
                attribute_id INTEGER NOT NULL,
                attribute_name VARCHAR(200) NOT NULL,
                comments VARCHAR(2000),
                owner VARCHAR(32),
                dj_attribute_value TEXT,
                dj_attribute_value_old TEXT,
                df_attribute_value TEXT,
                df_attribute_value_old TEXT,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                CONSTRAINT cm_ci_attribute_log_pk PRIMARY KEY (log_id)
);


CREATE INDEX cm_ci_attribute_log_ciid
 ON kloopzcm.cm_ci_attribute_log
 ( ci_id, ci_attribute_id );

CREATE TABLE kloopzcm.cm_ci_log (
                log_id BIGINT NOT NULL,
                log_time TIMESTAMP NOT NULL,
                log_event INTEGER NOT NULL,
                ci_id BIGINT NOT NULL,
                ci_name VARCHAR(200) NOT NULL,
                class_id INTEGER NOT NULL,
                class_name VARCHAR(200) NOT NULL,
                comments VARCHAR(2000),
                ci_state_id INTEGER NOT NULL,
                ci_state_id_old INTEGER,
                created_by VARCHAR(200),
                updated_by VARCHAR(200),
                CONSTRAINT cm_ci_log_pk PRIMARY KEY (log_id)
);


CREATE INDEX cm_ci_log_ciid
 ON kloopzcm.cm_ci_log
 ( ci_id );

CREATE TABLE kloopzcm.md_relations (
                relation_id INTEGER NOT NULL,
                relation_name VARCHAR(200) NOT NULL,
                short_relation_name VARCHAR(200) NOT NULL,
                description TEXT NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_relations_pk PRIMARY KEY (relation_id)
);


CREATE UNIQUE INDEX md_relations_rln_idx
 ON kloopzcm.md_relations
 ( relation_name );

CREATE INDEX md_relations_srn_idx
 ON kloopzcm.md_relations
 ( short_relation_name );

CREATE TABLE kloopzcm.md_relation_attributes (
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


CREATE INDEX md_relation_attributes_r_idx
 ON kloopzcm.md_relation_attributes
 ( relation_id );

CREATE TABLE kloopzcm.md_classes (
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


CREATE UNIQUE INDEX md_classes_cln_idx
 ON kloopzcm.md_classes
 ( class_name );

 CREATE INDEX md_classes_comp_names_idx
 ON kloopzcm.md_classes
   (class_name, short_class_name);

 CREATE INDEX md_classes_scln_idx
 ON md_classes  (short_class_name);

CREATE TABLE kloopzcm.md_class_actions (
                action_id INTEGER NOT NULL,
                class_id INTEGER NOT NULL,
                action_name VARCHAR(200) NOT NULL,
                is_inheritable BOOLEAN DEFAULT TRUE NOT NULL,
                description TEXT,
                arguments TEXT,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT md_class_actions_pk PRIMARY KEY (action_id)
);


CREATE INDEX md_class_actions_cl_idx
 ON kloopzcm.md_class_actions
 ( class_id );

CREATE TABLE kloopzcm.dj_rfc_ci (
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


CREATE INDEX dj_rfc_ci_rcid_idx
 ON kloopzcm.dj_rfc_ci
 ( release_id, ci_id );

CREATE INDEX dj_rfc_ci_3n_idx
 ON kloopzcm.dj_rfc_ci
 ( release_id, ns_id, class_id, ci_name );

CREATE INDEX dj_rfc_ci_ns_idx
 ON kloopzcm.dj_rfc_ci
 ( ns_id );

CREATE INDEX dj_rfc_ci_cl_idx
 ON kloopzcm.dj_rfc_ci
 ( class_id );

CREATE INDEX dj_rfc_ci_ciid_idx
 ON kloopzcm.dj_rfc_ci
 ( ci_id ASC, release_id ASC );

CREATE TABLE kloopzcm.dj_ns_opt (
                rfc_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                created TIMESTAMP NOT NULL,
                tag_id BIGINT NOT NULL,
                CONSTRAINT dj_ns_opt_pk PRIMARY KEY (rfc_id, ns_id)
);


CREATE INDEX dj_ns_opt_ns_id_idx
 ON kloopzcm.dj_ns_opt
 ( ns_id );

CREATE TABLE kloopzcm.dj_rfc_relation (
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


CREATE INDEX dj_rfc_relation_crid_idx
 ON kloopzcm.dj_rfc_relation
 ( release_id, ci_relation_id );

CREATE INDEX dj_rfc_rel_ns_idx
 ON kloopzcm.dj_rfc_relation
 ( ns_id );

CREATE INDEX dj_rfc_rel_frfc_idx
 ON kloopzcm.dj_rfc_relation
 ( from_rfc_id );

CREATE INDEX dj_rfc_rel_fcireltoci_idx
 ON kloopzcm.dj_rfc_relation
 ( from_ci_id, release_id, relation_id, to_ci_id );

CREATE INDEX dj_rfc_rel_r_idx
 ON kloopzcm.dj_rfc_relation
 ( relation_id );

CREATE INDEX dj_rfc_rel_trfc_idx
 ON kloopzcm.dj_rfc_relation
 ( to_rfc_id );

CREATE INDEX dj_rfc_rel_tcirlsfromci_idx
 ON kloopzcm.dj_rfc_relation
 ( to_ci_id, release_id, relation_id, from_ci_id );

CREATE INDEX dj_rfc_relation_cid_idx
ON kloopzcm.dj_rfc_relation  (ci_relation_id);

CREATE TABLE kloopzcm.dj_rfc_relation_attributes (
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


CREATE INDEX dj_rfc_relation_attr_rfc_idx
 ON kloopzcm.dj_rfc_relation_attributes
 ( rfc_id );

CREATE INDEX dj_rfc_relation_attr_a_idx
 ON kloopzcm.dj_rfc_relation_attributes
 ( attribute_id );

CREATE TABLE kloopzcm.cm_ci (
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


CREATE UNIQUE INDEX df_ci_goid_idx
 ON kloopzcm.cm_ci
 ( ci_goid );

CREATE UNIQUE INDEX cm_ci_3cols_idx
 ON kloopzcm.cm_ci
 ( ns_id, class_id, ci_name );

CREATE INDEX cm_ci_ns_idx
 ON kloopzcm.cm_ci
 ( ns_id );

CREATE INDEX cm_ci_cl_idx
 ON kloopzcm.cm_ci
 ( class_id );

CREATE TABLE kloopzcm.cm_ns_opt (
                ci_id BIGINT NOT NULL,
                ns_id BIGINT NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                tag_id BIGINT NOT NULL,
                CONSTRAINT cm_ns_opt_pk PRIMARY KEY (ci_id, ns_id)
);


CREATE INDEX cm_ns_opt_ns_id_idx
 ON kloopzcm.cm_ns_opt
 ( ns_id );

CREATE TABLE kloopzcm.cm_ops_procedures (
                ops_proc_id BIGINT NOT NULL,
                ci_id BIGINT NOT NULL,
                proc_name VARCHAR(64) NOT NULL,
                proc_ci_id BIGINT,
                created_by VARCHAR(200) NOT NULL,
                arglist TEXT,
                definition TEXT,
                state_id INTEGER NOT NULL,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cm_ops_procedures_pk PRIMARY KEY (ops_proc_id)
);


CREATE INDEX CONCURRENTLY cm_ops_proc_ci_state_idx
 ON kloopzcm.cm_ops_procedures
 ( ci_id, state_id, created );

CREATE INDEX cm_ops_procedures_ci_proc_idx
 ON kloopzcm.cm_ops_procedures
 ( ci_id, ops_proc_id );

CREATE TABLE kloopzcm.cm_ops_actions (
                ops_action_id BIGINT NOT NULL,
                ops_proc_id BIGINT NOT NULL,
                ci_id BIGINT NOT NULL,
                action_name VARCHAR(200) NOT NULL,
                arglist TEXT,
                payload TEXT,
                exec_order INTEGER NOT NULL,
                state_id INTEGER NOT NULL,
                is_critical BOOLEAN DEFAULT FALSE NOT NULL,
                extra_info TEXT,
                created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT cm_ops_actions_pk PRIMARY KEY (ops_action_id)
);


CREATE INDEX cm_ops_actions_proc_id_idx
 ON kloopzcm.cm_ops_actions
 ( ops_proc_id );

CREATE INDEX cm_ops_actions_ci_proc_idx
 ON kloopzcm.cm_ops_actions
 ( ci_id, ops_proc_id );

CREATE TABLE kloopzcm.cm_ci_relations (
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


CREATE UNIQUE INDEX cm_ci_relations_uniq_idx
 ON kloopzcm.cm_ci_relations
 ( from_ci_id, relation_id, to_ci_id );

CREATE UNIQUE INDEX cm_ci_relations_goid_idx
 ON kloopzcm.cm_ci_relations
 ( relation_goid );

CREATE INDEX cm_ci_relations_ns_idx
 ON kloopzcm.cm_ci_relations
 ( ns_id );

CREATE INDEX cm_ci_relations_fromci_idx
 ON kloopzcm.cm_ci_relations
 ( from_ci_id );

CREATE INDEX cm_ci_relations_toci_idx
 ON kloopzcm.cm_ci_relations
 ( to_ci_id );

CREATE INDEX cm_ci_relations_r_idx
 ON kloopzcm.cm_ci_relations
 ( relation_id );

CREATE INDEX cm_ci_relations_r_ns_idx
 ON kloopzcm.cm_ci_relations
 ( relation_id, ns_id );

CREATE TABLE kloopzcm.cm_ci_relation_attributes (
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


CREATE INDEX cm_ci_relation_attr_ridx
 ON kloopzcm.cm_ci_relation_attributes
 ( ci_relation_id );

CREATE INDEX cm_ci_relation_attr_a_idx
 ON kloopzcm.cm_ci_relation_attributes
 ( attribute_id );

CREATE INDEX cm_ci_relation_attr_dj_value_idx
 ON kloopzcm.cm_ci_relation_attributes
 ( attribute_id, dj_attribute_value );

CREATE TABLE kloopzcm.md_class_relations (
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


CREATE UNIQUE INDEX md_class_relations_idx
 ON kloopzcm.md_class_relations
 ( from_class_id, relation_id, to_class_id );

CREATE INDEX md_class_relations_f_idx1
 ON kloopzcm.md_class_relations
 ( from_class_id );

CREATE INDEX md_class_relations_t_idx1
 ON kloopzcm.md_class_relations
 ( to_class_id );

CREATE INDEX md_class_relations_r_idx1
 ON kloopzcm.md_class_relations
 ( relation_id );

CREATE TABLE kloopzcm.md_class_attributes (
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


CREATE INDEX md_class_attributes_cl_idx
 ON kloopzcm.md_class_attributes
 ( class_id );

CREATE TABLE kloopzcm.dj_rfc_ci_attributes (
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


CREATE INDEX dj_rfc_ci_attr_rfc_idx
 ON kloopzcm.dj_rfc_ci_attributes
 ( rfc_id );

CREATE INDEX dj_rfc_ci_attr_a_idx
 ON kloopzcm.dj_rfc_ci_attributes
 ( attribute_id );

CREATE TABLE kloopzcm.cm_ci_attributes (
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


CREATE UNIQUE INDEX cm_ci_attributes_uniq_attrid
 ON kloopzcm.cm_ci_attributes
 ( ci_id, attribute_id );

CREATE INDEX cm_ci_attributes_ci_idx
 ON kloopzcm.cm_ci_attributes
 ( ci_id );

CREATE INDEX cm_ci_attributes_attr_idx
 ON kloopzcm.cm_ci_attributes
 ( attribute_id );

ALTER TABLE kloopzcm.cm_ns_opt ADD CONSTRAINT ns_opt_tag_cm_ns_opt_fk
FOREIGN KEY (tag_id)
REFERENCES kloopzcm.ns_opt_tag (tag_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_ns_opt ADD CONSTRAINT ns_opt_tag_dj_ns_opt_fk
FOREIGN KEY (tag_id)
REFERENCES kloopzcm.ns_opt_tag (tag_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_dpmt_approvals ADD CONSTRAINT dj_dpmt_approvals_states_fk
FOREIGN KEY (state_id)
REFERENCES kloopzcm.dj_approval_states (state_id)
ON DELETE RESTRICT
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ops_procedures ADD CONSTRAINT cm_ops_procedures_st_fk
FOREIGN KEY (state_id)
REFERENCES kloopzcm.cm_ops_proc_state (state_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ops_actions ADD CONSTRAINT cm_ops_actions_st_fk
FOREIGN KEY (state_id)
REFERENCES kloopzcm.cm_ops_action_state (state_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cms_event_queue ADD CONSTRAINT cms_event_queue_etid_fk
FOREIGN KEY (event_type_id)
REFERENCES kloopzcm.cms_event_type (event_type_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cms_ci_event_queue ADD CONSTRAINT cms_ci_event_queue_etid_fk
FOREIGN KEY (event_type_id)
REFERENCES kloopzcm.cms_event_type (event_type_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_deployment ADD CONSTRAINT dj_deployment_states_dj_deployment_fk
FOREIGN KEY (state_id)
REFERENCES kloopzcm.dj_deployment_states (state_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci ADD CONSTRAINT cm_ci_ns_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_ci ADD CONSTRAINT dj_ci_rfc_ns_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_releases ADD CONSTRAINT cm_namespaces_dj_releases_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_deployment ADD CONSTRAINT cm_namespaces_dj_deployment_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation ADD CONSTRAINT dj_rfc_relation_ns_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relations ADD CONSTRAINT cm_ci_relations_ns_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ns_opt ADD CONSTRAINT ns_path_cm_ns_opt_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_ns_opt ADD CONSTRAINT ns_dj_ns_opt_fk
FOREIGN KEY (ns_id)
REFERENCES kloopzcm.ns_namespaces (ns_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_deployment_rfc ADD CONSTRAINT dj_deployment_rfc_dprfcstid_fk
FOREIGN KEY (state_id)
REFERENCES kloopzcm.dj_deployment_rfc_states (state_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci ADD CONSTRAINT cm_ci_stid_fk
FOREIGN KEY (ci_state_id)
REFERENCES kloopzcm.cm_ci_state (ci_state_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relations ADD CONSTRAINT cm_ci_relations_stid_fk
FOREIGN KEY (ci_state_id)
REFERENCES kloopzcm.cm_ci_state (ci_state_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_ci ADD CONSTRAINT dj_rfc_ci_ciaid_fk
FOREIGN KEY (action_id)
REFERENCES kloopzcm.dj_rfc_ci_actions (action_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation ADD CONSTRAINT dj_relation_rfc_actid_fk
FOREIGN KEY (action_id)
REFERENCES kloopzcm.dj_rfc_ci_actions (action_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_releases ADD CONSTRAINT dj_releases_rsid_fk
FOREIGN KEY (release_state_id)
REFERENCES kloopzcm.dj_release_states (release_state_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_ci ADD CONSTRAINT dj_rfc_rid_fk
FOREIGN KEY (release_id)
REFERENCES kloopzcm.dj_releases (release_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation ADD CONSTRAINT dj_relation_rfc_relid_fk
FOREIGN KEY (release_id)
REFERENCES kloopzcm.dj_releases (release_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_deployment ADD CONSTRAINT dj_deployment_rid_fk
FOREIGN KEY (release_id)
REFERENCES kloopzcm.dj_releases (release_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_deployment_rfc ADD CONSTRAINT dj_deployment_dj_deployment_rfc_fk
FOREIGN KEY (deployment_id)
REFERENCES kloopzcm.dj_deployment (deployment_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_deployment_state_hist ADD CONSTRAINT dj_deployment_state_hist_fk
FOREIGN KEY (deployment_id)
REFERENCES kloopzcm.dj_deployment (deployment_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_dpmt_approvals ADD CONSTRAINT dj_dpmt_approvals_dpmt_id_fk
FOREIGN KEY (deployment_id)
REFERENCES kloopzcm.dj_deployment (deployment_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.md_relation_attributes ADD CONSTRAINT md_relation_attributes_mdrid_fk
FOREIGN KEY (relation_id)
REFERENCES kloopzcm.md_relations (relation_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.md_class_relations ADD CONSTRAINT md_class_relations_mdrid_fk
FOREIGN KEY (relation_id)
REFERENCES kloopzcm.md_relations (relation_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relations ADD CONSTRAINT df_ci_relations_mdrid_fk
FOREIGN KEY (relation_id)
REFERENCES kloopzcm.md_relations (relation_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation ADD CONSTRAINT dj_relation_rfc_rid_fk
FOREIGN KEY (relation_id)
REFERENCES kloopzcm.md_relations (relation_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relation_attributes ADD CONSTRAINT cm_ci_relation_attributes_raid_fk
FOREIGN KEY (attribute_id)
REFERENCES kloopzcm.md_relation_attributes (attribute_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation_attributes ADD CONSTRAINT md_relation_attributes_dj_rfc_relation_attributes_fk
FOREIGN KEY (attribute_id)
REFERENCES kloopzcm.md_relation_attributes (attribute_id)
ON DELETE RESTRICT
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.md_class_attributes ADD CONSTRAINT md_class_attributes_clid_fk
FOREIGN KEY (class_id)
REFERENCES kloopzcm.md_classes (class_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.md_class_relations ADD CONSTRAINT md_class_relations_frcl_fk
FOREIGN KEY (from_class_id)
REFERENCES kloopzcm.md_classes (class_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.md_class_relations ADD CONSTRAINT md_class_relations_tocl_fk
FOREIGN KEY (to_class_id)
REFERENCES kloopzcm.md_classes (class_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci ADD CONSTRAINT cm_ci_clid_fk
FOREIGN KEY (class_id)
REFERENCES kloopzcm.md_classes (class_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_ci ADD CONSTRAINT dj_rfc_ci_clid_fk
FOREIGN KEY (class_id)
REFERENCES kloopzcm.md_classes (class_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.md_class_actions ADD CONSTRAINT md_class_actions_cl_fk
FOREIGN KEY (class_id)
REFERENCES kloopzcm.md_classes (class_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_ci_attributes ADD CONSTRAINT dj_rfc_ci_attributes_ciid_fk
FOREIGN KEY (rfc_id)
REFERENCES kloopzcm.dj_rfc_ci (rfc_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation ADD CONSTRAINT dj_rfc_ci_dj_rfc_relation_fk
FOREIGN KEY (from_rfc_id)
REFERENCES kloopzcm.dj_rfc_ci (rfc_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation ADD CONSTRAINT dj_rfc_ci_dj_rfc_relation_fk1
FOREIGN KEY (to_rfc_id)
REFERENCES kloopzcm.dj_rfc_ci (rfc_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_ns_opt ADD CONSTRAINT dj_rfc_ci_dj_ns_opt_fk
FOREIGN KEY (rfc_id)
REFERENCES kloopzcm.dj_rfc_ci (rfc_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_relation_attributes ADD CONSTRAINT rfc_relation_attributes_rfcrid_fk
FOREIGN KEY (rfc_id)
REFERENCES kloopzcm.dj_rfc_relation (rfc_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_attributes ADD CONSTRAINT cm_ci_attributes_ciid_fk
FOREIGN KEY (ci_id)
REFERENCES kloopzcm.cm_ci (ci_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relations ADD CONSTRAINT cm_ci_relations_frid_fk
FOREIGN KEY (from_ci_id)
REFERENCES kloopzcm.cm_ci (ci_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relations ADD CONSTRAINT cm_ci_relations_toid_fk
FOREIGN KEY (to_ci_id)
REFERENCES kloopzcm.cm_ci (ci_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ops_procedures ADD CONSTRAINT cm_ops_procedures_ci_fk
FOREIGN KEY (ci_id)
REFERENCES kloopzcm.cm_ci (ci_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ops_actions ADD CONSTRAINT cm_ops_actions_ci_fk
FOREIGN KEY (ci_id)
REFERENCES kloopzcm.cm_ci (ci_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ns_opt ADD CONSTRAINT cm_ci_cm_ns_opt_fk
FOREIGN KEY (ci_id)
REFERENCES kloopzcm.cm_ci (ci_id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ops_actions ADD CONSTRAINT cm_ops_actions_proc_fk
FOREIGN KEY (ops_proc_id)
REFERENCES kloopzcm.cm_ops_procedures (ops_proc_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_relation_attributes ADD CONSTRAINT cm_ci_relation_attributes_crid_fk
FOREIGN KEY (ci_relation_id)
REFERENCES kloopzcm.cm_ci_relations (ci_relation_id)
ON DELETE CASCADE
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.cm_ci_attributes ADD CONSTRAINT cm_ci_attributes_attr_fk
FOREIGN KEY (attribute_id)
REFERENCES kloopzcm.md_class_attributes (attribute_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;

ALTER TABLE kloopzcm.dj_rfc_ci_attributes ADD CONSTRAINT dj_rfc_ci_attributes_atrid_fk
FOREIGN KEY (attribute_id)
REFERENCES kloopzcm.md_class_attributes (attribute_id)
ON DELETE RESTRICT
ON UPDATE RESTRICT
NOT DEFERRABLE;


CREATE TABLE kloopzcm.dj_dpmt_execution (
    exec_id BIGSERIAL PRIMARY KEY,
    deployment_id BIGINT NOT NULL REFERENCES kloopzcm.dj_deployment(deployment_id) ON DELETE CASCADE,
    step SMALLINT NOT NULL,
    state_id INTEGER,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP
);

CREATE UNIQUE INDEX dj_dpmt_exec_idx
 ON kloopzcm.dj_dpmt_execution
 ( deployment_id, step );

CREATE TABLE kloopzcm.cm_procedure_execution (
    exec_id BIGSERIAL PRIMARY KEY,
    ops_proc_id BIGINT NOT NULL REFERENCES kloopzcm.cm_ops_procedures(ops_proc_id) ON DELETE CASCADE,
    step SMALLINT NOT NULL,
    state_id INTEGER,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP
);

CREATE UNIQUE INDEX cm_proc_exec_idx
 ON kloopzcm.cm_procedure_execution
 ( ops_proc_id, step );


CREATE INDEX dj_release_state_id_idx
  ON kloopzcm.dj_releases
  USING btree
  (release_state_id)
  WHERE release_state_id = 100;

