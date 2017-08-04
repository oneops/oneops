-- Function: cm_add_ci_attribute(bigint, integer, text, text, character varying, character varying)

-- DROP FUNCTION cm_add_ci_attribute(bigint, integer, text, text, character varying, character varying);

CREATE OR REPLACE FUNCTION cm_add_ci_attribute(IN p_ci_id bigint, IN p_attribute_id integer, IN p_df_value text, IN p_dj_value text, IN p_owner character varying, IN p_comments character varying, IN p_event boolean, OUT out_ci_attr_id bigint)
  RETURNS bigint AS
$BODY$
DECLARE
    l_attribute_name character varying;
BEGIN
    select into l_attribute_name a.attribute_name 
    from md_class_attributes a
    where a.attribute_id = p_attribute_id;

    insert into cm_ci_attributes (ci_attribute_id, ci_id, attribute_id, df_attribute_value, dj_attribute_value, owner, comments)
    values (nextval('cm_pk_seq'), p_ci_id, p_attribute_id, p_df_value, p_dj_value, p_owner, p_comments)
    returning ci_attribute_id into out_ci_attr_id;

    insert into cm_ci_attribute_log(log_id, log_time, log_event, ci_id, ci_attribute_id, attribute_id, attribute_name, comments, owner, dj_attribute_value, dj_attribute_value_old, df_attribute_value, df_attribute_value_old) 
    values (nextval('log_pk_seq'), now(), 100, p_ci_id, out_ci_attr_id, p_attribute_id, l_attribute_name, p_comments, p_owner, p_dj_value, p_dj_value, p_df_value, p_df_value);

    if p_event = true then
	    insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    	values (nextval('event_pk_seq'), p_ci_id, 'cm_ci' , 200);
	end if;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_add_ci_attribute(bigint, integer, text, text, character varying, character varying, boolean) OWNER TO :user;


-- Function: cm_add_ci_rel_attribute(bigint, integer, text, text, character varying, character varying)

-- DROP FUNCTION cm_add_ci_rel_attribute(bigint, integer, text, text, character varying, character varying);

CREATE OR REPLACE FUNCTION cm_add_ci_rel_attribute(IN p_ci_rel_id bigint, IN p_attribute_id integer, IN p_df_value text, IN p_dj_value text, IN p_owner character varying, IN p_comments character varying, IN p_event boolean, OUT out_ci_rel_attr_id bigint)
  RETURNS bigint AS
$BODY$
DECLARE
    l_attribute_name character varying;
BEGIN
    select into l_attribute_name a.attribute_name 
    from md_relation_attributes a
    where a.attribute_id = p_attribute_id;

    insert into cm_ci_relation_attributes (ci_rel_attribute_id, ci_relation_id, attribute_id, df_attribute_value, dj_attribute_value, owner, comments)
    values (nextval('cm_pk_seq'), p_ci_rel_id, p_attribute_id, p_df_value, p_dj_value, p_owner, p_comments)
    returning ci_rel_attribute_id into out_ci_rel_attr_id;

    insert into cm_ci_relation_attr_log(log_id, log_time, log_event, ci_relation_id, ci_rel_attribute_id, attribute_id, attribute_name, comments, owner, dj_attribute_value, dj_attribute_value_old, df_attribute_value, df_attribute_value_old) 
    values (nextval('log_pk_seq'), now(), 100, p_ci_rel_id, out_ci_rel_attr_id, p_attribute_id, l_attribute_name, p_comments, p_owner, p_dj_value, p_dj_value, p_df_value, p_df_value);

    if p_event = true then
	    insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    	values (nextval('event_pk_seq'), p_ci_rel_id, 'cm_ci_rel' , 200);
	end if;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_add_ci_rel_attribute(bigint, integer, text, text, character varying, character varying, boolean) OWNER TO :user;

-- Function: cm_create_ci(bigint, bigint, integer, character varying, character varying, character varying, integer, character varying)

-- DROP FUNCTION cm_create_ci(bigint, bigint, integer, character varying, character varying, character varying, integer, character varying);

CREATE OR REPLACE FUNCTION cm_create_ci(p_ci_id bigint, p_ns_id bigint, p_class_id integer, p_goid character varying, p_ci_name character varying, p_comments character varying, p_state_id integer, p_created_by character varying)
  RETURNS void AS
$BODY$
BEGIN
     perform cm_create_ci(p_ci_id, p_ns_id, p_class_id, p_goid, p_ci_name, p_comments, p_state_id, null, p_created_by);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_create_ci(bigint, bigint, integer, character varying, character varying, character varying, integer, character varying) OWNER TO :user;


-- Function: cm_create_ci(bigint, bigint, integer, character varying, character varying, character varying, integer, bigint, character varying)

-- DROP FUNCTION cm_create_ci(bigint, bigint, integer, character varying, character varying, character varying, integer, bigint, character varying);

CREATE OR REPLACE FUNCTION cm_create_ci(p_ci_id bigint, p_ns_id bigint, p_class_id integer, p_goid character varying, p_ci_name character varying, p_comments character varying, p_state_id integer, p_last_rfc_id bigint, p_created_by character varying)
  RETURNS void AS
$BODY$
DECLARE
	l_class_name character varying;
BEGIN
    select into l_class_name cl.class_name 
    from md_classes cl
    where cl.class_id = p_class_id;

    insert into cm_ci (ci_id, ns_id, class_id, ci_name, ci_goid, comments, ci_state_id, last_applied_rfc_id, created_by)
    values (p_ci_id, p_ns_id, p_class_id, p_ci_name, p_goid, p_comments, p_state_id, p_last_rfc_id, p_created_by);

    insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    values (nextval('event_pk_seq'), p_ci_id, 'cm_ci' , 100);

    insert into cm_ci_log(log_id, log_time, log_event, ci_id, ci_name, class_id, class_name, comments, ci_state_id, ci_state_id_old, created_by)
    values (nextval('log_pk_seq'), now(), 100, p_ci_id, p_ci_name, p_class_id, l_class_name, p_comments, p_state_id, p_state_id, p_created_by);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_create_ci(bigint, bigint, integer, character varying, character varying, character varying, integer, bigint, character varying) OWNER TO :user;


-- Function: cm_create_relation(bigint, bigint, bigint, integer, bigint, character varying, character varying, integer)

-- DROP FUNCTION cm_create_relation(bigint, bigint, bigint, integer, bigint, character varying, character varying, integer);

CREATE OR REPLACE FUNCTION cm_create_relation(p_ci_relation_id bigint, p_ns_id bigint, p_from_ci_id bigint, p_relation_id integer, p_to_ci_id bigint, p_rel_goid character varying, p_comments character varying, p_state_id integer)
  RETURNS void AS
$BODY$
BEGIN
    perform cm_create_relation(p_ci_relation_id, p_ns_id, p_from_ci_id, p_relation_id, p_to_ci_id, p_rel_goid, p_comments, p_state_id, null);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_create_relation(bigint, bigint, bigint, integer, bigint, character varying, character varying, integer) OWNER TO :user;


-- Function: cm_create_relation(bigint, bigint, bigint, integer, bigint, character varying, character varying, integer, bigint)

-- DROP FUNCTION cm_create_relation(bigint, bigint, bigint, integer, bigint, character varying, character varying, integer, bigint);

CREATE OR REPLACE FUNCTION cm_create_relation(p_ci_relation_id bigint, p_ns_id bigint, p_from_ci_id bigint, p_relation_id integer, p_to_ci_id bigint, p_rel_goid character varying, p_comments character varying, p_state_id integer, p_last_rfc_id bigint)
  RETURNS void AS
$BODY$
BEGIN

    begin 		
		insert into cm_ci_relations (ci_relation_id, ns_id, from_ci_id, relation_goid, relation_id, to_ci_id, ci_state_id, comments, last_applied_rfc_id)
		values (p_ci_relation_id, p_ns_id, p_from_ci_id, p_rel_goid, p_relation_id, p_to_ci_id, p_state_id, p_comments, p_last_rfc_id);
    
		insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    	values (nextval('event_pk_seq'), p_ci_relation_id, 'cm_ci_rel' , 200);

		insert into cm_ci_relation_log(log_id, log_time, log_event, ci_relation_id, from_ci_id, to_ci_id, ci_state_id, ci_state_id_old, comments) 
		values (nextval('log_pk_seq'), now(), 100, p_ci_relation_id, p_from_ci_id, p_to_ci_id, p_state_id, p_state_id, p_comments);
    exception when integrity_constraint_violation then
		raise notice '% %', sqlerrm, sqlstate;
    end;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_create_relation(bigint, bigint, bigint, integer, bigint, character varying, character varying, integer, bigint) OWNER TO :user;

-- Function: (bigint, boolean)

-- DROP FUNCTION cm_delete_ci(bigint, boolean);

CREATE OR REPLACE FUNCTION cm_delete_ci(p_ci_id bigint, p_delete4real boolean, p_deleted_by character varying)
  RETURNS void AS
$BODY$
BEGIN
   perform cm_delete_ci(p_ci_id, null, p_delete4real, p_deleted_by);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_delete_ci(bigint, boolean, character varying) OWNER TO :user;

