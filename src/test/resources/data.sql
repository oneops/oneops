insert into md_classes (class_id, class_name, short_class_name, access_level, is_namespace, description)
values (100, 'Ci','Ci','global', false,'This is basic super class, all classes will extend this one');

INSERT INTO md_class_attributes(
            attribute_id, class_id, attribute_name, data_type, is_mandatory, 
            is_inheritable, is_encrypted, is_immutable, force_on_dependent, 
            default_value, value_format, description, created, updated)
    VALUES (101, 100, 'testAttribute', 'string', false, 
            false, false, false, false,'', 
            '{"help":"Assembly description","category":"1.Configuration","order":1}', 'testAttrbitue', '2016-09-02 16:18:23.652701',  '2016-09-02 16:18:23.652701');