CREATE OR REPLACE FUNCTION cm_delete_ci(p_ci_id bigint, p_last_rfc_id bigint, p_delete4real boolean, p_deleted_by character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_ci_name character varying;
    l_is_namespace boolean;
    l_this_ns_path character varying;
    l_class_id integer;
    l_class_name character varying;
    l_comments character varying;
    l_created_by character varying;
    l_state_id integer;
    l_flags integer;
BEGIN

    select into l_ci_name, l_is_namespace, l_this_ns_path, l_class_id, l_class_name, l_comments, l_state_id, l_flags, l_created_by   
		ci.ci_name, cl.is_namespace, ns.ns_path, cl.class_id, cl.class_name, ci.comments, ci.ci_state_id, cl.flags, ci.created_by  
    from cm_ci ci, md_classes cl, ns_namespaces ns
    where ci.ci_id = p_ci_id
      and ci.class_id = cl.class_id
      and ci.ns_id = ns.ns_id;

    if l_ci_name is not null then  
	    if l_is_namespace = true then
	        if l_this_ns_path = '/' then
	           if (l_flags::bit(2) & B'10')::integer > 0 then
			      perform ns_delete_namespace('/' || l_class_name || '/' ||  l_ci_name);	
	           else
			      perform ns_delete_namespace('/' ||  l_ci_name);	
		       end if;
		    else 
		       if (l_flags::bit(2) & B'10')::integer > 0 then 
			      perform ns_delete_namespace(l_this_ns_path || '/' || l_class_name || '/' || l_ci_name);	
		       else
			      perform ns_delete_namespace(l_this_ns_path || '/' ||  l_ci_name);	
		       end if;
		    end if;	
	    end if;   
	
	    if p_delete4real then	
	
	    	insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
		    values (nextval('event_pk_seq'), p_ci_id, 'cm_ci' , 300);
		
		    insert into cm_ci_log(log_id, log_time, log_event, ci_id, ci_name, class_id, class_name, comments, ci_state_id, ci_state_id_old, created_by, updated_by)
		    values (nextval('log_pk_seq'), now(), 300, p_ci_id, l_ci_name, l_class_id, l_class_name, l_comments, l_state_id, l_state_id, l_created_by, p_deleted_by);
		
		    delete from cm_ci where ci_id = p_ci_id; 
	    else
	        update cm_ci
	        set ci_state_id = 200, --pending_delete
			last_applied_rfc_id = coalesce(p_last_rfc_id, last_applied_rfc_id),
	        	updated = now()
	        where ci_id = p_ci_id;
	    end if;
    end if;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_delete_ci(bigint, bigint, boolean, character varying) OWNER TO :user;

-- Function: cm_delete_relation(bigint, boolean)

-- DROP FUNCTION cm_delete_relation(bigint, boolean);

CREATE OR REPLACE FUNCTION cm_delete_relation(p_ci_relation_id bigint, p_delete4real boolean)
  RETURNS void AS
$BODY$
DECLARE
    l_from_ci_id bigint;
    l_to_ci_id bigint;
    l_comments character varying;
    l_state_id integer;
BEGIN

    select into l_from_ci_id, l_to_ci_id, l_comments, l_state_id   
                r.from_ci_id, r.to_ci_id, r.comments, r.ci_state_id  
    from cm_ci_relations r
    where r.ci_relation_id = p_ci_relation_id;

    if p_delete4real then

        delete from cm_ci_relations where ci_relation_id = p_ci_relation_id; 
        
        insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    	values (nextval('event_pk_seq'), p_ci_relation_id, 'cm_ci_rel' , 300);
    	
        -- if relation still exists and not deleted as cascade deletion on ci - put it in the log	
        if l_from_ci_id is not null then 
	       insert into cm_ci_relation_log(log_id, log_time, log_event, ci_relation_id, from_ci_id, to_ci_id, ci_state_id, ci_state_id_old, comments) 
	       values (nextval('log_pk_seq'), now(), 300, p_ci_relation_id, l_from_ci_id, l_to_ci_id, l_state_id, l_state_id, l_comments);
        end if;

    else

        update cm_ci_relations
	    set ci_state_id = 200, --pending deletion
	        updated = now()
	    where ci_relation_id = p_ci_relation_id;
    
	end if;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_delete_relation(bigint, boolean) OWNER TO :user;


-- Function: cm_update_ci(bigint, character varying, character varying, integer, character varying)

-- DROP FUNCTION cm_update_ci(bigint, character varying, character varying, integer, character varying);

CREATE OR REPLACE FUNCTION cm_update_ci(p_ci_id bigint, p_ci_name character varying, p_comments character varying, p_state_id integer, p_updated_by character varying)
  RETURNS void AS
$BODY$
BEGIN
   perform cm_update_ci(p_ci_id, p_ci_name, p_comments, p_state_id, null, p_updated_by);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_ci(bigint, character varying, character varying, integer, character varying) OWNER TO :user;


-- Function: cm_update_ci(bigint, character varying, character varying, integer, bigint, character varying)

-- DROP FUNCTION cm_update_ci(bigint, character varying, character varying, integer, bigint, character varying);

CREATE OR REPLACE FUNCTION cm_update_ci(p_ci_id bigint, p_ci_name character varying, p_comments character varying, p_state_id integer, p_last_rfc_id bigint, p_updated_by character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_class_id integer;
    l_class_name character varying;
    l_ci_name character varying;
    l_comments character varying;
    l_state_id integer;
BEGIN
    select into l_class_id, l_class_name, l_ci_name, l_comments, l_state_id   
		 cl.class_id, cl.class_name, ci.ci_name, ci.comments, ci.ci_state_id  
    from cm_ci ci, md_classes cl
    where ci.ci_id = p_ci_id
      and ci.class_id = cl.class_id;

    update cm_ci 
     set ci_name = coalesce(p_ci_name, ci_name), 
         comments = coalesce(p_comments, comments),
         ci_state_id = coalesce(p_state_id, ci_state_id),
         last_applied_rfc_id = coalesce(p_last_rfc_id, last_applied_rfc_id),
         updated_by = p_updated_by,
         updated = now()
    where ci_id = p_ci_id;

    insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    values (nextval('event_pk_seq'), p_ci_id, 'cm_ci' , 200);

    insert into cm_ci_log(log_id, log_time, log_event, ci_id, ci_name, class_id, class_name, comments, ci_state_id, ci_state_id_old, updated_by)
    values (nextval('log_pk_seq'), now(), 200, p_ci_id, coalesce(p_ci_name, l_ci_name), l_class_id, l_class_name, coalesce(p_comments, l_comments), l_state_id, coalesce(p_state_id, l_state_id), p_updated_by);
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_ci(bigint, character varying, character varying, integer, bigint, character varying) OWNER TO :user;


-- Function: cm_update_rel(bigint, character varying, integer, bigint)

-- DROP FUNCTION cm_update_rel(bigint, character varying, integer, bigint);

CREATE OR REPLACE FUNCTION cm_update_rel(p_rel_id bigint, p_comments character varying, p_state_id integer, p_last_rfc_id bigint)
  RETURNS void AS
$BODY$
BEGIN

    update cm_ci_relations 
     set comments = coalesce(p_comments, comments),
         ci_state_id = coalesce(p_state_id, ci_state_id),
         last_applied_rfc_id = coalesce(p_last_rfc_id, last_applied_rfc_id)
    where ci_relation_id = p_rel_id;

    insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
    values (nextval('event_pk_seq'), p_rel_id, 'cm_ci_rel' , 200);

    --insert into cm_ci_relation_log(log_id, log_time, log_event, ci_relation_id, from_ci_id, to_ci_id, ci_state_id, ci_state_id_old, comments) 
    --values (nextval('log_pk_seq'), now(), 200, p_ci_relation_id, p_from_ci_id, p_to_ci_id, p_state_id, p_state_id, p_comments);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_rel(bigint, character varying, integer, bigint) OWNER TO :user;



-- Function: cm_update_ci_attribute(bigint, text, text, character varying, character varying)

-- DROP FUNCTION cm_update_ci_attribute(bigint, text, text, character varying, character varying);

CREATE OR REPLACE FUNCTION cm_update_ci_attribute(p_ci_attr_id bigint, p_df_value text, p_dj_value text, p_owner character varying, p_comments character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_ci_id bigint;
    l_attribute_id integer;
    l_attribute_name character varying;
	l_dj_attribute_value text;
	l_df_attribute_value text;
BEGIN
    select into l_dj_attribute_value, l_df_attribute_value, l_attribute_id, l_attribute_name   
		 a.dj_attribute_value, a.df_attribute_value, a.attribute_id, cl.attribute_name   
    from cm_ci_attributes a, md_class_attributes cl
    where a.ci_attribute_id = p_ci_attr_id 
      and a.attribute_id = cl.attribute_id;

    update cm_ci_attributes set
    df_attribute_value = coalesce(p_df_value, df_attribute_value),
    dj_attribute_value = coalesce(p_dj_value, dj_attribute_value),
    owner = p_owner,
    comments = p_comments,
    updated = now()
    where ci_attribute_id = p_ci_attr_id
    returning ci_id into l_ci_id;

    update cm_ci
    set updated = now()
    where ci_id = l_ci_id;

    insert into cm_ci_attribute_log(log_id, log_time, log_event, ci_id, ci_attribute_id, attribute_id, attribute_name, comments, owner, dj_attribute_value, dj_attribute_value_old, df_attribute_value, df_attribute_value_old) 
    values (nextval('log_pk_seq'), now(), 200, l_ci_id, p_ci_attr_id, l_attribute_id, l_attribute_name, p_comments, p_owner, coalesce(p_dj_value, l_dj_attribute_value), l_dj_attribute_value, coalesce(p_df_value, l_df_attribute_value), l_df_attribute_value);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_ci_attribute(bigint, text, text, character varying, character varying) OWNER TO :user;

-- Function: cm_update_rel_attribute(bigint, text, text, character varying, character varying)

-- DROP FUNCTION cm_update_rel_attribute(bigint, text, text, character varying, character varying);

CREATE OR REPLACE FUNCTION cm_update_rel_attribute(p_ci_rel_attr_id bigint, p_df_value text, p_dj_value text, p_owner character varying, p_comments character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_ci_rel_id bigint;
    l_attribute_id integer;
    l_attribute_name character varying;
	l_dj_attribute_value text;
	l_df_attribute_value text;
BEGIN
    select into l_dj_attribute_value, l_df_attribute_value, l_attribute_id, l_attribute_name   
		 a.dj_attribute_value, a.df_attribute_value, a.attribute_id, cl.attribute_name   
    from cm_ci_relation_attributes a, md_relation_attributes cl
    where a.ci_rel_attribute_id = p_ci_rel_attr_id 
      and a.attribute_id = cl.attribute_id;

    update cm_ci_relation_attributes set
    df_attribute_value = coalesce(p_df_value, df_attribute_value),
    dj_attribute_value = coalesce(p_dj_value, dj_attribute_value),
    owner = p_owner,
    comments = p_comments,
    updated = now()
    where ci_rel_attribute_id = p_ci_rel_attr_id
    returning ci_relation_id into l_ci_rel_id;

    update cm_ci_relations
    set updated = now()
    where ci_relation_id = l_ci_rel_id;

    insert into cm_ci_relation_attr_log(log_id, log_time, log_event, ci_relation_id, ci_rel_attribute_id, attribute_id, attribute_name, comments, owner, dj_attribute_value, dj_attribute_value_old, df_attribute_value, df_attribute_value_old) 
    values (nextval('log_pk_seq'), now(), 200, l_ci_rel_id, p_ci_rel_attr_id, l_attribute_id, l_attribute_name, p_comments, p_owner, coalesce(p_dj_value, l_dj_attribute_value), l_dj_attribute_value, coalesce(p_df_value, l_df_attribute_value), l_df_attribute_value);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_rel_attribute(bigint, text, text, character varying, character varying) OWNER TO :user;

-- Function: cm_vac_ns(bigint)

-- DROP FUNCTION cm_vac_ns(bigint);

CREATE OR REPLACE FUNCTION cm_vac_ns(p_ns_id bigint, p_user character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_cm_ci cm_ci%ROWTYPE;
    l_cm_rel cm_ci_relations%ROWTYPE;    
    l_ns_like bigint[];
BEGIN

    select array(select ns_id from ns_namespaces
        where ns_path like (select ns_path || '%' from ns_namespaces where ns_id = p_ns_id)) into l_ns_like;

    for l_cm_ci in 
	    select * from cm_ci
	    where ns_id = ANY(l_ns_like)
	    and ci_state_id = 200
	    order by ci_id
    loop
	    perform cm_delete_ci(l_cm_ci.ci_id, true, p_user);	
    end loop;

    for l_cm_rel in 
	    select * from cm_ci_relations
	    where ns_id = ANY(l_ns_like)
	    and ci_state_id = 200
	    order by ci_relation_id
    loop
	    perform cm_delete_relation(l_cm_rel.ci_relation_id, true);	
    end loop;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_vac_ns(bigint, character varying) OWNER TO :user;

-- Function: dj_cancel_deployment(bigint, character varying, character varying, character varying)

-- DROP FUNCTION dj_cancel_deployment(bigint, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION dj_cancel_deployment(p_deployment_id bigint, p_updated_by character varying, p_desc character varying, p_comments character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_release_id bigint;
    l_old_state integer;
    l_desc text;
    l_comments text;
    l_ops text;
BEGIN

    select state_id into l_old_state
    from dj_deployment
    where deployment_id = p_deployment_id;	

    update dj_deployment 
	set state_id = 400,
	    updated_by = coalesce(p_updated_by, updated_by),
	    description = coalesce(p_desc, description),
	    comments = coalesce(p_comments, comments),
	    updated = now()
    where deployment_id = p_deployment_id
    returning release_id, description, comments, ops into l_release_id, l_desc, l_comments, l_ops;	

    insert into dj_deployment_state_hist (hist_id, deployment_id, old_state_id, new_state_id, description, comments, ops, updated_by)
    values (nextval('dj_pk_seq'), p_deployment_id, l_old_state, 400, l_desc, l_comments, l_ops, p_updated_by); 	

    update dj_deployment_rfc 
    set state_id = 400,
        updated = now()
    where deployment_id = p_deployment_id 
    and state_id = 10;	

    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), p_deployment_id, 'deployment' , 200);   

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_cancel_deployment(bigint, character varying, character varying, character varying)
  OWNER TO :user;


-- Function dj_create_release(bigint, bigint, bigint, character varying, character varying, bigint, character varying, character varying)

-- DROP FUNCTION dj_create_release(bigint, bigint, bigint, character varying, character varying, bigint, character varying, character varying);

CREATE OR REPLACE FUNCTION dj_create_release(p_release_id bigint, p_ns_id bigint, p_parent_release_id bigint, p_release_name character varying, p_created_by character varying, p_release_state_id bigint, p_description character varying, p_revision character varying)
  RETURNS void AS
$BODY$
DECLARE 
BEGIN

	INSERT INTO dj_releases(
            release_id, ns_id, parent_release_id, release_name, created_by, release_state_id, description, revision)
    	VALUES (p_release_id, p_ns_id, p_parent_release_id, p_release_name, p_created_by, p_release_state_id, p_description, p_revision);

    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), p_release_id, 'release' , 100);   

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_create_release(bigint, bigint, bigint, character varying, character varying, bigint, character varying, character varying) OWNER TO :user;



-- Function: dj_commit_release(bigint, boolean, integer, boolean, character varying, character varying)

-- DROP FUNCTION dj_commit_release(bigint, boolean, integer, boolean, character varying, character varying);

CREATE OR REPLACE FUNCTION dj_commit_release(p_release_id bigint, p_set_df_value boolean, p_new_ci_state_id integer, p_delete4real boolean, p_commited_by character varying, p_desc character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_release_state integer;
    l_current_release_state character varying;
    l_new_ci_state_id integer default coalesce(p_new_ci_state_id, 100);
    l_set_df_value boolean default coalesce(p_set_df_value, false);
BEGIN

    select into l_current_release_state rs.state_name
    from dj_releases r, dj_release_states rs
    where r.release_id = p_release_id 
    and r.release_state_id = rs.release_state_id;

    if l_current_release_state <> 'open' then
	RAISE EXCEPTION 'The release is in wrong state: %', l_current_release_state USING ERRCODE = '22000';
    end if;

    perform dj_commit_release_cis(p_release_id, p_set_df_value, p_new_ci_state_id, p_delete4real);
    perform dj_commit_release_relations(p_release_id, p_set_df_value, p_new_ci_state_id, p_delete4real);	

    select into l_release_state release_state_id
    from dj_release_states
    where state_name = 'closed';	

    if l_release_state is not null then
       update dj_releases
       set release_state_id = l_release_state,
           updated = now(),
       	   commited_by = p_commited_by,
       	   description = coalesce(p_desc, description)
       where release_id =  p_release_id;
    end if;
    
    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), p_release_id, 'release' , 200);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_commit_release(bigint, boolean, integer, boolean, character varying, character varying) OWNER TO :user;

-- Function: dj_commit_release_cis(bigint, boolean, integer, boolean)

-- DROP FUNCTION dj_commit_release_cis(bigint, boolean, integer, boolean);

CREATE OR REPLACE FUNCTION dj_commit_release_cis(p_release_id bigint, p_set_df_value boolean, p_new_ci_state_id integer, p_delete4real boolean)
  RETURNS void AS
$BODY$
DECLARE
    l_rfc_ci record;
    l_rfc_ci_attr dj_rfc_ci_attributes%ROWTYPE;
    l_ci_exists integer;
    l_action character varying;
    l_new_ci_state_id integer default coalesce(p_new_ci_state_id, 100);
    l_df_value text;
    l_ci_attr_id bigint;
    l_set_df_value boolean default coalesce(p_set_df_value, false);
BEGIN

    for l_rfc_ci in 
	    select rci.rfc_id, rci.ci_id, rci.ns_id, rci.class_id, rci.ci_name, rci.ci_goid, rci.comments, ra.action_name, rci.created_by, rci.updated_by
	    from dj_rfc_ci rci, dj_rfc_ci_actions ra 
	    where rci.release_id = p_release_id
	    and rci.is_active_in_release = true
	    and rci.action_id = ra.action_id
	    order by rci.execution_order
    loop
	select into l_ci_exists count(1) from cm_ci where ci_id = l_rfc_ci.ci_id;

	l_action := l_rfc_ci.action_name;
	
	if l_action = 'add' then
	   if l_ci_exists = 0 then

	      perform cm_create_ci(l_rfc_ci.ci_id, l_rfc_ci.ns_id, l_rfc_ci.class_id, l_rfc_ci.ci_goid, l_rfc_ci.ci_name, l_rfc_ci.comments, l_new_ci_state_id, l_rfc_ci.rfc_id, coalesce(l_rfc_ci.updated_by, l_rfc_ci.created_by));	

	      for l_rfc_ci_attr in 
			SELECT *
			FROM dj_rfc_ci_attributes cia
			where cia.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_ci_attributes a where a.rfc_id = l_rfc_ci.rfc_id group by a.attribute_id) 
          loop
			  if l_set_df_value then
			     l_df_value := l_rfc_ci_attr.new_attribute_value;	
			  end if;
			  perform cm_add_ci_attribute(l_rfc_ci.ci_id, l_rfc_ci_attr.attribute_id, l_df_value, l_rfc_ci_attr.new_attribute_value, l_rfc_ci_attr.owner, l_rfc_ci_attr.comments, false);
  	      end loop;	
	   else
	      l_action := 'update';	
	   end if;
	end if;
	
	if l_action = 'update' then
	   if l_ci_exists > 0 then
	   
	      perform cm_update_ci(l_rfc_ci.ci_id, l_rfc_ci.ci_name, l_rfc_ci.comments, null, l_rfc_ci.rfc_id, coalesce(l_rfc_ci.updated_by, l_rfc_ci.created_by));	

	      for l_rfc_ci_attr in 
		SELECT *
		FROM dj_rfc_ci_attributes cia
		where cia.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_ci_attributes a where a.rfc_id = l_rfc_ci.rfc_id group by a.attribute_id) 
              loop

		  select into l_ci_attr_id ci_attribute_id
		  from cm_ci_attributes
		  where ci_id = l_rfc_ci.ci_id
		  and attribute_id = l_rfc_ci_attr.attribute_id;
			
		  if l_set_df_value then
		     l_df_value := l_rfc_ci_attr.new_attribute_value;	
		  end if;

		  if l_ci_attr_id is null then
		     perform cm_add_ci_attribute(l_rfc_ci.ci_id, l_rfc_ci_attr.attribute_id, l_df_value, l_rfc_ci_attr.new_attribute_value, l_rfc_ci_attr.owner, l_rfc_ci_attr.comments, true);
		  else 
		     perform cm_update_ci_attribute(l_ci_attr_id, l_df_value, l_rfc_ci_attr.new_attribute_value, l_rfc_ci_attr.owner, l_rfc_ci_attr.comments);
		  end if;
	      end loop;	
	   else
	      RAISE 'CI does not exists with ci_id: %', l_rfc_ci.ci_id USING ERRCODE = '22000';	
	   end if;
	end if;

	if l_action = 'delete' then
	   perform cm_delete_ci(l_rfc_ci.ci_id, l_rfc_ci.rfc_id, p_delete4real, coalesce(l_rfc_ci.updated_by, l_rfc_ci.created_by));
	end if;

    end loop;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_commit_release_cis(bigint, boolean, integer, boolean) OWNER TO :user;

-- Function: dj_commit_release_relations(bigint, boolean, integer, boolean)

-- DROP FUNCTION dj_commit_release_relations(bigint, boolean, integer, boolean);

CREATE OR REPLACE FUNCTION dj_commit_release_relations(p_release_id bigint, p_set_df_value boolean, p_new_ci_state_id integer, p_delete4real boolean)
  RETURNS void AS
$BODY$
DECLARE
    l_rfc_rel record;
    l_rfc_rel_attr dj_rfc_relation_attributes%ROWTYPE;
    l_rel_exists integer;
    l_action character varying;
    l_new_ci_state_id integer default coalesce(p_new_ci_state_id, 100);
    l_df_value text;
    l_ci_rel_attr_id bigint;
    l_set_df_value boolean default coalesce(p_set_df_value, false);
BEGIN

    for l_rfc_rel in 
	    select rr.rfc_id, rr.ns_id, rr.ci_relation_id, rr.from_ci_id, rr.relation_id, rr.to_ci_id, rr.relation_goid, rr.comments, ra.action_name
	    from dj_rfc_relation rr, dj_rfc_ci_actions ra 
	    where rr.release_id = p_release_id
	    and rr.is_active_in_release = true
	    and rr.action_id = ra.action_id
	    order by rr.execution_order
    loop
	select into l_rel_exists count(1) from cm_ci_relations where ci_relation_id = l_rfc_rel.ci_relation_id;
	l_action := l_rfc_rel.action_name;
	
	if l_action = 'add' then
	   if l_rel_exists = 0 then
	      perform cm_create_relation(l_rfc_rel.ci_relation_id, l_rfc_rel.ns_id, l_rfc_rel.from_ci_id, l_rfc_rel.relation_id, l_rfc_rel.to_ci_id, l_rfc_rel.relation_goid, l_rfc_rel.comments, l_new_ci_state_id, l_rfc_rel.rfc_id);	

	      for l_rfc_rel_attr in 
		  SELECT *
		  FROM dj_rfc_relation_attributes ra
		  where ra.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_relation_attributes a where a.rfc_id = l_rfc_rel.rfc_id group by a.attribute_id) 
              loop
		  if l_set_df_value then
		     l_df_value := l_rfc_rel_attr.new_attribute_value;	
		  end if;
		  perform cm_add_ci_rel_attribute(l_rfc_rel.ci_relation_id, l_rfc_rel_attr.attribute_id, l_df_value, l_rfc_rel_attr.new_attribute_value, l_rfc_rel_attr.owner, l_rfc_rel_attr.comments, false);
	      end loop;	
	   else
	      l_action := 'update';	
	   end if;
	end if;
	
	if l_action = 'update' then
	   if l_rel_exists > 0 then
	   
	      perform cm_update_rel(l_rfc_rel.ci_relation_id, l_rfc_rel.comments, null, l_rfc_rel.rfc_id);
		
	      for l_rfc_rel_attr in 
		  SELECT *
		  FROM dj_rfc_relation_attributes ra
		  where ra.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_relation_attributes a where a.rfc_id = l_rfc_rel.rfc_id group by a.attribute_id) 
              loop

		  select into l_ci_rel_attr_id ci_rel_attribute_id
		  from cm_ci_relation_attributes
		  where ci_relation_id = l_rfc_rel.ci_relation_id
		  and attribute_id = l_rfc_rel_attr.attribute_id;
			
		  if l_set_df_value then
		     l_df_value := l_rfc_rel_attr.new_attribute_value;	
		  end if;

		  if l_ci_rel_attr_id is null then
		     perform cm_add_ci_rel_attribute(l_rfc_rel.ci_relation_id, l_rfc_rel_attr.attribute_id, l_df_value, l_rfc_rel_attr.new_attribute_value, l_rfc_rel_attr.owner, l_rfc_rel_attr.comments, true);
		  else 
		     perform cm_update_rel_attribute(l_ci_rel_attr_id, l_df_value, l_rfc_rel_attr.new_attribute_value, l_rfc_rel_attr.owner, l_rfc_rel_attr.comments);
		  end if;
	      end loop;	
	   else
	      RAISE 'Ci Relation does not exists with ci_relation_id: %', l_rfc_rel.ci_relation_id USING ERRCODE = '22000';	
	   end if;
	end if;

	if l_action = 'delete' then
	   perform cm_delete_relation(l_rfc_rel.ci_relation_id, p_delete4real);	
	end if;

    end loop;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_commit_release_relations(bigint, boolean, integer, boolean) OWNER TO :user;

-- Function: dj_update_release(bigint, bigint, character varying, integer, character varying, integer, character varying)

-- DROP FUNCTION dj_update_release(bigint, bigint, character varying, integer, character varying, integer, character varying);

CREATE OR REPLACE FUNCTION dj_update_release(p_release_id bigint, p_parent_release_id bigint, p_release_name character varying, p_release_state_id integer, p_commited_by character varying, p_revision integer, p_desc character varying)
  RETURNS void AS
$BODY$

BEGIN

    update dj_releases set
    parent_release_id = p_parent_release_id,
    release_name = p_release_name, 
    release_state_id = p_release_state_id,
    description = p_desc, 
    revision = p_revision,
    commited_by = p_commited_by, 
    updated = now()
    where release_id = p_release_id;

    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), p_release_id, 'release' , 200); 

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_update_release(bigint, bigint, character varying, integer, character varying, integer, character varying)
  OWNER TO :user;
  
-- Function: dj_delete_release(bigint)
-- DROP FUNCTION dj_delete_release(bigint);

CREATE OR REPLACE FUNCTION dj_delete_release(p_release_id bigint)
  RETURNS void AS
$BODY$

BEGIN

    delete from dj_releases 
    where release_id = p_release_id;

    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), p_release_id, 'release' , 300); 

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_delete_release(bigint) OWNER TO :user;

-- Function: dj_complete_deployment(bigint)

-- DROP FUNCTION dj_complete_deployment(bigint);

CREATE OR REPLACE FUNCTION dj_complete_deployment(p_deployment_id bigint)
  RETURNS void AS
$BODY$
declare
l_incomplete integer;
l_release_state integer;
l_release_id bigint;
l_manifest_release_id bigint;
l_manifest_ns_id bigint;
l_rel_rfc_id bigint;
l_dpmt_complete_id integer;
l_dpmt_created_by character varying;
l_dpmt_updated_by character varying;
l_desc text;
l_comments text;
l_ops text;
l_old_state integer;  
l_continue_on_failure bool;
BEGIN

	select into l_incomplete count(1) 
	from dj_deployment_rfc dpmt, dj_rfc_ci rfc
	where dpmt.deployment_id = p_deployment_id
	and dpmt.state_id <> 200
	and dpmt.rfc_id = rfc.rfc_id;	

	select into l_continue_on_failure (d.flags&1>0) from dj_deployment d where d.deployment_id = p_deployment_id;
	if l_incomplete > 0 AND not(l_continue_on_failure) then
	RAISE EXCEPTION 'Not all rfc are complete in deployment: %', p_deployment_id USING ERRCODE = '22000';
	end if;

	for l_rel_rfc_id in
		select dpmt.rfc_id 
		from dj_deployment_rfc dpmt, dj_rfc_relation rfc
		where dpmt.deployment_id = p_deployment_id
		and dpmt.state_id <> 200
		and dpmt.rfc_id = rfc.rfc_id	
	loop
		perform dj_promote_rfc_relations(l_rel_rfc_id, true, 100);

		select into l_dpmt_complete_id state_id
		from dj_deployment_rfc_states
		where state_name = 'complete';	

		update dj_deployment_rfc 
		set state_id = l_dpmt_complete_id, 
		    updated = now()
		where deployment_id = p_deployment_id
		  and rfc_id = l_rel_rfc_id;

	end loop;	   

        select state_id into l_old_state
        from dj_deployment
        where deployment_id = p_deployment_id;	

	update dj_deployment 
	set state_id = 200,
	    updated = now()
	where deployment_id = p_deployment_id
	returning release_id, created_by, updated_by, description, comments, ops into l_release_id, l_dpmt_created_by, l_dpmt_updated_by, l_desc, l_comments, l_ops;	

	insert into dj_deployment_state_hist (hist_id, deployment_id, old_state_id, new_state_id, description, comments, ops, updated_by)
	values (nextval('dj_pk_seq'), p_deployment_id, l_old_state, 200, l_desc, l_comments, l_ops, l_dpmt_updated_by); 	

	select into l_release_state release_state_id
	from dj_release_states
	where state_name = 'closed';	

	update dj_releases
	set release_state_id = l_release_state,
	    commited_by = coalesce(l_dpmt_updated_by, l_dpmt_created_by),
	    updated = now()
	where release_id =  l_release_id;

	INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
	VALUES (nextval('event_pk_seq'), p_deployment_id, 'deployment' , 200);

	INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), l_release_id, 'release' , 200);


END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_complete_deployment(bigint)
  OWNER TO :user;

-- Function: dj_deploy_release(bigint, character varying, character varying, character varying, character varying, character varying)

-- DROP FUNCTION dj_deploy_release(bigint, character varying, character varying, character varying, character varying, character varying);


CREATE OR REPLACE FUNCTION dj_deploy_release(IN p_release_id bigint, IN p_state character varying, IN p_created_by character varying, IN p_description character varying, IN p_comments character varying, IN p_ops character varying, IN p_flags bigint, IN p_auto_pause_exec_orders character varying, OUT out_deployment_id bigint)
  RETURNS bigint AS
$BODY$
DECLARE
    l_rfc_ci record;
    l_ns_id bigint;
    l_revision smallint;	
    l_deployment_id bigint;
    l_dpmt_state_id integer;
BEGIN

    select into l_ns_id, l_revision ns_id, revision from dj_releases where release_id = p_release_id;

    select into l_dpmt_state_id state_id from dj_deployment_states where state_name = p_state;

    if not found then
	RAISE EXCEPTION 'Given deployment state % is wrong.', p_state USING ERRCODE = '22000';
    end if;

    insert into dj_deployment (deployment_id, ns_id, release_id, release_revision, state_id, created_by, description, comments, ops, auto_pause_exec_orders, flags )
    values (nextval('dj_pk_seq'), l_ns_id, p_release_id, l_revision, l_dpmt_state_id, p_created_by, p_description, p_comments, p_ops, p_auto_pause_exec_orders, COALESCE (p_flags,0))
    returning deployment_id into l_deployment_id;	

    insert into dj_deployment_state_hist (hist_id, deployment_id, old_state_id, new_state_id, description, comments, ops, updated_by)
    values (nextval('dj_pk_seq'), l_deployment_id, null, l_dpmt_state_id, p_description, p_comments, p_ops, p_created_by); 	

    for l_rfc_ci in 
	    select rci.rfc_id
	    from dj_rfc_ci rci
	    where rci.release_id = p_release_id
	    and rci.is_active_in_release = true
	    and not exists (select 1 from dj_deployment_rfc drfc where drfc.rfc_id = rci.rfc_id and drfc.state_id = 200)
	    order by rci.execution_order
    loop

        insert into dj_deployment_rfc (deployment_rfc_id, deployment_id, state_id, rfc_id)
        values (nextval('dj_pk_seq'), l_deployment_id, 10, l_rfc_ci.rfc_id);

    end loop;

    for l_rfc_ci in 
	    select rci.rfc_id
	    from dj_rfc_relation rci
	    where rci.release_id = p_release_id
	    and rci.is_active_in_release = true
	    and not exists (select 1 from dj_deployment_rfc drfc where drfc.rfc_id = rci.rfc_id and drfc.state_id = 200)
	    order by rci.execution_order
    loop

        insert into dj_deployment_rfc (deployment_rfc_id, deployment_id, state_id, rfc_id)
        values (nextval('dj_pk_seq'), l_deployment_id, 10, l_rfc_ci.rfc_id);

    end loop;

    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), l_deployment_id, 'deployment' , 100);

    out_deployment_id := l_deployment_id;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_deploy_release(bigint, character varying, character varying, character varying, character varying, character varying, bigint, character varying)
  OWNER TO :user;

CREATE OR REPLACE FUNCTION dj_deploy_release(IN p_release_id bigint, IN p_state character varying, IN p_created_by character varying, IN p_description character varying, IN p_comments character varying, IN p_ops character varying, OUT out_deployment_id bigint)
  RETURNS bigint AS
$BODY$
BEGIN
    out_deployment_id = dj_deploy_release(p_release_id, p_state, p_created_by, p_description, p_comments, p_ops, null);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_deploy_release(bigint, character varying, character varying, character varying, character varying, character varying)
  OWNER TO :user;

CREATE OR REPLACE FUNCTION dj_deploy_release(IN p_release_id bigint, IN p_state character varying, IN p_created_by character varying, IN p_description character varying, IN p_comments character varying, IN p_ops character varying, IN p_auto_pause_exec_orders character varying, OUT out_deployment_id bigint)
  RETURNS bigint AS
$BODY$
DECLARE
    l_rfc_ci record;
    l_ns_id bigint;
    l_revision smallint;	
    l_deployment_id bigint;
    l_dpmt_state_id integer;
BEGIN
     out_deployment_id = dj_deploy_release(p_release_id, p_state, p_created_by, p_description, p_comments, p_ops, 0, null);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
ALTER FUNCTION dj_deploy_release(bigint, character varying, character varying, character varying, character varying, character varying, character varying)
  OWNER TO :user;



-- Function: dj_promote_rfc_ci(bigint, boolean, integer, bigint)

-- DROP FUNCTION dj_promote_rfc_ci(bigint, boolean, integer, bigint);

CREATE OR REPLACE FUNCTION dj_promote_rfc_ci(p_rfc_id bigint, p_set_df_value boolean, p_new_ci_state_id integer, p_dpmt_id bigint)
  RETURNS void AS
$BODY$
DECLARE
    l_rfc_ci record;
    l_dpmt record;
    l_rfc_ci_attr dj_rfc_ci_attributes%ROWTYPE;
    l_ci_exists integer;
    l_action character varying;
    l_new_ci_state_id integer default coalesce(p_new_ci_state_id, 100);
    l_df_value text;
    l_ci_attr_id bigint;
    l_set_df_value boolean default coalesce(p_set_df_value, false);
    l_rel_rfc_id bigint;
    l_dpmt_complete_id integer;
BEGIN

	select into l_rfc_ci rci.rfc_id, rci.ci_id, rci.ns_id, rci.class_id, rci.ci_name, rci.ci_goid, rci.comments, ra.action_name, rci.created_by, rci.updated_by
	    from dj_rfc_ci rci, dj_rfc_ci_actions ra 
	    where rci.rfc_id = p_rfc_id
	    and rci.is_active_in_release = true
	    and rci.action_id = ra.action_id;

	IF NOT FOUND THEN
	    RAISE EXCEPTION 'rfc % not found', p_rfc_id;
	END IF;

	select into l_ci_exists count(1) from cm_ci where ci_id = l_rfc_ci.ci_id;

	l_action := l_rfc_ci.action_name;

	select into l_dpmt d.created_by, d.updated_by
	 from dj_deployment d
	 where d.deployment_id = p_dpmt_id;
	
	if l_action = 'add' then
	   if l_ci_exists = 0 then
	      perform cm_create_ci(l_rfc_ci.ci_id, l_rfc_ci.ns_id, l_rfc_ci.class_id, l_rfc_ci.ci_goid, l_rfc_ci.ci_name, l_rfc_ci.comments, l_new_ci_state_id, l_rfc_ci.rfc_id, l_dpmt.created_by);	

	      for l_rfc_ci_attr in 
			SELECT *
			FROM dj_rfc_ci_attributes cia
			where cia.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_ci_attributes a where a.rfc_id = l_rfc_ci.rfc_id group by a.attribute_id) 
		  loop
			  if l_set_df_value then
			     l_df_value := l_rfc_ci_attr.new_attribute_value;	
			  end if;
			  perform cm_add_ci_attribute(l_rfc_ci.ci_id, l_rfc_ci_attr.attribute_id, l_df_value, l_rfc_ci_attr.new_attribute_value, l_rfc_ci_attr.owner, l_rfc_ci_attr.comments,false);
	      end loop;	
	   else
	      l_action := 'update';	
	   end if;
	end if;

	if l_action = 'update' or l_action = 'replace' then
	   if l_ci_exists > 0 then

	      if l_action = 'replace' then
		perform cm_update_ci(l_rfc_ci.ci_id, l_rfc_ci.ci_name, l_rfc_ci.comments, 100, l_dpmt.created_by);	
		update cm_ci
		set created = now(), created_by = coalesce(l_dpmt.created_by, created_by)
		where ci_id = l_rfc_ci.ci_id;
	      else		
		perform cm_update_ci(l_rfc_ci.ci_id, l_rfc_ci.ci_name, l_rfc_ci.comments, null, l_dpmt.created_by);	
	      end if;
	      
	      for l_rfc_ci_attr in 
			SELECT *
			FROM dj_rfc_ci_attributes cia
			where cia.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_ci_attributes a where a.rfc_id = l_rfc_ci.rfc_id group by a.attribute_id) 
	      loop
			  select into l_ci_attr_id ci_attribute_id
			  from cm_ci_attributes
			  where ci_id = l_rfc_ci.ci_id
			  and attribute_id = l_rfc_ci_attr.attribute_id;
				
			  if l_set_df_value then
			     l_df_value := l_rfc_ci_attr.new_attribute_value;	
			  end if;
	
			  if l_ci_attr_id is null then
			     perform cm_add_ci_attribute(l_rfc_ci.ci_id, l_rfc_ci_attr.attribute_id, l_df_value, l_rfc_ci_attr.new_attribute_value, l_rfc_ci_attr.owner, l_rfc_ci_attr.comments, true);
			  else 
			     perform cm_update_ci_attribute(l_ci_attr_id, l_df_value, l_rfc_ci_attr.new_attribute_value, l_rfc_ci_attr.owner, l_rfc_ci_attr.comments);
			  end if;
	      end loop;	
	   else
	      RAISE 'CI does not exists with ci_id: %', l_rfc_ci.ci_id USING ERRCODE = '22000';	
	   end if;
	end if;

	if l_action = 'delete' then
	   perform cm_delete_ci(l_rfc_ci.ci_id, true, l_dpmt.created_by);	

	   if p_dpmt_id is not null then
		select into l_dpmt_complete_id state_id
		from dj_deployment_rfc_states
		where state_name = 'complete';	

		update dj_deployment_rfc 
		set state_id = l_dpmt_complete_id, 
		    updated = now()
		where deployment_id = p_dpmt_id
		  and rfc_id in (select rel.rfc_id 
				from dj_rfc_relation rel
				where rel.from_ci_id = l_rfc_ci.ci_id
				union all
				select rel.rfc_id 
				from dj_rfc_relation rel
				where rel.to_ci_id = l_rfc_ci.ci_id);
	   end if;

	end if;

	for l_rel_rfc_id in
		select rel.rfc_id 
		from dj_rfc_relation rel, cm_ci ci
		where rel.from_rfc_id = p_rfc_id
		   and ci.ci_id = rel.to_ci_id
		union all
		select rel.rfc_id 
		from dj_rfc_relation rel, cm_ci ci
		where rel.to_rfc_id = p_rfc_id
		   and ci.ci_id = rel.from_ci_id
	loop
		perform dj_promote_rfc_relations(l_rel_rfc_id, p_set_df_value, p_new_ci_state_id);
		-- if dpmt_id is not null lets complete the dpmt_records for the relation
		if p_dpmt_id is not null then
			select into l_dpmt_complete_id state_id
			from dj_deployment_rfc_states
			where state_name = 'complete';	

			update dj_deployment_rfc 
			set state_id = l_dpmt_complete_id, 
			    updated = now()
			where deployment_id = p_dpmt_id
			  and rfc_id = l_rel_rfc_id;
		end if;

	end loop;	   

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_promote_rfc_ci(bigint, boolean, integer, bigint) OWNER TO :user;

-- Function: dj_promote_rfc_relations(bigint, boolean, integer)

-- DROP FUNCTION dj_promote_rfc_relations(bigint, boolean, integer);

CREATE OR REPLACE FUNCTION dj_promote_rfc_relations(p_rfc_id bigint, p_set_df_value boolean, p_new_ci_state_id integer)
  RETURNS void AS
$BODY$
DECLARE
    l_rfc_rel record;
    l_rfc_rel_attr dj_rfc_relation_attributes%ROWTYPE;
    l_rel_exists integer;
    l_action character varying;
    l_new_ci_state_id integer default coalesce(p_new_ci_state_id, 100);
    l_df_value text;
    l_ci_rel_attr_id bigint;
    l_set_df_value boolean default true;
BEGIN

	select into l_rfc_rel rr.rfc_id, rr.ns_id, rr.ci_relation_id, rr.from_ci_id, rr.relation_id, rr.to_ci_id, rr.relation_goid, rr.comments, ra.action_name
	    from dj_rfc_relation rr, dj_rfc_ci_actions ra 
	    where rr.rfc_id = p_rfc_id
	    and rr.is_active_in_release = true
	    and rr.action_id = ra.action_id;

	IF NOT FOUND THEN
	    RAISE EXCEPTION 'rfc % not found', p_rfc_id;
	END IF;

	select into l_rel_exists count(1) from cm_ci_relations where ci_relation_id = l_rfc_rel.ci_relation_id;
	l_action := l_rfc_rel.action_name;
	
	if l_action = 'add' then
	   if l_rel_exists = 0 then
	      perform cm_create_relation(l_rfc_rel.ci_relation_id, l_rfc_rel.ns_id, l_rfc_rel.from_ci_id, l_rfc_rel.relation_id, l_rfc_rel.to_ci_id, l_rfc_rel.relation_goid, l_rfc_rel.comments, l_new_ci_state_id);	

	      for l_rfc_rel_attr in 
		  SELECT *
		  FROM dj_rfc_relation_attributes ra
		  where ra.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_relation_attributes a where a.rfc_id = l_rfc_rel.rfc_id group by a.attribute_id) 
              loop
		  if l_set_df_value then
		     l_df_value := l_rfc_rel_attr.new_attribute_value;	
		  end if;
		  perform cm_add_ci_rel_attribute(l_rfc_rel.ci_relation_id, l_rfc_rel_attr.attribute_id, l_df_value, l_rfc_rel_attr.new_attribute_value, l_rfc_rel_attr.owner, l_rfc_rel_attr.comments, false);
	      end loop;	
	   else
	      l_action := 'update';	
	   end if;
	end if;
	
	if l_action = 'update' then
	   if l_rel_exists > 0 then
	   
	      for l_rfc_rel_attr in 
		  SELECT *
		  FROM dj_rfc_relation_attributes ra
		  where ra.rfc_attr_id in (select max(a.rfc_attr_id) from dj_rfc_relation_attributes a where a.rfc_id = l_rfc_rel.rfc_id group by a.attribute_id) 
              loop

		  select into l_ci_rel_attr_id ci_rel_attribute_id
		  from cm_ci_relation_attributes
		  where ci_relation_id = l_rfc_rel.ci_relation_id
		  and attribute_id = l_rfc_rel_attr.attribute_id;
			
		  if l_set_df_value then
		     l_df_value := l_rfc_rel_attr.new_attribute_value;	
		  end if;

		  if l_ci_rel_attr_id is null then
		     perform cm_add_ci_rel_attribute(l_rfc_rel.ci_relation_id, l_rfc_rel_attr.attribute_id, l_df_value, l_rfc_rel_attr.new_attribute_value, l_rfc_rel_attr.owner, l_rfc_rel_attr.comments, true);
		  else 
		     perform cm_update_rel_attribute(l_ci_rel_attr_id, l_df_value, l_rfc_rel_attr.new_attribute_value, l_rfc_rel_attr.owner, l_rfc_rel_attr.comments);
		  end if;
	      end loop;	
	   else
	      RAISE 'Ci Relation does not exists with ci_relation_id: %', l_rfc_rel.ci_relation_id USING ERRCODE = '22000';	
	   end if;
	end if;

	if l_action = 'delete' then
	   perform cm_delete_relation(l_rfc_rel.ci_relation_id, true);	
	end if;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_promote_rfc_relations(bigint, boolean, integer) OWNER TO :user;


-- Function: dj_retry_deployment(bigint, character varying, character varying, character varying)

-- DROP FUNCTION dj_retry_deployment(bigint, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION dj_retry_deployment(p_deployment_id bigint, p_updated_by character varying, p_desc character varying, p_comments character varying)
  RETURNS void AS
$BODY$
DECLARE
l_old_state integer;
l_desc text;
l_comments text;
l_ops text;
l_updated_by character varying;
BEGIN

    select state_id into l_old_state
    from dj_deployment
    where deployment_id = p_deployment_id;	

    update dj_deployment 
	set state_id = 100,
	    updated_by = coalesce(p_updated_by, updated_by),
	    description = coalesce(p_desc, description),
	    comments = coalesce(p_comments, comments),
	    updated = now()
    where deployment_id = p_deployment_id
    returning description, comments, ops, updated_by into l_desc, l_comments, l_ops, l_updated_by;

    insert into dj_deployment_state_hist (hist_id, deployment_id, old_state_id, new_state_id, description, comments, ops, updated_by)
    values (nextval('dj_pk_seq'), p_deployment_id, l_old_state, 100, l_desc, l_comments, l_ops, l_updated_by); 	

    update dj_deployment_rfc 
    set state_id = 10,
        updated = now()
    where deployment_id = p_deployment_id 
    and state_id = 300;	

    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
    VALUES (nextval('event_pk_seq'), p_deployment_id, 'deployment' , 200);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_retry_deployment(bigint, character varying, character varying, character varying)
  OWNER TO :user;

-- Function: dj_rm_rfc_ci(bigint)

-- DROP FUNCTION dj_rm_rfc_ci(bigint);

CREATE OR REPLACE FUNCTION dj_rm_rfc_ci(p_rfc_id bigint)
  RETURNS void AS
$BODY$
DECLARE
	l_ci_id bigint;
	l_ci_exists integer;
	l_rel_rfc_id bigint;
BEGIN
	update dj_rfc_ci
	set is_active_in_release = false,
	    updated = now()
	where rfc_id = p_rfc_id
	returning ci_id into l_ci_id;

	select count(1) into l_ci_exists 
	from cm_ci where ci_id = l_ci_id;

	if l_ci_exists = 0 then
        -- we need to clean up all the rels rfcs for this guy since no ci exists
	      for l_rel_rfc_id in 
			select rfc_id from dj_rfc_relation where from_rfc_id = p_rfc_id
			union all
			select rfc_id from dj_rfc_relation where to_rfc_id = p_rfc_id	      
	      loop
		  perform dj_rm_rfc_rel(l_rel_rfc_id);
	      end loop;	 
	end if;
	
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_rm_rfc_ci(bigint) OWNER TO :user;

-- Function: dj_rm_rfc_rel(bigint)

-- DROP FUNCTION dj_rm_rfc_rel(bigint);

CREATE OR REPLACE FUNCTION dj_rm_rfc_rel(p_rfc_id bigint)
  RETURNS void AS
$BODY$
BEGIN
	update dj_rfc_relation
	set is_active_in_release = false,
	    updated = now()
	where rfc_id = p_rfc_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_rm_rfc_rel(bigint) OWNER TO :user;


-- Function: dj_upd_deployment(bigint, character varying, character varying, character varying, character varying, character varying)

-- DROP FUNCTION dj_upd_deployment(bigint, character varying, character varying, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION dj_upd_deployment(p_deployment_id bigint, p_state character varying, p_updated_by character varying, p_desc character varying, p_comments character varying, p_process_id character varying, p_auto_pause_exec_orders character varying, p_flags bigint)
  RETURNS void AS
$BODY$
DECLARE
 l_state_id integer;
 l_old_state integer;
 l_desc text;
 l_comments text;
 l_ops text; 
BEGIN

    if 	p_state is not null then
	    select into l_state_id state_id
	    from dj_deployment_states
	    where state_name = p_state;	

	    if l_state_id is null then
		RAISE EXCEPTION 'Can not resolve state: %', p_state USING ERRCODE = '22000';
	    end if;

	    select into l_old_state state_id
	    from dj_deployment
	    where deployment_id = p_deployment_id;	
    end if;
	
    update dj_deployment 
	set state_id = coalesce(l_state_id, state_id),
	    updated_by = coalesce(p_updated_by, updated_by),
	    description = coalesce(p_desc, description),
	    comments = coalesce(p_comments, comments),
	    flags = coalesce(p_flags, flags),
	    process_id = coalesce(p_process_id, process_id),
            auto_pause_exec_orders = coalesce(p_auto_pause_exec_orders, auto_pause_exec_orders),
	    updated = now()
    where deployment_id = p_deployment_id
    returning description, comments, ops into  l_desc, l_comments, l_ops;	

    if l_state_id is not null then
	insert into dj_deployment_state_hist (hist_id, deployment_id, old_state_id, new_state_id, description, comments, ops, updated_by)
	values (nextval('dj_pk_seq'), p_deployment_id, l_old_state, l_state_id, l_desc, l_comments, l_ops, p_updated_by); 	
    end if;

    if p_state is not null and l_old_state != l_state_id then	
	    INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
	    VALUES (nextval('event_pk_seq'), p_deployment_id, 'deployment' , 200);
    end if;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_upd_deployment(bigint, character varying, character varying, character varying, character varying, character varying, character varying, bigint)
  OWNER TO :user;



CREATE OR REPLACE FUNCTION dj_upd_deployment(p_deployment_id bigint, p_state character varying, p_updated_by character varying, p_desc character varying, p_comments character varying, p_process_id character varying)
  RETURNS void AS
$BODY$
BEGIN
        perform dj_upd_deployment(p_deployment_id, p_state, p_updated_by, p_desc, p_comments, p_process_id, null);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION dj_upd_deployment(bigint, character varying, character varying, character varying, character varying, character varying)
  OWNER TO :user;

CREATE OR REPLACE FUNCTION dj_upd_deployment(p_deployment_id bigint, p_state character varying, p_updated_by character varying, p_desc character varying, p_comments character varying, p_process_id character varying, p_auto_pause_exec_orders character varying)
  RETURNS void AS
$BODY$
BEGIN
        perform dj_upd_deployment(p_deployment_id, p_state, p_updated_by, p_desc, p_comments, p_process_id, null, null);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_upd_deployment(bigint, character varying, character varying, character varying, character varying, character varying, character varying)
  OWNER TO :user;


-- Function: dj_upd_dpmt_record_state(bigint, character varying, character varying)

-- DROP FUNCTION dj_upd_dpmt_record_state(bigint, character varying, character varying);

CREATE OR REPLACE FUNCTION dj_upd_dpmt_record_state(p_dpmt_rfc_id bigint, p_state character varying, p_comments character varying)
  RETURNS void AS
$BODY$
DECLARE
 l_state_id integer;
 l_rfc_id bigint;
 l_dpmt_id bigint;
BEGIN

    select into l_state_id state_id
    from dj_deployment_rfc_states
    where state_name = p_state;	

    if l_state_id is null then
	RAISE EXCEPTION 'Can not resolve state: %', p_state USING ERRCODE = '22000';
    end if;

    update dj_deployment_rfc 
	set state_id = l_state_id, 
	    comments = coalesce(p_comments,comments),
	    updated = now()
    where deployment_rfc_id = p_dpmt_rfc_id
    returning rfc_id, deployment_id into l_rfc_id, l_dpmt_id;

    if p_state = 'complete' then
       perform dj_promote_rfc_ci(l_rfc_id, false, null, l_dpmt_id);	
    end if;


END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_upd_dpmt_record_state(bigint, character varying, character varying) OWNER TO :user;


-- Function: ns_create_namespace(character varying)

-- DROP FUNCTION ns_create_namespace(character varying);

CREATE OR REPLACE FUNCTION ns_create_namespace(IN p_ns_path character varying, OUT out_ns_id bigint)
  RETURNS bigint AS
$BODY$
BEGIN

    insert into ns_namespaces (ns_id, ns_path)
    values (nextval('cm_pk_seq'), p_ns_path)
    returning ns_id into out_ns_id;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION ns_create_namespace(character varying) OWNER TO :user;

-- Function: ns_delete_namespace(character varying)

-- DROP FUNCTION ns_delete_namespace(character varying);

CREATE OR REPLACE FUNCTION ns_delete_namespace(p_ns_path character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_ns_id bigint;
BEGIN

	for l_ns_id in
	select ns_id from ns_namespaces
	where (ns_path like p_ns_path || '/%' or ns_path = p_ns_path)
	loop
		insert into cms_ci_event_queue(event_id, source_pk, source_name, event_type_id)
		values (nextval('event_pk_seq'), l_ns_id, 'namespace' , 300);
	end loop;

	delete from ns_namespaces 
	where ns_path like p_ns_path || '/%';

	delete from ns_namespaces 
	where ns_path = p_ns_path;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION ns_delete_namespace(character varying)
  OWNER TO :user;


CREATE OR REPLACE FUNCTION force_complete_dpmt(IN p_dpmt_id bigint)
  RETURNS void AS
$BODY$
DECLARE
    dpmt_rfc_id bigint;
BEGIN

	for dpmt_rfc_id in
	select dpr.deployment_rfc_id
	from dj_deployment_rfc dpr, dj_rfc_ci rfc
	where dpr.deployment_id = p_dpmt_id
	and dpr.rfc_id = rfc.rfc_id
	loop

	   perform dj_upd_dpmt_record_state(dpmt_rfc_id, 'complete', 'kire screwed up');

	end loop;

	perform dj_complete_deployment(p_dpmt_id);


END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION force_complete_dpmt(bigint) OWNER TO :user;

-- Function: md_create_class(integer, character varying, character varying, integer, character varying, boolean, integer, character varying, character varying, character varying))

-- DROP FUNCTION md_create_class, character varying)(integer, character varying, character varying, integer, character varying, boolean, integer, character varying, character varying);

CREATE OR REPLACE FUNCTION md_create_class(p_class_id integer, p_class_name character varying, p_short_class_name character varying, p_super_class_id integer, p_impl character varying, p_is_namespace boolean, p_flags integer, p_access_level character varying, p_descr character varying, p_format character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_new_class_id integer;
BEGIN
    insert into md_classes (class_id, class_name, short_class_name, super_class_id, impl, is_namespace, flags, access_level, description, format)
    values (p_class_id, p_class_name, p_short_class_name, nullif(p_super_class_id,0), p_impl, p_is_namespace, p_flags, p_access_level, p_descr, p_format);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_create_class(integer, character varying, character varying, integer, character varying, boolean, integer, character varying, character varying, character varying) OWNER TO :user;

/**
 * Add md_class_attribute. This function is provided for backward compatibility with 'p_is_immutable' is set to False.
 */

-- Function: md_add_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, character varying, character varying, character varying)

-- DROP FUNCTION md_add_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION md_add_class_attribute(IN p_class_id integer, IN p_attribute_name character varying, IN p_data_type character varying, IN p_is_mandatory boolean, IN p_is_inheritable boolean, IN p_is_encrypted boolean, IN p_force_on_dependent boolean, IN p_default_value character varying, IN p_value_format character varying, IN p_descr character varying, OUT out_attribute_id integer)
  RETURNS integer AS
$BODY$
DECLARE
    l_new_attr_id integer;
BEGIN
    out_attribute_id = md_add_class_attribute(p_class_id, p_attribute_name, p_data_type, p_is_mandatory, p_is_inheritable, p_is_encrypted, false, p_force_on_dependent, p_default_value, p_value_format, p_descr);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_add_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, character varying, character varying, character varying)
  OWNER TO :user;

/**
 * Add md_class_attributes.
 */

-- Function: md_add_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, boolean, character varying, character varying, character varying)

-- DROP FUNCTION md_add_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, boolean, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION md_add_class_attribute(IN p_class_id integer, IN p_attribute_name character varying, IN p_data_type character varying, IN p_is_mandatory boolean, IN p_is_inheritable boolean, IN p_is_encrypted boolean, IN p_is_immutable boolean, IN p_force_on_dependent boolean, IN p_default_value character varying, IN p_value_format character varying, IN p_descr character varying, OUT out_attribute_id integer)
  RETURNS integer AS
$BODY$
DECLARE
    l_new_attr_id integer;
BEGIN

    insert into md_class_attributes (attribute_id, class_id, attribute_name, data_type, is_mandatory, is_inheritable, is_encrypted, is_immutable, force_on_dependent, default_value, value_format, description)
    values (nextval('md_pk_seq'), p_class_id, p_attribute_name, p_data_type, p_is_mandatory, p_is_inheritable, p_is_encrypted, p_is_immutable, p_force_on_dependent, p_default_value, p_value_format, p_descr)
    returning attribute_id into out_attribute_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_add_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, boolean, character varying, character varying, character varying)
  OWNER TO :user;
  
  
-- Function: md_delete_class(integer, boolean)

-- DROP FUNCTION md_delete_class(integer, boolean);

CREATE OR REPLACE FUNCTION md_delete_class(p_class_id integer, p_delete_all boolean)
  RETURNS void AS
$BODY$
DECLARE
BEGIN
	if p_delete_all = true then
	    delete from dj_rfc_ci where class_id = p_class_id; 
	    delete from cm_ci where class_id = p_class_id; 
	end if;   
	delete from md_class_attributes where class_id = p_class_id; 
	delete from md_class_actions where class_id = p_class_id; 
	delete from md_class_relations where from_class_id = p_class_id or to_class_id = p_class_id; 
	delete from md_classes where class_id = p_class_id; 
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_delete_class(integer, boolean) OWNER TO :user;

/**
 * Delete md_class_attribute
 */

-- Function: md_delete_class_attribute(integer, boolean)

-- DROP FUNCTION md_delete_class_attribute(integer, boolean);

CREATE OR REPLACE FUNCTION md_delete_class_attribute(p_attribute_id integer, p_delete_all boolean)
  RETURNS void AS
$BODY$
DECLARE
BEGIN

	if p_delete_all = true then
	    delete from dj_rfc_ci_attributes where attribute_id = p_attribute_id; 
	    delete from cm_ci_attributes where attribute_id = p_attribute_id; 
	end if;   
	delete from md_class_attributes where attribute_id = p_attribute_id; 
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_delete_class_attribute(integer, boolean) OWNER TO :user;

-- Function: md_update_class(integer, character varying, integer, character varying, boolean, integer, character varying, character varying, character varying))

-- DROP FUNCTION md_update_class(integer, character varying, integer, character varying, boolean, integer, character varying, character varying, character varying));

CREATE OR REPLACE FUNCTION md_update_class(p_class_id integer, p_short_class_name character varying, p_super_class_id integer, p_impl character varying, p_is_namespace boolean, p_flags integer, p_access_level character varying, p_descr character varying, p_format character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_new_class_id integer;
BEGIN
                   
    update md_classes set
        short_class_name = coalesce(p_short_class_name, short_class_name),
        super_class_id = coalesce(nullif(p_super_class_id,0), super_class_id),
        impl = coalesce(p_impl, impl),
        is_namespace = coalesce(p_is_namespace, is_namespace),
        flags = coalesce(p_flags, flags),
        access_level = coalesce(p_access_level, access_level),
        description = coalesce(p_descr, description),
        format = coalesce(p_format, format)
    where class_id = p_class_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_update_class(integer, character varying, integer, character varying, boolean, integer, character varying, character varying, character varying) OWNER TO :user;


/**
 * Update md_class_attributes.
 */

-- Function: md_update_class_attribute(integer, integer, character varying, character varying, boolean, boolean, boolean, boolean, boolean, character varying, character varying, character varying)

-- DROP FUNCTION md_update_class_attribute(integer, integer, character varying, character varying, boolean, boolean, boolean, boolean, boolean, character varying, character varying, character varying)

CREATE OR REPLACE FUNCTION md_update_class_attribute(IN p_attribute_id  integer, IN p_attribute_name character varying, IN p_data_type character varying, IN p_is_mandatory boolean, IN p_is_inheritable boolean, IN p_is_encrypted boolean, IN p_is_immutable boolean, IN p_force_on_dependent boolean, IN p_default_value character varying, IN p_value_format character varying, IN p_descr character varying)
  RETURNS void AS
$BODY$
DECLARE
BEGIN
    update md_class_attributes set
        attribute_name = coalesce(p_attribute_name, attribute_name),
        data_type = coalesce(p_data_type, data_type),
        is_mandatory = coalesce(p_is_mandatory, is_mandatory),
        is_inheritable = coalesce(p_is_inheritable, is_inheritable),
        is_encrypted = coalesce(p_is_encrypted, is_encrypted),
        is_immutable = coalesce(p_is_immutable,is_immutable),
        force_on_dependent = coalesce(p_force_on_dependent, force_on_dependent),
        default_value = p_default_value,
        value_format = coalesce(p_value_format, value_format),
        description = coalesce(p_descr, description)
    where attribute_id = p_attribute_id;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_update_class_attribute(integer, character varying, character varying, boolean, boolean, boolean, boolean, boolean, character varying, character varying, character varying) OWNER TO :user;

-- Function: md_create_relation(integer, character varying, character varying, character varying)

-- DROP FUNCTION md_create_relation(integer, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION md_create_relation(p_rel_id integer, p_rel_name character varying, p_short_rel_name character varying, p_descr character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_new_rel_id integer;
BEGIN

    insert into md_relations (relation_id, relation_name, short_relation_name, description)
    values (p_rel_id, p_rel_name, p_short_rel_name, p_descr);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_create_relation(integer, character varying, character varying, character varying) OWNER TO :user;

-- Function: md_add_relation_attribute(integer, integer, character varying, character varying, boolean, character varying, character varying, character varying)

-- DROP FUNCTION md_add_relation_attribute(integer, integer, character varying, character varying, boolean, character varying, character varying, character varying)

CREATE OR REPLACE FUNCTION md_add_relation_attribute(IN p_rel_id  integer, IN p_attribute_name character varying, IN p_data_type character varying, IN p_is_mandatory boolean, IN p_default_value character varying, IN p_value_format character varying, IN p_descr character varying, OUT out_attribute_id integer)
  RETURNS integer AS
$BODY$
DECLARE
    l_new_attr_id integer;
BEGIN

    insert into md_relation_attributes (attribute_id, relation_id, attribute_name, data_type, is_mandatory, default_value, value_format, description)
    values (nextval('md_pk_seq'), p_rel_id, p_attribute_name, p_data_type, p_is_mandatory, p_default_value, p_value_format, p_descr)
    returning attribute_id into out_attribute_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_add_relation_attribute(integer, character varying, character varying, boolean, character varying, character varying, character varying) OWNER TO :user;

-- Function: md_delete_relation(integer, boolean)

-- DROP FUNCTION md_delete_relation(integer, boolean);

CREATE OR REPLACE FUNCTION md_delete_relation(p_rel_id integer, p_delete_all boolean)
  RETURNS void AS
$BODY$
DECLARE
BEGIN

	if p_delete_all = true then
	    delete from dj_rfc_relation where relation_id = p_rel_id;
	    delete from cm_ci_relations where relation_id = p_rel_id; 
	end if;   
	delete from md_class_relations where relation_id = p_rel_id; 
	delete from md_relation_attributes where relation_id = p_rel_id; 
	delete from md_relations where relation_id = p_rel_id; 
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_delete_relation(integer, boolean) OWNER TO :user;

-- Function: md_delete_relation_attribute(integer, boolean)

-- DROP FUNCTION md_delete_relation_attribute(integer, boolean);

CREATE OR REPLACE FUNCTION md_delete_relation_attribute(p_attr_id integer, p_delete_all boolean)
  RETURNS void AS
$BODY$
DECLARE
BEGIN

	if p_delete_all = true then
	    delete from dj_rfc_relation_attributes where attribute_id = p_attr_id;
	    delete from cm_ci_relation_attributes where attribute_id = p_attr_id; 
	end if;   
	delete from md_relation_attributes where attribute_id = p_attr_id; 
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_delete_relation_attribute(integer, boolean) OWNER TO :user;

-- Function: md_add_relation_target(integer, integer, integer, boolean, character varying, character varying)

-- DROP FUNCTION md_add_relation_target(integer, integer, integer, boolean, character varying, character varying)

CREATE OR REPLACE FUNCTION md_add_relation_target(IN p_rel_id  integer, IN p_from_class_id integer, IN p_to_class_id integer, IN p_is_strong boolean, IN p_link_type character varying, IN p_descr character varying, OUT out_link_id integer)
  RETURNS integer AS
$BODY$
DECLARE
    l_new_target_id integer;
BEGIN

    insert into md_class_relations (link_id, relation_id, from_class_id, to_class_id, is_strong, link_type, description)
    values (nextval('md_pk_seq'), p_rel_id, p_from_class_id, p_to_class_id, p_is_strong, p_link_type, p_descr)
    returning link_id into out_link_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_add_relation_target(integer, integer, integer, boolean, character varying, character varying) OWNER TO :user;

-- Function: md_delete_relation_target(integer)

-- DROP FUNCTION md_delete_relation_target(integer);

CREATE OR REPLACE FUNCTION md_delete_relation_target(p_link_id integer)
  RETURNS void AS
$BODY$
DECLARE
BEGIN

	delete from md_class_relations where link_id = p_link_id; 
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_delete_relation_target(integer) OWNER TO :user;

-- Function: md_update_relation(integer, character varying, character varying, character varying)

-- DROP FUNCTION md_update_relation(integer, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION md_update_relation(p_rel_id integer, p_rel_name character varying, p_short_rel_name character varying, p_descr character varying)
  RETURNS void AS
$BODY$
BEGIN
    update md_relations set
        relation_name = coalesce(p_rel_name, relation_name),
        short_relation_name = coalesce(p_short_rel_name, short_relation_name),
        description = coalesce(p_descr, description)
    where relation_id = p_rel_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_update_relation(integer, character varying, character varying, character varying) OWNER TO :user;

-- Function: md_update_relation_attribute(integer, integer, character varying, character varying, boolean, character varying, character varying, character varying)

-- DROP FUNCTION md_update_relation_attribute(integer, integer, character varying, character varying, boolean, character varying, character varying, character varying)

CREATE OR REPLACE FUNCTION md_update_relation_attribute(IN p_attribute_id  integer, IN p_rel_id  integer, IN p_attribute_name character varying, IN p_data_type character varying, IN p_is_mandatory boolean, IN p_default_value character varying, IN p_value_format character varying, IN p_descr character varying)
  RETURNS void AS
$BODY$
BEGIN
    update md_relation_attributes set
        data_type = coalesce(p_data_type, data_type),
        is_mandatory = coalesce(p_is_mandatory, is_mandatory),
        default_value = coalesce(p_default_value, default_value),
        value_format = coalesce(p_value_format, value_format),
        description = coalesce(p_descr, description)
    where attribute_id = p_attribute_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_update_relation_attribute(integer, integer, character varying, character varying, boolean, character varying, character varying, character varying) OWNER TO :user;

-- Function: md_add_class_action(integer, character varying, boolean, character varying, text)

-- DROP FUNCTION md_add_class_action(integer, character varying, boolean, character varying, text);

CREATE OR REPLACE FUNCTION md_add_class_action(IN p_class_id integer, IN p_action_name character varying, IN p_is_inheritable boolean, IN p_descr character varying, IN p_args text, OUT out_action_id integer)
  RETURNS integer AS
$BODY$
DECLARE
    l_new_act_id integer;
BEGIN

    insert into md_class_actions (action_id, class_id, action_name, is_inheritable, description, arguments)
    values (nextval('md_pk_seq'), p_class_id, p_action_name, p_is_inheritable, p_descr, p_args)
    returning action_id into out_action_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_add_class_action(integer, character varying, boolean, character varying, text) OWNER TO :user;


-- Function: md_update_class_action(integer, character varying, boolean, character varying)

-- DROP FUNCTION md_update_class_action(integer, character varying, boolean, character varying);

CREATE OR REPLACE FUNCTION md_update_class_action(p_action_id integer, p_action_name character varying, p_is_inheritable boolean, p_descr character varying, p_args text)
  RETURNS void AS
$BODY$
DECLARE
BEGIN
    update md_class_actions set
        action_name = coalesce(p_action_name, action_name),
        is_inheritable = coalesce(p_is_inheritable, is_inheritable),
        description = coalesce(p_descr, description),
        arguments=coalesce(p_args, arguments)
    where action_id = p_action_id;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_update_class_action(integer, character varying, boolean, character varying, text)
  OWNER TO :user;

-- Function: md_delete_class_action(integer)

-- DROP FUNCTION md_delete_class_action(integer);

CREATE OR REPLACE FUNCTION md_delete_class_action(p_action_id integer)
  RETURNS void AS
$BODY$
DECLARE
BEGIN

	delete from md_class_actions where action_id = p_action_id; 
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION md_delete_class_action(integer) OWNER TO :user;

-- Function: cm_create_ops_action(character varying, bigint, bigint, integer, integer, integer, text, text, text);
-- DROP FUNCTION cm_create_ops_action(character varying, bigint, bigint, integer, integer, integer, text, text, text);

CREATE OR REPLACE FUNCTION cm_create_ops_action(IN p_action_name character varying, IN p_ops_proc_id bigint, IN p_ci_id bigint, IN p_state_name character varying, IN p_exec_order integer, IN p_critical boolean, IN p_extra_info text, IN p_arglist text, IN p_payload text)
  RETURNS void AS
$BODY$
DECLARE
    l_state_id integer;
BEGIN
    select state_id into l_state_id from cm_ops_action_state where state_name = p_state_name;

    if not found then
	RAISE EXCEPTION 'Given ops action state % is wrong.', p_state_name USING ERRCODE = '22000';
    end if;

    insert into cm_ops_actions (ops_action_id, ops_proc_id, ci_id, action_name, state_id, exec_order, is_critical, extra_info, arglist, payload)
    values (nextval('dj_pk_seq'), p_ops_proc_id, p_ci_id, p_action_name, l_state_id, p_exec_order, p_critical, p_extra_info, p_arglist, p_payload);
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_create_ops_action(character varying, bigint, bigint, character varying, integer, boolean, text, text, text) OWNER TO :user;


-- Function: cm_create_ops_procedure(bigint, character varying, bigint, character varying, text, character varying, text, bigint);
-- DROP FUNCTION cm_create_ops_procedure(bigint, character varying, bigint, character varying, text, character varying, text, bigint);

CREATE OR REPLACE FUNCTION cm_create_ops_procedure(IN p_procedure_id bigint, IN p_procedure_name character varying, IN p_ci_id bigint, IN p_state_name character varying, IN p_arglist text, IN p_created_by character varying, IN p_definition text, IN p_proc_ci_id bigint)
  RETURNS void AS
$BODY$
DECLARE
    l_state_id integer;
BEGIN

    select state_id into l_state_id from cm_ops_proc_state where state_name = p_state_name;

    if not found then
	RAISE EXCEPTION 'Can not resolve state: %', p_state_name USING ERRCODE = '22000';
    end if;

    insert into cm_ops_procedures (ops_proc_id, ci_id, proc_name, state_id, arglist, created_by, definition, proc_ci_id)
    values (p_procedure_id, p_ci_id, p_procedure_name, l_state_id, p_arglist, p_created_by, p_definition, nullif(p_proc_ci_id,0));

    insert into cms_event_queue(event_id, source_pk, source_name, event_type_id)
    values (nextval('event_pk_seq'), p_procedure_id, 'opsprocedure' , 100);
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_create_ops_procedure(bigint, character varying, bigint, character varying, text, character varying, text, bigint) OWNER TO :user;

-- Function: cm_update_ops_procedure_state(bigint, character varying)

-- DROP FUNCTION cm_update_ops_procedure_state(bigint, character varying);

CREATE OR REPLACE FUNCTION cm_update_ops_procedure_state(p_proc_id bigint, p_state character varying)
  RETURNS void AS
$BODY$
DECLARE
 l_state_id integer;
BEGIN

    select state_id into l_state_id from cm_ops_proc_state where state_name = p_state;

    if not found then
		RAISE EXCEPTION 'Can not resolve state: %', p_state USING ERRCODE = '22000';
    end if;

    update cm_ops_procedures set state_id = l_state_id, updated = now()
    where ops_proc_id = p_proc_id;	

    -- lets check if it's cancled then we cancel all pending action orders for this proc

    if l_state_id = 400 then
    
		update cm_ops_actions
		set state_id = 400
		where ops_proc_id = p_proc_id
		and state_id = 10;
	
    end if;

    insert into cms_event_queue(event_id, source_pk, source_name, event_type_id)
    values (nextval('event_pk_seq'), p_proc_id, 'opsprocedure' , 200);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_ops_procedure_state(bigint, character varying)
  OWNER TO :user;

-- Function: cm_update_ops_action_state(bigint, character varying)

-- DROP FUNCTION cm_update_ops_action_state(bigint, character varying);

CREATE OR REPLACE FUNCTION cm_update_ops_action_state(p_action_id bigint, p_state character varying)
  RETURNS void AS
$BODY$
DECLARE
 l_state_id integer;
BEGIN

    select state_id into l_state_id from cm_ops_action_state where state_name = p_state;

    if not found then
	RAISE EXCEPTION 'Can not resolve state: %', p_state USING ERRCODE = '22000';
    end if;

    update cm_ops_actions set state_id = l_state_id, updated = now()
    where ops_action_id = p_action_id;	

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_update_ops_action_state(bigint, character varying) OWNER TO :user;

-- Function: cm_is_ops_procedure_active_for_ci(bigint)

-- DROP FUNCTION cm_is_ops_procedure_active_for_ci(bigint)

CREATE OR REPLACE FUNCTION cm_is_ops_procedure_active_for_ci(IN p_ci_id bigint)
  RETURNS boolean AS
$BODY$
DECLARE
 l_active_proc bigint;
BEGIN

    select count(*) into l_active_proc from cm_ops_procedures where ci_id = p_ci_id and state_id in (10,100);

    if l_active_proc = 0 then
	    select count(*) into l_active_proc from cm_ops_procedures p, cm_ops_actions a where p.state_id in (10,100) and a.ops_proc_id = p.ops_proc_id and a.ci_id = p_ci_id;
    end if;

    return l_active_proc > 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_is_ops_procedure_active_for_ci(bigint) OWNER TO :user;

-- Function: cm_is_opened_release_for_ci(bigint)

-- DROP FUNCTION cm_is_opened_release_for_ci(bigint)

CREATE OR REPLACE FUNCTION cm_is_opened_release_for_ci(IN p_ci_id bigint)
  RETURNS boolean AS
$BODY$
DECLARE
 l_open_release integer;
BEGIN

	select count(*) into l_open_release from dj_rfc_ci a, dj_releases b where b.release_id=a.release_id and b.release_state_id = 100 and a.ci_id = p_ci_id;
    
    return l_open_release > 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cm_is_opened_release_for_ci(bigint) OWNER TO :user;


-- Function: dj_create_release(bigint, bigint, bigint, character varying, character varying, integer, character varying, character varying, integer)

-- DROP FUNCTION dj_create_release(bigint, bigint, bigint, character varying, character varying, integer, character varying, character varying, integer);

CREATE OR REPLACE FUNCTION dj_create_release(p_release_id bigint, p_ns_id bigint, p_parent_release_id bigint, p_release_name character varying, p_created_by character varying, p_release_state_id integer, p_release_type character varying, p_description character varying, p_revision integer)
  RETURNS void AS
$BODY$
DECLARE
   l_parent_release_id bigint;
 BEGIN
	if p_parent_release_id is null then
	  select into l_parent_release_id max(parent_release_id) from dj_releases where ns_id = p_ns_id;	
	else
	  l_parent_release_id := p_parent_release_id;
	end if;
 
	INSERT INTO dj_releases(
            release_id, ns_id, parent_release_id, release_name, created_by, release_state_id, release_type, description, revision)
    	VALUES (p_release_id, p_ns_id, l_parent_release_id, p_release_name, p_created_by, p_release_state_id, p_release_type, p_description, p_revision);

	INSERT INTO cms_event_queue(event_id, source_pk, source_name, event_type_id)
	VALUES (nextval('event_pk_seq'), p_release_id, 'release' , 100);
 END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_create_release(bigint, bigint, bigint, character varying, character varying, integer, character varying, character varying, integer) OWNER TO :user;


-- Function: dj_brush_exec_order(bigint)

-- DROP FUNCTION dj_brush_exec_order(bigint);

CREATE OR REPLACE FUNCTION dj_brush_exec_order(p_release_id bigint)
  RETURNS void AS
$BODY$
DECLARE
    l_rfc_ci record;
    l_exec_order integer;
    l_last_exec_order integer;
BEGIN

    l_exec_order = 0;
    l_last_exec_order = 0; 	
    for l_rfc_ci in 
	    select rci.rfc_id, rci.execution_order
	    from dj_rfc_ci rci
	    where rci.release_id = p_release_id
	    and rci.is_active_in_release = true
	    order by rci.execution_order
    loop
	if l_rfc_ci.execution_order > l_last_exec_order then
	   l_exec_order = l_exec_order + 1;
	   l_last_exec_order = l_rfc_ci.execution_order;
	end if;

	update dj_rfc_ci set execution_order = l_exec_order where rfc_id = l_rfc_ci.rfc_id;
    end loop;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_brush_exec_order(bigint) OWNER TO :user;

-- Function: cms_acquire_lock(character varying, character varying, integer)

-- Function: cms_acquire_lock(character varying, character varying, integer)

-- DROP FUNCTION cms_acquire_lock(character varying, character varying, integer);

CREATE OR REPLACE FUNCTION cms_acquire_lock(p_lock_name character varying, p_locked_by character varying, p_stale_timeout integer)
  RETURNS boolean AS
$BODY$
DECLARE
    l_lock_row cms_lock%ROWTYPE;
    l_lock_cnt integer;
BEGIN

    -- first lets check if we have a lock record
    select into l_lock_cnt count(1) from cms_lock where lock_name = p_lock_name;

    if l_lock_cnt = 0 then
	-- we are first lets create a row
	BEGIN
	    insert into cms_lock(lock_id, lock_name, locked_by)
	    values (nextval('cm_pk_seq'), p_lock_name, p_locked_by);
        EXCEPTION WHEN unique_violation THEN
            --somebody beat us on this return false
            return false;
        END;
    end if;

    -- try to lock it
    select into l_lock_row cl.lock_id, cl.lock_name, cl.locked_by, cl.created, cl.updated
    from cms_lock cl where cl.lock_name = p_lock_name for update;	

    if l_lock_row.locked_by = p_locked_by then 
	-- this is my lock lets update timestamp
	update cms_lock set updated=current_timestamp where lock_id = l_lock_row.lock_id;
	return true;
    elsif cast(extract(epoch from (current_timestamp - l_lock_row.updated)) as integer) > p_stale_timeout then
	-- seems like the lock is stale I will hijack it
	update cms_lock 
	set locked_by = p_locked_by, 
            created = current_timestamp, 
            updated=current_timestamp 
        where lock_id = l_lock_row.lock_id;
        return true;
    else
	-- the lock is fresh and not mine
	return false;	
    end if;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cms_acquire_lock(character varying, character varying, integer)
  OWNER TO :user;

-- Function: cms_set_var(character varying, text, character varying, character varying)

-- DROP FUNCTION cms_set_var(character varying, text, character varying);

CREATE OR REPLACE FUNCTION cms_set_var(p_var_name character varying, p_var_value text, p_criteria character varying, p_updated_by character varying)
  RETURNS void AS
$BODY$
DECLARE
    l_var_id integer;
BEGIN

    -- first lets check if we have a var record
    select into l_var_id var_id
    from cms_vars
    where var_name = p_var_name
     and ((p_criteria is null and criteria is null) or criteria = p_criteria);

    if l_var_id is null then
	insert into cms_vars(var_id, var_name, var_value, criteria, updated_by, created, updated)
	values (nextval('cm_pk_seq'), p_var_name, p_var_value, p_criteria, p_updated_by, now(), now());
    else 
    	update cms_vars
    	set var_value = p_var_value,
            criteria = p_criteria,
    	    updated_by = p_updated_by,
    	    updated = now()
    	where var_id = l_var_id;
    	    
    end if;

 
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cms_set_var(character varying, text, character varying, character varying)
  OWNER TO :user;

  
-- Function: dj_reset_failed_records(bigint)

-- DROP FUNCTION dj_reset_failed_records(bigint);

CREATE OR REPLACE FUNCTION dj_reset_failed_records(p_deployment_id bigint)
  RETURNS void AS
$BODY$
DECLARE
BEGIN

    update dj_deployment_rfc 
    set state_id = 10,
        updated = now()
    where deployment_id = p_deployment_id 
    and state_id = 300;	

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_reset_failed_records(bigint)
  OWNER TO :user;


-- Function: dj_create_dpmt_approval(bigint, bigint, text, integer)

-- DROP FUNCTION dj_create_dpmt_approval(bigint, bigint, text, integer);

CREATE OR REPLACE FUNCTION dj_create_dpmt_approval(IN p_dpmt_id bigint, IN p_govern_ci_id bigint, IN p_govern_ci text, IN p_expires_in integer, OUT out_approval_id bigint)
  RETURNS bigint AS
$BODY$
BEGIN
	INSERT INTO dj_dpmt_approvals(
            approval_id, deployment_id, govern_ci_id, govern_ci, state_id, expires_in)
    	VALUES (nextval('dj_pk_seq'), p_dpmt_id, p_govern_ci_id, p_govern_ci, 100, coalesce(p_expires_in,-1)) -- by default create in pending state
    	returning approval_id into out_approval_id;

 END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_create_dpmt_approval(bigint, bigint, text, integer)
  OWNER TO :user;
  

 -- Function: dj_dpmt_upd_approvla_rec(bigint, character varying, integer, text, character varying)

-- DROP FUNCTION dj_dpmt_upd_approvla_rec(bigint, character varying, integer, text, character varying);

CREATE OR REPLACE FUNCTION dj_dpmt_upd_approvla_rec(p_approval_id bigint, p_updated_by character varying, p_expires_in integer, p_comments text, p_state character varying)
  RETURNS void AS
$BODY$
DECLARE
  l_state_id integer;
BEGIN

	select into l_state_id state_id
	from dj_approval_states
	where state_name = p_state;	

	if l_state_id is null then
	   RAISE EXCEPTION 'Can not resolve state: %', p_state USING ERRCODE = '22000';
	end if;

	update dj_dpmt_approvals
	set state_id = l_state_id,
	    updated_by = p_updated_by,
	    updated = now(),
	    comments = p_comments,
	    expires_in = coalesce(p_expires_in, expires_in) 
	where approval_id = p_approval_id;    

 END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_dpmt_upd_approvla_rec(bigint, character varying, integer, text, character varying)
  OWNER TO :user;
 
  
-- Function: dj_dpmt_approve(bigint, character varying, integer, text)

-- DROP FUNCTION dj_dpmt_approve(bigint, character varying, integer, text);

CREATE OR REPLACE FUNCTION dj_dpmt_approve(p_approval_id bigint, p_updated_by character varying, p_expires_in integer, p_comments text)
  RETURNS void AS
$BODY$
BEGIN
	update dj_dpmt_approvals
	set state_id = 200,
	    updated_by = p_approved_by,
	    updated = now(),
	    comments = p_comments,
	    expires_in = coalesce(p_expires_in, expires_in) 
	where approval_id = p_approval_id;    

 END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_dpmt_approve(bigint, character varying, integer, text)
 OWNER TO :user;


CREATE OR REPLACE FUNCTION dj_rm_rfcs(p_ns_path character varying)
  RETURNS integer AS
$BODY$
DECLARE
    l_loop_counter integer;
    l_rel_rfc_id bigint;
BEGIN
    l_loop_counter = 0;
    for l_rel_rfc_id in 
	SELECT rfc_id
	FROM dj_rfc_ci rci, dj_releases r, dj_release_states rs, ns_namespaces ns
	WHERE rci.release_id = r.release_id
	   AND r.release_state_id = rs.release_state_id
	   AND rs.state_name = 'open'
	   AND rci.is_active_in_release = true
	   AND rci.ns_id = ns.ns_id
	   AND ns.ns_path = p_ns_path
    loop
	perform dj_rm_rfc_ci(l_rel_rfc_id);
	l_loop_counter:= l_loop_counter+1;
    end loop;
    return l_loop_counter;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION dj_rm_rfcs(character varying)
  OWNER TO :user;



-- Function: dj_create_alt_namespace(bigint, character varying, bigint)

DROP FUNCTION dj_create_alt_namespace(bigint, character varying, bigint);

CREATE OR REPLACE FUNCTION dj_create_alt_namespace(p_ns_id bigint, p_tag character varying, p_rfc_id bigint)
  RETURNS void AS
$BODY$
DECLARE
    l_tag_id bigint;
BEGIN
    select tag_id into l_tag_id from ns_opt_tag where tag = p_tag;	

    if not found then
	    insert into ns_opt_tag (tag_id, tag)
        values
        (nextval('cm_pk_seq'), p_tag)
        returning tag_id into l_tag_id;
    end if;
        
    insert into dj_ns_opt (rfc_id, ns_id, created, tag_id) values (p_rfc_id, p_ns_id, now(), l_tag_id);    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION dj_create_alt_namespace(bigint, character varying, bigint) OWNER TO :user;



-- Function: dj_delete_alt_namespace(bigint, bigint)

-- DROP FUNCTION dj_delete_alt_namespace(bigint, bigint);

CREATE OR REPLACE FUNCTION dj_delete_alt_namespace( p_ns_id bigint, p_rfc_id bigint)
  RETURNS bigint AS
$BODY$
BEGIN
    delete from dj_ns_opt where rfc_id= p_rfc_id and ns_id=p_ns_id;    
    return p_ns_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION dj_delete_alt_namespace(bigint, bigint) OWNER TO :user;




-- Function: cm_create_alt_namespace(bigint, character varying, bigint)

DROP FUNCTION cm_create_alt_namespace(bigint, character varying, bigint);

CREATE OR REPLACE FUNCTION cm_create_alt_namespace(p_ns_id bigint, p_tag character varying, p_ci_id bigint)
  RETURNS void AS
$BODY$
DECLARE
    l_tag_id bigint;
BEGIN
    select tag_id into l_tag_id from ns_opt_tag where tag = p_tag;	

    if not found then
	    insert into ns_opt_tag (tag_id, tag)
        values
        (nextval('cm_pk_seq'), p_tag)
        returning tag_id into l_tag_id;
    end if;
        
    insert into cm_ns_opt (ci_id, ns_id, created, tag_id) values (p_ci_id, p_ns_id, now(), l_tag_id);    

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION cm_create_alt_namespace(bigint, character varying, bigint) OWNER TO :user;




-- Function: cm_delete_alt_namespace(bigint, bigint)

-- DROP FUNCTION cm_delete_alt_namespace(bigint, bigint);

CREATE OR REPLACE FUNCTION cm_delete_alt_namespace( p_ns_id bigint, p_ci_id bigint)
  RETURNS bigint AS
$BODY$
BEGIN
    delete from cm_ns_opt where ci_id= p_ci_id and ns_id=p_ns_id;    
    return p_ns_id;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION cm_delete_alt_namespace(bigint, bigint) OWNER TO :user;


