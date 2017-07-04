INSERT INTO dj_deployment_rfc_states (state_id,state_name) VALUES (10,'pending');

INSERT INTO dj_deployment_rfc_states (state_id,state_name) VALUES (100,'inprogress');

INSERT INTO dj_deployment_rfc_states (state_id,state_name) VALUES (200,'complete');

INSERT INTO dj_deployment_rfc_states (state_id,state_name) VALUES (300,'failed');

INSERT INTO dj_deployment_rfc_states (state_id,state_name) VALUES (400,'canceled');

INSERT INTO dj_deployment_states (state_id,state_name) VALUES (10,'pending');

INSERT INTO dj_deployment_states (state_id,state_name) VALUES (100,'active');

INSERT INTO dj_deployment_states (state_id,state_name) VALUES (200,'complete');

INSERT INTO dj_deployment_states (state_id,state_name) VALUES (300,'failed');

INSERT INTO dj_deployment_states (state_id,state_name) VALUES (400,'canceled');

INSERT INTO dj_deployment_states (state_id,state_name) VALUES (500,'paused');

INSERT INTO dj_release_states (release_state_id,state_name) VALUES (100,'open');

INSERT INTO dj_release_states (release_state_id,state_name) VALUES (200,'closed');

INSERT INTO dj_release_states (release_state_id,state_name) VALUES (300,'canceled');

INSERT INTO dj_release_states (release_state_id,state_name) VALUES (10,'pending');

INSERT INTO cm_ci_state (ci_state_id,state_name) VALUES (100,'default');

INSERT INTO cm_ci_state (ci_state_id,state_name) VALUES (200,'pending_deletion');

INSERT INTO cm_ci_state (ci_state_id,state_name) VALUES (300,'replace');

INSERT INTO cm_ci_state (ci_state_id,state_name) VALUES (400,'locked');

INSERT INTO cm_ci_state (ci_state_id,state_name) VALUES (500,'manifest_locked');

INSERT INTO dj_rfc_ci_actions (action_id,action_name) VALUES (100,'add');

INSERT INTO dj_rfc_ci_actions (action_id,action_name) VALUES (200,'update');

INSERT INTO dj_rfc_ci_actions (action_id,action_name) VALUES (300,'delete');

INSERT INTO dj_rfc_ci_actions (action_id,action_name) VALUES (400,'replace');

INSERT INTO ns_namespaces (ns_id,ns_path) VALUES (840155,'/local-dev/prod1/dev/bom');

INSERT INTO ns_namespaces (ns_id,ns_path) VALUES (2268356,'/local-dev/prod1/dev/manifest/srv1/1');

INSERT INTO ns_namespaces (ns_id,ns_path) VALUES (2269003,'/local-dev/prod1/dev/bom/srv1/1');

INSERT INTO ns_namespaces (ns_id,ns_path,created) VALUES (173346,'/local-dev/_clouds',TIMESTAMP '2015-07-29 17:38:10.105');

INSERT INTO ns_namespaces (ns_id,ns_path,created) VALUES (277111,'/local-dev/prod1',TIMESTAMP '2015-08-10 10:24:41.660');

INSERT INTO ns_namespaces (ns_id,ns_path,created) VALUES (173345,'/local-dev',TIMESTAMP '2015-07-29 14:49:27.115');

INSERT INTO ns_namespaces (ns_id,ns_path,created) VALUES (100,'/',TIMESTAMP '2015-07-27 15:13:18.393');

INSERT INTO ns_namespaces (ns_id,ns_path,created) VALUES (1220883,'/public/oneops/packs/tomcat/1/redundant',TIMESTAMP '2016-08-09 14:36:56.108');

INSERT INTO ns_namespaces (ns_id,ns_path,created) VALUES (368279,'/local-dev/_clouds/dev-dfwstg2',TIMESTAMP '2015-08-28 11:09:07.307');




INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1116,'manifest.Localvar','Localvar',1109,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.818',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1160,'manifest.Platform','Platform',1147,false,'oo::chef-11.4.0','global','Assembly platforms',NULL,TIMESTAMP '2015-07-29 14:13:43.162',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1135,'manifest.Monitor','Monitor',1121,false,'oo::chef-11.4.0','global','Monitor',NULL,TIMESTAMP '2015-07-29 14:13:42.969',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7506,'manifest.oneops.1.Artifact','Artifact',7477,false,'oo::chef-11.18.12','global','Installs/Configures software artifacts',NULL,TIMESTAMP '2015-12-10 14:20:28.185',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7509,'bom.oneops.1.Artifact','Artifact',7477,false,'oo::chef-11.18.12','global','Installs/Configures software artifacts',NULL,TIMESTAMP '2015-12-10 14:20:28.207',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7953,'manifest.oneops.1.Daemon','Daemon',7924,false,'oo::chef-11.18.12','global','Daemon/OS Level Service',NULL,TIMESTAMP '2015-12-10 14:20:29.636',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7959,'bom.oneops.1.Daemon','Daemon',7924,false,'oo::chef-11.18.12','global','Daemon/OS Level Service',NULL,TIMESTAMP '2015-12-10 14:20:29.655',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7815,'bom.oneops.1.Compute','Compute',7790,false,'oo::chef-11.18.12','global','Installs/Configures compute',NULL,TIMESTAMP '2015-12-10 14:20:29.276',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8193,'manifest.oneops.1.Fqdn','Fqdn',8175,false,'oo::chef-11.18.12','global','Updates FQDN records',NULL,TIMESTAMP '2015-12-10 14:20:30.871',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8196,'bom.oneops.1.Fqdn','Fqdn',8175,false,'oo::chef-11.18.12','global','Updates FQDN records',NULL,TIMESTAMP '2015-12-10 14:20:30.886',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8370,'manifest.oneops.1.Java','Java',8354,false,'oo::chef-11.18.12','global','Installs/Configures Java',NULL,TIMESTAMP '2015-12-10 14:20:31.501',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8372,'bom.oneops.1.Java','Java',8354,false,'oo::chef-11.18.12','global','Installs/Configures Java',NULL,TIMESTAMP '2015-12-10 14:20:31.516',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8460,'manifest.oneops.1.Keypair','Keypair',8447,false,'oo::chef-11.18.12','global','General purpose key pairs',NULL,TIMESTAMP '2015-12-10 14:20:31.836',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8462,'bom.oneops.1.Keypair','Keypair',8447,false,'oo::chef-11.18.12','global','General purpose key pairs',NULL,TIMESTAMP '2015-12-10 14:20:31.852',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8528,'manifest.oneops.1.Lb','Lb',8497,false,'oo::chef-11.18.12','global','Installs/Configures load balancer',NULL,TIMESTAMP '2015-12-10 14:20:32.163',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8533,'bom.oneops.1.Lb','Lb',8497,false,'oo::chef-11.18.12','global','Installs/Configures load balancer',NULL,TIMESTAMP '2015-12-10 14:20:32.179',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9029,'bom.oneops.1.Os','Os',8996,false,'oo::chef-11.18.12','global','Installs/Configures OperatingSystem',NULL,TIMESTAMP '2015-12-10 14:20:33.927',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9036,'manifest.oneops.1.Os','Os',8996,false,'oo::chef-11.18.12','global','Installs/Configures OperatingSystem',NULL,TIMESTAMP '2015-12-10 14:20:33.945',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9264,'manifest.oneops.1.Secgroup','Secgroup',9252,false,'oo::chef-11.18.12','global','Security group',NULL,TIMESTAMP '2015-12-10 14:20:35.143',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9266,'bom.oneops.1.Secgroup','Secgroup',9252,false,'oo::chef-11.18.12','global','Security group',NULL,TIMESTAMP '2015-12-10 14:20:35.159',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9478,'manifest.oneops.1.Volume','Volume',9457,false,'oo::chef-11.18.12','global','Volume',NULL,TIMESTAMP '2015-12-10 14:20:35.871',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9482,'bom.oneops.1.Volume','Volume',9457,false,'oo::chef-11.18.12','global','Volume',NULL,TIMESTAMP '2015-12-10 14:20:35.887',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7839,'manifest.oneops.1.Compute','Compute',7790,false,'oo::chef-11.18.12','global','Installs/Configures compute',NULL,TIMESTAMP '2015-12-10 14:20:29.309',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9389,'manifest.oneops.1.Tomcat','Tomcat',9290,false,'oo::chef-11.18.12','global','Installs/Configures tomcat',NULL,TIMESTAMP '2015-12-10 14:20:35.485',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9400,'bom.oneops.1.Tomcat','Tomcat',9290,false,'oo::chef-11.18.12','global','Installs/Configures tomcat',NULL,TIMESTAMP '2015-12-10 14:20:35.509',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1035,'base.Assembly','Assembly',100,true,'oo::chef-11.4.0','global','Assembly',NULL,TIMESTAMP '2015-07-29 14:13:42.039',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1038,'mgmt.Assembly','Assembly',1035,true,'oo::chef-11.4.0','global','Assembly',NULL,TIMESTAMP '2015-07-29 14:13:42.057',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1039,'account.Assembly','Assembly',1035,true,'oo::chef-11.4.0','global','Assembly',NULL,TIMESTAMP '2015-07-29 14:13:42.078',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1059,'account.Cloud','Cloud',1055,true,'oo::chef-11.4.0','global','Collection of Cloud Services',NULL,TIMESTAMP '2015-07-29 14:13:42.283',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1058,'mgmt.Cloud','Cloud',1055,true,'oo::chef-11.4.0','global','Collection of Cloud Services',NULL,TIMESTAMP '2015-07-29 14:13:42.263',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1055,'base.Cloud','Cloud',100,true,'oo::chef-11.4.0','global','Collection of Cloud Services',NULL,TIMESTAMP '2015-07-29 14:13:42.217',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1089,'account.Environment','Environment',1074,true,'oo::chef-11.4.0','global','Environment',NULL,TIMESTAMP '2015-07-29 14:13:42.500',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1090,'manifest.Environment','Environment',1074,true,'oo::chef-11.4.0','global','Environment',NULL,TIMESTAMP '2015-07-29 14:13:42.526',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1074,'base.Environment','Environment',100,true,'oo::chef-11.4.0','global','Environment',NULL,TIMESTAMP '2015-07-29 14:13:42.473',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1115,'catalog.Localvar','Localvar',1109,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.798',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1113,'mgmt.catalog.Localvar','Localvar',1109,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.756',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1114,'mgmt.manifest.Localvar','Localvar',1109,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.777',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1093,'base.Globalvar','Globalvar',100,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.569',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1098,'manifest.Globalvar','Globalvar',1093,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.607',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1097,'catalog.Globalvar','Globalvar',1093,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.587',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1070,'account.Cloudvar','Cloudvar',1066,false,'oo::chef-11.4.0','global','Cloud Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.382',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1066,'base.Cloudvar','Cloudvar',100,false,'oo::chef-11.4.0','global','Cloud Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.364',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1109,'base.Localvar','Localvar',100,false,'oo::chef-11.4.0','global','Global Variables',NULL,TIMESTAMP '2015-07-29 14:13:42.741',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1121,'base.Monitor','Monitor',100,false,'oo::chef-11.4.0','global','Monitor',NULL,TIMESTAMP '2015-07-29 14:13:42.921',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7477,'base.oneops.1.Artifact','Artifact',100,false,'oo::chef-11.18.12','global','Installs/Configures software artifacts',NULL,TIMESTAMP '2015-12-10 14:20:28.068',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7924,'base.oneops.1.Daemon','Daemon',100,false,'oo::chef-11.18.12','global','Daemon/OS Level Service',NULL,TIMESTAMP '2015-12-10 14:20:29.555',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7790,'base.oneops.1.Compute','Compute',100,false,'oo::chef-11.18.12','global','Installs/Configures compute',NULL,TIMESTAMP '2015-12-10 14:20:29.202',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8175,'base.oneops.1.Fqdn','Fqdn',100,false,'oo::chef-11.18.12','global','Updates FQDN records',NULL,TIMESTAMP '2015-12-10 14:20:30.805',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8354,'base.oneops.1.Java','Java',100,false,'oo::chef-11.18.12','global','Installs/Configures Java',NULL,TIMESTAMP '2015-12-10 14:20:31.410',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8447,'base.oneops.1.Keypair','Keypair',100,false,'oo::chef-11.18.12','global','General purpose key pairs',NULL,TIMESTAMP '2015-12-10 14:20:31.755',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8497,'base.oneops.1.Lb','Lb',100,false,'oo::chef-11.18.12','global','Installs/Configures load balancer',NULL,TIMESTAMP '2015-12-10 14:20:32.095',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8996,'base.oneops.1.Os','Os',100,false,'oo::chef-11.18.12','global','Installs/Configures OperatingSystem',NULL,TIMESTAMP '2015-12-10 14:20:33.852',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9252,'base.oneops.1.Secgroup','Secgroup',100,false,'oo::chef-11.18.12','global','Security group',NULL,TIMESTAMP '2015-12-10 14:20:35.072',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9457,'base.oneops.1.Volume','Volume',100,false,'oo::chef-11.18.12','global','Volume',NULL,TIMESTAMP '2015-12-10 14:20:35.809',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1147,'base.Platform','Platform',100,false,'oo::chef-11.4.0','global','Assembly platforms',NULL,TIMESTAMP '2015-07-29 14:13:43.082',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9290,'base.oneops.1.Tomcat','Tomcat',100,false,'oo::chef-11.18.12','global','Installs/Configures tomcat',NULL,TIMESTAMP '2015-12-10 14:20:35.383',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1140,'account.Organization','Organization',1136,true,'oo::chef-11.4.0','global','Organization',NULL,TIMESTAMP '2015-07-29 14:13:43.011',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8489,'mgmt.manifest.oneops.1.Keystore','Keystore',8483,false,'oo::chef-11.18.12','global','Keystore',NULL,TIMESTAMP '2015-12-10 14:20:32.027',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8518,'mgmt.manifest.oneops.1.Lb','Lb',8497,false,'oo::chef-11.18.12','global','Installs/Configures load balancer',NULL,TIMESTAMP '2015-12-10 14:20:32.128',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9260,'mgmt.manifest.oneops.1.Secgroup','Secgroup',9252,false,'oo::chef-11.18.12','global','Security group',NULL,TIMESTAMP '2015-12-10 14:20:35.111',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8211,'mgmt.manifest.oneops.1.Glusterfs','Glusterfs',8201,false,'oo::chef-11.18.12','global','GlusterFS',NULL,TIMESTAMP '2015-12-10 14:20:30.935',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9281,'mgmt.manifest.oneops.1.Storage','Storage',9275,false,'oo::chef-11.18.12','global','Storage',NULL,TIMESTAMP '2015-12-10 14:20:35.273',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9021,'mgmt.manifest.oneops.1.Os','Os',8996,false,'oo::chef-11.18.12','global','Installs/Configures OperatingSystem',NULL,TIMESTAMP '2015-12-10 14:20:33.892',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (10578,'mgmt.manifest.oneops.1.Objectstore','Objectstore',10573,false,'oo::chef-11.18.12','global','Installs/Configures object-store',NULL,TIMESTAMP '2016-07-05 16:56:41.072',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8366,'mgmt.manifest.oneops.1.Java','Java',8354,false,'oo::chef-11.18.12','global','Installs/Configures Java',NULL,TIMESTAMP '2015-12-10 14:20:31.441',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (10993,'mgmt.manifest.oneops.1.Sensuclient','Sensuclient',10970,false,'oo::chef-11.18.12','global','Installs/Configures sensuclient',NULL,TIMESTAMP '2016-08-09 14:08:36.072',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8547,'mgmt.manifest.oneops.1.Library','Library',8542,false,'oo::chef-11.18.12','global','Software library items',NULL,TIMESTAMP '2015-12-10 14:20:32.227',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9367,'mgmt.manifest.oneops.1.Tomcat','Tomcat',9290,false,'oo::chef-11.18.12','global','Installs/Configures tomcat',NULL,TIMESTAMP '2015-12-10 14:20:35.436',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8456,'mgmt.manifest.oneops.1.Keypair','Keypair',8447,false,'oo::chef-11.18.12','global','General purpose key pairs',NULL,TIMESTAMP '2015-12-10 14:20:31.804',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (14815,'mgmt.manifest.oneops.1.Firewall','Firewall',14812,true,'oo::chef-11.18.12','global','Installs/Configures firewall',NULL,TIMESTAMP '2016-12-12 20:50:02.642',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8571,'mgmt.manifest.oneops.1.Logstash','Logstash',8555,false,'oo::chef-11.18.12','global','Logstash',NULL,TIMESTAMP '2015-12-10 14:20:32.324',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7805,'mgmt.manifest.oneops.1.Compute','Compute',7790,false,'oo::chef-11.18.12','global','Installs/Configures compute',NULL,TIMESTAMP '2015-12-10 14:20:29.237',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1184,'mgmt.manifest.Qpath','Qpath',1181,false,'oo::chef-11.4.0','global','Query Path',NULL,TIMESTAMP '2015-07-29 14:13:43.325',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8187,'mgmt.manifest.oneops.1.Fqdn','Fqdn',8175,false,'oo::chef-11.18.12','global','Updates FQDN records',NULL,TIMESTAMP '2015-12-10 14:20:30.836',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8151,'mgmt.manifest.oneops.1.File','File',8144,false,'oo::chef-11.18.12','global','Custom file',NULL,TIMESTAMP '2015-12-10 14:20:30.642',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8439,'mgmt.manifest.oneops.1.Job','Job',8426,false,'oo::chef-11.18.12','global','Job',NULL,TIMESTAMP '2015-12-10 14:20:31.686',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1134,'mgmt.manifest.Monitor','Monitor',1121,false,'oo::chef-11.4.0','global','Monitor',NULL,TIMESTAMP '2015-07-29 14:13:42.946',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7500,'mgmt.manifest.oneops.1.Artifact','Artifact',7477,false,'oo::chef-11.18.12','global','Installs/Configures software artifacts',NULL,TIMESTAMP '2015-12-10 14:20:28.117',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9437,'mgmt.manifest.oneops.1.User','User',9424,false,'oo::chef-11.18.12','global','User',NULL,TIMESTAMP '2015-12-10 14:20:35.683',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7941,'mgmt.manifest.oneops.1.Daemon','Daemon',7924,false,'oo::chef-11.18.12','global','Daemon/OS Level Service',NULL,TIMESTAMP '2015-12-10 14:20:29.587',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1155,'mgmt.manifest.Platform','Platform',1147,false,'oo::chef-11.4.0','global','Assembly platforms',NULL,TIMESTAMP '2015-07-29 14:13:43.140',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (8057,'mgmt.manifest.oneops.1.Download','Download',8046,false,'oo::chef-11.18.12','global','Downloads external files or directories',NULL,TIMESTAMP '2015-12-10 14:20:30.187',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (9470,'mgmt.manifest.oneops.1.Volume','Volume',9457,false,'oo::chef-11.18.12','global','Volume',NULL,TIMESTAMP '2015-12-10 14:20:35.837',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7572,'mgmt.manifest.oneops.1.Build','Build',7548,false,'oo::chef-11.18.12','global','Installs/Configures code builds',NULL,TIMESTAMP '2015-12-10 14:20:28.498',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (7739,'mgmt.manifest.oneops.1.Certificate','Certificate',7729,false,'oo::chef-11.18.12','global','Certificate',NULL,TIMESTAMP '2015-12-10 14:20:28.899',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1181,'base.Qpath','Qpath',100,false,'oo::chef-11.4.0','global','Query Path',NULL,TIMESTAMP '2015-07-29 14:13:43.309',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (1136,'base.Organization','Organization',100,true,'oo::chef-11.4.0','global','Organization',NULL,TIMESTAMP '2015-07-29 14:13:42.995',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (3339,'cloud.service.Nexus','Nexus',3323,true,'oo::chef-11.4.0','global','Nexus cloud service',NULL,TIMESTAMP '2015-07-29 14:16:27.266',2);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (2671,'cloud.service.Infoblox','Infoblox',2664,true,'oo::chef-11.4.0','global','DNS Cloud Service',NULL,TIMESTAMP '2015-07-29 14:16:24.985',2);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (3625,'cloud.service.Openstack','Openstack',3597,true,'oo::chef-11.4.0','global','Compute Cloud Service',NULL,TIMESTAMP '2015-07-29 14:16:28.016',2);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (11019,'cloud.compliance.Security','Security',11012,false,'oo::chef-11.4.0','global','Cloud Security Compliance',NULL,TIMESTAMP '2016-08-09 15:09:31.988',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (3171,'cloud.service.Mirror','Mirror',3138,true,'oo::chef-11.4.0','global','Installs/Configures Software Download Mirrors',NULL,TIMESTAMP '2015-07-29 14:16:26.632',2);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (11454,'cloud.Zone','Zone',11365,true,'oo::chef-11.4.0','global','Cloud Zone',NULL,TIMESTAMP '2016-09-19 20:46:42.785',0);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (3321,'cloud.service.Netscaler','Netscaler',3299,true,'oo::chef-11.4.0','global','Netscaler',NULL,TIMESTAMP '2015-07-29 14:16:27.117',2);

INSERT INTO md_classes (class_id,class_name,short_class_name,super_class_id,is_namespace,impl,access_level,description,format,created,flags) VALUES (3597,'base.Openstack','Openstack',100,true,'oo::chef-11.4.0','global','Compute Cloud Service',NULL,TIMESTAMP '2015-07-29 14:16:27.962',0);




INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3612,3597,'max_instances','string',false,true,false,false,false,NULL,'{"help":"Max total instances for tenant","category":"5.Quota","order":1,"editable":false}','Max Total Instances',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3609,3597,'repo_map','hash',false,true,false,false,false,'{}','{"help":"Map of repositories by OS Type containing add commands - ex) yum-config-manager --add-repo repository_url or deb http://us.archive.ubuntu.com/ubuntu/ hardy main restricted ","category":"4.Operating System","order":2}','OS Package Repositories keyed by OS Name',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3613,3597,'max_cores','string',false,true,false,false,false,NULL,'{"help":"Max total cores for tenant","category":"5.Quota","order":2,"editable":false}','Max Total Cores',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3598,3597,'endpoint','string',true,true,false,false,false,NULL,'{"help":"API Endpoint URL","category":"1.Authentication","order":1}','API Endpoint',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3599,3597,'tenant','string',true,true,false,false,false,NULL,'{"help":"Tenant Name","category":"1.Authentication","order":2}','Tenant',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3600,3597,'username','string',true,true,false,false,false,NULL,'{"help":"API Username","category":"1.Authentication","order":3}','Username',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3601,3597,'password','string',true,true,true,false,false,NULL,'{"help":"API Password","category":"1.Authentication","order":4}','Password',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3603,3597,'availability_zones','array',false,true,false,false,false,'[]','{"help":"Availability Zones - Singles will round robin, Redundant will use index","category":"2.Placement","order":2}','Availability Zones',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3604,3597,'subnet','string',false,true,false,false,false,NULL,'{"help":"Subnet Name is optional for placement of compute instances","category":"2.Placement","order":3}','Subnet Name',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3614,3597,'max_ram','string',false,true,false,false,false,NULL,'{"help":"Max total RAM size for tenant","category":"5.Quota","order":3,"editable":false}','Max Total RAM Size',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3615,3597,'max_keypairs','string',false,true,false,false,false,NULL,'{"help":"Max total keypairs for tenant","category":"5.Quota","order":4,"editable":false}','Max Total Keypairs',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3616,3597,'max_secgroups','string',false,true,false,false,false,NULL,'{"help":"Max total security groups for tenant","category":"5.Quota","order":5,"editable":false}','Max Total Security Groups',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3606,3597,'public_subnet','string',false,true,false,false,false,NULL,'{"help":"Public Subnet Name is optional for placement of compute instances","category":"2.Placement","order":5}','Public Subnet Name',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3607,3597,'sizemap','hash',false,true,false,false,false,'{ "XS":"1","S":"2","M":"3","L":"4","XL":"5","XXL":"12","3XL":"11","3XL-IO":"15" }','{"help":"Map of generic compute sizes to provider specific","category":"3.Mappings","order":1}','Sizes Map',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3610,3597,'env_vars','hash',false,true,false,false,false,'{}','{"help":"Environment variables - ex) http =\u003e http://yourproxy, https =\u003e https://yourhttpsproxy, etc","category":"4.Operating System","order":3}','System Environment Variables',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3602,3597,'region','string',false,true,false,false,false,NULL,'{"help":"Region Name","category":"2.Placement","order":1}','Region',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3605,3597,'public_network_type','string',false,true,false,false,false,'flat','{"help":"Public network type. Flat for standard openstack. Floating for needing a floating ip to be accessable.","category":"2.Placement","form":{"field":"select","options_for_select":[["Flat","flat"],["Floating","floatingip"]]},"order":4}','Public Network Type',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (16150,3597,'enabled_networks','array',false,true,false,false,false,'[]','{"help":"Enabled Network List is optional for placement of compute instances","category":"2.Placement","order":6}','Enabled Networks',TIMESTAMP '2017-03-28 19:45:12.346',TIMESTAMP '2017-03-28 19:45:12.346');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (11868,3597,'flavormap','hash',false,true,false,false,false,'{}','{"help":"Map of generic flavors to number of vcpus/ram/ephermal disk size","category":"3.Mappings","order":2}','Flavor Map',TIMESTAMP '2016-11-14 11:17:28.661',TIMESTAMP '2016-11-14 11:17:28.661');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3608,3597,'imagemap','hash',false,true,false,false,false,'{"ubuntu-14.04":"",
                "ubuntu-13.10":"",
                "ubuntu-13.04":"",
                "ubuntu-12.10":"",
                "ubuntu-12.04":"",
                "ubuntu-10.04":"",
                "redhat-7.0":"",
		"redhat-6.7":"",
		"redhat-6.6":"",
                "redhat-6.5":"",
                "redhat-6.4":"",
                "redhat-6.2":"",
                "redhat-5.9":"",
                "centos-7.0":"",
		"centos-6.7":"",
		"centos-6.6":"",
                "centos-6.5":"",
                "centos-6.4":"",
                "fedora-20":"",
                "fedora-19":""}','{"help":"Map of generic OS image types to provider specific 64-bit OS image types","category":"3.Mappings","order":3}','Images Map',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3611,3597,'ostype','string',true,true,false,false,false,'centos-6.4','{"help":"OS types are mapped to the correct cloud provider OS images - see provider documentation for details","category":"4.Operating System","order":4,"form":{"field":"select","options_for_select":[["Ubuntu 14.04.1 (trusty)","ubuntu-14.04"],["Ubuntu 13.10 (saucy)","ubuntu-13.10"],["Ubuntu 13.04 (raring)","ubuntu-13.04"],["Ubuntu 12.10 (quantal)","ubuntu-12.10"],["Ubuntu 12.04.5 (precise)","ubuntu-12.04"],["Ubuntu 10.04.4 (lucid)","ubuntu-10.04"],["RedHat 7.0","redhat-7.0"],["RedHat 7.2","redhat-7.2"],["RedHat 6.8","redhat-6.8"],["RedHat 6.7","redhat-6.7"],["RedHat 6.6","redhat-6.6"],["RedHat 6.5","redhat-6.5"],["RedHat 6.4","redhat-6.4"],["RedHat 6.2","redhat-6.2"],["RedHat 5.9","redhat-5.9"],["CentOS 7.0","centos-7.0"],["CentOS 7.2","centos-7.2"],["CentOS 6.8","centos-6.8"],["CentOS 6.7","centos-6.7"],["CentOS 6.6","centos-6.6"],["CentOS 6.5","centos-6.5"],["CentOS 6.4","centos-6.4"],["Fedora 20","fedora-20"],["Fedora 19","fedora-19"]]}}','OS Type',TIMESTAMP '2015-07-29 14:16:27.962',TIMESTAMP '2015-07-29 14:16:27.962');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3340,3339,'url','string',true,true,false,false,false,NULL,'{"help":"Nexus repository URL","category":"1.Repository","order":1}','Repository URL',TIMESTAMP '2015-07-29 14:16:27.266',TIMESTAMP '2015-07-29 14:16:27.266');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3341,3339,'repository','string',true,true,false,false,false,NULL,'{"help":"Default repository if not specified in configuration","category":"1.Repository","order":2}','Default Repository',TIMESTAMP '2015-07-29 14:16:27.266',TIMESTAMP '2015-07-29 14:16:27.266');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3342,3339,'username','string',false,true,false,false,false,NULL,'{"help":"Username to authenticate against the Nexus repository","category":"2.Authentication","order":1}','Username',TIMESTAMP '2015-07-29 14:16:27.266',TIMESTAMP '2015-07-29 14:16:27.266');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (3343,3339,'password','string',false,true,true,false,false,NULL,'{"help":"Password to authenticate against the Nexus repository","category":"2.Authentication","order":2}','Password',TIMESTAMP '2015-07-29 14:16:27.266',TIMESTAMP '2015-07-29 14:16:27.266');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1137,1136,'full_name','string',false,true,false,false,false,NULL,'{"help":"Organization long name","category":"1.Information","order":1,"pattern":"(.*){1,100}"}','Full Name',TIMESTAMP '2015-07-29 14:13:42.995',TIMESTAMP '2015-07-29 14:13:42.995');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1138,1136,'description','string',false,true,false,false,false,NULL,'{"help":"Additional information about the organization","category":"1.Information","order":2,"form":{"field":"textarea"}}','Description',TIMESTAMP '2015-07-29 14:13:42.995',TIMESTAMP '2015-07-29 14:13:42.995');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1139,1136,'owner','string',true,true,false,false,false,NULL,'{"help":"Set the email address of the admin/owner for this organization","category":"1.Information","order":3,"pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$"}','Owner Email',TIMESTAMP '2015-07-29 14:13:42.995',TIMESTAMP '2015-07-29 14:13:42.995');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (13290,1136,'tags','hash',false,true,false,false,false,'{}','{"help":"Various option key/value pairs to tag organization.","category":"1.Information","order":4}','Miscelaneous Tags',TIMESTAMP '2016-12-07 14:17:28.702',TIMESTAMP '2016-12-07 14:17:28.702');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1182,1181,'description','string',false,true,false,false,false,NULL,'{"help":"Enter description for this environment","category":"1.Global","order":1,"form":{"field":"textarea"}}','Description',TIMESTAMP '2015-07-29 14:13:43.309',TIMESTAMP '2015-07-29 14:13:43.309');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1183,1181,'definition','struct',true,true,false,false,false,'{}','{"help":"Define custom data structure to describe the query path (Note: see developer documentation for creating query paths)","category":"1.Global","order":2}','Query Path',TIMESTAMP '2015-07-29 14:13:43.309',TIMESTAMP '2015-07-29 14:13:43.309');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1156,1155,'is_active','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (11021,1155,'autocomply','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1157,1155,'availability','string',false,true,false,false,false,'default');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (15209,1155,'autorepair_exponential_backoff','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9525,1155,'autoreplace','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1158,1155,'replace_after_minutes','string',true,true,false,false,false,'9999999');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9523,1155,'autorepair','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1159,1155,'replace_after_repairs','string',true,true,false,false,false,'9999999');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (16048,1155,'fail_on_delete_failure','string',false,true,false,false,false,'[]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9524,1155,'autoscale','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1125,1121,'cmd','string',true,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1126,1121,'cmd_options','hash',true,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1127,1121,'cmd_line','string',true,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1130,1121,'heartbeat','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1131,1121,'duration','string',false,true,false,false,false,'3');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9006,8996,'deny_rules','array',false,true,false,false,false,'[]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9005,8996,'allow_rules','array',false,true,false,false,false,'["-p tcp --dport 22"]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9315,9290,'enable_method_get','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9007,8996,'nat_rules','array',false,true,false,false,false,'[]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9009,8996,'limits','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9013,8996,'pam_groupdn','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1132,1121,'thresholds','struct',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1133,1121,'chart','struct',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9316,9290,'enable_method_put','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1112,1109,'encrypted_value','string',false,true,true,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7478,7477,'url','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7479,7477,'repository','string',true,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7480,7477,'username','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7481,7477,'password','string',false,true,true,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1128,1121,'metrics','struct',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1129,1121,'sample_interval','string',false,true,false,false,false,'60');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9333,9290,'stop_time','string',false,true,false,false,false,'45');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9334,9290,'use_security_manager','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7794,7790,'ports','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7925,7924,'service_name','string',true,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (10967,8996,'applied_compliance','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8355,8354,'flavor','string',true,true,false,false,false,'openjdk');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7791,7790,'size','string',true,true,false,false,false,'S');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8356,8354,'jrejdk','string',true,true,false,false,false,'jdk');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9307,9290,'logfiles_path','string',true,true,false,false,false,'/log/apache-tomcat/');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9313,9290,'tlsv11_protocol_enabled','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8498,8497,'listeners','array',false,true,false,false,false,'["http 80 http 8080"]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9312,9290,'tlsv1_protocol_enabled','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8448,8447,'description','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8176,8175,'aliases','array',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9318,9290,'enable_method_delete','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9319,9290,'enable_method_connect','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8177,8175,'full_aliases','array',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9327,9290,'min_spare_threads','string',true,true,false,false,false,'25');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9328,9290,'java_options','string',false,true,false,false,false,'-Djava.awt.headless=true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9329,9290,'system_properties','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7482,7477,'location','string',true,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7483,7477,'version','string',true,true,false,false,false,'latest');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1122,1121,'enable','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1123,1121,'custom','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8998,8996,'hosts','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1124,1121,'description','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9341,9290,'pre_shutdown_command','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9253,9252,'description','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8357,8354,'version','string',true,true,false,false,false,'8');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9254,9252,'inbound','array',false,true,false,false,false,'[ "22 22 tcp 0.0.0.0/0" ]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9291,9290,'install_type','string',true,true,false,false,false,'repository');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7484,7477,'checksum','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7485,7477,'path','string',false,true,false,false,false,'/nexus');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7486,7477,'install_dir','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7487,7477,'as_user','string',false,true,false,false,false,'ooadmin');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8178,8175,'ttl','string',false,true,false,false,false,'60');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8179,8175,'ptr_enabled','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (14820,8175,'hijackable_full_aliases','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8359,8354,'binpath','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8361,8354,'install_dir','string',false,true,false,false,false,'/usr/lib/jvm');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7795,7790,'require_public_ip','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9010,8996,'sshd_config','hash',false,true,false,false,false,'{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7927,7924,'use_script_status','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7928,7924,'control_script_location','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8180,8175,'ptr_source','string',true,true,false,false,false,'platform');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8181,8175,'distribution','string',true,true,false,false,false,'proximity');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8502,8497,'cookie_domain','string',false,true,false,false,false,'default');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8505,8497,'lb_attrs','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8501,8497,'persistence_type','string',false,true,false,false,false,'cookieinsert');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9294,9290,'version','string',true,true,false,false,false,'7.0');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9295,9290,'build_version','string',true,true,false,false,false,'70');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9296,9290,'webapp_install_dir','string',false,true,false,false,false,'/opt/tomcat7/webapps');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8999,8996,'additional_search_domains','array',false,true,false,false,false,'[]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7492,7477,'configure','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7493,7477,'migrate','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7488,7477,'as_group','string',false,true,false,false,false,'ooadmin');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7491,7477,'should_expand','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7494,7477,'restart','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9292,9290,'tomcat_install_dir','string',true,true,false,false,false,'/opt');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9297,9290,'tomcat_user','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9298,9290,'tomcat_group','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9299,9290,'protocol','string',true,true,false,false,false,'HTTP/1.1');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9308,9290,'autodeploy_enabled','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9330,9290,'startup_params','array',false,true,false,false,false,'["+UseConcMarkSweepGC"]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9331,9290,'mem_max','string',false,true,false,false,false,'128M');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9335,9290,'policy','text',false,true,false,false,false,'grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9336,9290,'access_log_dir','string',false,true,false,false,false,'logs');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9337,9290,'access_log_prefix','string',false,true,false,false,false,'access_log');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9339,9290,'access_log_suffix','string',false,true,false,false,false,'.log');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9340,9290,'access_log_pattern','string',false,true,false,false,false,'%h %l %u %t &quot;%r&quot; %s %b %D %F');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9342,9290,'time_to_wait_before_shutdown','string',false,true,false,false,false,'30');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9343,9290,'post_startup_command','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9344,9290,'polling_frequency_post_startup_check','string',false,true,false,false,false,'1');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9345,9290,'max_number_of_retries_for_post_startup_check','string',false,true,false,false,false,'15');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8358,8354,'uversion','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8362,8354,'sysdefault','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9002,8996,'iptables_enabled','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9003,8996,'drop_policy','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8499,8497,'lbmethod','string',true,true,false,false,false,'roundrobin');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8500,8497,'stickiness','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9322,9290,'enable_method_trace','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9458,9457,'size','string',true,true,false,false,false,'100%FREE');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9323,9290,'server_header_attribute','string',true,true,false,false,false,'web');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9324,9290,'enable_error_report_valve','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9459,9457,'device','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (12285,9457,'mode','string',false,true,false,false,false,'no-raid');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8997,8996,'repo_list','array',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9000,8996,'proxy_map','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9332,9290,'mem_start','string',false,true,false,false,false,'128M');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9001,8996,'dhclient','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9004,8996,'allow_loopback','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9008,8996,'timezone','string',false,true,false,false,false,'UTC');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9011,8996,'sysctl','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9012,8996,'env_vars','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (10233,7924,'secondary_down','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9338,9290,'access_log_file_date_format','string',false,true,false,false,false,'yyyy-MM-dd');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8503,8497,'enable_lb_group','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8504,8497,'create_cloud_level_vips','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8506,8497,'required_availability_zone','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8507,8497,'ecv_map','hash',true,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8508,8497,'servicegroup_attrs','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9301,9290,'advanced_connector_config','hash',true,true,false,false,false,'{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9304,9290,'server_port','string',true,true,false,false,false,'8005');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9305,9290,'ajp_port','string',true,true,false,false,false,'8009');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9302,9290,'port','string',true,true,false,false,false,'8080');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9306,9290,'environment','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9462,9457,'options','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9460,9457,'fstype','string',false,true,false,false,false,'ext3');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9461,9457,'mount_point','string',true,true,false,false,false,'/volume');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9293,9290,'mirrors','array',false,true,false,false,false,'["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9300,9290,'http_connector_enabled','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9303,9290,'ssl_port','string',true,true,false,false,false,'8443');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9309,9290,'context_enabled','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9310,9290,'context_tomcat','text',false,true,false,false,false,'<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9311,9290,'advanced_security_options','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9314,9290,'tlsv12_protocol_enabled','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9317,9290,'enable_method_post','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7489,7477,'environment','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7490,7477,'persist','array',false,true,false,false,false,'[]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7926,7924,'pattern','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7929,7924,'control_script_content','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (10584,8996,'ostype','string',true,true,false,false,false,'centos-7.2');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (10585,8996,'image_id','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9320,9290,'enable_method_options','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9321,9290,'enable_method_head','string',false,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9325,9290,'executor_name','string',true,true,false,false,false,'tomcatThreadPool');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9326,9290,'max_threads','string',true,true,false,false,false,'50');



INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1067,1066,'secure','string',false,true,false,false,false,'false','{"category":"Value","order":1,"tip":"NOTE: Make sure to always re-enter the variable value when changing this attribute.","help":"Secure variable values are encrypted on save and stored in encrypted format.","form":{"field":"checkbox"}}','Secure variable',TIMESTAMP '2015-07-29 14:13:42.364',TIMESTAMP '2015-07-29 14:13:42.364');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1068,1066,'value','string',false,true,false,false,false,NULL,'{"category":"Value","pattern":"\\S(.*\\S)?","order":2,"help":"Enter the variable value","form":{"field":"textarea"},"filter":{"all":{"visible":"secure:neq:true","editable":"secure:neq:true"}}}','Value',TIMESTAMP '2015-07-29 14:13:42.364',TIMESTAMP '2015-07-29 14:13:42.364');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1069,1066,'encrypted_value','string',false,true,true,false,false,NULL,'{"category":"Value","pattern":"\\S(.*\\S)?","order":3,"help":"Enter the variable value. The provided value will be encrypted on save and stored securely.","filter":{"all":{"visible":"secure:eq:true","editable":"secure:eq:true"}}}','Encrypted value',TIMESTAMP '2015-07-29 14:13:42.364',TIMESTAMP '2015-07-29 14:13:42.364');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1094,1093,'secure','string',false,true,false,false,false,'false','{"category":"Value","order":1,"tip":"NOTE: Make sure to always re-enter the variable value when changing this attribute.","help":"Secure variable values are encrypted on save and stored in encrypted format.","form":{"field":"checkbox"}}','Secure variable',TIMESTAMP '2015-07-29 14:13:42.569',TIMESTAMP '2015-07-29 14:13:42.569');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1095,1093,'value','string',false,true,false,false,false,NULL,'{"category":"Value","pattern":"\\S(.*\\S)?","order":2,"help":"Enter the variable value","form":{"field":"textarea"},"filter":{"all":{"visible":"secure:neq:true","editable":"secure:neq:true"}}}','Value',TIMESTAMP '2015-07-29 14:13:42.569',TIMESTAMP '2015-07-29 14:13:42.569');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1096,1093,'encrypted_value','string',false,true,true,false,false,NULL,'{"category":"Value","pattern":"\\S(.*\\S)?","order":3,"help":"Enter the variable value. The provided value will be encrypted on save and stored securely.","filter":{"all":{"visible":"secure:eq:true","editable":"secure:eq:true"}}}','Encrypted value',TIMESTAMP '2015-07-29 14:13:42.569',TIMESTAMP '2015-07-29 14:13:42.569');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1110,1109,'secure','string',false,true,false,false,false,'false','{"category":"Value","order":1,"tip":"NOTE: Make sure to always re-enter the variable value when changing this attribute.","help":"Secure variable values are encrypted on save and stored in encrypted format.","form":{"field":"checkbox"}}','Secure variable',TIMESTAMP '2015-07-29 14:13:42.741',TIMESTAMP '2015-07-29 14:13:42.741');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1111,1109,'value','string',false,true,false,false,false,NULL,'{"category":"Value","pattern":"\\S(.*\\S)?","order":2,"help":"Enter the variable value. You can reference this variable in component attribute values as $OO_LOCAL{varname}","form":{"field":"textarea"},"filter":{"all":{"visible":"secure:neq:true","editable":"secure:neq:true"}}}','Value',TIMESTAMP '2015-07-29 14:13:42.741',TIMESTAMP '2015-07-29 14:13:42.741');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1091,1090,'profile','struct',false,true,false,false,false,NULL,'{"help":"Organization environment profile used for this environment","category":"1.Global","order":1,"filter":{"new":{"visible":"availability:eq:none"}}}','Profile',TIMESTAMP '2015-07-29 14:13:42.526',TIMESTAMP '2015-07-29 14:13:42.526');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1092,1090,'adminstatus','string',true,true,false,false,false,'preparation','{"category":"1.Global","order":2,"help":"Select administrative status","form":{"field":"select","options_for_select":[["Provision","provision"],["Active","active"],["Inactive","inactive"],["Decommission","decommission"]]}}','Administrative Status',TIMESTAMP '2015-07-29 14:13:42.526',TIMESTAMP '2015-07-29 14:13:42.526');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1078,1074,'dpmtdelay','string',false,true,false,false,false,'60','{"category":"2. Deployment","order":2,"help":"Delay in seconds for an automated deployment to start after an update was received and a new release was created","pattern":"[0-9]+","filter":{"all":{"visible":"false"}}}','Continuous Deployment Delay',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1082,1074,'autorepair','string',true,true,false,false,false,'true','{"category":"4.Availability","order":2,"filter":{"all":{"visible":"false"}},"help":"Autorepair enables automatic repair of component instances based on monitors with enabled heartbeats and metrics you define with Unhealthy event triggers (Note: repairs are executed by invoking and action with the name \u003cem\u003erepair\u003c/em\u003e for each component)","form":{"field":"checkbox"}}','Auto Repair',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1079,1074,'subdomain','struct',false,true,false,false,false,NULL,'{"help":"Modify the full subdomain name to ensure uniqueness of the platform entrypoint names in your environment","category":"3.DNS","order":1,"pattern":"[a-zA-Z0-9-]+(.[a-zA-Z0-9-]+)","editable":false}','DNS Subdomain',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1083,1074,'autoscale','string',true,true,false,false,false,'false','{"category":"4.Availability","order":3,"filter":{"all":{"visible":"false"}},"tip":"NOTE: Autoscale will apply only to platforms running in redundant mode in this environment.","help":"Autoscales enables scaling up and down based on metrics you define with Over-Utilized and Under-Utilized event triggers (Note: the scale step up and down along with the limits and metrics can be customized on a platform level after saving the environment)","form":{"field":"checkbox"}}','Auto Scale',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (15295,1074,'debug','string',true,true,false,false,false,'false','{"category":"5.Other","order":1,"help":"For developers troubleshooting","form":{"field":"checkbox"},"filter":{"all":{"visible":"false"}}}','Debug Mode',TIMESTAMP '2017-01-17 20:50:07.676',TIMESTAMP '2017-01-17 20:50:07.676');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1081,1074,'monitoring','string',true,true,false,false,false,'true','{"category":"4.Availability","order":1,"help":"This disables monitoring and prevents deployment of the monitoring agent software (nagios and flume) on each compute instance (Note: autorepair and autoscale will not function properly without monitoring enabled)","editable":false,"form":{"field":"checkbox"},"filter":{"all":{"visible":"availability:eq:none"}}}','Monitoring',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1075,1074,'description','string',false,true,false,false,false,NULL,'{"help":"Enter description for this environment","category":"1.Global","order":3,"form":{"field":"textarea"}}','Description',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1080,1074,'global_dns','string',false,true,false,false,false,'false','{"help":"Create global DNS names when using multiple clouds","category":"3.DNS","order":2,"form":{"field":"checkbox"}}','Global DNS',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1087,1074,'logging','string',true,true,false,false,false,'false','{"category":"5.Other","order":2,"help":"To enable logging","form":{"field":"checkbox"},"filter":{"all":{"visible":"availability:eq:none"}}}','Enable Logging',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1077,1074,'codpmt','string',true,true,false,false,false,'false','{"category":"2. Deployment","order":1,"help":"Enable continuous deployment by using webhooks (for ex. integration with Project and SCM tools) or our API to update components and let our system automatically deploy those updates","form":{"field":"checkbox"},"filter":{"all":{"visible":"false"}}}','Continuous Deployment',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1084,1074,'autoreplace','string',true,true,false,false,false,'false','{"category":"4.Availability","order":4,"tip":"NOTE: Autoreplace works only if the auto-repair is ON for this environment. You also need to set valid values for the replace related 2 attributes in your platform configuration in order to enable auto-replace for that particular platform","filter":{"all":{"visible":"false"}},"help":"Replaces an unhealthy component  after some duration if the repair action can not recover the component","form":{"field":"checkbox"}}','Auto Replace',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1085,1074,'availability','string',true,true,false,false,false,'single','{"category":"4.Availability","order":5,"editable":false,"help":"The Availability Mode \u003cb\u003eSingle\u003c/b\u003e will generate an environment without loadbalancers, clusters, etc.\n \u003cb\u003eRedundant\u003c/b\u003e will insert and configure clusters, loadbalancers, rings, etc depending on whats best practice for each platform.\n \u003cb\u003eHigh-Availability\u003c/b\u003e will add multi-provider or multi-region to a redundant environment.\n You can change availability mode on a per-platform basis below.","form":{"field":"select","options_for_select":[["Single","single"],["Redundant","redundant"]]}}','Availability Mode',TIMESTAMP '2015-07-29 14:13:42.473',TIMESTAMP '2015-07-29 14:13:42.473');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1060,1059,'location','struct',true,true,false,false,false,NULL,'{"important":true,"editable":false,"help":"Management cloud location path for delivering workorders","category":"2.Management","order":1}','Location',TIMESTAMP '2015-07-29 14:13:42.283',TIMESTAMP '2015-07-29 14:13:42.283');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (16502,1058,'is_location','string',false,true,false,false,false,'false','{"help":"If set to true it will show up in locations drop down","category":"2.Management","order":3}','Location flag',TIMESTAMP '2017-04-25 21:39:13.206',TIMESTAMP '2017-04-25 21:39:13.206');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (7000,1055,'adminstatus','string',false,true,false,false,false,'active','{"important":true,"help":"Indicates admin status of the cloud. Possible values: ''active'' - normal operations, ''inert'' - being phazed out and may not be added to environments, ''offine'' - decomissioned and should not be used.","category":"1.Global","order":2,"form":{"field":"select","options_for_select":[["Active (normal operations)","active"],["Inert (being phased out)","inert"],["Offline (decommissioned)","offline"]]},"filter":{"all":{"visible":"false"}}}','Status',TIMESTAMP '2015-10-23 17:44:51.102',TIMESTAMP '2015-10-23 17:44:51.102');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1056,1055,'description','string',false,true,false,false,false,NULL,'{"help":"Description","category":"1.Global","order":1}','Description',TIMESTAMP '2015-07-29 14:13:42.217',TIMESTAMP '2015-07-29 14:13:42.217');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1057,1055,'auth','struct',false,true,false,false,false,NULL,'{"help":"Authorization key for the specified location path","category":"2.Management","order":2}','Authorization Key',TIMESTAMP '2015-07-29 14:13:42.217',TIMESTAMP '2015-07-29 14:13:42.217');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1036,1035,'description','string',false,true,false,false,false,NULL,'{"help":"Assembly description","category":"1.Configuration","order":1}','Description',TIMESTAMP '2015-07-29 14:13:42.039',TIMESTAMP '2015-07-29 14:13:42.039');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1037,1035,'owner','string',true,true,false,false,false,NULL,'{"help":"Set the email address of the owner of this assembly","category":"1.Configuration","order":2,"pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$"}','Owner (Email Address)',TIMESTAMP '2015-07-29 14:13:42.039',TIMESTAMP '2015-07-29 14:13:42.039');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (13288,1035,'tags','hash',false,true,false,false,false,'{}','{"help":"Various option key/value pairs to tag assembly.","category":"1.Configuration","order":3}','Miscelaneous Tags',TIMESTAMP '2016-12-07 14:17:27.866',TIMESTAMP '2016-12-07 14:17:27.866');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1161,1160,'is_active','string',false,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1163,1160,'replace_after_minutes','string',true,true,false,false,false,'9999999');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (11022,1160,'autocomply','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1162,1160,'availability','string',false,true,false,false,false,'default');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7834,7815,'dns_record','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (1164,1160,'replace_after_repairs','string',true,true,false,false,false,'9999999');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9527,1160,'autoscale','string',true,true,false,false,false,'false');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (15210,1160,'autorepair_exponential_backoff','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7816,7815,'instance_name','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7817,7815,'instance_id','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8197,8196,'entries','hash',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8198,8196,'gslb_vnames','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7818,7815,'host_id','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8463,8462,'fingerprint','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7822,7815,'tags','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9030,9029,'hostname','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9267,9266,'group_id','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8464,8462,'certificate','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8465,8462,'key_name','text',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8534,8533,'vnames','hash',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7824,7815,'task_state','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7840,7839,'required_availability_zone','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7825,7815,'vm_state','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7823,7815,'instance_state','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7826,7815,'cores','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9031,9029,'tags','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9032,9029,'osname','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7827,7815,'ram','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9526,1160,'autorepair','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7819,7815,'hypervisor','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8535,8533,'dns_record','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8536,8533,'availability_zone','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (8537,8533,'inames','array',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (16049,1160,'fail_on_delete_failure','string',false,true,false,false,false,'[]');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9268,9266,'group_name','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7832,7815,'private_dns','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7833,7815,'public_dns','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7828,7815,'server_image_name','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7829,7815,'server_image_id','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7830,7815,'private_ip','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7831,7815,'public_ip','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (9528,1160,'autoreplace','string',true,true,false,false,false,'true');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (10519,8462,'private','text',true,true,true,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (10520,8462,'public','text',true,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7820,7815,'availability_zone','string',false,true,false,false,false,NULL);

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value) VALUES (7821,7815,'metadata','hash',false,true,false,false,false,'{}');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1152,1147,'major_version','string',false,true,false,false,false,'1','{"help":"Major version of the platform should only be increased if you are doing a full \u003cem\u003eupgrade\u003c/em\u003e and replacing all component instances","category":"3.Version","order":1}','Version',TIMESTAMP '2015-07-29 14:13:43.082',TIMESTAMP '2015-07-29 14:13:43.082');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1148,1147,'description','string',false,true,false,false,false,NULL,'{"help":"Description","category":"1.Global","order":1}','Description',TIMESTAMP '2015-07-29 14:13:43.082',TIMESTAMP '2015-07-29 14:13:43.082');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1149,1147,'source','string',false,true,false,false,false,NULL,'{"help":"Pack source name","category":"2.Platform Pack","order":1}','Pack Source',TIMESTAMP '2015-07-29 14:13:43.082',TIMESTAMP '2015-07-29 14:13:43.082');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1150,1147,'pack','string',false,true,false,false,false,NULL,'{"help":"Pack name","category":"2.Platform Pack","order":2}','Pack Name',TIMESTAMP '2015-07-29 14:13:43.082',TIMESTAMP '2015-07-29 14:13:43.082');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (1151,1147,'version','string',false,true,false,false,false,NULL,'{"help":"Pack version","category":"2.Platform Pack","order":3}','Pack Version',TIMESTAMP '2015-07-29 14:13:43.082',TIMESTAMP '2015-07-29 14:13:43.082');

INSERT INTO md_class_attributes (attribute_id,class_id,attribute_name,data_type,is_mandatory,is_inheritable,is_encrypted,is_immutable,force_on_dependent,default_value,value_format,description,created,updated) VALUES (13291,1147,'pack_digest','string',false,true,false,false,false,NULL,'{"help":"Current pack digest","filter":{"all":{"visible":"false"}},"category":"2.Platform Pack","order":4}','Current pack digest',TIMESTAMP '2016-12-07 14:17:28.785',TIMESTAMP '2016-12-07 14:17:28.785');




INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6478,'manifest.ValueFor','ValueFor','Relation between variables and targets',TIMESTAMP '2015-07-29 14:27:34.290');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1296,'manifest.ComposedOf','ComposedOf','Relation between assembly and platform',TIMESTAMP '2015-07-29 14:13:45.184');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1300,'base.Consumes','Consumes','Relation to indicate cloud usage',TIMESTAMP '2015-07-29 14:13:45.223');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1317,'bom.DependsOn','DependsOn','Dependency relation between platform resources',TIMESTAMP '2015-07-29 14:13:45.378');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1355,'manifest.DependsOn','DependsOn','Dependency relation between platform resources',TIMESTAMP '2015-07-29 14:13:45.458');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1367,'base.DeployedTo','DeployedTo','Dependency relation from platform resources to provider binding',TIMESTAMP '2015-07-29 14:13:45.481');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1371,'base.Entrypoint','Entrypoint','Relation between platform and the entrypoint resources',TIMESTAMP '2015-07-29 14:13:45.513');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6490,'manifest.WatchedBy','WatchedBy','Pack resources watched by monitors',TIMESTAMP '2015-07-29 14:27:34.433');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1373,'manifest.Entrypoint','Entrypoint','Relation between platform and the entrypoint resources',TIMESTAMP '2015-07-29 14:13:45.525');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6393,'bom.ManagedVia','ManagedVia','Configuration proxy relation',TIMESTAMP '2015-07-29 14:27:33.477');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6399,'manifest.ManagedVia','ManagedVia','Configuration proxy relation',TIMESTAMP '2015-07-29 14:27:33.515');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6414,'base.RealizedAs','RealizedAs','Relation between manifest and bom resources',TIMESTAMP '2015-07-29 14:27:33.651');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6427,'manifest.Requires','Requires','Relation between platform and the required resources',TIMESTAMP '2015-07-29 14:27:33.723');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6449,'bom.SecuredBy','SecuredBy','Key pairs used for securing environments and other resources',TIMESTAMP '2015-07-29 14:27:33.835');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6446,'manifest.SecuredBy','SecuredBy','Key pairs used for securing environments and other resources',TIMESTAMP '2015-07-29 14:27:33.814');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6418,'base.RealizedIn','RealizedIn','Relation between assembly and environment',TIMESTAMP '2015-07-29 14:27:33.662');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6402,'base.Manages','Manages','Relation between organization and assembly',TIMESTAMP '2015-07-29 14:27:33.537');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6486,'mgmt.manifest.WatchedBy','WatchedBy','Pack resources watched by monitors',TIMESTAMP '2015-07-29 14:27:34.397');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1331,'mgmt.manifest.DependsOn','DependsOn','Dependency relation between platform resources',TIMESTAMP '2015-07-29 14:13:45.415');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1375,'mgmt.Entrypoint','Entrypoint','Relation between platform and the entrypoint resources',TIMESTAMP '2015-07-29 14:13:45.537');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6396,'mgmt.manifest.ManagedVia','ManagedVia','Configuration proxy relation',TIMESTAMP '2015-07-29 14:27:33.496');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6406,'mgmt.manifest.Payload','Payload','Relation between platform components and query path in templates',TIMESTAMP '2015-07-29 14:27:33.592');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6453,'mgmt.manifest.SecuredBy','SecuredBy','Key pairs used for securing environments and other resources',TIMESTAMP '2015-07-29 14:27:33.855');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6434,'mgmt.Requires','Requires','Relation between platform and the required resources',TIMESTAMP '2015-07-29 14:27:33.751');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (1291,'base.CompliesWith','CompliesWith','Links cloud to compliance',TIMESTAMP '2015-07-29 14:13:45.132');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6481,'account.ValueFor','ValueFor','Relation between variables and targets',TIMESTAMP '2015-07-29 14:27:34.343');

INSERT INTO md_relations (relation_id,relation_name,short_relation_name,description,created) VALUES (6408,'base.Provides','Provides','Provider provides zones',TIMESTAMP '2015-07-29 14:27:33.618');



INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,default_value,value_format,description,created,updated) VALUES (6409,6408,'service','string',false,false,NULL,'{"help":"Type of cloud service","category":"1.Global","order":1}','Cloud Service Type',TIMESTAMP '2015-07-29 14:27:33.618',TIMESTAMP '2015-07-29 14:27:33.618');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,default_value,value_format,description,created,updated) VALUES (6864,6408,'template_ns','string',false,false,NULL,'{"help":"Namespace of location where base template is stored.","category":"1.Global","order":2}','Base template namespace',TIMESTAMP '2015-10-23 17:21:45.967',TIMESTAMP '2015-10-23 17:21:45.967');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1336,1331,'min','string',true,false,'Minimum Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1337,1331,'current','string',true,false,'Current Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1338,1331,'max','string',true,false,'Maximum Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1339,1331,'pct_dpmt','string',false,false,'Deployment Percentage');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1340,1331,'source','string',false,false,'Source');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1332,1331,'flex','string',true,false,'Flexible Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1333,1331,'converge','string',false,false,'Converge');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1334,1331,'step_up','string',true,false,'Scale Up Step');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1335,1331,'step_down','string',true,false,'Scale Down Step');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6487,6486,'docUrl','string',false,false,'URL to a page having resolution or escalation details');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6488,6486,'notifyOnlyOnStateChange','string',false,false,'Recieve Email Notifications only On state change. ');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1341,1331,'propagate_to','string',false,false,'Propagate Change to one or both directions');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (14409,6486,'source','string',false,false,'Source');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6435,6434,'services','string',false,false,'Cloud Services Required');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6436,6434,'priority','string',false,false,'Deployment Priority');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6437,6434,'constraint','string',true,false,'Cardinality Constraint');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6438,6434,'template','string',true,false,'Management Template');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6439,6434,'help','string',false,false,'Help');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1303,1300,'priority','string',false,false,'Priority');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1358,1355,'step_up','string',true,false,'Scale Up Step');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6428,6427,'services','string',false,false,'Cloud Services Required');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1359,1355,'step_down','string',true,false,'Scale Down Step');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1360,1355,'min','string',true,false,'Minimum Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1361,1355,'current','string',true,false,'Current Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1362,1355,'max','string',true,false,'Maximum Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1363,1355,'pct_dpmt','string',false,false,'Deployment Percentage');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6415,6414,'last_manifest_rfc','string',false,false,'Last Manifest RFC');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6416,6414,'priority','string',true,false,'Priority');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6429,6427,'priority','string',false,false,'Deployment Priority');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1364,1355,'source','string',false,false,'Source');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1365,1355,'propagate_to','string',false,false,'Propagate Change to one or both directions');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1368,1367,'priority','string',true,false,'Priority');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1356,1355,'flex','string',true,false,'Flexible Capacity');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1357,1355,'converge','string',false,false,'Converge');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1302,1300,'pct_scale','string',false,false,'Percent scale');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1304,1300,'adminstatus','string',false,false,'admin status');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6491,6490,'docUrl','string',false,false,'URL to a page having resolution or escalation details');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1297,1296,'enabled','boolean',true,false,'Enabled');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (1301,1300,'dpmt_order','string',false,false,'Deployment order');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6430,6427,'constraint','string',true,false,'Cardinality Constraint');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6431,6427,'template','string',true,false,'Management Template');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (6492,6490,'notifyOnlyOnStateChange','string',false,false,'Recieve Email Notifications only On state change. ');

INSERT INTO md_relation_attributes (attribute_id,relation_id,attribute_name,data_type,is_mandatory,is_encrypted,description) VALUES (14411,6490,'source','string',false,false,'Source');


INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16507,1244,1282,1054,true,'one-to-many','account.provider.ec2.Token-base.Authenticates-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:17.957');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16508,1253,1282,1054,true,'one-to-many','account.provider.rackspace.Token-base.Authenticates-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:17.957');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16509,1250,1282,1054,true,'one-to-many','account.provider.openstack.Token-base.Authenticates-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:17.957');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16510,1247,1282,1054,true,'one-to-many','account.provider.cloudstack.Token-base.Authenticates-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:17.957');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16511,1090,1287,1089,false,'one-to-one','manifest.Environment-base.BasedOn-account.Environment',TIMESTAMP '2017-04-25 21:39:17.996');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16512,1054,1289,11368,false,'one-to-one','account.provider.Binding-base.BindsTo-account.provider.Zone',TIMESTAMP '2017-04-25 21:39:18.017');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16513,1073,1292,1154,true,'one-to-many','account.Design-base.ComposedOf-catalog.Platform',TIMESTAMP '2017-04-25 21:39:18.078');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16514,1039,1292,1160,true,'one-to-many','account.Assembly-base.ComposedOf-manifest.Platform',TIMESTAMP '2017-04-25 21:39:18.078');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16515,1039,1292,1154,true,'one-to-many','account.Assembly-base.ComposedOf-catalog.Platform',TIMESTAMP '2017-04-25 21:39:18.078');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16516,1090,1296,1160,true,'one-to-many','manifest.Environment-manifest.ComposedOf-manifest.Platform',TIMESTAMP '2017-04-25 21:39:18.107');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16517,1090,1296,1108,true,'one-to-many','manifest.Environment-manifest.ComposedOf-manifest.Iaas',TIMESTAMP '2017-04-25 21:39:18.107');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16518,1089,1300,1059,false,'one-to-many','account.Environment-base.Consumes-account.Cloud',TIMESTAMP '2017-04-25 21:39:18.132');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16519,1090,1300,1059,false,'one-to-many','manifest.Environment-base.Consumes-account.Cloud',TIMESTAMP '2017-04-25 21:39:18.132');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16520,1160,1300,1059,false,'one-to-many','manifest.Platform-base.Consumes-account.Cloud',TIMESTAMP '2017-04-25 21:39:18.132');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16521,1155,1309,1175,false,'one-to-many','mgmt.manifest.Platform-mgmt.manifest.ControlledBy-mgmt.manifest.Procedure',TIMESTAMP '2017-04-25 21:39:18.161');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16522,1089,1311,1199,false,'one-to-many','account.Environment-account.Delivers-account.relay.email.Relay',TIMESTAMP '2017-04-25 21:39:18.183');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16523,1090,1311,1201,false,'one-to-many','manifest.Environment-manifest.Delivers-manifest.relay.email.Relay',TIMESTAMP '2017-04-25 21:39:18.183');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16524,200,1317,200,false,'one-to-one','Component-bom.DependsOn-Component',TIMESTAMP '2017-04-25 21:39:18.234');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16525,200,1319,200,false,'one-to-one','Component-mgmt.catalog.DependsOn-Component',TIMESTAMP '2017-04-25 21:39:18.248');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16526,200,1331,200,false,'one-to-one','Component-mgmt.manifest.DependsOn-Component',TIMESTAMP '2017-04-25 21:39:18.287');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16527,200,1343,200,false,'one-to-one','Component-catalog.DependsOn-Component',TIMESTAMP '2017-04-25 21:39:18.430');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16528,200,1355,200,false,'one-to-one','Component-manifest.DependsOn-Component',TIMESTAMP '2017-04-25 21:39:18.452');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16529,200,1367,1054,false,'many-to-one','Component-base.DeployedTo-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:18.473');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16530,200,1367,1059,false,'many-to-one','Component-base.DeployedTo-account.Cloud',TIMESTAMP '2017-04-25 21:39:18.473');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16531,200,1371,200,false,'one-to-one','Component-base.Entrypoint-Component',TIMESTAMP '2017-04-25 21:39:18.487');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16532,200,1373,200,false,'one-to-one','Component-manifest.Entrypoint-Component',TIMESTAMP '2017-04-25 21:39:18.496');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16533,200,1377,1051,true,'one-to-many','Component-catalog.EscortedBy-catalog.Attachment',TIMESTAMP '2017-04-25 21:39:18.557');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16534,200,1379,1052,true,'one-to-many','Component-manifest.EscortedBy-manifest.Attachment',TIMESTAMP '2017-04-25 21:39:18.565');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16535,1244,1381,1180,false,'one-to-one','account.provider.ec2.Token-base.Exposes-account.Provider',TIMESTAMP '2017-04-25 21:39:18.576');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16536,1253,1381,1180,false,'one-to-one','account.provider.rackspace.Token-base.Exposes-account.Provider',TIMESTAMP '2017-04-25 21:39:18.576');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16537,1247,1381,1180,false,'one-to-one','account.provider.cloudstack.Token-base.Exposes-account.Provider',TIMESTAMP '2017-04-25 21:39:18.576');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16538,1250,1381,1180,false,'one-to-one','account.provider.openstack.Token-base.Exposes-account.Provider',TIMESTAMP '2017-04-25 21:39:18.576');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16539,1140,1386,1220,false,'one-to-many','account.Organization-base.ForwardsTo-account.notification.sns.Sink',TIMESTAMP '2017-04-25 21:39:18.593');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16540,1140,1386,1224,false,'one-to-many','account.Organization-base.ForwardsTo-account.notification.url.Sink',TIMESTAMP '2017-04-25 21:39:18.593');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16541,1154,1390,1154,false,'many-to-many','catalog.Platform-catalog.LinksTo-catalog.Platform',TIMESTAMP '2017-04-25 21:39:18.631');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16542,1160,1392,1160,false,'many-to-many','manifest.Platform-manifest.LinksTo-manifest.Platform',TIMESTAMP '2017-04-25 21:39:18.639');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16543,200,1395,1119,true,'one-to-one','Component-mgmt.manifest.LoggedBy-mgmt.manifest.Log',TIMESTAMP '2017-04-25 21:39:18.658');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16544,200,1397,1120,true,'one-to-one','Component-manifest.LoggedBy-manifest.Log',TIMESTAMP '2017-04-25 21:39:18.667');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16545,200,1399,200,false,'one-to-one','Component-base.ManagedVia-Component',TIMESTAMP '2017-04-25 21:39:18.678');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16546,200,6393,200,false,'one-to-one','Component-bom.ManagedVia-Component',TIMESTAMP '2017-04-25 21:39:18.687');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16547,200,6396,200,false,'one-to-one','Component-mgmt.manifest.ManagedVia-Component',TIMESTAMP '2017-04-25 21:39:18.695');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16548,200,6399,200,false,'one-to-one','Component-manifest.ManagedVia-Component',TIMESTAMP '2017-04-25 21:39:18.703');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16549,1140,6402,1039,true,'one-to-many','account.Organization-base.Manages-account.Assembly',TIMESTAMP '2017-04-25 21:39:18.714');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16550,1140,6402,1073,true,'one-to-many','account.Organization-base.Manages-account.Design',TIMESTAMP '2017-04-25 21:39:18.714');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16551,200,6406,1184,false,'one-to-many','Component-mgmt.manifest.Payload-mgmt.manifest.Qpath',TIMESTAMP '2017-04-25 21:39:18.747');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16552,200,11500,11454,false,'many-to-one','Component-base.PlacedIn-cloud.Zone',TIMESTAMP '2017-04-25 21:39:18.762');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16553,1180,6408,1191,true,'one-to-many','account.Provider-base.BindsTo-account.provider.Region',TIMESTAMP '2017-04-25 21:39:18.820');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16554,1180,6408,11368,true,'one-to-many','account.Provider-base.BindsTo-account.provider.Zone',TIMESTAMP '2017-04-25 21:39:18.820');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16555,200,6414,200,false,'one-to-many','Component-base.RealizedAs-Component',TIMESTAMP '2017-04-25 21:39:18.852');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16556,1039,6418,1090,true,'one-to-many','account.Assembly-base.RealizedIn-manifest.Environment',TIMESTAMP '2017-04-25 21:39:18.864');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16557,1154,6420,200,true,'one-to-one','catalog.Platform-base.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.883');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16558,1106,6420,200,true,'one-to-one','catalog.Iaas-base.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.883');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16559,1160,6427,200,true,'one-to-one','manifest.Platform-manifest.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.900');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16560,1108,6427,200,true,'one-to-one','manifest.Iaas-manifest.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.900');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16561,1153,6434,200,true,'one-to-one','mgmt.catalog.Platform-mgmt.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.913');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16562,1105,6434,200,true,'one-to-one','mgmt.catalog.Iaas-mgmt.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.913');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16563,1155,6434,200,true,'one-to-one','mgmt.manifest.Platform-mgmt.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.913');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16564,1107,6434,200,true,'one-to-one','mgmt.manifest.Iaas-mgmt.Requires-Component',TIMESTAMP '2017-04-25 21:39:18.913');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16566,200,6449,200,false,'one-to-one','Component-bom.SecuredBy-Component',TIMESTAMP '2017-04-25 21:39:19.028');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16567,200,6451,200,false,'one-to-one','Component-mgmt.catalog.SecuredBy-Component',TIMESTAMP '2017-04-25 21:39:19.036');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16568,200,6453,200,false,'one-to-one','Component-mgmt.manifest.SecuredBy-Component',TIMESTAMP '2017-04-25 21:39:19.045');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16569,200,6455,200,false,'one-to-one','Component-catalog.SecuredBy-Component',TIMESTAMP '2017-04-25 21:39:19.053');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16570,200,6446,200,false,'one-to-one','Component-manifest.SecuredBy-Component',TIMESTAMP '2017-04-25 21:39:19.060');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16571,1059,6463,1242,false,'one-to-many','account.Cloud-cloud.SupportedBy-cloud.Support',TIMESTAMP '2017-04-25 21:39:19.114');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16572,1090,6465,1054,false,'one-to-many','manifest.Environment-base.Utilizes-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:19.158');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16573,1108,6465,1054,false,'one-to-one','manifest.Iaas-base.Utilizes-account.provider.Binding',TIMESTAMP '2017-04-25 21:39:19.158');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16574,1097,6470,1039,false,'many-to-one','catalog.Globalvar-base.ValueFor-account.Assembly',TIMESTAMP '2017-04-25 21:39:19.175');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16575,1113,6472,1153,false,'many-to-one','mgmt.catalog.Localvar-mgmt.catalog.ValueFor-mgmt.catalog.Platform',TIMESTAMP '2017-04-25 21:39:19.189');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16576,1114,6474,1155,false,'many-to-one','mgmt.manifest.Localvar-mgmt.manifest.ValueFor-mgmt.manifest.Platform',TIMESTAMP '2017-04-25 21:39:19.202');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16577,1115,6476,1154,false,'many-to-one','catalog.Localvar-catalog.ValueFor-catalog.Platform',TIMESTAMP '2017-04-25 21:39:19.220');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16578,1098,6478,1090,false,'many-to-one','manifest.Globalvar-manifest.ValueFor-manifest.Environment',TIMESTAMP '2017-04-25 21:39:19.238');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16579,1116,6478,1160,false,'many-to-one','manifest.Localvar-manifest.ValueFor-manifest.Platform',TIMESTAMP '2017-04-25 21:39:19.238');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16580,1070,6481,1059,false,'many-to-one','account.Cloudvar-account.ValueFor-account.Cloud',TIMESTAMP '2017-04-25 21:39:19.266');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16581,200,6486,1134,true,'one-to-many','Component-mgmt.manifest.WatchedBy-mgmt.manifest.Monitor',TIMESTAMP '2017-04-25 21:39:19.301');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16582,200,6490,1135,true,'one-to-many','Component-manifest.WatchedBy-manifest.Monitor',TIMESTAMP '2017-04-25 21:39:19.330');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16583,200,13198,12322,true,'one-to-many','Component-mgmt.catalog.WatchedBy-mgmt.catalog.Monitor',TIMESTAMP '2017-04-25 21:39:19.346');

INSERT INTO md_class_relations (link_id,from_class_id,relation_id,to_class_id,is_strong,link_type,description,created) VALUES (16584,200,13202,12323,true,'one-to-many','Component-catalog.WatchedBy-catalog.Monitor',TIMESTAMP '2017-04-25 21:39:19.363');




INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268357,2268356,1160,'srv1','2268356-1160-2268357','',100,1990188,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268359,2268356,1135,'srv1-compute-ssh','2268356-1135-2268359',NULL,100,1990208,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268360,2268356,7839,'compute','2268356-7839-2268360',NULL,100,1990221,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268362,2268356,1135,'srv1-artifact-exceptions','2268356-1135-2268362',NULL,100,1990229,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268363,2268356,7506,'artifact','2268356-7506-2268363','',100,1990242,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268365,2268356,1135,'srv1-tomcat-JvmInfo','2268356-1135-2268365',NULL,100,1990265,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268366,2268356,9389,'tomcat','2268356-9389-2268366',NULL,100,1990278,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268368,2268356,1135,'srv1-os-cpu','2268356-1135-2268368',NULL,100,1990334,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268369,2268356,9036,'os','2268356-9036-2268369',NULL,100,1990347,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268371,2268356,1135,'srv1-tomcat-daemon-tomcatprocess','2268356-1135-2268371',NULL,100,1990369,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268372,2268356,7953,'tomcat-daemon','2268356-7953-2268372','',100,1990382,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268374,2268356,1135,'srv1-os-disk','2268356-1135-2268374',NULL,100,1990394,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268376,2268356,1135,'srv1-os-network','2268356-1135-2268376',NULL,100,1990411,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268378,2268356,1135,'srv1-tomcat-RequestInfo','2268356-1135-2268378',NULL,100,1990428,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268380,2268356,1135,'srv1-tomcat-Log','2268356-1135-2268380',NULL,100,1990445,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268382,2268356,1135,'srv1-artifact-URL','2268356-1135-2268382',NULL,100,1990462,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268384,2268356,1135,'srv1-os-load','2268356-1135-2268384',NULL,100,1990479,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268386,2268356,1135,'srv1-os-mem','2268356-1135-2268386',NULL,100,1990496,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268388,2268356,1135,'srv1-tomcat-HttpValue','2268356-1135-2268388',NULL,100,1990513,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268390,2268356,1135,'srv1-volume1-usage','2268356-1135-2268390',NULL,100,1990530,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268391,2268356,9478,'volume1','2268356-9478-2268391','',100,1990543,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268393,2268356,1135,'srv1-tomcat-ThreadInfo','2268356-1135-2268393',NULL,100,1990555,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268397,2268356,8528,'lb','2268356-8528-2268397',NULL,100,1990583,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268398,2268356,8193,'fqdn','2268356-8193-2268398',NULL,100,1990594,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268400,2268356,8370,'java','2268356-8370-2268400',NULL,100,1990611,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268419,2268356,8460,'sshkeys','2268356-8460-2268419',NULL,100,1990741,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268421,2268356,9264,'secgroup','2268356-9264-2268421',NULL,100,1990743,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268435,2268356,1116,'paas-perf','2268356-1116-2268435','',100,1990798,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268437,2268356,1116,'deployContext','2268356-1116-2268437','',100,1990804,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268439,2268356,1116,'name','2268356-1116-2268439','',100,1990810,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268441,2268356,1116,'repository','2268356-1116-2268441','',100,1990816,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (2268443,2268356,1116,'appVersion','2268356-1116-2268443','',100,1990822,'bannama',NULL,TIMESTAMP '2017-06-28 14:36:15.944',TIMESTAMP '2017-06-28 14:36:15.944');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (368275,173346,1059,'dev-dfwstg2','173346-1059-368275','',100,NULL,'bannama',NULL,TIMESTAMP '2015-08-28 11:09:07.307',TIMESTAMP '2015-08-28 11:09:07.307');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (839880,277111,1090,'dev','277111-1090-839880','SUCCESS: Generation time taken: 0 seconds.',100,NULL,'bannama','bannama',TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2017-06-28 15:01:23.685');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (277108,173345,1039,'prod1','173345-1039-277108','',100,NULL,'bannama',NULL,TIMESTAMP '2015-08-10 10:24:41.660',TIMESTAMP '2015-08-10 10:24:41.660');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (173343,100,1140,'local-dev','100-1140-173343',NULL,100,NULL,'bannama','bannama',TIMESTAMP '2015-07-29 14:49:27.115',TIMESTAMP '2015-08-07 14:20:08.405');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1465573,1220883,1134,'ExpiryMetrics','1220883-1134-1465573','',100,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1351861,1220883,1184,'computes','1220883-1184-1351861','',100,NULL,NULL,NULL,TIMESTAMP '2016-12-15 13:42:08.572',TIMESTAMP '2016-12-15 13:42:08.572');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1351857,1220883,1184,'volumes','1220883-1184-1351857','',100,NULL,NULL,NULL,TIMESTAMP '2016-12-15 13:42:08.530',TIMESTAMP '2016-12-15 13:42:08.530');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222154,1220883,1184,'activeclouds','1220883-1184-1222154','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.507',TIMESTAMP '2016-12-15 13:42:08.405');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1351765,1220883,14815,'firewall','1220883-14815-1351765','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.606',TIMESTAMP '2016-12-15 13:42:05.606');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1351753,1220883,10578,'objectstore','1220883-10578-1351753','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.505',TIMESTAMP '2016-12-15 13:42:05.505');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222183,1220883,1184,'primaryactiveclouds','1220883-1184-1222183','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.927',TIMESTAMP '2016-08-09 14:37:00.927');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222178,1220883,1184,'secures','1220883-1184-1222178','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.818',TIMESTAMP '2016-08-09 14:37:00.818');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222174,1220883,1184,'region','1220883-1184-1222174','',200,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.755',TIMESTAMP '2016-12-15 13:41:59.067');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222170,1220883,1184,'remotegdns','1220883-1184-1222170','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.713',TIMESTAMP '2016-12-15 13:42:08.482');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222166,1220883,1184,'remotedns','1220883-1184-1222166','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.660',TIMESTAMP '2016-08-09 14:37:00.660');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222162,1220883,1184,'lb','1220883-1184-1222162','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.608',TIMESTAMP '2016-08-09 14:37:00.608');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222158,1220883,1184,'organization','1220883-1184-1222158','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.557',TIMESTAMP '2016-08-09 14:37:00.557');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222150,1220883,1184,'environment','1220883-1184-1222150','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.460',TIMESTAMP '2016-08-09 14:37:00.460');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222146,1220883,1184,'linksto','1220883-1184-1222146','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.419',TIMESTAMP '2016-08-09 14:37:00.419');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222142,1220883,1184,'os','1220883-1184-1222142','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.375',TIMESTAMP '2016-08-09 14:37:00.375');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222126,1220883,1134,'exceptions','1220883-1134-1222126','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222110,1220883,1134,'URL','1220883-1134-1222110','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222094,1220883,1134,'tomcatprocess','1220883-1134-1222094','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222078,1220883,1134,'RequestInfo','1220883-1134-1222078','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222062,1220883,1134,'ThreadInfo','1220883-1134-1222062','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222046,1220883,1134,'JvmInfo','1220883-1134-1222046','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222030,1220883,1134,'Log','1220883-1134-1222030','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1222014,1220883,1134,'HttpValue','1220883-1134-1222014','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221998,1220883,1134,'process','1220883-1134-1221998','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221982,1220883,1134,'usage','1220883-1134-1221982','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221966,1220883,1134,'logstashprocess','1220883-1134-1221966','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221950,1220883,1134,'network','1220883-1134-1221950','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221934,1220883,1134,'mem','1220883-1134-1221934','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221918,1220883,1134,'disk','1220883-1134-1221918','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221902,1220883,1134,'load','1220883-1134-1221902','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221886,1220883,1134,'cpu','1220883-1134-1221886','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221870,1220883,1134,'ssh','1220883-1134-1221870','',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221297,1220883,8366,'java','1220883-8366-1221297','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221272,1220883,7572,'build','1220883-7572-1221272','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221248,1220883,7500,'artifact','1220883-7500-1221248','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221239,1220883,8489,'keystore','1220883-8489-1221239','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.344',TIMESTAMP '2016-08-09 14:36:57.344');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221226,1220883,7941,'tomcat-daemon','1220883-7941-1221226','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221164,1220883,9367,'tomcat','1220883-9367-1221164','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221148,1220883,7739,'lb-certificate','1220883-7739-1221148','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221130,1220883,8518,'lb','1220883-8518-1221130','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221115,1220883,10993,'sensuclient','1220883-10993-1221115','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221102,1220883,8187,'hostname','1220883-8187-1221102','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221086,1220883,7739,'certificate','1220883-7739-1221086','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221077,1220883,9260,'secgroup','1220883-9260-1221077','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.887',TIMESTAMP '2016-08-09 14:36:56.887');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221069,1220883,8456,'sshkeys','1220883-8456-1221069','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.840',TIMESTAMP '2016-08-09 14:36:56.840');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221056,1220883,7941,'daemon','1220883-7941-1221056','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221042,1220883,8057,'download','1220883-8057-1221042','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221032,1220883,8151,'file','1220883-8151-1221032','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.685',TIMESTAMP '2016-08-09 14:36:56.685');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221024,1220883,8547,'library','1220883-8547-1221024','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.639',TIMESTAMP '2016-08-09 14:36:56.639');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221012,1220883,8211,'share','1220883-8211-1221012','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.592',TIMESTAMP '2016-08-09 14:36:56.592');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1221000,1220883,9470,'volume','1220883-9470-1221000','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.545',TIMESTAMP '2016-08-09 14:36:56.545');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220990,1220883,9281,'storage','1220883-9281-1220990','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.499',TIMESTAMP '2016-08-09 14:36:56.499');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220974,1220883,8439,'job','1220883-8439-1220974','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220958,1220883,9437,'user','1220883-9437-1220958','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220945,1220883,8187,'fqdn','1220883-8187-1220945','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220934,1220883,8571,'logstash','1220883-8571-1220934','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.311',TIMESTAMP '2016-08-09 14:36:56.311');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220907,1220883,9021,'os','1220883-9021-1220907','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220897,1220883,7805,'compute','1220883-7805-1220897','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.184',TIMESTAMP '2016-08-09 14:36:56.184');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (1220884,1220883,1155,'tomcat','1220883-1155-1220884','bannama:/Users/bannama/.rvm/gems/ruby-2.0.0-p645/bin/knife',100,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci (ci_id,ns_id,class_id,ci_name,ci_goid,comments,ci_state_id,last_applied_rfc_id,created_by,updated_by,created,updated) VALUES (368280,368279,3625,'dfwstg2','368279-3625-368280','',100,NULL,'bannama','bannama',TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2017-04-26 21:33:25.542');




INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (2187622,368280,16150,'[]','[]',NULL,NULL,TIMESTAMP '2017-04-26 21:33:25.542',TIMESTAMP '2017-04-26 21:33:25.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1476570,368280,11868,'{}','{}',NULL,NULL,TIMESTAMP '2017-03-08 10:57:22.668',TIMESTAMP '2017-03-08 10:57:22.668');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368294,368280,3604,'P_N','P_N',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2017-03-08 10:57:22.668');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368283,368280,3609,'{"redhat-6.5":"yum clean all ; yum -q makecache"}','{"redhat-6.5":"yum clean all ; yum -q makecache"}',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2017-04-26 21:33:25.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368298,368280,3610,'{"rubygems":"http://repo1.test.com/","ruby":"",}','{"rubygems":"http://repo1.test.com/","ruby":"",}',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:38:18.809');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368291,368280,3608,'{"redhat-6.5":"aaaa-bbbbb-cccccc"}','{"redhat-6.5":"aaaa-bbbbb-cccccc"}',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2017-04-26 21:33:25.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368286,368280,3613,'20','20',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:21:40.920');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368295,368280,3615,'100','100',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:21:40.920');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368285,368280,3614,'40960','40960',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:21:40.920');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368282,368280,3612,'20','20',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:21:40.920');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368299,368280,3616,'10','10',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:21:40.920');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368292,368280,3600,'tn1','tn1',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368296,368280,3599,'tn1','tn1',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368281,368280,3602,'RegionOne','RegionOne',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368288,368280,3607,'{ "XS":"1","S":"2","M":"3","L":"4","XL":"5" }','{ "XS":"1","S":"2","M":"3","L":"4","XL":"5" }',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368289,368280,3601,'::ENCRYPTED::sldfjldfjksfodfjsdfjdfkd3432','::ENCRYPTED::sldfjldfjksfodfjsdfjdfkd3432',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368297,368280,3606,'','',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368293,368280,3603,'["az1","az2"]','["az1","az2"]',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368290,368280,3598,'https://api-end.test.com','https://api-end.test.com',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2016-08-20 07:33:16.323');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368287,368280,3605,'flat','flat',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-09-22 18:14:19.041');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368284,368280,3611,'centos-6.8','centos-6.8',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2017-04-26 21:33:25.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465585,1465573,1126,'{}','{}',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465584,1465573,1133,'{"min":0,"unit":"Per Minute"}','{"min":0,"unit":"Per Minute"}',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465583,1465573,1129,'60','60',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465582,1465573,1128,'{"minutes_remaining":{"display":true,"unit":"count","description":"Minutes remaining to Expiry","dstype":"GAUGE"},"hours_remaining":{"display":true,"unit":"count","description":"Hours remaining to Expiry","dstype":"GAUGE"},"days_remaining":{"display":true,"unit":"count","description":"Days remaining to Expiry","dstype":"GAUGE"}}','{"minutes_remaining":{"display":true,"unit":"count","description":"Minutes remaining to Expiry","dstype":"GAUGE"},"hours_remaining":{"display":true,"unit":"count","description":"Hours remaining to Expiry","dstype":"GAUGE"},"days_remaining":{"display":true,"unit":"count","description":"Days remaining to Expiry","dstype":"GAUGE"}}',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465581,1465573,1125,'check_cert!:::node.expiry_date_in_seconds:::','check_cert!:::node.expiry_date_in_seconds:::',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465580,1465573,1124,'ExpiryMetrics','ExpiryMetrics',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465579,1465573,1123,'false','false',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465578,1465573,1122,'true','true',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465577,1465573,1130,'false','false',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465576,1465573,1127,'/opt/nagios/libexec/check_cert $ARG1$','/opt/nagios/libexec/check_cert $ARG1$',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465575,1465573,1132,'{"cert-expiring-soon":{"bucket":"1m","stat":"avg","metric":"days_remaining","trigger":{"operator":"\u003c=","value":30,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":90,"duration":1,"numocc":1},"state":"notify"}}','{"cert-expiring-soon":{"bucket":"1m","stat":"avg","metric":"days_remaining","trigger":{"operator":"\u003c=","value":30,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":90,"duration":1,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465574,1465573,1131,'3','3',NULL,NULL,TIMESTAMP '2017-02-16 14:19:11.324',TIMESTAMP '2017-02-16 14:19:11.324');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351863,1351861,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Firewall",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.DependsOn",
           "direction": "from",
           "targetClassName": "manifest.oneops.1.Compute",
           "relations": [
             { "returnObject": true,
             "returnRelation": false,
             "relationName": "base.RealizedAs",
             "direction": "from",
             "targetClassName": "bom.oneops.1.Compute"
             }
           ]
         }
       ]
     }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Firewall",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.DependsOn",
           "direction": "from",
           "targetClassName": "manifest.oneops.1.Compute",
           "relations": [
             { "returnObject": true,
             "returnRelation": false,
             "relationName": "base.RealizedAs",
             "direction": "from",
             "targetClassName": "bom.oneops.1.Compute"
             }
           ]
         }
       ]
     }',NULL,NULL,TIMESTAMP '2016-12-15 13:42:08.572',TIMESTAMP '2016-12-15 13:42:08.572');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351862,1351861,1182,'computes','computes',NULL,NULL,TIMESTAMP '2016-12-15 13:42:08.572',TIMESTAMP '2016-12-15 13:42:08.572');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351859,1351857,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Storage",
       "relations": [
         { "returnObject": true,
           "returnRelation": false,
           "relationName": "manifest.DependsOn",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Volume"
         }
       ]
     }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Storage",
       "relations": [
         { "returnObject": true,
           "returnRelation": false,
           "relationName": "manifest.DependsOn",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Volume"
         }
       ]
     }',NULL,NULL,TIMESTAMP '2016-12-15 13:42:08.530',TIMESTAMP '2016-12-15 13:42:08.530');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351858,1351857,1182,'volumes','volumes',NULL,NULL,TIMESTAMP '2016-12-15 13:42:08.530',TIMESTAMP '2016-12-15 13:42:08.530');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222156,1222154,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationAttrs":[{"attributeName":"priority", "condition":"eq", "avalue":"1"},
                                {"attributeName":"adminstatus", "condition":"eq", "avalue":"active"}],
               "relationName": "base.Consumes",
               "direction": "from",
               "targetClassName": "account.Cloud",
               "relations": [
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "relationName": "base.Provides",
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Netscaler"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.Netscaler"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Route53"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Designate"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "relationName": "base.Provides",
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Azuretrafficmanager"
                 }
               ]
             }
           ]
         }
       ]
    }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationAttrs":[{"attributeName":"priority", "condition":"eq", "avalue":"1"},
                                {"attributeName":"adminstatus", "condition":"eq", "avalue":"active"}],
               "relationName": "base.Consumes",
               "direction": "from",
               "targetClassName": "account.Cloud",
               "relations": [
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "relationName": "base.Provides",
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Netscaler"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.Netscaler"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Route53"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Designate"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "relationName": "base.Provides",
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Azuretrafficmanager"
                 }
               ]
             }
           ]
         }
       ]
    }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.507',TIMESTAMP '2016-12-15 13:42:08.405');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222155,1222154,1182,'activeclouds','activeclouds',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.507',TIMESTAMP '2016-08-09 14:37:00.507');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465566,1351765,15933,'[]','[]',NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.753',TIMESTAMP '2017-02-16 14:19:08.753');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351756,1351753,10574,'','',NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.505',TIMESTAMP '2016-12-15 13:42:05.505');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351755,1351753,10576,NULL,NULL,NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.505',TIMESTAMP '2016-12-15 13:42:05.505');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351754,1351753,10575,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3',NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.505',TIMESTAMP '2016-12-15 13:42:05.505');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222185,1222183,1183,'{
         "returnObject": false,
         "returnRelation": false,
         "relationName": "base.RealizedAs",
         "direction": "to",
         "targetClassName": "manifest.Lb",
         "relations": [
           { "returnObject": false,
             "returnRelation": false,
             "relationName": "manifest.Requires",
             "direction": "to",
             "targetClassName": "manifest.Platform",
             "relations": [
               { "returnObject": false,
                 "returnRelation": false,
                 "relationAttrs":[{"attributeName":"priority", "condition":"eq", "avalue":"1"},
                                  {"attributeName":"adminstatus", "condition":"neq", "avalue":"offline"}],
                 "relationName": "base.Consumes",
                 "direction": "from",
                 "targetClassName": "account.Cloud",
                 "relations": [
                   { "returnObject": true,
                     "returnRelation": false,
                     "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"lb"}],
                     "relationName": "base.Provides",
                     "direction": "from",
                     "targetClassName": "cloud.service.Netscaler"
                   }
                 ]
               }
             ]
           }
         ]
      }','{
         "returnObject": false,
         "returnRelation": false,
         "relationName": "base.RealizedAs",
         "direction": "to",
         "targetClassName": "manifest.Lb",
         "relations": [
           { "returnObject": false,
             "returnRelation": false,
             "relationName": "manifest.Requires",
             "direction": "to",
             "targetClassName": "manifest.Platform",
             "relations": [
               { "returnObject": false,
                 "returnRelation": false,
                 "relationAttrs":[{"attributeName":"priority", "condition":"eq", "avalue":"1"},
                                  {"attributeName":"adminstatus", "condition":"neq", "avalue":"offline"}],
                 "relationName": "base.Consumes",
                 "direction": "from",
                 "targetClassName": "account.Cloud",
                 "relations": [
                   { "returnObject": true,
                     "returnRelation": false,
                     "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"lb"}],
                     "relationName": "base.Provides",
                     "direction": "from",
                     "targetClassName": "cloud.service.Netscaler"
                   }
                 ]
               }
             ]
           }
         ]
      }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.927',TIMESTAMP '2016-08-09 14:37:00.927');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222184,1222183,1182,'primaryactiveclouds','primaryactiveclouds',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.927',TIMESTAMP '2016-08-09 14:37:00.927');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222180,1222178,1183,'{
       "returnObject": true,
       "returnRelation": false,
       "relationName": "bom.SecuredBy",
       "direction": "to"
    }','{
       "returnObject": true,
       "returnRelation": false,
       "relationName": "bom.SecuredBy",
       "direction": "to"
    }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.818',TIMESTAMP '2016-08-09 14:37:00.818');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222179,1222178,1182,'Secures','Secures',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.818',TIMESTAMP '2016-08-09 14:37:00.818');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222176,1222174,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.DeployedTo",
       "direction": "from",
       "targetClassName": "account.provider.Binding",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "base.BindsTo",
           "direction": "from",
           "targetClassName": "account.provider.Zone",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "base.Provides",
               "direction": "to",
               "targetClassName": "account.provider.Region"
             }
           ]
         }
       ]
    }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.DeployedTo",
       "direction": "from",
       "targetClassName": "account.provider.Binding",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "base.BindsTo",
           "direction": "from",
           "targetClassName": "account.provider.Zone",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "base.Provides",
               "direction": "to",
               "targetClassName": "account.provider.Region"
             }
           ]
         }
       ]
    }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.755',TIMESTAMP '2016-08-09 14:37:00.755');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222175,1222174,1182,'Region','Region',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.755',TIMESTAMP '2016-08-09 14:37:00.755');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222172,1222170,1183,'{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Netscaler"
                     },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Netscaler"
                     },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Route53"
                      },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Designate"
                      },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                      },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Azuretrafficmanager"
                     }
                   ]
                 }
               ]
             }
           ]
      }','{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Netscaler"
                     },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Netscaler"
                     },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Route53"
                      },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Designate"
                      },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                      },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Azuretrafficmanager"
                     }
                   ]
                 }
               ]
             }
           ]
      }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.713',TIMESTAMP '2016-12-15 13:42:08.482');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222171,1222170,1182,'Other clouds gdns services','Other clouds gdns services',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.713',TIMESTAMP '2016-08-09 14:37:00.713');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222168,1222166,1183,'{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Infoblox"
                     },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Route53"
                    },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Designate"
                    },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                    },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Infoblox"
                     }
                   ]
                 }
               ]
             }
           ]
      }','{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Infoblox"
                     },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Route53"
                    },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Designate"
                    },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                    },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Infoblox"
                     }
                   ]
                 }
               ]
             }
           ]
      }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.660',TIMESTAMP '2016-08-09 14:37:00.660');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222167,1222166,1182,'Other clouds dns services','Other clouds dns services',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.660',TIMESTAMP '2016-08-09 14:37:00.660');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222164,1222162,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "bom.DependsOn",
       "direction": "from",
       "targetClassName": "bom.oneops.1.Lb",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Lb",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "base.RealizedAs",
               "direction": "from",
               "targetClassName": "bom.oneops.1.Lb"
             }
           ]
         }
       ]
    }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "bom.DependsOn",
       "direction": "from",
       "targetClassName": "bom.oneops.1.Lb",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Lb",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "base.RealizedAs",
               "direction": "from",
               "targetClassName": "bom.oneops.1.Lb"
             }
           ]
         }
       ]
    }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.608',TIMESTAMP '2016-08-09 14:37:00.608');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222163,1222162,1182,'all loadbalancers','all loadbalancers',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.608',TIMESTAMP '2016-08-09 14:37:00.608');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222160,1222158,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.ComposedOf",
               "direction": "to",
               "targetClassName": "manifest.Environment",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.RealizedIn",
                   "direction": "to",
                   "targetClassName": "account.Assembly",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Manages",
                       "direction": "to",
                       "targetClassName": "account.Organization"
                     }
                   ]
                 }
               ]
             }
           ]
         }
       ]
    }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.ComposedOf",
               "direction": "to",
               "targetClassName": "manifest.Environment",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.RealizedIn",
                   "direction": "to",
                   "targetClassName": "account.Assembly",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Manages",
                       "direction": "to",
                       "targetClassName": "account.Organization"
                     }
                   ]
                 }
               ]
             }
           ]
         }
       ]
    }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.557',TIMESTAMP '2016-08-09 14:37:00.557');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222159,1222158,1182,'Organization','Organization',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.557',TIMESTAMP '2016-08-09 14:37:00.557');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222152,1222150,1183,'{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "manifest.ComposedOf",
               "direction": "to",
               "targetClassName": "manifest.Environment"
             }
           ]
         }
       ]
    }','{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "manifest.ComposedOf",
               "direction": "to",
               "targetClassName": "manifest.Environment"
             }
           ]
         }
       ]
    }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.460',TIMESTAMP '2016-08-09 14:37:00.460');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222151,1222150,1182,'Environment','Environment',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.460',TIMESTAMP '2016-08-09 14:37:00.460');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222148,1222146,1183,'{
        "returnObject": false,
        "returnRelation": false,
        "relationName": "base.RealizedAs",
        "direction": "to",
        "relations": [
          { "returnObject": false,
            "returnRelation": false,
            "relationName": "manifest.Requires",
            "direction": "to",
            "targetClassName": "manifest.Platform",
            "relations": [
              { "returnObject": false,
                "returnRelation": false,
                "relationName": "manifest.LinksTo",
                "direction": "from",
                "targetClassName": "manifest.Platform",
                "relations": [
                  { "returnObject": true,
                    "returnRelation": false,
                    "relationName": "manifest.Entrypoint",
                    "direction": "from"
                  }
                ]
              }
            ]
          }
        ]
      }','{
        "returnObject": false,
        "returnRelation": false,
        "relationName": "base.RealizedAs",
        "direction": "to",
        "relations": [
          { "returnObject": false,
            "returnRelation": false,
            "relationName": "manifest.Requires",
            "direction": "to",
            "targetClassName": "manifest.Platform",
            "relations": [
              { "returnObject": false,
                "returnRelation": false,
                "relationName": "manifest.LinksTo",
                "direction": "from",
                "targetClassName": "manifest.Platform",
                "relations": [
                  { "returnObject": true,
                    "returnRelation": false,
                    "relationName": "manifest.Entrypoint",
                    "direction": "from"
                  }
                ]
              }
            ]
          }
        ]
      }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.419',TIMESTAMP '2016-08-09 14:37:00.419');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222147,1222146,1182,'LinksTo','LinksTo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.419',TIMESTAMP '2016-08-09 14:37:00.419');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222144,1222142,1183,'{
         "returnObject": false,
         "returnRelation": false,
         "relationName": "base.RealizedAs",
         "direction": "to",
         "targetClassName": "manifest.oneops.1.Compute",
         "relations": [
           { "returnObject": true,
             "returnRelation": false,
             "relationName": "manifest.DependsOn",
             "direction": "to",
             "targetClassName": "manifest.oneops.1.Os"
           }
         ]
      }','{
         "returnObject": false,
         "returnRelation": false,
         "relationName": "base.RealizedAs",
         "direction": "to",
         "targetClassName": "manifest.oneops.1.Compute",
         "relations": [
           { "returnObject": true,
             "returnRelation": false,
             "relationName": "manifest.DependsOn",
             "direction": "to",
             "targetClassName": "manifest.oneops.1.Os"
           }
         ]
      }',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.375',TIMESTAMP '2016-08-09 14:37:00.375');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222143,1222142,1182,'os','os',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.375',TIMESTAMP '2016-08-09 14:37:00.375');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222138,1222126,1126,'{"logfile":"/log/logmon/logmon.log","warningpattern":"Exception","criticalpattern":"Exception"}','{"logfile":"/log/logmon/logmon.log","warningpattern":"Exception","criticalpattern":"Exception"}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222137,1222126,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222136,1222126,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222135,1222126,1128,'{"logexc_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logexc_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logexc_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logexc_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}','{"logexc_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logexc_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logexc_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logexc_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222134,1222126,1125,'check_logfiles!logexc!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}','check_logfiles!logexc!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222133,1222126,1124,'Exceptions','Exceptions',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222132,1222126,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222131,1222126,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222130,1222126,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222129,1222126,1127,'/opt/nagios/libexec/check_logfiles   --noprotocol  --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"','/opt/nagios/libexec/check_logfiles   --noprotocol  --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222128,1222126,1132,'{"CriticalExceptions":{"bucket":"15m","stat":"avg","metric":"logexc_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}','{"CriticalExceptions":{"bucket":"15m","stat":"avg","metric":"logexc_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222127,1222126,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.325',TIMESTAMP '2016-08-09 14:37:00.325');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222122,1222110,1126,'{"host":"localhost","port":"8080","url":"/","wait":"15","expect":"200 OK","regex":""}','{"host":"localhost","port":"8080","url":"/","wait":"15","expect":"200 OK","regex":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222121,1222110,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222120,1222110,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222119,1222110,1128,'{"time":{"display":true,"unit":"s","description":"Response Time","dstype":"GAUGE"},"up":{"display":true,"unit":"","description":"Status","dstype":"GAUGE"},"size":{"display":false,"unit":"B","description":"Content Size","dstype":"GAUGE"}}','{"time":{"display":true,"unit":"s","description":"Response Time","dstype":"GAUGE"},"up":{"display":true,"unit":"","description":"Status","dstype":"GAUGE"},"size":{"display":false,"unit":"B","description":"Content Size","dstype":"GAUGE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222118,1222110,1125,'check_http_status!#{cmd_options[:host]}!#{cmd_options[:port]}!#{cmd_options[:url]}!#{cmd_options[:wait]}!#{cmd_options[:expect]}!#{cmd_options[:regex]}','check_http_status!#{cmd_options[:host]}!#{cmd_options[:port]}!#{cmd_options[:url]}!#{cmd_options[:wait]}!#{cmd_options[:expect]}!#{cmd_options[:regex]}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222117,1222110,1124,'URL','URL',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222116,1222110,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222115,1222110,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222114,1222110,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222113,1222110,1127,'/opt/nagios/libexec/check_http_status.sh $ARG1$ $ARG2$ "$ARG3$" $ARG4$ "$ARG5$" "$ARG6$"','/opt/nagios/libexec/check_http_status.sh $ARG1$ $ARG2$ "$ARG3$" $ARG4$ "$ARG5$" "$ARG6$"',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222112,1222110,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222111,1222110,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.266',TIMESTAMP '2016-08-09 14:37:00.266');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222106,1222094,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222105,1222094,1133,'{"min":"0","max":"100","unit":"Percent"}','{"min":"0","max":"100","unit":"Percent"}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222104,1222094,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222103,1222094,1128,'{"up":{"display":true,"unit":"%","description":"Percent Up"}}','{"up":{"display":true,"unit":"%","description":"Percent Up"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222102,1222094,1125,'check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::','check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222101,1222094,1124,'TomcatProcess','TomcatProcess',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222100,1222094,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222099,1222094,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222098,1222094,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222097,1222094,1127,'/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"','/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222096,1222094,1132,'{"TomcatDaemonProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"notify"}}','{"TomcatDaemonProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222095,1222094,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.217',TIMESTAMP '2016-08-09 14:37:00.217');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222090,1222078,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222089,1222078,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222088,1222078,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222087,1222078,1128,'{"bytesSent":{"display":true,"unit":"B/sec","description":"Traffic Out /sec","dstype":"DERIVE"},"bytesReceived":{"display":true,"unit":"B/sec","description":"Traffic In /sec","dstype":"DERIVE"},"requestCount":{"display":true,"unit":"reqs /sec","description":"Requests /sec","dstype":"DERIVE"},"errorCount":{"display":true,"unit":"errors /sec","description":"Errors /sec","dstype":"DERIVE"},"maxTime":{"display":true,"unit":"ms","description":"Max Time","dstype":"GAUGE"},"processingTime":{"display":true,"unit":"ms","description":"Processing Time /sec","dstype":"DERIVE"}}','{"bytesSent":{"display":true,"unit":"B/sec","description":"Traffic Out /sec","dstype":"DERIVE"},"bytesReceived":{"display":true,"unit":"B/sec","description":"Traffic In /sec","dstype":"DERIVE"},"requestCount":{"display":true,"unit":"reqs /sec","description":"Requests /sec","dstype":"DERIVE"},"errorCount":{"display":true,"unit":"errors /sec","description":"Errors /sec","dstype":"DERIVE"},"maxTime":{"display":true,"unit":"ms","description":"Max Time","dstype":"GAUGE"},"processingTime":{"display":true,"unit":"ms","description":"Processing Time /sec","dstype":"DERIVE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222086,1222078,1125,'check_tomcat_request','check_tomcat_request',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222085,1222078,1124,'RequestInfo','RequestInfo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222084,1222078,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222083,1222078,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222082,1222078,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222081,1222078,1127,'/opt/nagios/libexec/check_tomcat.rb RequestInfo','/opt/nagios/libexec/check_tomcat.rb RequestInfo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222080,1222078,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222079,1222078,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.144',TIMESTAMP '2016-08-09 14:37:00.144');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222074,1222062,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222073,1222062,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222072,1222062,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222071,1222062,1128,'{"currentThreadsBusy":{"display":true,"unit":"","description":"Busy Threads","dstype":"GAUGE"},"maxThreads":{"display":true,"unit":"","description":"Maximum Threads","dstype":"GAUGE"},"currentThreadCount":{"display":true,"unit":"","description":"Ready Threads","dstype":"GAUGE"},"percentBusy":{"display":true,"unit":"Percent","description":"Percent Busy Threads","dstype":"GAUGE"}}','{"currentThreadsBusy":{"display":true,"unit":"","description":"Busy Threads","dstype":"GAUGE"},"maxThreads":{"display":true,"unit":"","description":"Maximum Threads","dstype":"GAUGE"},"currentThreadCount":{"display":true,"unit":"","description":"Ready Threads","dstype":"GAUGE"},"percentBusy":{"display":true,"unit":"Percent","description":"Percent Busy Threads","dstype":"GAUGE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222070,1222062,1125,'check_tomcat_thread','check_tomcat_thread',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222069,1222062,1124,'ThreadInfo','ThreadInfo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222068,1222062,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222067,1222062,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222066,1222062,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222065,1222062,1127,'/opt/nagios/libexec/check_tomcat.rb ThreadInfo','/opt/nagios/libexec/check_tomcat.rb ThreadInfo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222064,1222062,1132,'{"HighThreadUse":{"bucket":"5m","stat":"avg","metric":"percentBusy","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}','{"HighThreadUse":{"bucket":"5m","stat":"avg","metric":"percentBusy","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222063,1222062,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.087',TIMESTAMP '2016-08-09 14:37:00.087');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222058,1222046,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222057,1222046,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222056,1222046,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222055,1222046,1128,'{"max":{"display":true,"unit":"B","description":"Max Allowed","dstype":"GAUGE"},"free":{"display":true,"unit":"B","description":"Free","dstype":"GAUGE"},"total":{"display":true,"unit":"B","description":"Allocated","dstype":"GAUGE"},"percentUsed":{"display":true,"unit":"Percent","description":"Percent Memory Used","dstype":"GAUGE"}}','{"max":{"display":true,"unit":"B","description":"Max Allowed","dstype":"GAUGE"},"free":{"display":true,"unit":"B","description":"Free","dstype":"GAUGE"},"total":{"display":true,"unit":"B","description":"Allocated","dstype":"GAUGE"},"percentUsed":{"display":true,"unit":"Percent","description":"Percent Memory Used","dstype":"GAUGE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222054,1222046,1125,'check_tomcat_jvm','check_tomcat_jvm',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222053,1222046,1124,'JvmInfo','JvmInfo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222052,1222046,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222051,1222046,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222050,1222046,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222049,1222046,1127,'/opt/nagios/libexec/check_tomcat.rb JvmInfo','/opt/nagios/libexec/check_tomcat.rb JvmInfo',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222048,1222046,1132,'{"HighMemUse":{"bucket":"5m","stat":"avg","metric":"percentUsed","trigger":{"operator":"\u003e","value":98,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":98,"duration":5,"numocc":1},"state":"notify"}}','{"HighMemUse":{"bucket":"5m","stat":"avg","metric":"percentUsed","trigger":{"operator":"\u003e","value":98,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":98,"duration":5,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222047,1222046,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:37:00.028',TIMESTAMP '2016-08-09 14:37:00.028');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222042,1222030,1126,'{"logfile":"/log/apache-tomcat/catalina.out","warningpattern":"WARNING","criticalpattern":"CRITICAL"}','{"logfile":"/log/apache-tomcat/catalina.out","warningpattern":"WARNING","criticalpattern":"CRITICAL"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222041,1222030,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222040,1222030,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222039,1222030,1128,'{"logtomcat_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logtomcat_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logtomcat_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logtomcat_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}','{"logtomcat_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logtomcat_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logtomcat_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logtomcat_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222038,1222030,1125,'check_logfiles!logtomcat!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}','check_logfiles!logtomcat!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222037,1222030,1124,'Log','Log',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222036,1222030,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222035,1222030,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222034,1222030,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222033,1222030,1127,'/opt/nagios/libexec/check_logfiles   --noprotocol --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"','/opt/nagios/libexec/check_logfiles   --noprotocol --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222032,1222030,1132,'{"CriticalLogException":{"bucket":"15m","stat":"avg","metric":"logtomcat_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}','{"CriticalLogException":{"bucket":"15m","stat":"avg","metric":"logtomcat_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222031,1222030,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.961',TIMESTAMP '2016-08-09 14:36:59.961');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222026,1222014,1126,'{"url":"","format":""}','{"url":"","format":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222025,1222014,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222024,1222014,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222023,1222014,1128,'{"value":{"display":true,"unit":"","description":"value","dstype":"DERIVE"}}','{"value":{"display":true,"unit":"","description":"value","dstype":"DERIVE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222022,1222014,1125,'check_http_value!#{cmd_options[:url]}!#{cmd_options[:format]}','check_http_value!#{cmd_options[:url]}!#{cmd_options[:format]}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222021,1222014,1124,'HttpValue','HttpValue',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222020,1222014,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222019,1222014,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222018,1222014,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222017,1222014,1127,'/opt/nagios/libexec/check_http_value.rb $ARG1$ $ARG2$','/opt/nagios/libexec/check_http_value.rb $ARG1$ $ARG2$',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222016,1222014,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222015,1222014,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.902',TIMESTAMP '2016-08-09 14:36:59.902');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222010,1221998,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222009,1221998,1133,'{"min":"0","max":"100","unit":"Percent"}','{"min":"0","max":"100","unit":"Percent"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222008,1221998,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222007,1221998,1128,'{"up":{"display":true,"unit":"%","description":"Percent Up"}}','{"up":{"display":true,"unit":"%","description":"Percent Up"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222006,1221998,1125,'check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::','check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222005,1221998,1124,'Process','Process',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222004,1221998,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222003,1221998,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222002,1221998,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222001,1221998,1127,'/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"','/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1222000,1221998,1132,'{"ProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"notify"}}','{"ProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221999,1221998,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.836',TIMESTAMP '2016-08-09 14:36:59.836');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221994,1221982,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221993,1221982,1133,'{"min":0,"unit":"Percent used"}','{"min":0,"unit":"Percent used"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221992,1221982,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221991,1221982,1128,'{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}','{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221990,1221982,1125,'check_disk_use!:::node.workorder.rfcCi.ciAttributes.mount_point:::','check_disk_use!:::node.workorder.rfcCi.ciAttributes.mount_point:::',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221989,1221982,1124,'Usage','Usage',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221988,1221982,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221987,1221982,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221986,1221982,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221985,1221982,1127,'/opt/nagios/libexec/check_disk_use.sh $ARG1$','/opt/nagios/libexec/check_disk_use.sh $ARG1$',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221984,1221982,1132,'{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}','{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221983,1221982,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.789',TIMESTAMP '2016-08-09 14:36:59.789');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221978,1221966,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221977,1221966,1133,'{"min":"0","max":"100","unit":"Percent"}','{"min":"0","max":"100","unit":"Percent"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221976,1221966,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221975,1221966,1128,'{"up":{"display":true,"unit":"%","description":"Percent Up"}}','{"up":{"display":true,"unit":"%","description":"Percent Up"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221974,1221966,1125,'check_process!logstash!false!logstash','check_process!logstash!false!logstash',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221973,1221966,1124,'LogstashProcess','LogstashProcess',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221972,1221966,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221971,1221966,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221970,1221966,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221969,1221966,1127,'/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$"','/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$"',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221968,1221966,1132,'{"LogstashProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"unhealthy"}}','{"LogstashProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"unhealthy"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221967,1221966,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.739',TIMESTAMP '2016-08-09 14:36:59.739');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221962,1221950,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221961,1221950,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221960,1221950,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221959,1221950,1128,'{"rx_bytes":{"display":true,"unit":"bytes","description":"RX Bytes","dstype":"DERIVE"},"tx_bytes":{"display":true,"unit":"bytes","description":"TX Bytes","dstype":"DERIVE"}}','{"rx_bytes":{"display":true,"unit":"bytes","description":"RX Bytes","dstype":"DERIVE"},"tx_bytes":{"display":true,"unit":"bytes","description":"TX Bytes","dstype":"DERIVE"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221958,1221950,1125,'check_network_bandwidth','check_network_bandwidth',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221957,1221950,1124,'Network','Network',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221956,1221950,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221955,1221950,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221954,1221950,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221953,1221950,1127,'/opt/nagios/libexec/check_network_bandwidth.sh','/opt/nagios/libexec/check_network_bandwidth.sh',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221952,1221950,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221951,1221950,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.689',TIMESTAMP '2016-08-09 14:36:59.689');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221946,1221934,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221945,1221934,1133,'{"min":0,"unit":"KB"}','{"min":0,"unit":"KB"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221944,1221934,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221943,1221934,1128,'{"total":{"display":true,"unit":"KB","description":"Total Memory"},"used":{"display":true,"unit":"KB","description":"Used Memory"},"free":{"display":true,"unit":"KB","description":"Free Memory"},"caches":{"display":true,"unit":"KB","description":"Cache Memory"}}','{"total":{"display":true,"unit":"KB","description":"Total Memory"},"used":{"display":true,"unit":"KB","description":"Used Memory"},"free":{"display":true,"unit":"KB","description":"Free Memory"},"caches":{"display":true,"unit":"KB","description":"Cache Memory"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221942,1221934,1125,'check_local_mem!90!95','check_local_mem!90!95',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221941,1221934,1124,'Memory','Memory',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221940,1221934,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221939,1221934,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221938,1221934,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221937,1221934,1127,'/opt/nagios/libexec/check_mem.pl -Cu -w $ARG1$ -c $ARG2$','/opt/nagios/libexec/check_mem.pl -Cu -w $ARG1$ -c $ARG2$',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221936,1221934,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221935,1221934,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.629',TIMESTAMP '2016-08-09 14:36:59.629');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221930,1221918,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221929,1221918,1133,'{"min":0,"unit":"%"}','{"min":0,"unit":"%"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221928,1221918,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221927,1221918,1128,'{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}','{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221926,1221918,1125,'check_disk_use!/','check_disk_use!/',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221925,1221918,1124,'Disk','Disk',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221924,1221918,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221923,1221918,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221922,1221918,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221921,1221918,1127,'/opt/nagios/libexec/check_disk_use.sh $ARG1$','/opt/nagios/libexec/check_disk_use.sh $ARG1$',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221920,1221918,1132,'{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}','{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221919,1221918,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.542',TIMESTAMP '2016-08-09 14:36:59.542');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221914,1221902,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221913,1221902,1133,'{"min":0}','{"min":0}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221912,1221902,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221911,1221902,1128,'{"load1":{"display":true,"unit":"","description":"Load 1min Average"},"load5":{"display":true,"unit":"","description":"Load 5min Average"},"load15":{"display":true,"unit":"","description":"Load 15min Average"}}','{"load1":{"display":true,"unit":"","description":"Load 1min Average"},"load5":{"display":true,"unit":"","description":"Load 5min Average"},"load15":{"display":true,"unit":"","description":"Load 15min Average"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221910,1221902,1125,'check_local_load!5.0,4.0,3.0!10.0,6.0,4.0','check_local_load!5.0,4.0,3.0!10.0,6.0,4.0',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221909,1221902,1124,'Load','Load',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221908,1221902,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221907,1221902,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221906,1221902,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221905,1221902,1127,'/opt/nagios/libexec/check_load -w $ARG1$ -c $ARG2$','/opt/nagios/libexec/check_load -w $ARG1$ -c $ARG2$',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221904,1221902,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221903,1221902,1131,'5','5',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.485',TIMESTAMP '2016-08-09 14:36:59.485');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221898,1221886,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221897,1221886,1133,'{"min":0,"max":100,"unit":"Percent"}','{"min":0,"max":100,"unit":"Percent"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221896,1221886,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221895,1221886,1128,'{"CpuUser":{"display":true,"unit":"%","description":"User %"},"CpuNice":{"display":true,"unit":"%","description":"Nice %"},"CpuSystem":{"display":true,"unit":"%","description":"System %"},"CpuSteal":{"display":true,"unit":"%","description":"Steal %"},"CpuIowait":{"display":true,"unit":"%","description":"IO Wait %"},"CpuIdle":{"display":false,"unit":"%","description":"Idle %"}}','{"CpuUser":{"display":true,"unit":"%","description":"User %"},"CpuNice":{"display":true,"unit":"%","description":"Nice %"},"CpuSystem":{"display":true,"unit":"%","description":"System %"},"CpuSteal":{"display":true,"unit":"%","description":"Steal %"},"CpuIowait":{"display":true,"unit":"%","description":"IO Wait %"},"CpuIdle":{"display":false,"unit":"%","description":"Idle %"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221894,1221886,1125,'check_local_cpu!10!5','check_local_cpu!10!5',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221893,1221886,1124,'CPU','CPU',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221892,1221886,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221891,1221886,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221890,1221886,1130,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221889,1221886,1127,'/opt/nagios/libexec/check_cpu.sh $ARG1$ $ARG2$','/opt/nagios/libexec/check_cpu.sh $ARG1$ $ARG2$',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221888,1221886,1132,'{"HighCpuPeak":{"bucket":"5m","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":10,"duration":5,"numocc":1},"reset":{"operator":"\u003e","value":20,"duration":5,"numocc":1},"state":"notify"},"HighCpuUtil":{"bucket":"1h","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":20,"duration":60,"numocc":1},"reset":{"operator":"\u003e","value":30,"duration":60,"numocc":1},"state":"notify"}}','{"HighCpuPeak":{"bucket":"5m","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":10,"duration":5,"numocc":1},"reset":{"operator":"\u003e","value":20,"duration":5,"numocc":1},"state":"notify"},"HighCpuUtil":{"bucket":"1h","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":20,"duration":60,"numocc":1},"reset":{"operator":"\u003e","value":30,"duration":60,"numocc":1},"state":"notify"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221887,1221886,1131,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.428',TIMESTAMP '2016-08-09 14:36:59.428');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221882,1221870,1126,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221881,1221870,1133,'{"min":0}','{"min":0}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221880,1221870,1129,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221879,1221870,1128,'{"up":{"display":true,"unit":"%","description":"Up %"}}','{"up":{"display":true,"unit":"%","description":"Up %"}}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221878,1221870,1125,'check_port','check_port',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221877,1221870,1124,'SSH Port','SSH Port',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221876,1221870,1123,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221875,1221870,1122,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221874,1221870,1130,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221873,1221870,1127,'/opt/nagios/libexec/check_port.sh','/opt/nagios/libexec/check_port.sh',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221872,1221870,1132,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221871,1221870,1131,'5','5',NULL,NULL,TIMESTAMP '2016-08-09 14:36:59.383',TIMESTAMP '2016-08-09 14:36:59.383');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221304,1221297,8359,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221303,1221297,8357,'8','8',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221302,1221297,8358,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221301,1221297,8362,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221300,1221297,8356,'jdk','jdk',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221299,1221297,8361,'/usr/lib/jvm','/usr/lib/jvm',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221298,1221297,8355,'openjdk','openjdk',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.535',TIMESTAMP '2016-08-09 14:36:57.535');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221290,1221272,7554,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221289,1221272,7566,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221288,1221272,7556,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221287,1221272,7549,'git','git',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221286,1221272,7564,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221285,1221272,7565,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221284,1221272,7552,'1','1',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221283,1221272,7558,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221282,1221272,7555,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221281,1221272,7560,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221280,1221272,7551,'HEAD','HEAD',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221279,1221272,7550,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221278,1221272,7562,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221277,1221272,7559,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221276,1221272,7553,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221275,1221272,7561,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221274,1221272,7557,'/usr/local/build','/usr/local/build',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221273,1221272,7563,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.442',TIMESTAMP '2016-08-09 14:36:57.442');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221265,1221248,7480,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221264,1221248,7493,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221263,1221248,7490,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221262,1221248,7482,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221261,1221248,7484,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221260,1221248,7487,'ooadmin','ooadmin',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221259,1221248,7481,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221258,1221248,7489,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221257,1221248,7485,'/nexus','/nexus',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221256,1221248,7478,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221255,1221248,7483,'latest','latest',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221254,1221248,7479,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221253,1221248,7492,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221252,1221248,7488,'ooadmin','ooadmin',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221251,1221248,7491,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221250,1221248,7494,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221249,1221248,7486,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.389',TIMESTAMP '2016-08-09 14:36:57.389');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221241,1221239,8484,'/var/lib/certs/keystore.jks','/var/lib/certs/keystore.jks',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.344',TIMESTAMP '2016-08-09 14:36:57.344');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221240,1221239,8485,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.344',TIMESTAMP '2016-08-09 14:36:57.344');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221232,1221226,7928,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221231,1221226,10233,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221230,1221226,7929,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221229,1221226,7927,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221228,1221226,7926,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221227,1221226,7925,'tomcat7','tomcat7',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.297',TIMESTAMP '2016-08-09 14:36:57.297');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221219,1221164,9321,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221218,1221164,9300,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221217,1221164,9331,'128M','128M',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221216,1221164,9342,'30','30',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221215,1221164,9306,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221214,1221164,9305,'8009','8009',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221213,1221164,9327,'25','25',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221212,1221164,9336,'logs','logs',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221211,1221164,9295,'70','70',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221210,1221164,9301,'{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}','{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221209,1221164,9320,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221208,1221164,9335,'grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
','grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221207,1221164,9334,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221206,1221164,9324,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221205,1221164,9330,'["+UseConcMarkSweepGC"]','["+UseConcMarkSweepGC"]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221204,1221164,9338,'yyyy-MM-dd','yyyy-MM-dd',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221203,1221164,9326,'50','50',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221202,1221164,9339,'.log','.log',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221201,1221164,9332,'128M','128M',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221200,1221164,9315,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221199,1221164,9325,'tomcatThreadPool','tomcatThreadPool',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221198,1221164,9337,'access_log','access_log',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221197,1221164,9333,'45','45',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221196,1221164,9298,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221195,1221164,9344,'1','1',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221194,1221164,9311,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221193,1221164,9317,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221192,1221164,9313,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221191,1221164,9292,'/opt','/opt',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221190,1221164,9304,'8005','8005',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221189,1221164,9297,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221188,1221164,9302,'8080','8080',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221187,1221164,9294,'7.0','7.0',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221186,1221164,9312,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221185,1221164,9296,'/opt/tomcat7/webapps','/opt/tomcat7/webapps',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221184,1221164,9309,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221183,1221164,9322,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221182,1221164,9340,'%h %l %u %t &quot;%r&quot; %s %b %D %F','%h %l %u %t &quot;%r&quot; %s %b %D %F',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221181,1221164,9293,'["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]','["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221180,1221164,9308,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221179,1221164,9323,'web','web',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221178,1221164,9303,'8443','8443',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221177,1221164,9345,'15','15',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221176,1221164,9318,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221175,1221164,9307,'/log/apache-tomcat/','/log/apache-tomcat/',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221174,1221164,9328,'-Djava.awt.headless=true','-Djava.awt.headless=true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221173,1221164,9343,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221172,1221164,9329,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221171,1221164,9299,'HTTP/1.1','HTTP/1.1',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221170,1221164,9291,'binary','binary',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221169,1221164,9314,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221168,1221164,9316,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221167,1221164,9341,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221166,1221164,9310,'<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
','<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221165,1221164,9319,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.221',TIMESTAMP '2016-08-09 14:36:57.221');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465572,1221148,15926,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.775',TIMESTAMP '2017-02-16 14:19:08.775');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465571,1221148,15925,'false','false',NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.775',TIMESTAMP '2017-02-16 14:19:08.775');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465570,1221148,15927,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.775',TIMESTAMP '2017-02-16 14:19:08.775');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465569,1221148,15930,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.775',TIMESTAMP '2017-02-16 14:19:08.775');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465568,1221148,15929,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.775',TIMESTAMP '2017-02-16 14:19:08.775');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465567,1221148,15928,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.775',TIMESTAMP '2017-02-16 14:19:08.775');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221157,1221148,7730,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221156,1221148,7734,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221155,1221148,10232,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221154,1221148,7733,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221153,1221148,7731,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221152,1221148,10230,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221151,1221148,10231,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221150,1221148,7732,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221149,1221148,7735,'/var/lib/certs','/var/lib/certs',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.170',TIMESTAMP '2016-08-09 14:36:57.170');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221141,1221130,8503,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221140,1221130,8508,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221139,1221130,8501,'cookieinsert','cookieinsert',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221138,1221130,8504,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221137,1221130,8500,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221136,1221130,8507,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221135,1221130,8502,'default','default',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221134,1221130,8498,'["http 80 http 8080"]','["http 80 http 8080"]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221133,1221130,8506,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221132,1221130,8505,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221131,1221130,8499,'roundrobin','roundrobin',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.111',TIMESTAMP '2016-08-09 14:36:57.111');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351764,1221115,15016,'::ENCRYPTED::9249699824df9fe6b5a4739fdf54e32e9fa7226f1f8de9af','::ENCRYPTED::9249699824df9fe6b5a4739fdf54e32e9fa7226f1f8de9af',NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.592',TIMESTAMP '2016-12-15 13:42:05.592');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221123,1221115,10972,'generic_email_notify','generic_email_notify',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221122,1221115,10978,'http://something.example.com/sensu-community-oneops.tar.gz','http://something.example.com/sensu-community-oneops.tar.gz',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221121,1221115,10975,'sensu-0.21.0-2','sensu-0.21.0-2',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221120,1221115,10971,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221119,1221115,10973,'OneOps','OneOps',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221118,1221115,10976,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221117,1221115,10977,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221116,1221115,10974,'rabbitmqserver.example.com,rabbitmqserver2.example.com','rabbitmqserver.example.com,rabbitmqserver2.example.com',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.063',TIMESTAMP '2016-08-09 14:36:57.063');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351763,1221102,14820,'false','false',NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.582',TIMESTAMP '2016-12-15 13:42:05.582');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221108,1221102,8179,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221107,1221102,8178,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221106,1221102,8181,'proximity','proximity',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221105,1221102,8177,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221104,1221102,8180,'platform','platform',NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221103,1221102,8176,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:57.016',TIMESTAMP '2016-08-09 14:36:57.016');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465565,1221086,15926,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.724',TIMESTAMP '2017-02-16 14:19:08.724');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465564,1221086,15925,'false','false',NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.724',TIMESTAMP '2017-02-16 14:19:08.724');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465563,1221086,15927,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.724',TIMESTAMP '2017-02-16 14:19:08.724');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465562,1221086,15930,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.724',TIMESTAMP '2017-02-16 14:19:08.724');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465561,1221086,15929,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.724',TIMESTAMP '2017-02-16 14:19:08.724');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465560,1221086,15928,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.724',TIMESTAMP '2017-02-16 14:19:08.724');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221095,1221086,7730,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221094,1221086,7734,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221093,1221086,10232,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221092,1221086,7733,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221091,1221086,7731,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221090,1221086,10230,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221089,1221086,10231,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221088,1221086,7732,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221087,1221086,7735,'/var/lib/certs','/var/lib/certs',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.959',TIMESTAMP '2016-08-09 14:36:56.959');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221079,1221077,9253,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.887',TIMESTAMP '2016-08-09 14:36:56.887');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221078,1221077,9254,'[ "22 22 tcp 0.0.0.0/0", "8080 8080 tcp 0.0.0.0/0", "8009 8009 tcp 0.0.0.0/0", "8443 8443 tcp 0.0.0.0/0" ]','[ "22 22 tcp 0.0.0.0/0", "8080 8080 tcp 0.0.0.0/0", "8009 8009 tcp 0.0.0.0/0", "8443 8443 tcp 0.0.0.0/0" ]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.887',TIMESTAMP '2016-08-09 14:36:56.887');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221070,1221069,8448,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.840',TIMESTAMP '2016-08-09 14:36:56.840');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221062,1221056,7928,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221061,1221056,10233,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221060,1221056,7929,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221059,1221056,7927,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221058,1221056,7926,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221057,1221056,7925,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.777',TIMESTAMP '2016-08-09 14:36:56.777');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221049,1221042,8053,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221048,1221042,8047,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221047,1221042,8051,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221046,1221042,8048,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221045,1221042,8049,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221044,1221042,8052,'/tmp/download_file','/tmp/download_file',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221043,1221042,8050,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.729',TIMESTAMP '2016-08-09 14:36:56.729');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221035,1221032,8145,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.685',TIMESTAMP '2016-08-09 14:36:56.685');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221034,1221032,8147,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.685',TIMESTAMP '2016-08-09 14:36:56.685');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221033,1221032,8146,'/tmp/download_file','/tmp/download_file',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.685',TIMESTAMP '2016-08-09 14:36:56.685');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221025,1221024,8543,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.639',TIMESTAMP '2016-08-09 14:36:56.639');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221017,1221012,10398,'3.6','3.6',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.592',TIMESTAMP '2016-08-09 14:36:56.592');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221016,1221012,8203,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.592',TIMESTAMP '2016-08-09 14:36:56.592');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221015,1221012,8202,'/data','/data',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.592',TIMESTAMP '2016-08-09 14:36:56.592');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221014,1221012,8205,'/share','/share',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.592',TIMESTAMP '2016-08-09 14:36:56.592');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221013,1221012,8204,'2','2',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.592',TIMESTAMP '2016-08-09 14:36:56.592');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1312354,1221000,12285,'no-raid','no-raid',NULL,NULL,TIMESTAMP '2016-11-14 12:06:20.343',TIMESTAMP '2016-11-14 12:06:20.343');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221005,1221000,9460,'xfs','xfs',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.545',TIMESTAMP '2016-08-09 14:36:56.545');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221004,1221000,9459,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.545',TIMESTAMP '2016-08-09 14:36:56.545');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221003,1221000,9462,'','',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.545',TIMESTAMP '2016-08-09 14:36:56.545');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221002,1221000,9461,'/data','/data',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.545',TIMESTAMP '2016-08-09 14:36:56.545');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1221001,1221000,9458,'100%FREE','100%FREE',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.545',TIMESTAMP '2016-08-09 14:36:56.545');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220993,1220990,9277,'1','1',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.499',TIMESTAMP '2016-08-09 14:36:56.499');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220992,1220990,9276,'20G','20G',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.499',TIMESTAMP '2016-08-09 14:36:56.499');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220991,1220990,10797,'GENERAL','GENERAL',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.499',TIMESTAMP '2016-08-09 14:36:56.499');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220983,1220974,8428,'0','0',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220982,1220974,8434,'root','root',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220981,1220974,8430,'*','*',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220980,1220974,8433,'/bin/true','/bin/true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220979,1220974,8427,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220978,1220974,8432,'*','*',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220977,1220974,8431,'*','*',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220976,1220974,8429,'*','*',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220975,1220974,8435,'{"HOME":"","SHELL":"","MAILTO":"","PATH":""}','{"HOME":"","SHELL":"","MAILTO":"","PATH":""}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.451',TIMESTAMP '2016-08-09 14:36:56.451');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465559,1220958,16039,'[]','[]',NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.640',TIMESTAMP '2017-02-16 14:19:08.640');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465558,1220958,16040,NULL,NULL,NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.640',TIMESTAMP '2017-02-16 14:19:08.640');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220967,1220958,9425,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220966,1220958,9426,'16384','16384',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220965,1220958,9430,'/bin/bash','/bin/bash',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220964,1220958,9427,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220963,1220958,9433,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220962,1220958,9432,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220961,1220958,9428,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220960,1220958,9431,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220959,1220958,9429,'755','755',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.404',TIMESTAMP '2016-08-09 14:36:56.404');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351752,1220945,14820,'false','false',NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.477',TIMESTAMP '2016-12-15 13:42:05.477');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220951,1220945,8179,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220950,1220945,8178,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220949,1220945,8181,'proximity','proximity',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220948,1220945,8177,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220947,1220945,8180,'platform','platform',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220946,1220945,8176,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.357',TIMESTAMP '2016-08-09 14:36:56.357');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220938,1220934,8556,'1.5.3','1.5.3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.311',TIMESTAMP '2016-08-09 14:36:56.311');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220937,1220934,8558,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.311',TIMESTAMP '2016-08-09 14:36:56.311');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220936,1220934,8557,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.311',TIMESTAMP '2016-08-09 14:36:56.311');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220935,1220934,8559,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.311',TIMESTAMP '2016-08-09 14:36:56.311');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220927,1220907,9007,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220926,1220907,9001,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220925,1220907,9009,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220924,1220907,10585,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220923,1220907,9006,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220922,1220907,9003,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220921,1220907,9013,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220920,1220907,9011,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220919,1220907,9012,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220918,1220907,9000,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220917,1220907,10967,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220916,1220907,10584,'centos-7.0','centos-7.0',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220915,1220907,8997,NULL,NULL,NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220914,1220907,9010,'{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}','{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220913,1220907,8999,'[]','[]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220912,1220907,9004,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220911,1220907,9008,'UTC','UTC',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220910,1220907,8998,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220909,1220907,9002,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220908,1220907,9005,'["-p tcp --dport 22"]','["-p tcp --dport 22"]',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.230',TIMESTAMP '2016-08-09 14:36:56.230');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220900,1220897,7794,'{}','{}',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.184',TIMESTAMP '2016-08-09 14:36:56.184');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220899,1220897,7795,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.184',TIMESTAMP '2016-08-09 14:36:56.184');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220898,1220897,7791,'S','S',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.184',TIMESTAMP '2016-08-09 14:36:56.184');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1465557,1220884,15209,'true','true',NULL,NULL,TIMESTAMP '2017-02-16 14:19:08.576',TIMESTAMP '2017-02-16 14:19:08.576');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1351751,1220884,13291,NULL,NULL,NULL,NULL,TIMESTAMP '2016-12-15 13:42:05.424',TIMESTAMP '2016-12-15 13:42:05.424');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1312353,1220884,11021,'false','false',NULL,NULL,TIMESTAMP '2016-11-14 12:06:20.255',TIMESTAMP '2016-11-14 12:06:20.255');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220896,1220884,9523,'true','true',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220895,1220884,1151,'1','1',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220894,1220884,1150,'Tomcat','Tomcat',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220893,1220884,9524,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220892,1220884,9525,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220891,1220884,1149,'oneops','oneops',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220890,1220884,1157,'default','default',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220889,1220884,1158,'60','60',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220888,1220884,1148,'Tomcat','Tomcat',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220887,1220884,1152,'1','1',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220886,1220884,1156,'false','false',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1220885,1220884,1159,'3','3',NULL,NULL,TIMESTAMP '2016-08-09 14:36:56.139',TIMESTAMP '2016-08-09 14:36:56.139');



INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (1401799,839880,15295,'false','false',NULL,NULL,TIMESTAMP '2017-01-30 11:32:57.491',TIMESTAMP '2017-01-30 11:32:57.491');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839902,839880,1091,NULL,NULL,NULL,NULL,TIMESTAMP '2015-12-28 14:29:21.074',TIMESTAMP '2015-12-28 14:29:21.074');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839895,839880,1082,'true','true',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839893,839880,1084,'false','false',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839892,839880,1087,'false','false',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839891,839880,1079,'dev.prod1.local-dev','dev.prod1.local-dev',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839890,839880,1078,'60','60',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839889,839880,1080,'false','false',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839887,839880,1092,'provision','provision',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839886,839880,1083,'false','false',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839885,839880,1081,'true','true',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839884,839880,1085,'single','single',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839883,839880,1075,'','',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (839882,839880,1077,'false','false',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368278,368275,1057,'','',NULL,NULL,TIMESTAMP '2015-08-28 11:09:07.307',TIMESTAMP '2015-08-28 11:09:07.307');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368277,368275,1056,'','',NULL,NULL,TIMESTAMP '2015-08-28 11:09:07.307',TIMESTAMP '2015-08-28 11:09:07.307');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368276,368275,1060,'/test/clouds/openstack2','/test/clouds/openstack2',NULL,NULL,TIMESTAMP '2015-08-28 11:09:07.307',TIMESTAMP '2015-08-28 11:09:07.307');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268788,2268357,9527,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268787,2268357,15210,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268786,2268357,13291,'aa501531091085a06381fcc5644a23f4','aa501531091085a06381fcc5644a23f4');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268785,2268357,9528,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268784,2268357,1161,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268783,2268357,1148,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268782,2268357,1163,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268781,2268357,1162,'redundant','redundant');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268780,2268357,1151,'1','1');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268779,2268357,1152,'1','1');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268778,2268357,1164,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268777,2268357,9526,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268775,2268357,1150,'tomcat','tomcat');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268774,2268357,11022,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268773,2268357,1149,'oneops','oneops');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268772,2268357,16049,'[]','[]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268771,2268359,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268770,2268359,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268769,2268359,1131,'5','5');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268768,2268359,1128,'{"up":{"display":true,"unit":"%","description":"Up %"}}','{"up":{"display":true,"unit":"%","description":"Up %"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268767,2268359,1133,'{"min":0}','{"min":0}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268766,2268359,1125,'check_port','check_port');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268765,2268359,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268764,2268359,1124,'SSH Port','SSH Port');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268763,2268359,1130,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268762,2268359,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268761,2268359,1127,'/opt/nagios/libexec/check_port.sh','/opt/nagios/libexec/check_port.sh');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268760,2268359,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268759,2268360,7791,'S','S');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268758,2268360,7795,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268757,2268360,7794,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268756,2268362,1128,'{"logexc_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logexc_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logexc_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logexc_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}','{"logexc_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logexc_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logexc_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logexc_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268755,2268362,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268754,2268362,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268753,2268362,1132,'{"CriticalExceptions":{"bucket":"15m","stat":"avg","metric":"logexc_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}','{"CriticalExceptions":{"bucket":"15m","stat":"avg","metric":"logexc_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268752,2268362,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268751,2268362,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268750,2268362,1124,'Exceptions','Exceptions');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268749,2268362,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268748,2268362,1127,'/opt/nagios/libexec/check_logfiles   --noprotocol  --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"','/opt/nagios/libexec/check_logfiles   --noprotocol  --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268747,2268362,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268746,2268362,1125,'check_logfiles!logexc!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}','check_logfiles!logexc!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268745,2268362,1126,'{"logfile":"/log/logmon/logmon.log","warningpattern":"Exception","criticalpattern":"Exception"}','{"logfile":"/log/logmon/logmon.log","warningpattern":"Exception","criticalpattern":"Exception"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268744,2268363,7490,'[]','[]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268743,2268363,7491,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268742,2268363,7486,'/app/paas-perf-test','/app/paas-perf-test');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268741,2268363,7478,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268739,2268363,7483,'$OO_LOCAL{appVersion}','$OO_LOCAL{appVersion}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268738,2268363,7493,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268737,2268363,7494,'execute "rm -fr /app/tomcat7/webapps/$OO_LOCAL{deployContext}" 

link "/app/tomcat7/webapps/$OO_LOCAL{deployContext}" do 
  to "/app/paas-perf-test/current" 
end 

','execute "rm -fr /app/tomcat7/webapps/$OO_LOCAL{deployContext}" 

link "/app/tomcat7/webapps/$OO_LOCAL{deployContext}" do 
  to "/app/paas-perf-test/current" 
end 

');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268736,2268363,7481,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268735,2268363,7479,'$OO_LOCAL{repository}','$OO_LOCAL{repository}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268734,2268363,7488,'app','app');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268733,2268363,7484,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268732,2268363,7485,'/nexus','/nexus');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268731,2268363,7482,'plt:paas-perf-test:war','plt:paas-perf-test:war');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268730,2268363,7487,'app','app');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268729,2268363,7492,'%w[ /log/apache-tomcat /log/logmon /app/localConfig ].each do |path|
  directory path do
    owner ''app''
    group ''app''
  end
end

','%w[ /log/apache-tomcat /log/logmon /app/localConfig ].each do |path|
  directory path do
    owner ''app''
    group ''app''
  end
end

');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268728,2268363,7489,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268727,2268363,7480,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268726,2268365,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268725,2268365,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268724,2268365,1125,'check_tomcat_jvm','check_tomcat_jvm');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268723,2268365,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268722,2268365,1128,'{"max":{"display":true,"unit":"B","description":"Max Allowed","dstype":"GAUGE"},"free":{"display":true,"unit":"B","description":"Free","dstype":"GAUGE"},"total":{"display":true,"unit":"B","description":"Allocated","dstype":"GAUGE"},"percentUsed":{"display":true,"unit":"Percent","description":"Percent Memory Used","dstype":"GAUGE"}}','{"max":{"display":true,"unit":"B","description":"Max Allowed","dstype":"GAUGE"},"free":{"display":true,"unit":"B","description":"Free","dstype":"GAUGE"},"total":{"display":true,"unit":"B","description":"Allocated","dstype":"GAUGE"},"percentUsed":{"display":true,"unit":"Percent","description":"Percent Memory Used","dstype":"GAUGE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268721,2268365,1132,'{"HighMemUse":{"bucket":"5m","stat":"avg","metric":"percentUsed","trigger":{"operator":"\u003e","value":98,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":98,"duration":5,"numocc":1},"state":"notify"}}','{"HighMemUse":{"bucket":"5m","stat":"avg","metric":"percentUsed","trigger":{"operator":"\u003e","value":98,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":98,"duration":5,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268720,2268365,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268719,2268365,1127,'/opt/nagios/libexec/check_tomcat.rb JvmInfo','/opt/nagios/libexec/check_tomcat.rb JvmInfo');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268718,2268365,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268717,2268365,1124,'JvmInfo','JvmInfo');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268716,2268365,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268715,2268365,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268714,2268366,9335,'grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
','grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268713,2268366,9302,'8080','8080');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268712,2268366,9321,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268711,2268366,9324,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268710,2268366,9331,'128M','128M');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268709,2268366,9318,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268708,2268366,9330,'["+UseConcMarkSweepGC"]','["+UseConcMarkSweepGC"]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268707,2268366,9340,'%h %l %u %t &quot;%r&quot; %s %b %D %F','%h %l %u %t &quot;%r&quot; %s %b %D %F');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268706,2268366,9299,'HTTP/1.1','HTTP/1.1');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268705,2268366,9316,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268704,2268366,9339,'.log','.log');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268703,2268366,9312,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268702,2268366,9337,'access_log','access_log');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268701,2268366,9313,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268700,2268366,9293,'["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]','["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268699,2268366,9334,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268698,2268366,9342,'30','30');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268697,2268366,9319,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268696,2268366,9338,'yyyy-MM-dd','yyyy-MM-dd');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268695,2268366,9292,'/opt','/opt');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268694,2268366,9296,'/opt/tomcat7/webapps','/opt/tomcat7/webapps');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268693,2268366,9291,'binary','binary');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268692,2268366,9295,'70','70');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268691,2268366,9300,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268690,2268366,9311,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268689,2268366,9329,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268688,2268366,9322,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268687,2268366,9332,'128M','128M');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268686,2268366,9294,'7.0','7.0');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268685,2268366,9345,'15','15');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268684,2268366,9326,'50','50');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268683,2268366,9320,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268682,2268366,9308,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268681,2268366,9301,'{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}','{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268680,2268366,9336,'logs','logs');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268679,2268366,9315,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268678,2268366,9344,'1','1');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268677,2268366,9303,'8443','8443');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268676,2268366,9310,'<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
','<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268675,2268366,9317,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268674,2268366,9314,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268673,2268366,9325,'tomcatThreadPool','tomcatThreadPool');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268672,2268366,9305,'8009','8009');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268671,2268366,9327,'25','25');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268670,2268366,9304,'8005','8005');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268669,2268366,9323,'web','web');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268668,2268366,9328,'-Djava.awt.headless=true','-Djava.awt.headless=true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268667,2268366,9307,'/log/apache-tomcat/','/log/apache-tomcat/');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268666,2268366,9333,'45','45');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268665,2268366,9306,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268664,2268366,9309,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268663,2268368,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268662,2268368,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268661,2268368,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268660,2268368,1124,'CPU','CPU');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268659,2268368,1132,'{"HighCpuPeak":{"bucket":"5m","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":10,"duration":5,"numocc":1},"reset":{"operator":"\u003e","value":20,"duration":5,"numocc":1},"state":"notify"},"HighCpuUtil":{"bucket":"1h","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":20,"duration":60,"numocc":1},"reset":{"operator":"\u003e","value":30,"duration":60,"numocc":1},"state":"notify"}}','{"HighCpuPeak":{"bucket":"5m","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":10,"duration":5,"numocc":1},"reset":{"operator":"\u003e","value":20,"duration":5,"numocc":1},"state":"notify"},"HighCpuUtil":{"bucket":"1h","stat":"avg","metric":"CpuIdle","trigger":{"operator":"\u003c=","value":20,"duration":60,"numocc":1},"reset":{"operator":"\u003e","value":30,"duration":60,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268658,2268368,1127,'/opt/nagios/libexec/check_cpu.sh $ARG1$ $ARG2$','/opt/nagios/libexec/check_cpu.sh $ARG1$ $ARG2$');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268657,2268368,1128,'{"CpuUser":{"display":true,"unit":"%","description":"User %"},"CpuNice":{"display":true,"unit":"%","description":"Nice %"},"CpuSystem":{"display":true,"unit":"%","description":"System %"},"CpuSteal":{"display":true,"unit":"%","description":"Steal %"},"CpuIowait":{"display":true,"unit":"%","description":"IO Wait %"},"CpuIdle":{"display":false,"unit":"%","description":"Idle %"}}','{"CpuUser":{"display":true,"unit":"%","description":"User %"},"CpuNice":{"display":true,"unit":"%","description":"Nice %"},"CpuSystem":{"display":true,"unit":"%","description":"System %"},"CpuSteal":{"display":true,"unit":"%","description":"Steal %"},"CpuIowait":{"display":true,"unit":"%","description":"IO Wait %"},"CpuIdle":{"display":false,"unit":"%","description":"Idle %"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268656,2268368,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268655,2268368,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268654,2268368,1125,'check_local_cpu!10!5','check_local_cpu!10!5');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268653,2268368,1133,'{"min":0,"max":100,"unit":"Percent"}','{"min":0,"max":100,"unit":"Percent"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268652,2268368,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268651,2268369,10967,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268650,2268369,9004,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268649,2268369,8999,'[]','[]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268648,2268369,9010,'{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}','{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268647,2268369,9000,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268646,2268369,9001,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268645,2268369,9002,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268644,2268369,9011,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268643,2268369,9008,'UTC','UTC');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268642,2268369,9007,'[]','[]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268641,2268369,10584,'centos-7.0','centos-7.0');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268640,2268369,9003,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268639,2268369,9005,'["-p tcp --dport 22"]','["-p tcp --dport 22"]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268638,2268369,9012,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268637,2268369,9009,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268636,2268369,9006,'[]','[]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268635,2268369,8998,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268634,2268371,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268633,2268371,1132,'{"TomcatDaemonProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"notify"}}','{"TomcatDaemonProcessDown":{"bucket":"1m","stat":"avg","metric":"up","trigger":{"operator":"\u003c=","value":98,"duration":1,"numocc":1},"reset":{"operator":"\u003e","value":95,"duration":1,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268632,2268371,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268631,2268371,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268630,2268371,1128,'{"up":{"display":true,"unit":"%","description":"Percent Up"}}','{"up":{"display":true,"unit":"%","description":"Percent Up"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268629,2268371,1125,'check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::','check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268628,2268371,1124,'TomcatProcess','TomcatProcess');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268627,2268371,1133,'{"min":"0","max":"100","unit":"Percent"}','{"min":"0","max":"100","unit":"Percent"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268626,2268371,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268625,2268371,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268624,2268371,1127,'/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"','/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268623,2268371,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268622,2268372,7928,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268621,2268372,7927,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268620,2268372,10233,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268619,2268372,7925,'tomcat7','tomcat7');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268617,2268372,7929,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268616,2268372,7926,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268615,2268374,1128,'{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}','{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268614,2268374,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268613,2268374,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268612,2268374,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268611,2268374,1127,'/opt/nagios/libexec/check_disk_use.sh $ARG1$','/opt/nagios/libexec/check_disk_use.sh $ARG1$');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268610,2268374,1124,'Disk','Disk');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268609,2268374,1133,'{"min":0,"unit":"%"}','{"min":0,"unit":"%"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268608,2268374,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268607,2268374,1125,'check_disk_use!/','check_disk_use!/');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268606,2268374,1132,'{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}','{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268605,2268374,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268604,2268374,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268603,2268376,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268602,2268376,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268601,2268376,1124,'Network','Network');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268600,2268376,1125,'check_network_bandwidth','check_network_bandwidth');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268599,2268376,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268598,2268376,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268597,2268376,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268596,2268376,1127,'/opt/nagios/libexec/check_network_bandwidth.sh','/opt/nagios/libexec/check_network_bandwidth.sh');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268595,2268376,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268594,2268376,1128,'{"rx_bytes":{"display":true,"unit":"bytes","description":"RX Bytes","dstype":"DERIVE"},"tx_bytes":{"display":true,"unit":"bytes","description":"TX Bytes","dstype":"DERIVE"}}','{"rx_bytes":{"display":true,"unit":"bytes","description":"RX Bytes","dstype":"DERIVE"},"tx_bytes":{"display":true,"unit":"bytes","description":"TX Bytes","dstype":"DERIVE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268593,2268376,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268592,2268376,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268591,2268378,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268590,2268378,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268589,2268378,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268588,2268378,1127,'/opt/nagios/libexec/check_tomcat.rb RequestInfo','/opt/nagios/libexec/check_tomcat.rb RequestInfo');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268587,2268378,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268586,2268378,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268585,2268378,1124,'RequestInfo','RequestInfo');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268584,2268378,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268583,2268378,1128,'{"bytesSent":{"display":true,"unit":"B/sec","description":"Traffic Out /sec","dstype":"DERIVE"},"bytesReceived":{"display":true,"unit":"B/sec","description":"Traffic In /sec","dstype":"DERIVE"},"requestCount":{"display":true,"unit":"reqs /sec","description":"Requests /sec","dstype":"DERIVE"},"errorCount":{"display":true,"unit":"errors /sec","description":"Errors /sec","dstype":"DERIVE"},"maxTime":{"display":true,"unit":"ms","description":"Max Time","dstype":"GAUGE"},"processingTime":{"display":true,"unit":"ms","description":"Processing Time /sec","dstype":"DERIVE"}}','{"bytesSent":{"display":true,"unit":"B/sec","description":"Traffic Out /sec","dstype":"DERIVE"},"bytesReceived":{"display":true,"unit":"B/sec","description":"Traffic In /sec","dstype":"DERIVE"},"requestCount":{"display":true,"unit":"reqs /sec","description":"Requests /sec","dstype":"DERIVE"},"errorCount":{"display":true,"unit":"errors /sec","description":"Errors /sec","dstype":"DERIVE"},"maxTime":{"display":true,"unit":"ms","description":"Max Time","dstype":"GAUGE"},"processingTime":{"display":true,"unit":"ms","description":"Processing Time /sec","dstype":"DERIVE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268582,2268378,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268581,2268378,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268580,2268378,1125,'check_tomcat_request','check_tomcat_request');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268579,2268380,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268578,2268380,1125,'check_logfiles!logtomcat!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}','check_logfiles!logtomcat!#{cmd_options[:logfile]}!#{cmd_options[:warningpattern]}!#{cmd_options[:criticalpattern]}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268577,2268380,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268576,2268380,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268575,2268380,1128,'{"logtomcat_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logtomcat_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logtomcat_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logtomcat_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}','{"logtomcat_lines":{"display":true,"unit":"lines","description":"Scanned Lines","dstype":"GAUGE"},"logtomcat_warnings":{"display":true,"unit":"warnings","description":"Warnings","dstype":"GAUGE"},"logtomcat_criticals":{"display":true,"unit":"criticals","description":"Criticals","dstype":"GAUGE"},"logtomcat_unknowns":{"display":true,"unit":"unknowns","description":"Unknowns","dstype":"GAUGE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268574,2268380,1126,'{"logfile":"/log/apache-tomcat/catalina.out","warningpattern":"WARNING","criticalpattern":"CRITICAL"}','{"logfile":"/log/apache-tomcat/catalina.out","warningpattern":"WARNING","criticalpattern":"CRITICAL"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268573,2268380,1124,'Log','Log');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268572,2268380,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268571,2268380,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268570,2268380,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268569,2268380,1127,'/opt/nagios/libexec/check_logfiles   --noprotocol --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"','/opt/nagios/libexec/check_logfiles   --noprotocol --tag=$ARG1$ --logfile=$ARG2$ --warningpattern="$ARG3$" --criticalpattern="$ARG4$"');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268568,2268380,1132,'{"CriticalLogException":{"bucket":"15m","stat":"avg","metric":"logtomcat_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}','{"CriticalLogException":{"bucket":"15m","stat":"avg","metric":"logtomcat_criticals","trigger":{"operator":"\u003e=","value":1,"duration":15,"numocc":1},"reset":{"operator":"\u003c","value":1,"duration":15,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268567,2268382,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268566,2268382,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268565,2268382,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268564,2268382,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268563,2268382,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268562,2268382,1128,'{"time":{"display":true,"unit":"s","description":"Response Time","dstype":"GAUGE"},"up":{"display":true,"unit":"","description":"Status","dstype":"GAUGE"},"size":{"display":false,"unit":"B","description":"Content Size","dstype":"GAUGE"}}','{"time":{"display":true,"unit":"s","description":"Response Time","dstype":"GAUGE"},"up":{"display":true,"unit":"","description":"Status","dstype":"GAUGE"},"size":{"display":false,"unit":"B","description":"Content Size","dstype":"GAUGE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268561,2268382,1124,'URL','URL');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268560,2268382,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268559,2268382,1126,'{"host":"localhost","port":"8080","url":"/","wait":"15","expect":"200 OK","regex":""}','{"host":"localhost","port":"8080","url":"/","wait":"15","expect":"200 OK","regex":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268558,2268382,1127,'/opt/nagios/libexec/check_http_status.sh $ARG1$ $ARG2$ "$ARG3$" $ARG4$ "$ARG5$" "$ARG6$"','/opt/nagios/libexec/check_http_status.sh $ARG1$ $ARG2$ "$ARG3$" $ARG4$ "$ARG5$" "$ARG6$"');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268557,2268382,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268556,2268382,1125,'check_http_status!#{cmd_options[:host]}!#{cmd_options[:port]}!#{cmd_options[:url]}!#{cmd_options[:wait]}!#{cmd_options[:expect]}!#{cmd_options[:regex]}','check_http_status!#{cmd_options[:host]}!#{cmd_options[:port]}!#{cmd_options[:url]}!#{cmd_options[:wait]}!#{cmd_options[:expect]}!#{cmd_options[:regex]}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268555,2268384,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268554,2268384,1127,'/opt/nagios/libexec/check_load -w $ARG1$ -c $ARG2$','/opt/nagios/libexec/check_load -w $ARG1$ -c $ARG2$');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268553,2268384,1133,'{"min":0}','{"min":0}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268552,2268384,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268551,2268384,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268550,2268384,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268549,2268384,1125,'check_local_load!5.0,4.0,3.0!10.0,6.0,4.0','check_local_load!5.0,4.0,3.0!10.0,6.0,4.0');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268548,2268384,1128,'{"load1":{"display":true,"unit":"","description":"Load 1min Average"},"load5":{"display":true,"unit":"","description":"Load 5min Average"},"load15":{"display":true,"unit":"","description":"Load 15min Average"}}','{"load1":{"display":true,"unit":"","description":"Load 1min Average"},"load5":{"display":true,"unit":"","description":"Load 5min Average"},"load15":{"display":true,"unit":"","description":"Load 15min Average"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268547,2268384,1124,'Load','Load');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268546,2268384,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268545,2268384,1131,'5','5');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268544,2268384,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268543,2268386,1128,'{"total":{"display":true,"unit":"KB","description":"Total Memory"},"used":{"display":true,"unit":"KB","description":"Used Memory"},"free":{"display":true,"unit":"KB","description":"Free Memory"},"caches":{"display":true,"unit":"KB","description":"Cache Memory"}}','{"total":{"display":true,"unit":"KB","description":"Total Memory"},"used":{"display":true,"unit":"KB","description":"Used Memory"},"free":{"display":true,"unit":"KB","description":"Free Memory"},"caches":{"display":true,"unit":"KB","description":"Cache Memory"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268542,2268386,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268541,2268386,1127,'/opt/nagios/libexec/check_mem.pl -Cu -w $ARG1$ -c $ARG2$','/opt/nagios/libexec/check_mem.pl -Cu -w $ARG1$ -c $ARG2$');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268540,2268386,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268539,2268386,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268538,2268386,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268537,2268386,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268536,2268386,1125,'check_local_mem!90!95','check_local_mem!90!95');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268535,2268386,1124,'Memory','Memory');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268534,2268386,1133,'{"min":0,"unit":"KB"}','{"min":0,"unit":"KB"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268533,2268386,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268532,2268386,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268531,2268388,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268530,2268388,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268529,2268388,1126,'{"url":"","format":""}','{"url":"","format":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268528,2268388,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268527,2268388,1127,'/opt/nagios/libexec/check_http_value.rb $ARG1$ $ARG2$','/opt/nagios/libexec/check_http_value.rb $ARG1$ $ARG2$');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268526,2268388,1124,'HttpValue','HttpValue');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268525,2268388,1125,'check_http_value!#{cmd_options[:url]}!#{cmd_options[:format]}','check_http_value!#{cmd_options[:url]}!#{cmd_options[:format]}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268524,2268388,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268523,2268388,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268522,2268388,1128,'{"value":{"display":true,"unit":"","description":"value","dstype":"DERIVE"}}','{"value":{"display":true,"unit":"","description":"value","dstype":"DERIVE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268521,2268388,1132,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268520,2268388,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268519,2268390,1124,'Usage','Usage');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268518,2268390,1133,'{"min":0,"unit":"Percent used"}','{"min":0,"unit":"Percent used"}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268517,2268390,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268516,2268390,1127,'/opt/nagios/libexec/check_disk_use.sh $ARG1$','/opt/nagios/libexec/check_disk_use.sh $ARG1$');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268515,2268390,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268514,2268390,1128,'{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}','{"space_used":{"display":true,"unit":"%","description":"Disk Space Percent Used"},"inode_used":{"display":true,"unit":"%","description":"Disk Inode Percent Used"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268513,2268390,1125,'check_disk_use!:::node.workorder.rfcCi.ciAttributes.mount_point:::','check_disk_use!:::node.workorder.rfcCi.ciAttributes.mount_point:::');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268512,2268390,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268511,2268390,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268510,2268390,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268509,2268390,1132,'{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}','{"LowDiskSpace":{"bucket":"5m","stat":"avg","metric":"space_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"},"LowDiskInode":{"bucket":"5m","stat":"avg","metric":"inode_used","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268508,2268390,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268506,2268391,9461,'/app','/app');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268505,2268391,12285,'no-raid','no-raid');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268504,2268391,9458,'100%FREE','100%FREE');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268503,2268391,9460,'ext4','ext4');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268502,2268391,9462,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268501,2268391,9459,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268500,2268393,1131,'3','3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268499,2268393,1132,'{"HighThreadUse":{"bucket":"5m","stat":"avg","metric":"percentBusy","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}','{"HighThreadUse":{"bucket":"5m","stat":"avg","metric":"percentBusy","trigger":{"operator":"\u003e","value":90,"duration":5,"numocc":1},"reset":{"operator":"\u003c","value":90,"duration":5,"numocc":1},"state":"notify"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268498,2268393,1129,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268497,2268393,1128,'{"currentThreadsBusy":{"display":true,"unit":"","description":"Busy Threads","dstype":"GAUGE"},"maxThreads":{"display":true,"unit":"","description":"Maximum Threads","dstype":"GAUGE"},"currentThreadCount":{"display":true,"unit":"","description":"Ready Threads","dstype":"GAUGE"},"percentBusy":{"display":true,"unit":"Percent","description":"Percent Busy Threads","dstype":"GAUGE"}}','{"currentThreadsBusy":{"display":true,"unit":"","description":"Busy Threads","dstype":"GAUGE"},"maxThreads":{"display":true,"unit":"","description":"Maximum Threads","dstype":"GAUGE"},"currentThreadCount":{"display":true,"unit":"","description":"Ready Threads","dstype":"GAUGE"},"percentBusy":{"display":true,"unit":"Percent","description":"Percent Busy Threads","dstype":"GAUGE"}}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268496,2268393,1130,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268495,2268393,1126,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268494,2268393,1124,'ThreadInfo','ThreadInfo');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268493,2268393,1125,'check_tomcat_thread','check_tomcat_thread');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268492,2268393,1123,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268491,2268393,1127,'/opt/nagios/libexec/check_tomcat.rb ThreadInfo','/opt/nagios/libexec/check_tomcat.rb ThreadInfo');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268490,2268393,1133,'{"min":0,"unit":""}','{"min":0,"unit":""}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268489,2268393,1122,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268488,2268397,8508,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268487,2268397,8507,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268486,2268397,8499,'roundrobin','roundrobin');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268485,2268397,8503,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268484,2268397,8500,'','');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268483,2268397,8505,'{}','{}');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268482,2268397,8498,'["http 80 http 8080"]','["http 80 http 8080"]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268481,2268397,8502,'default','default');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268480,2268397,8504,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268479,2268397,8501,'cookieinsert','cookieinsert');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268478,2268398,8181,'proximity','proximity');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268477,2268398,8179,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268476,2268398,8180,'platform','platform');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268475,2268398,8178,'60','60');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268474,2268398,14820,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268473,2268398,8176,'[]','[]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268472,2268400,8356,'jdk','jdk');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268471,2268400,8357,'8','8');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268470,2268400,8361,'/usr/lib/jvm','/usr/lib/jvm');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268469,2268400,8355,'openjdk','openjdk');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268468,2268400,8362,'true','true');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268467,2268421,9254,'[ "22 22 tcp 0.0.0.0/0", "8080 8080 tcp 0.0.0.0/0", "8009 8009 tcp 0.0.0.0/0", "8443 8443 tcp 0.0.0.0/0" ]','[ "22 22 tcp 0.0.0.0/0", "8080 8080 tcp 0.0.0.0/0", "8009 8009 tcp 0.0.0.0/0", "8443 8443 tcp 0.0.0.0/0" ]');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268466,2268435,1110,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268465,2268435,1111,'com.test.pl.paasdemos','com.test.pl.paasdemos');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268464,2268435,1112,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268462,2268437,1110,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268461,2268437,1112,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268459,2268437,1111,'paas-perf','paas-perf');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268458,2268439,1111,'PaaS','PaaS');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268457,2268439,1112,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268455,2268439,1110,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268454,2268441,1110,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268453,2268441,1111,'pox_releases','pox_releases');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268452,2268441,1112,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268449,2268443,1110,'false','false');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268448,2268443,1111,'2.59','2.59');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268447,2268443,1112,'::ENCRYPTED::d016fa166427beb3','::ENCRYPTED::d016fa166427beb3');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (185200,173343,1137,'local-dev','local-dev',NULL,NULL,TIMESTAMP '2015-08-07 14:20:08.405',TIMESTAMP '2015-08-07 14:20:08.405');

INSERT INTO cm_ci_attributes (ci_attribute_id,ci_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (185199,173343,1138,'','',NULL,NULL,TIMESTAMP '2015-08-07 14:20:08.405',TIMESTAMP '2015-08-07 14:20:08.405');




INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268358,2268356,839880,'839880-1296-2268357',1296,2268357,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268445,2268356,2268357,'2268357-1300-368275',1300,368275,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268444,2268356,2268443,'2268443-6478-2268357',6478,2268357,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268442,2268356,2268441,'2268441-6478-2268357',6478,2268357,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268440,2268356,2268439,'2268439-6478-2268357',6478,2268357,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268438,2268356,2268437,'2268437-6478-2268357',6478,2268357,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268436,2268356,2268435,'2268435-6478-2268357',6478,2268357,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268434,2268356,2268357,'2268357-6427-2268372',6427,2268372,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268433,2268356,2268357,'2268357-6427-2268421',6427,2268421,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268432,2268356,2268357,'2268357-6427-2268391',6427,2268391,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268431,2268356,2268357,'2268357-6427-2268366',6427,2268366,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268430,2268356,2268357,'2268357-6427-2268419',6427,2268419,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268429,2268356,2268357,'2268357-6427-2268360',6427,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268428,2268356,2268357,'2268357-6427-2268400',6427,2268400,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268427,2268356,2268357,'2268357-6427-2268398',6427,2268398,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268426,2268356,2268357,'2268357-6427-2268369',6427,2268369,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268425,2268356,2268357,'2268357-6427-2268397',6427,2268397,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268424,2268356,2268357,'2268357-6427-2268363',6427,2268363,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268423,2268356,2268357,'2268357-1373-2268398',1373,2268398,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268422,2268356,2268360,'2268360-1355-2268421',1355,2268421,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268420,2268356,2268360,'2268360-6446-2268419',6446,2268419,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268418,2268356,2268391,'2268391-1355-2268369',1355,2268369,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268417,2268356,2268391,'2268391-6399-2268360',6399,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268416,2268356,2268397,'2268397-1355-2268360',1355,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268415,2268356,2268366,'2268366-1355-2268369',1355,2268369,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268414,2268356,2268366,'2268366-1355-2268400',1355,2268400,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268413,2268356,2268366,'2268366-1355-2268391',1355,2268391,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268412,2268356,2268366,'2268366-6399-2268360',6399,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268411,2268356,2268372,'2268372-1355-2268360',1355,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268410,2268356,2268372,'2268372-1355-2268363',1355,2268363,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268409,2268356,2268372,'2268372-1355-2268366',1355,2268366,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268408,2268356,2268372,'2268372-6399-2268360',6399,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268407,2268356,2268363,'2268363-1355-2268366',1355,2268366,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268406,2268356,2268363,'2268363-1355-2268391',1355,2268391,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268405,2268356,2268363,'2268363-1355-2268369',1355,2268369,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268404,2268356,2268363,'2268363-6399-2268360',6399,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268403,2268356,2268400,'2268400-1355-2268360',1355,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268402,2268356,2268400,'2268400-1355-2268369',1355,2268369,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268401,2268356,2268400,'2268400-6399-2268360',6399,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268399,2268356,2268398,'2268398-1355-2268397',1355,2268397,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268396,2268356,2268369,'2268369-1355-2268360',1355,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268395,2268356,2268369,'2268369-6399-2268360',6399,2268360,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268394,2268356,2268366,'2268366-6490-2268393',6490,2268393,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268392,2268356,2268391,'2268391-6490-2268390',6490,2268390,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268389,2268356,2268366,'2268366-6490-2268388',6490,2268388,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268387,2268356,2268369,'2268369-6490-2268386',6490,2268386,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268385,2268356,2268369,'2268369-6490-2268384',6490,2268384,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268383,2268356,2268363,'2268363-6490-2268382',6490,2268382,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268381,2268356,2268366,'2268366-6490-2268380',6490,2268380,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268379,2268356,2268366,'2268366-6490-2268378',6490,2268378,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268377,2268356,2268369,'2268369-6490-2268376',6490,2268376,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268375,2268356,2268369,'2268369-6490-2268374',6490,2268374,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268373,2268356,2268372,'2268372-6490-2268371',6490,2268371,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268370,2268356,2268369,'2268369-6490-2268368',6490,2268368,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268367,2268356,2268366,'2268366-6490-2268365',6490,2268365,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268364,2268356,2268363,'2268363-6490-2268362',6490,2268362,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (2268361,2268356,2268360,'2268360-6490-2268359',6490,2268359,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id,last_applied_rfc_id,comments,created_by,update_by,created,updated) VALUES (840151,839903,840150,'840150-6478-839880',6478,839880,100,510880,'{"toCiName":"dev","toCiClass":"manifest.Environment","fromCiClass":"manifest.Globalvar","fromCiName":"testkey"}',NULL,NULL,TIMESTAMP '2015-12-28 14:33:46.148',TIMESTAMP '2015-12-28 14:33:46.148');

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id,last_applied_rfc_id,comments,created_by,update_by,created,updated) VALUES (2267162,277111,277108,'277108-1292-2267117',1292,2267117,100,1989283,'{"toCiName":"srv1","toCiClass":"catalog.Platform","fromCiClass":"account.Assembly","fromCiName":"prod1"}',NULL,NULL,TIMESTAMP '2017-06-28 14:30:27.374',TIMESTAMP '2017-06-28 14:30:27.374');

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id,last_applied_rfc_id,comments,created_by,update_by,created,updated) VALUES (839897,277111,277108,'277108-6418-839880',6418,839880,100,NULL,'',NULL,NULL,TIMESTAMP '2015-12-28 14:29:20.984',TIMESTAMP '2015-12-28 14:29:20.984');

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id,last_applied_rfc_id,comments,created_by,update_by,created,updated) VALUES (658687,277111,658686,'658686-6470-277108',6470,277108,100,289040,'{"toCiName":"prod1","toCiClass":"account.Assembly","fromCiClass":"catalog.Globalvar","fromCiName":"testkey"}',NULL,NULL,TIMESTAMP '2015-10-27 11:13:34.305',TIMESTAMP '2015-10-27 11:13:34.305');

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id,last_applied_rfc_id,comments,created_by,update_by,created,updated) VALUES (277112,173345,173343,'173343-6402-277108',6402,277108,100,NULL,'',NULL,NULL,TIMESTAMP '2015-08-10 10:24:41.660',TIMESTAMP '2015-08-10 10:24:41.660');

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1465586,1220883,1221086,'1221086-6486-1465573',6486,1465573,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221096,1220883,1220884,'1220884-6434-1221086',6434,1221086,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220928,1220883,1220884,'1220884-6434-1220907',6434,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351864,1220883,1351765,'1351765-6406-1351861',6406,1351861,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351860,1220883,1220990,'1220990-6406-1351857',6406,1351857,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351829,1220883,1221226,'1221226-1331-1221164',1331,1221164,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351818,1220883,1351765,'1351765-1331-1221130',1331,1221130,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351817,1220883,1351753,'1351753-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221443,1220883,1221000,'1221000-1331-1220990',1331,1220990,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351806,1220883,1351753,'1351753-1331-1220958',1331,1220958,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351795,1220883,1351753,'1351753-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351784,1220883,1221248,'1221248-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351773,1220883,1220974,'1220974-1331-1220958',1331,1220958,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351767,1220883,1220884,'1220884-6434-1351765',6434,1351765,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1351757,1220883,1220884,'1220884-6434-1351753',6434,1351753,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220952,1220883,1220884,'1220884-6434-1220945',6434,1220945,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222186,1220883,1221130,'1221130-6406-1222183',6406,1222183,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222182,1220883,1221069,'1221069-6406-1222174',6406,1222174,200);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222181,1220883,1221069,'1221069-6406-1222178',6406,1222178,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222177,1220883,1221000,'1221000-6406-1222174',6406,1222174,200);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222173,1220883,1220945,'1220945-6406-1222170',6406,1222170,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222169,1220883,1220945,'1220945-6406-1222166',6406,1222166,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222165,1220883,1220945,'1220945-6406-1222162',6406,1222162,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222161,1220883,1220945,'1220945-6406-1222158',6406,1222158,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222157,1220883,1220945,'1220945-6406-1222154',6406,1222154,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222153,1220883,1220945,'1220945-6406-1222150',6406,1222150,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222149,1220883,1220907,'1220907-6406-1222146',6406,1222146,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222145,1220883,1220897,'1220897-6406-1222142',6406,1222142,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222139,1220883,1221248,'1221248-6486-1222126',6486,1222126,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222123,1220883,1221248,'1221248-6486-1222110',6486,1222110,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222107,1220883,1221226,'1221226-6486-1222094',6486,1222094,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222091,1220883,1221164,'1221164-6486-1222078',6486,1222078,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222075,1220883,1221164,'1221164-6486-1222062',6486,1222062,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222059,1220883,1221164,'1221164-6486-1222046',6486,1222046,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222043,1220883,1221164,'1221164-6486-1222030',6486,1222030,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222027,1220883,1221164,'1221164-6486-1222014',6486,1222014,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1222011,1220883,1221056,'1221056-6486-1221998',6486,1221998,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221995,1220883,1221000,'1221000-6486-1221982',6486,1221982,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221979,1220883,1220934,'1220934-6486-1221966',6486,1221966,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221963,1220883,1220907,'1220907-6486-1221950',6486,1221950,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221947,1220883,1220907,'1220907-6486-1221934',6486,1221934,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221931,1220883,1220907,'1220907-6486-1221918',6486,1221918,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221915,1220883,1220907,'1220907-6486-1221902',6486,1221902,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221899,1220883,1220907,'1220907-6486-1221886',6486,1221886,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221883,1220883,1220897,'1220897-6486-1221870',6486,1221870,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221869,1220883,1220884,'1220884-1375-1220945',1375,1220945,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221868,1220883,1221226,'1221226-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221867,1220883,1221239,'1221239-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221866,1220883,1221297,'1221297-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221865,1220883,1221272,'1221272-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221864,1220883,1221248,'1221248-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221863,1220883,1221164,'1221164-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221852,1220883,1221239,'1221239-1331-1221086',1331,1221086,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221841,1220883,1221226,'1221226-1331-1221239',1331,1221239,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221830,1220883,1221226,'1221226-1331-1221248',1331,1221248,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221819,1220883,1221297,'1221297-1331-1221042',1331,1221042,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221808,1220883,1221239,'1221239-1331-1221297',1331,1221297,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221797,1220883,1221297,'1221297-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221786,1220883,1221297,'1221297-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221775,1220883,1221056,'1221056-1331-1221272',1331,1221272,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221764,1220883,1221056,'1221056-1331-1221248',1331,1221248,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221753,1220883,1221272,'1221272-1331-1221042',1331,1221042,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221742,1220883,1221272,'1221272-1331-1221164',1331,1221164,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221731,1220883,1221272,'1221272-1331-1221024',1331,1221024,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221720,1220883,1221248,'1221248-1331-1221000',1331,1221000,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221709,1220883,1221248,'1221248-1331-1221272',1331,1221272,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221698,1220883,1221248,'1221248-1331-1221042',1331,1221042,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221687,1220883,1221248,'1221248-1331-1221164',1331,1221164,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221676,1220883,1221248,'1221248-1331-1221024',1331,1221024,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221665,1220883,1221164,'1221164-1331-1221239',1331,1221239,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221654,1220883,1221164,'1221164-1331-1221000',1331,1221000,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221643,1220883,1221164,'1221164-1331-1221297',1331,1221297,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221632,1220883,1221226,'1221226-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221621,1220883,1221164,'1221164-1331-1220958',1331,1220958,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221610,1220883,1221164,'1221164-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221599,1220883,1220945,'1220945-1331-1221130',1331,1221130,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221588,1220883,1221130,'1221130-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221577,1220883,1221130,'1221130-1331-1221148',1331,1221148,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221576,1220883,1220897,'1220897-6453-1221069',6453,1221069,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221575,1220883,1221115,'1221115-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221574,1220883,1220934,'1220934-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221573,1220883,1221086,'1221086-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221572,1220883,1221056,'1221056-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221571,1220883,1221024,'1221024-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221570,1220883,1221042,'1221042-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221569,1220883,1221012,'1221012-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221568,1220883,1221000,'1221000-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221567,1220883,1221032,'1221032-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221566,1220883,1220974,'1220974-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221565,1220883,1220958,'1220958-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221564,1220883,1220907,'1220907-6396-1220897',6396,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221553,1220883,1221024,'1221024-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221542,1220883,1221115,'1221115-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221531,1220883,1221032,'1221032-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221520,1220883,1221032,'1221032-1331-1221000',1331,1221000,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221509,1220883,1221042,'1221042-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221498,1220883,1221056,'1221056-1331-1221024',1331,1221024,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221487,1220883,1221056,'1221056-1331-1221042',1331,1221042,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221476,1220883,1221056,'1221056-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221465,1220883,1221000,'1221000-1331-1220958',1331,1220958,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221454,1220883,1221012,'1221012-1331-1221000',1331,1221000,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221432,1220883,1220990,'1220990-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221421,1220883,1220934,'1220934-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221410,1220883,1220934,'1220934-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221399,1220883,1221012,'1221012-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221388,1220883,1221086,'1221086-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221377,1220883,1221000,'1221000-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221366,1220883,1220974,'1220974-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221355,1220883,1220958,'1220958-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221344,1220883,1221102,'1221102-1331-1220907',1331,1220907,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221333,1220883,1221102,'1221102-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221322,1220883,1220907,'1220907-1331-1220897',1331,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221311,1220883,1220897,'1220897-1331-1221077',1331,1221077,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221305,1220883,1220884,'1220884-6434-1221297',6434,1221297,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221291,1220883,1220884,'1220884-6434-1221272',6434,1221272,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221266,1220883,1220884,'1220884-6434-1221248',6434,1221248,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221242,1220883,1220884,'1220884-6434-1221239',6434,1221239,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221233,1220883,1220884,'1220884-6434-1221226',6434,1221226,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221220,1220883,1220884,'1220884-6434-1221164',6434,1221164,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221158,1220883,1220884,'1220884-6434-1221148',6434,1221148,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221142,1220883,1220884,'1220884-6434-1221130',6434,1221130,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221124,1220883,1220884,'1220884-6434-1221115',6434,1221115,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221109,1220883,1220884,'1220884-6434-1221102',6434,1221102,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221080,1220883,1220884,'1220884-6434-1221077',6434,1221077,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221071,1220883,1220884,'1220884-6434-1221069',6434,1221069,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221063,1220883,1220884,'1220884-6434-1221056',6434,1221056,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221050,1220883,1220884,'1220884-6434-1221042',6434,1221042,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221036,1220883,1220884,'1220884-6434-1221032',6434,1221032,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221026,1220883,1220884,'1220884-6434-1221024',6434,1221024,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221018,1220883,1220884,'1220884-6434-1221012',6434,1221012,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1221006,1220883,1220884,'1220884-6434-1221000',6434,1221000,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220994,1220883,1220884,'1220884-6434-1220990',6434,1220990,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220984,1220883,1220884,'1220884-6434-1220974',6434,1220974,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220968,1220883,1220884,'1220884-6434-1220958',6434,1220958,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220939,1220883,1220884,'1220884-6434-1220934',6434,1220934,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id) VALUES (1220901,1220883,1220884,'1220884-6434-1220897',6434,1220897,100);

INSERT INTO cm_ci_relations (ci_relation_id,ns_id,from_ci_id,relation_goid,relation_id,to_ci_id,ci_state_id,last_applied_rfc_id,comments,created_by,update_by,created,updated) VALUES (368301,173346,368275,'368275-6408-368280',6408,368280,100,NULL,'',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-08-28 11:11:06.808');




INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value,owner,comments,created,updated) VALUES (368302,368301,6409,'compute','compute',NULL,NULL,TIMESTAMP '2015-08-28 11:11:06.808',TIMESTAMP '2015-08-28 11:11:06.808');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1465589,1465586,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1465588,1465586,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1465587,1465586,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221100,1221096,6435,'*certificate','*certificate');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221101,1221096,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221099,1221096,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221098,1221096,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221097,1221096,6438,'certificate','certificate');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220932,1220928,6435,'compute,dns,*mirror,*ntp,*windows-domain','compute,dns,*mirror,*ntp,*windows-domain');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220933,1220928,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220931,1220928,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220930,1220928,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220929,1220928,6438,'os','os');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351839,1351829,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351838,1351829,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351837,1351829,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351836,1351829,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351835,1351829,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351834,1351829,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351833,1351829,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351832,1351829,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351831,1351829,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351830,1351829,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351828,1351818,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351827,1351818,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351826,1351818,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351825,1351818,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351824,1351818,1341,'both','both');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351823,1351818,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351822,1351818,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351821,1351818,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351820,1351818,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351819,1351818,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221449,1221443,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221453,1221443,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221452,1221443,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221451,1221443,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221450,1221443,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221448,1221443,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221447,1221443,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221446,1221443,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221445,1221443,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221444,1221443,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351816,1351806,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351815,1351806,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351814,1351806,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351813,1351806,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351812,1351806,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351811,1351806,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351810,1351806,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351809,1351806,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351808,1351806,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351807,1351806,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351805,1351795,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351804,1351795,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351803,1351795,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351802,1351795,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351801,1351795,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351800,1351795,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351799,1351795,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351798,1351795,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351797,1351795,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351796,1351795,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351794,1351784,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351793,1351784,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351792,1351784,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351791,1351784,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351790,1351784,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351789,1351784,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351788,1351784,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351787,1351784,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351786,1351784,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351785,1351784,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351783,1351773,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351782,1351773,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351781,1351773,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351780,1351773,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351779,1351773,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351778,1351773,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351777,1351773,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351776,1351773,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351775,1351773,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351774,1351773,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351772,1351767,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351771,1351767,6435,'firewall','firewall');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351770,1351767,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351769,1351767,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351768,1351767,6438,'firewall','firewall');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351762,1351757,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351761,1351757,6435,'filestore','filestore');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351760,1351757,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351759,1351757,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351758,1351757,6438,'objectstore','objectstore');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220956,1220952,6435,'compute,dns,*gdns','compute,dns,*gdns');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220957,1220952,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220955,1220952,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220954,1220952,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220953,1220952,6438,'fqdn','fqdn');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351850,1222139,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222141,1222139,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222140,1222139,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351849,1222123,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222125,1222123,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222124,1222123,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351856,1222107,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222109,1222107,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222108,1222107,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351855,1222091,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222093,1222091,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222092,1222091,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351854,1222075,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222077,1222075,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222076,1222075,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351853,1222059,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222061,1222059,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222060,1222059,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351852,1222043,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222045,1222043,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222044,1222043,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351851,1222027,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222029,1222027,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222028,1222027,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351848,1222011,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222013,1222011,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1222012,1222011,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351847,1221995,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221997,1221995,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221996,1221995,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351846,1221979,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221981,1221979,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221980,1221979,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351845,1221963,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221965,1221963,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221964,1221963,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351844,1221947,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221949,1221947,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221948,1221947,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351843,1221931,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221933,1221931,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221932,1221931,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351842,1221915,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221917,1221915,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221916,1221915,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351841,1221899,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221901,1221899,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221900,1221899,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1351840,1221883,14409,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221885,1221883,6487,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221884,1221883,6488,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221862,1221852,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221861,1221852,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221860,1221852,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221859,1221852,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221858,1221852,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221857,1221852,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221856,1221852,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221855,1221852,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221854,1221852,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221853,1221852,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221851,1221841,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221850,1221841,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221849,1221841,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221848,1221841,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221847,1221841,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221846,1221841,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221845,1221841,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221844,1221841,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221843,1221841,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221842,1221841,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221840,1221830,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221839,1221830,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221838,1221830,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221837,1221830,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221836,1221830,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221835,1221830,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221834,1221830,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221833,1221830,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221832,1221830,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221831,1221830,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221829,1221819,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221828,1221819,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221827,1221819,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221826,1221819,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221825,1221819,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221824,1221819,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221823,1221819,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221822,1221819,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221821,1221819,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221820,1221819,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221818,1221808,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221817,1221808,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221816,1221808,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221815,1221808,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221814,1221808,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221813,1221808,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221812,1221808,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221811,1221808,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221810,1221808,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221809,1221808,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221807,1221797,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221806,1221797,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221805,1221797,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221804,1221797,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221803,1221797,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221802,1221797,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221801,1221797,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221800,1221797,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221799,1221797,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221798,1221797,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221796,1221786,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221795,1221786,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221794,1221786,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221793,1221786,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221792,1221786,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221791,1221786,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221790,1221786,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221789,1221786,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221788,1221786,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221787,1221786,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221785,1221775,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221784,1221775,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221783,1221775,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221782,1221775,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221781,1221775,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221780,1221775,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221779,1221775,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221778,1221775,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221777,1221775,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221776,1221775,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221774,1221764,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221773,1221764,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221772,1221764,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221771,1221764,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221770,1221764,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221769,1221764,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221768,1221764,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221767,1221764,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221766,1221764,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221765,1221764,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221763,1221753,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221762,1221753,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221761,1221753,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221760,1221753,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221759,1221753,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221758,1221753,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221757,1221753,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221756,1221753,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221755,1221753,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221754,1221753,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221752,1221742,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221751,1221742,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221750,1221742,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221749,1221742,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221748,1221742,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221747,1221742,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221746,1221742,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221745,1221742,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221744,1221742,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221743,1221742,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221741,1221731,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221740,1221731,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221739,1221731,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221738,1221731,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221737,1221731,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221736,1221731,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221735,1221731,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221734,1221731,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221733,1221731,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221732,1221731,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221730,1221720,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221729,1221720,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221728,1221720,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221727,1221720,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221726,1221720,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221725,1221720,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221724,1221720,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221723,1221720,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221722,1221720,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221721,1221720,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221719,1221709,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221718,1221709,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221717,1221709,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221716,1221709,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221715,1221709,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221714,1221709,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221713,1221709,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221712,1221709,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221711,1221709,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221710,1221709,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221708,1221698,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221707,1221698,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221706,1221698,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221705,1221698,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221704,1221698,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221703,1221698,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221702,1221698,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221701,1221698,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221700,1221698,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221699,1221698,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221697,1221687,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221696,1221687,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221695,1221687,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221694,1221687,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221693,1221687,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221692,1221687,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221691,1221687,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221690,1221687,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221689,1221687,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221688,1221687,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221686,1221676,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221685,1221676,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221684,1221676,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221683,1221676,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221682,1221676,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221681,1221676,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221680,1221676,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221679,1221676,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221678,1221676,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221677,1221676,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221675,1221665,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221674,1221665,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221673,1221665,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221672,1221665,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221671,1221665,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221670,1221665,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221669,1221665,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221668,1221665,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221667,1221665,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221666,1221665,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221664,1221654,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221663,1221654,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221662,1221654,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221661,1221654,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221660,1221654,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221659,1221654,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221658,1221654,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221657,1221654,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221656,1221654,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221655,1221654,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221653,1221643,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221652,1221643,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221651,1221643,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221650,1221643,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221649,1221643,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221648,1221643,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221647,1221643,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221646,1221643,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221645,1221643,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221644,1221643,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221642,1221632,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221641,1221632,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221640,1221632,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221639,1221632,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221638,1221632,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221637,1221632,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221636,1221632,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221635,1221632,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221634,1221632,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221633,1221632,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221631,1221621,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221630,1221621,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221629,1221621,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221628,1221621,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221627,1221621,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221626,1221621,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221625,1221621,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221624,1221621,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221623,1221621,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221622,1221621,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221620,1221610,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221619,1221610,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221618,1221610,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221617,1221610,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221616,1221610,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221615,1221610,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221614,1221610,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221613,1221610,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221612,1221610,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221611,1221610,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221609,1221599,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221608,1221599,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221607,1221599,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221606,1221599,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221605,1221599,1341,'both','both');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221604,1221599,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221603,1221599,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221602,1221599,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221601,1221599,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221600,1221599,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221598,1221588,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221597,1221588,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221596,1221588,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221595,1221588,1332,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221594,1221588,1341,'both','both');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221593,1221588,1338,'10','10');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221592,1221588,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221591,1221588,1336,'2','2');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221590,1221588,1337,'2','2');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221589,1221588,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221587,1221577,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221586,1221577,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221585,1221577,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221584,1221577,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221583,1221577,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221582,1221577,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221581,1221577,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221580,1221577,1336,'0','0');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221579,1221577,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221578,1221577,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221563,1221553,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221562,1221553,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221561,1221553,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221560,1221553,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221559,1221553,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221558,1221553,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221557,1221553,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221556,1221553,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221555,1221553,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221554,1221553,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221552,1221542,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221551,1221542,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221550,1221542,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221549,1221542,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221548,1221542,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221547,1221542,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221546,1221542,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221545,1221542,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221544,1221542,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221543,1221542,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221541,1221531,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221540,1221531,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221539,1221531,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221538,1221531,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221537,1221531,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221536,1221531,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221535,1221531,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221534,1221531,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221533,1221531,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221532,1221531,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221530,1221520,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221529,1221520,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221528,1221520,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221527,1221520,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221526,1221520,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221525,1221520,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221524,1221520,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221523,1221520,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221522,1221520,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221521,1221520,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221519,1221509,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221518,1221509,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221517,1221509,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221516,1221509,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221515,1221509,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221514,1221509,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221513,1221509,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221512,1221509,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221511,1221509,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221510,1221509,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221508,1221498,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221507,1221498,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221506,1221498,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221505,1221498,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221504,1221498,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221503,1221498,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221502,1221498,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221501,1221498,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221500,1221498,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221499,1221498,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221497,1221487,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221496,1221487,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221495,1221487,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221494,1221487,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221493,1221487,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221492,1221487,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221491,1221487,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221490,1221487,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221489,1221487,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221488,1221487,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221486,1221476,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221485,1221476,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221484,1221476,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221483,1221476,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221482,1221476,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221481,1221476,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221480,1221476,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221479,1221476,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221478,1221476,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221477,1221476,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221475,1221465,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221474,1221465,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221473,1221465,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221472,1221465,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221471,1221465,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221470,1221465,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221469,1221465,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221468,1221465,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221467,1221465,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221466,1221465,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221464,1221454,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221463,1221454,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221462,1221454,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221461,1221454,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221460,1221454,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221459,1221454,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221458,1221454,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221457,1221454,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221456,1221454,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221455,1221454,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221442,1221432,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221441,1221432,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221440,1221432,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221439,1221432,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221438,1221432,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221437,1221432,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221436,1221432,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221435,1221432,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221434,1221432,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221433,1221432,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221431,1221421,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221430,1221421,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221429,1221421,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221428,1221421,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221427,1221421,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221426,1221421,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221425,1221421,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221424,1221421,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221423,1221421,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221422,1221421,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221420,1221410,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221419,1221410,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221418,1221410,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221417,1221410,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221416,1221410,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221415,1221410,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221414,1221410,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221413,1221410,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221412,1221410,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221411,1221410,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221409,1221399,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221408,1221399,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221407,1221399,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221406,1221399,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221405,1221399,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221404,1221399,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221403,1221399,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221402,1221399,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221401,1221399,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221400,1221399,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221398,1221388,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221397,1221388,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221396,1221388,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221395,1221388,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221394,1221388,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221393,1221388,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221392,1221388,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221391,1221388,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221390,1221388,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221389,1221388,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221387,1221377,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221386,1221377,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221385,1221377,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221384,1221377,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221383,1221377,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221382,1221377,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221381,1221377,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221380,1221377,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221379,1221377,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221378,1221377,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221376,1221366,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221375,1221366,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221374,1221366,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221373,1221366,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221372,1221366,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221371,1221366,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221370,1221366,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221369,1221366,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221368,1221366,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221367,1221366,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221365,1221355,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221364,1221355,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221363,1221355,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221362,1221355,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221361,1221355,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221360,1221355,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221359,1221355,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221358,1221355,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221357,1221355,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221356,1221355,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221354,1221344,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221353,1221344,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221352,1221344,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221351,1221344,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221350,1221344,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221349,1221344,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221348,1221344,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221347,1221344,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221346,1221344,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221345,1221344,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221343,1221333,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221342,1221333,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221341,1221333,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221340,1221333,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221339,1221333,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221338,1221333,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221337,1221333,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221336,1221333,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221335,1221333,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221334,1221333,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221328,1221322,1341,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221332,1221322,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221331,1221322,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221330,1221322,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221329,1221322,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221327,1221322,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221326,1221322,1333,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221325,1221322,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221324,1221322,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221323,1221322,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221321,1221311,1334,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221320,1221311,1340,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221319,1221311,1339,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221318,1221311,1332,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221317,1221311,1341,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221316,1221311,1338,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221315,1221311,1333,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221314,1221311,1336,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221313,1221311,1337,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221312,1221311,1335,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221310,1221305,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221309,1221305,6435,'*mirror','*mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221308,1221305,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221307,1221305,6439,'Java Programming Language Environment','Java Programming Language Environment');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221306,1221305,6438,'java','java');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221296,1221291,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221295,1221291,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221294,1221291,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221293,1221291,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221292,1221291,6438,'build','build');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221271,1221266,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221270,1221266,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221269,1221266,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221268,1221266,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221267,1221266,6438,'artifact','artifact');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221247,1221242,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221246,1221242,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221245,1221242,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221244,1221242,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221243,1221242,6438,'keystore','keystore');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221238,1221233,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221237,1221233,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221236,1221233,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221235,1221233,6439,'Restarts Tomcat','Restarts Tomcat');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221234,1221233,6438,'tomcat-daemon','tomcat-daemon');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221225,1221220,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221224,1221220,6435,'mirror','mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221223,1221220,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221222,1221220,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221221,1221220,6438,'tomcat','tomcat');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221162,1221158,6435,'*certificate','*certificate');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221163,1221158,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221161,1221158,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221160,1221158,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221159,1221158,6438,'lb-certificate','lb-certificate');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221147,1221142,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221146,1221142,6435,'compute,lb,dns','compute,lb,dns');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221145,1221142,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221144,1221142,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221143,1221142,6438,'lb','lb');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221129,1221124,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221128,1221124,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221127,1221124,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221126,1221124,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221125,1221124,6438,'sensuclient','sensuclient');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221114,1221109,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221113,1221109,6435,'dns','dns');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221112,1221109,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221111,1221109,6439,'optional hostname dns entry','optional hostname dns entry');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221110,1221109,6438,'hostname','hostname');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221085,1221080,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221084,1221080,6435,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221083,1221080,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221082,1221080,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221081,1221080,6438,'secgroup','secgroup');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221076,1221071,6436,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221075,1221071,6435,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221074,1221071,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221073,1221071,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221072,1221071,6438,'sshkeys','sshkeys');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221068,1221063,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221067,1221063,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221066,1221063,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221065,1221063,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221064,1221063,6438,'daemon','daemon');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221055,1221050,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221054,1221050,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221053,1221050,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221052,1221050,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221051,1221050,6438,'download','download');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221041,1221036,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221040,1221036,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221039,1221036,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221038,1221036,6439,'The optional <strong>file</strong> component can be used to create customized files.
For example, you can create configuration file needed for your applications or other components.
A file can also be a shell script which can be executed with the optional execute command attribute.
','The optional <strong>file</strong> component can be used to create customized files.
For example, you can create configuration file needed for your applications or other components.
A file can also be a shell script which can be executed with the optional execute command attribute.
');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221037,1221036,6438,'file','file');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221031,1221026,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221030,1221026,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221029,1221026,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221028,1221026,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221027,1221026,6438,'library','library');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221023,1221018,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221022,1221018,6435,'mirror','mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221021,1221018,6437,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221020,1221018,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221019,1221018,6438,'share','share');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221011,1221006,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221010,1221006,6435,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221009,1221006,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221008,1221006,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1221007,1221006,6438,'volume','volume');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220999,1220994,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220998,1220994,6435,'storage','storage');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220997,1220994,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220996,1220994,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220995,1220994,6438,'storage','storage');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220989,1220984,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220988,1220984,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220987,1220984,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220986,1220984,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220985,1220984,6438,'job','job');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220973,1220968,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220972,1220968,6435,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220971,1220968,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220970,1220968,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220969,1220968,6438,'user','user');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220944,1220939,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220943,1220939,6435,'mirror','mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220942,1220939,6437,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220941,1220939,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220940,1220939,6438,'logstash','logstash');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220905,1220901,6435,'compute,dns,*mirror','compute,dns,*mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220906,1220901,6436,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220904,1220901,6437,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220903,1220901,6439,NULL,NULL);

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (1220902,1220901,6438,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268789,2268358,1297,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2269002,2268445,1303,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2269001,2268445,1304,'active','active');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2269000,2268445,1302,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268999,2268445,1301,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268998,2268434,6430,'0..1','0..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268997,2268434,6431,'tomcat-daemon','tomcat-daemon');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268996,2268433,6431,'secgroup','secgroup');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268995,2268433,6428,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268994,2268433,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268993,2268432,6430,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268992,2268432,6428,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268991,2268432,6431,'volume','volume');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268990,2268431,6428,'mirror','mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268989,2268431,6431,'tomcat','tomcat');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268988,2268431,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268987,2268430,6428,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268986,2268430,6429,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268985,2268430,6431,'sshkeys','sshkeys');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268984,2268430,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268983,2268429,6431,'compute','compute');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268982,2268429,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268981,2268429,6428,'compute,dns,*mirror','compute,dns,*mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268980,2268428,6428,'*mirror','*mirror');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268979,2268428,6431,'java','java');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268978,2268428,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268977,2268427,6431,'fqdn','fqdn');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268976,2268427,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268975,2268427,6428,'compute,dns,*gdns','compute,dns,*gdns');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268974,2268426,6431,'os','os');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268973,2268426,6428,'compute,dns,*mirror,*ntp,*windows-domain','compute,dns,*mirror,*ntp,*windows-domain');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268972,2268426,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268971,2268425,6430,'1..1','1..1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268970,2268425,6431,'lb','lb');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268969,2268425,6428,'compute,lb,dns','compute,lb,dns');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268968,2268424,6431,'artifact','artifact');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268967,2268424,6430,'0..*','0..*');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268966,2268422,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268965,2268422,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268964,2268422,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268963,2268422,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268962,2268422,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268961,2268422,1357,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268960,2268422,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268959,2268422,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268958,2268418,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268957,2268418,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268956,2268418,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268955,2268418,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268954,2268418,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268953,2268418,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268952,2268418,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268951,2268418,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268950,2268416,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268949,2268416,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268948,2268416,1362,'10','10');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268947,2268416,1361,'2','2');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268946,2268416,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268945,2268416,1365,'both','both');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268944,2268416,1356,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268943,2268416,1360,'2','2');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268942,2268416,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268941,2268415,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268940,2268415,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268939,2268415,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268938,2268415,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268937,2268415,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268936,2268415,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268935,2268415,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268934,2268415,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268933,2268414,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268932,2268414,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268931,2268414,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268930,2268414,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268929,2268414,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268928,2268414,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268927,2268414,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268926,2268414,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268925,2268413,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268924,2268413,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268923,2268413,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268922,2268413,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268921,2268413,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268920,2268413,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268919,2268413,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268918,2268413,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268917,2268411,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268916,2268411,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268915,2268411,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268914,2268411,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268913,2268411,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268912,2268411,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268911,2268411,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268910,2268411,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268909,2268410,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268908,2268410,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268907,2268410,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268906,2268410,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268905,2268410,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268904,2268410,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268903,2268410,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268902,2268410,1365,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268901,2268410,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268900,2268409,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268899,2268409,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268898,2268409,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268897,2268409,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268896,2268409,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268895,2268409,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268894,2268409,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268893,2268409,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268892,2268407,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268891,2268407,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268890,2268407,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268889,2268407,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268888,2268407,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268887,2268407,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268886,2268407,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268885,2268407,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268884,2268406,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268883,2268406,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268882,2268406,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268881,2268406,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268880,2268406,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268879,2268406,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268878,2268406,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268877,2268406,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268876,2268405,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268875,2268405,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268874,2268405,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268873,2268405,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268872,2268405,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268871,2268405,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268870,2268405,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268869,2268405,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268868,2268403,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268867,2268403,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268866,2268403,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268865,2268403,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268864,2268403,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268863,2268403,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268862,2268403,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268861,2268403,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268860,2268402,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268859,2268402,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268858,2268402,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268857,2268402,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268856,2268402,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268855,2268402,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268854,2268402,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268853,2268402,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268852,2268399,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268851,2268399,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268850,2268399,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268849,2268399,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268848,2268399,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268847,2268399,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268846,2268399,1365,'both','both');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268845,2268399,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268844,2268399,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268843,2268396,1356,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268842,2268396,1363,'100','100');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268841,2268396,1359,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268840,2268396,1357,'false','false');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268839,2268396,1358,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268838,2268396,1362,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268837,2268396,1361,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268836,2268396,1365,'from','from');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268835,2268396,1360,'1','1');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268834,2268394,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268833,2268394,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268832,2268394,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268831,2268392,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268830,2268392,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268829,2268392,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268828,2268389,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268827,2268389,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268826,2268389,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268825,2268387,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268824,2268387,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268823,2268387,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268822,2268385,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268821,2268385,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268820,2268385,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268819,2268383,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268818,2268383,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268817,2268383,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268816,2268381,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268815,2268381,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268814,2268381,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268813,2268379,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268812,2268379,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268811,2268379,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268810,2268377,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268809,2268377,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268808,2268377,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268807,2268375,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268806,2268375,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268805,2268375,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268804,2268373,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268803,2268373,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268802,2268373,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268801,2268370,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268800,2268370,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268799,2268370,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268798,2268367,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268797,2268367,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268796,2268367,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268795,2268364,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268794,2268364,14411,'design','design');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268793,2268364,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268792,2268361,6492,'true','true');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268791,2268361,6491,' ',' ');

INSERT INTO cm_ci_relation_attributes (ci_rel_attribute_id,ci_relation_id,attribute_id,df_attribute_value,dj_attribute_value) VALUES (2268790,2268361,14411,'design','design');

INSERT INTO dj_releases (release_id,ns_id,release_name,created_by,release_state_id,revision) VALUES (1991429,840155,'/local-dev/prod1/dev/bom1991429','bannama',100,1);



INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991430,1991429,2269403,2269003,9266,'secgroup-368275-1','2269003-9266-2269403',100,'bannama',NULL,2,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991437,1991429,2269406,2269003,8462,'sshkeys-368275-1','2269003-8462-2269406',100,'bannama',NULL,1,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991443,1991429,2269409,2269003,7815,'compute-368275-1','2269003-7815-2269409',100,'bannama',NULL,3,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991454,1991429,2269412,2269003,7815,'compute-368275-2','2269003-7815-2269412',100,'bannama',NULL,3,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991465,1991429,2269415,2269003,9029,'os-368275-1','2269003-9029-2269415',100,'bannama',NULL,4,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991489,1991429,2269418,2269003,8533,'lb-368275-1','2269003-8533-2269418',100,'bannama',NULL,4,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991505,1991429,2269421,2269003,9029,'os-368275-2','2269003-9029-2269421',100,'bannama',NULL,4,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991529,1991429,2269424,2269003,9482,'volume1-368275-1','2269003-9482-2269424',100,'bannama',NULL,5,true,NULL,'',TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991541,1991429,2269427,2269003,8372,'java-368275-1','2269003-8372-2269427',100,'bannama',NULL,5,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991552,1991429,2269430,2269003,8372,'java-368275-2','2269003-8372-2269430',100,'bannama',NULL,5,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991563,1991429,2269433,2269003,9482,'volume1-368275-2','2269003-9482-2269433',100,'bannama',NULL,5,true,NULL,'',TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991575,1991429,2269436,2269003,8196,'fqdn-368275-1','2269003-8196-2269436',100,'bannama',NULL,5,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991588,1991429,2269439,2269003,9400,'tomcat-368275-1','2269003-9400-2269439',100,'bannama',NULL,6,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991645,1991429,2269442,2269003,9400,'tomcat-368275-2','2269003-9400-2269442',100,'bannama',NULL,6,true,NULL,NULL,TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991702,1991429,2269445,2269003,7509,'artifact-368275-2','2269003-7509-2269445',100,'bannama',NULL,7,true,NULL,'',TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991725,1991429,2269448,2269003,7509,'artifact-368275-1','2269003-7509-2269448',100,'bannama',NULL,7,true,NULL,'',TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991748,1991429,2269451,2269003,7959,'tomcat-daemon-368275-1','2269003-7959-2269451',100,'bannama',NULL,8,true,NULL,'',TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);

INSERT INTO dj_rfc_ci (rfc_id,release_id,ci_id,ns_id,class_id,ci_name,ci_goid,action_id,created_by,updated_by,execution_order,is_active_in_release,last_rfc_id,comments,created,updated,hint) VALUES (1991760,1991429,2269454,2269003,7959,'tomcat-daemon-368275-2','2269003-7959-2269454',100,'bannama',NULL,8,true,NULL,'',TIMESTAMP '2017-06-28 15:01:22.874',TIMESTAMP '2017-06-28 15:01:22.874',NULL);



INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991431,1991430,9254,NULL,'[ "22 22 tcp 0.0.0.0/0", "8080 8080 tcp 0.0.0.0/0", "8009 8009 tcp 0.0.0.0/0", "8443 8443 tcp 0.0.0.0/0" ]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991448,1991443,7822,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991447,1991443,7794,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991446,1991443,7795,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991445,1991443,7791,NULL,'S',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991444,1991443,7821,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991459,1991454,7822,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991458,1991454,7794,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991457,1991454,7795,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991456,1991454,7791,NULL,'S',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991455,1991454,7821,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991483,1991465,9007,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991482,1991465,9001,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991481,1991465,9009,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991480,1991465,9006,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991479,1991465,9003,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991478,1991465,9011,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991477,1991465,9031,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991476,1991465,9012,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991475,1991465,9000,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991474,1991465,10967,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991473,1991465,10584,NULL,'centos-7.0',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991472,1991465,9010,NULL,'{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991471,1991465,8999,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991470,1991465,9004,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991469,1991465,9008,NULL,'UTC',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991468,1991465,8998,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991467,1991465,9002,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991466,1991465,9005,NULL,'["-p tcp --dport 22"]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991499,1991489,8503,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991498,1991489,8508,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991497,1991489,8501,NULL,'cookieinsert',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991496,1991489,8504,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991495,1991489,8500,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991494,1991489,8507,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991493,1991489,8502,NULL,'default',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991492,1991489,8498,NULL,'["http 80 http 8080"]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991491,1991489,8505,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991490,1991489,8499,NULL,'roundrobin',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991523,1991505,9007,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991522,1991505,9001,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991521,1991505,9009,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991520,1991505,9006,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991519,1991505,9003,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991518,1991505,9011,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991517,1991505,9031,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991516,1991505,9012,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991515,1991505,9000,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991514,1991505,10967,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991513,1991505,10584,NULL,'centos-7.0',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991512,1991505,9010,NULL,'{"Ciphers":"aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,arcfour","Macs":"hmac-sha1,hmac-ripemd160"}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991511,1991505,8999,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991510,1991505,9004,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991509,1991505,9008,NULL,'UTC',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991508,1991505,8998,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991507,1991505,9002,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991506,1991505,9005,NULL,'["-p tcp --dport 22"]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991535,1991529,9460,NULL,'ext4',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991534,1991529,9459,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991533,1991529,9462,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991532,1991529,9461,NULL,'/app',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991531,1991529,9458,NULL,'100%FREE',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991530,1991529,12285,NULL,'no-raid',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991546,1991541,8357,NULL,'8',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991545,1991541,8362,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991544,1991541,8356,NULL,'jdk',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991543,1991541,8361,NULL,'/usr/lib/jvm',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991542,1991541,8355,NULL,'openjdk',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991557,1991552,8357,NULL,'8',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991556,1991552,8362,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991555,1991552,8356,NULL,'jdk',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991554,1991552,8361,NULL,'/usr/lib/jvm',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991553,1991552,8355,NULL,'openjdk',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991569,1991563,9460,NULL,'ext4',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991568,1991563,9459,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991567,1991563,9462,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991566,1991563,9461,NULL,'/app',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991565,1991563,9458,NULL,'100%FREE',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991564,1991563,12285,NULL,'no-raid',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991582,1991575,8179,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991581,1991575,8178,NULL,'60',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991580,1991575,14820,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991579,1991575,8181,NULL,'proximity',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991578,1991575,8180,NULL,'platform',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991577,1991575,8198,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991576,1991575,8176,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991639,1991588,9321,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991638,1991588,9300,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991637,1991588,9331,NULL,'128M',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991636,1991588,9342,NULL,'30',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991635,1991588,9306,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991634,1991588,9305,NULL,'8009',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991633,1991588,9327,NULL,'25',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991632,1991588,9336,NULL,'logs',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991631,1991588,9295,NULL,'70',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991630,1991588,9301,NULL,'{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991629,1991588,9320,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991628,1991588,9335,NULL,'grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991627,1991588,9334,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991626,1991588,9324,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991625,1991588,9330,NULL,'["+UseConcMarkSweepGC"]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991624,1991588,9338,NULL,'yyyy-MM-dd',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991623,1991588,9326,NULL,'50',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991622,1991588,9339,NULL,'.log',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991621,1991588,9332,NULL,'128M',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991620,1991588,9315,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991619,1991588,9325,NULL,'tomcatThreadPool',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991618,1991588,9337,NULL,'access_log',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991617,1991588,9333,NULL,'45',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991616,1991588,9344,NULL,'1',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991615,1991588,9311,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991614,1991588,9317,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991613,1991588,9313,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991612,1991588,9292,NULL,'/opt',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991611,1991588,9304,NULL,'8005',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991610,1991588,9302,NULL,'8080',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991609,1991588,9294,NULL,'7.0',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991608,1991588,9312,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991607,1991588,9296,NULL,'/opt/tomcat7/webapps',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991606,1991588,9309,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991605,1991588,9322,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991604,1991588,9340,NULL,'%h %l %u %t &quot;%r&quot; %s %b %D %F',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991603,1991588,9293,NULL,'["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991602,1991588,9308,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991601,1991588,9323,NULL,'web',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991600,1991588,9303,NULL,'8443',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991599,1991588,9345,NULL,'15',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991598,1991588,9318,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991597,1991588,9307,NULL,'/log/apache-tomcat/',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991596,1991588,9328,NULL,'-Djava.awt.headless=true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991595,1991588,9329,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991594,1991588,9299,NULL,'HTTP/1.1',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991593,1991588,9291,NULL,'binary',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991592,1991588,9314,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991591,1991588,9316,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991590,1991588,9310,NULL,'<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991589,1991588,9319,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991696,1991645,9321,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991695,1991645,9300,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991694,1991645,9331,NULL,'128M',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991693,1991645,9342,NULL,'30',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991692,1991645,9306,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991691,1991645,9305,NULL,'8009',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991690,1991645,9327,NULL,'25',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991689,1991645,9336,NULL,'logs',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991688,1991645,9295,NULL,'70',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991687,1991645,9301,NULL,'{"connectionTimeout":"20000","maxKeepAliveRequests":"100"}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991686,1991645,9320,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991685,1991645,9335,NULL,'grant codeBase "file:${catalina.base}/webapps/-" {
        permission java.security.AllPermission;
};
',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991684,1991645,9334,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991683,1991645,9324,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991682,1991645,9330,NULL,'["+UseConcMarkSweepGC"]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991681,1991645,9338,NULL,'yyyy-MM-dd',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991680,1991645,9326,NULL,'50',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991679,1991645,9339,NULL,'.log',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991678,1991645,9332,NULL,'128M',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991677,1991645,9315,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991676,1991645,9325,NULL,'tomcatThreadPool',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991675,1991645,9337,NULL,'access_log',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991674,1991645,9333,NULL,'45',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991673,1991645,9344,NULL,'1',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991672,1991645,9311,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991671,1991645,9317,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991670,1991645,9313,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991669,1991645,9292,NULL,'/opt',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991668,1991645,9304,NULL,'8005',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991667,1991645,9302,NULL,'8080',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991666,1991645,9294,NULL,'7.0',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991665,1991645,9312,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991664,1991645,9296,NULL,'/opt/tomcat7/webapps',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991663,1991645,9309,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991662,1991645,9322,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991661,1991645,9340,NULL,'%h %l %u %t &quot;%r&quot; %s %b %D %F',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991660,1991645,9293,NULL,'["http://archive.apache.org/dist","http://apache.cs.utah.edu" ]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991659,1991645,9308,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991658,1991645,9323,NULL,'web',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991657,1991645,9303,NULL,'8443',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991656,1991645,9345,NULL,'15',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991655,1991645,9318,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991654,1991645,9307,NULL,'/log/apache-tomcat/',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991653,1991645,9328,NULL,'-Djava.awt.headless=true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991652,1991645,9329,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991651,1991645,9299,NULL,'HTTP/1.1',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991650,1991645,9291,NULL,'binary',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991649,1991645,9314,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991648,1991645,9316,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991647,1991645,9310,NULL,'<?xml version=''1.0'' encoding=''utf-8''?>
<!-- The contents of this file will be loaded for each web application -->
<Context reloadable="false" allowLinking="false" antiJARLocking="true" useHttpOnly="true">

    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>

    <!-- Uncomment this to disable session persistence across Tomcat restarts -->
    <!--
    <Manager pathname="" />
    -->

    <!-- Uncomment this to enable Comet connection tacking (provides events
         on session expiration as well as webapp lifecycle) -->
    <!--
    <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" />
    -->

</Context>
',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991646,1991645,9319,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991719,1991702,7480,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991718,1991702,7493,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991717,1991702,7490,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991716,1991702,7482,NULL,'plt:paas-perf-test:war',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991715,1991702,7484,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991714,1991702,7487,NULL,'app',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991713,1991702,7481,NULL,'::ENCRYPTED::d016fa166427beb3',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991712,1991702,7489,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991711,1991702,7485,NULL,'/nexus',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991710,1991702,7478,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991709,1991702,7483,NULL,'2.59',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991708,1991702,7479,NULL,'pox_releases',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991707,1991702,7492,NULL,'%w[ /log/apache-tomcat /log/logmon /app/localConfig ].each do |path|
  directory path do
    owner ''app''
    group ''app''
  end
end

',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991706,1991702,7488,NULL,'app',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991705,1991702,7491,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991704,1991702,7494,NULL,'execute "rm -fr /app/tomcat7/webapps/paas-perf" 

link "/app/tomcat7/webapps/paas-perf" do 
  to "/app/paas-perf-test/current" 
end 

',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991703,1991702,7486,NULL,'/app/paas-perf-test',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991742,1991725,7480,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991741,1991725,7493,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991740,1991725,7490,NULL,'[]',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991739,1991725,7482,NULL,'plt:paas-perf-test:war',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991738,1991725,7484,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991737,1991725,7487,NULL,'app',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991736,1991725,7481,NULL,'::ENCRYPTED::d016fa166427beb3',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991735,1991725,7489,NULL,'{}',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991734,1991725,7485,NULL,'/nexus',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991733,1991725,7478,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991732,1991725,7483,NULL,'2.59',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991731,1991725,7479,NULL,'pox_releases',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991730,1991725,7492,NULL,'%w[ /log/apache-tomcat /log/logmon /app/localConfig ].each do |path|
  directory path do
    owner ''app''
    group ''app''
  end
end

',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991729,1991725,7488,NULL,'app',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991728,1991725,7491,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991727,1991725,7494,NULL,'execute "rm -fr /app/tomcat7/webapps/paas-perf" 

link "/app/tomcat7/webapps/paas-perf" do 
  to "/app/paas-perf-test/current" 
end 

',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991726,1991725,7486,NULL,'/app/paas-perf-test',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991754,1991748,7928,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991753,1991748,7929,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991752,1991748,10233,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991751,1991748,7927,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991750,1991748,7926,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991749,1991748,7925,NULL,'tomcat7',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991766,1991760,7928,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991765,1991760,7929,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991764,1991760,10233,NULL,'false',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991763,1991760,7927,NULL,'true',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991762,1991760,7926,NULL,'',NULL);

INSERT INTO dj_rfc_ci_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value,owner) VALUES (1991761,1991760,7925,NULL,'tomcat7',NULL);



INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991432,1991429,2269003,2269404,NULL,2268421,6414,'2268421-6414-2269403',1991430,2269403,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991435,1991429,2269003,2269405,1991430,2269403,1367,'2269403-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991438,1991429,2269003,2269407,NULL,2268419,6414,'2268419-6414-2269406',1991437,2269406,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991441,1991429,2269003,2269408,1991437,2269406,1367,'2269406-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991449,1991429,2269003,2269410,NULL,2268360,6414,'2268360-6414-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991452,1991429,2269003,2269411,1991443,2269409,1367,'2269409-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991460,1991429,2269003,2269413,NULL,2268360,6414,'2268360-6414-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991463,1991429,2269003,2269414,1991454,2269412,1367,'2269412-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991484,1991429,2269003,2269416,NULL,2268369,6414,'2268369-6414-2269415',1991465,2269415,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991487,1991429,2269003,2269417,1991465,2269415,1367,'2269415-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991500,1991429,2269003,2269419,NULL,2268397,6414,'2268397-6414-2269418',1991489,2269418,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991503,1991429,2269003,2269420,1991489,2269418,1367,'2269418-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991524,1991429,2269003,2269422,NULL,2268369,6414,'2268369-6414-2269421',1991505,2269421,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991527,1991429,2269003,2269423,1991505,2269421,1367,'2269421-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991536,1991429,2269003,2269425,NULL,2268391,6414,'2268391-6414-2269424',1991529,2269424,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991539,1991429,2269003,2269426,1991529,2269424,1367,'2269424-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991547,1991429,2269003,2269428,NULL,2268400,6414,'2268400-6414-2269427',1991541,2269427,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991550,1991429,2269003,2269429,1991541,2269427,1367,'2269427-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991558,1991429,2269003,2269431,NULL,2268400,6414,'2268400-6414-2269430',1991552,2269430,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991561,1991429,2269003,2269432,1991552,2269430,1367,'2269430-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991570,1991429,2269003,2269434,NULL,2268391,6414,'2268391-6414-2269433',1991563,2269433,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991573,1991429,2269003,2269435,1991563,2269433,1367,'2269433-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991583,1991429,2269003,2269437,NULL,2268398,6414,'2268398-6414-2269436',1991575,2269436,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991586,1991429,2269003,2269438,1991575,2269436,1367,'2269436-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991640,1991429,2269003,2269440,NULL,2268366,6414,'2268366-6414-2269439',1991588,2269439,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991643,1991429,2269003,2269441,1991588,2269439,1367,'2269439-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991697,1991429,2269003,2269443,NULL,2268366,6414,'2268366-6414-2269442',1991645,2269442,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991700,1991429,2269003,2269444,1991645,2269442,1367,'2269442-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991720,1991429,2269003,2269446,NULL,2268363,6414,'2268363-6414-2269445',1991702,2269445,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991723,1991429,2269003,2269447,1991702,2269445,1367,'2269445-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991743,1991429,2269003,2269449,NULL,2268363,6414,'2268363-6414-2269448',1991725,2269448,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991746,1991429,2269003,2269450,1991725,2269448,1367,'2269448-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991755,1991429,2269003,2269452,NULL,2268372,6414,'2268372-6414-2269451',1991748,2269451,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991758,1991429,2269003,2269453,1991748,2269451,1367,'2269451-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991767,1991429,2269003,2269455,NULL,2268372,6414,'2268372-6414-2269454',1991760,2269454,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991770,1991429,2269003,2269456,1991760,2269454,1367,'2269454-1367-368275',NULL,368275,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991772,1991429,2269003,2269457,1991748,2269451,1317,'2269451-1317-2269448',1991725,2269448,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991773,1991429,2269003,2269458,1991588,2269439,1317,'2269439-1317-2269427',1991541,2269427,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991774,1991429,2269003,2269459,1991725,2269448,1317,'2269448-1317-2269415',1991465,2269415,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991775,1991429,2269003,2269460,1991748,2269451,1317,'2269451-1317-2269439',1991588,2269439,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991776,1991429,2269003,2269461,1991725,2269448,1317,'2269448-1317-2269439',1991588,2269439,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991777,1991429,2269003,2269462,1991563,2269433,1317,'2269433-1317-2269421',1991505,2269421,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991778,1991429,2269003,2269463,1991529,2269424,1317,'2269424-1317-2269415',1991465,2269415,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991779,1991429,2269003,2269464,1991443,2269409,1317,'2269409-1317-2269403',1991430,2269403,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991780,1991429,2269003,2269465,1991575,2269436,1317,'2269436-1317-2269418',1991489,2269418,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991781,1991429,2269003,2269466,1991760,2269454,1317,'2269454-1317-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991782,1991429,2269003,2269467,1991702,2269445,1317,'2269445-1317-2269421',1991505,2269421,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991783,1991429,2269003,2269468,1991465,2269415,1317,'2269415-1317-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991784,1991429,2269003,2269469,1991645,2269442,1317,'2269442-1317-2269421',1991505,2269421,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991785,1991429,2269003,2269470,1991541,2269427,1317,'2269427-1317-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991786,1991429,2269003,2269471,1991588,2269439,1317,'2269439-1317-2269415',1991465,2269415,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991787,1991429,2269003,2269472,1991454,2269412,1317,'2269412-1317-2269403',1991430,2269403,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991788,1991429,2269003,2269473,1991748,2269451,1317,'2269451-1317-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991789,1991429,2269003,2269474,1991588,2269439,1317,'2269439-1317-2269424',1991529,2269424,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991790,1991429,2269003,2269475,1991760,2269454,1317,'2269454-1317-2269442',1991645,2269442,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991791,1991429,2269003,2269476,1991645,2269442,1317,'2269442-1317-2269430',1991552,2269430,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991792,1991429,2269003,2269477,1991489,2269418,1317,'2269418-1317-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991793,1991429,2269003,2269478,1991489,2269418,1317,'2269418-1317-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991794,1991429,2269003,2269479,1991505,2269421,1317,'2269421-1317-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991795,1991429,2269003,2269480,1991552,2269430,1317,'2269430-1317-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991796,1991429,2269003,2269481,1991702,2269445,1317,'2269445-1317-2269442',1991645,2269442,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991797,1991429,2269003,2269482,1991702,2269445,1317,'2269445-1317-2269433',1991563,2269433,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991798,1991429,2269003,2269483,1991645,2269442,1317,'2269442-1317-2269433',1991563,2269433,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991799,1991429,2269003,2269484,1991541,2269427,1317,'2269427-1317-2269415',1991465,2269415,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991800,1991429,2269003,2269485,1991725,2269448,1317,'2269448-1317-2269424',1991529,2269424,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991801,1991429,2269003,2269486,1991552,2269430,1317,'2269430-1317-2269421',1991505,2269421,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991802,1991429,2269003,2269487,1991760,2269454,1317,'2269454-1317-2269445',1991702,2269445,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991803,1991429,2269003,2269488,1991748,2269451,6393,'2269451-6393-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991804,1991429,2269003,2269489,1991760,2269454,6393,'2269454-6393-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991805,1991429,2269003,2269490,1991529,2269424,6393,'2269424-6393-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991806,1991429,2269003,2269491,1991563,2269433,6393,'2269433-6393-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991807,1991429,2269003,2269492,1991588,2269439,6393,'2269439-6393-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991808,1991429,2269003,2269493,1991645,2269442,6393,'2269442-6393-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991809,1991429,2269003,2269494,1991541,2269427,6393,'2269427-6393-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991810,1991429,2269003,2269495,1991552,2269430,6393,'2269430-6393-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991811,1991429,2269003,2269496,1991465,2269415,6393,'2269415-6393-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991812,1991429,2269003,2269497,1991505,2269421,6393,'2269421-6393-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991813,1991429,2269003,2269498,1991702,2269445,6393,'2269445-6393-2269412',1991454,2269412,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991814,1991429,2269003,2269499,1991725,2269448,6393,'2269448-6393-2269409',1991443,2269409,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991815,1991429,2269003,2269500,1991443,2269409,6449,'2269409-6449-2269406',1991437,2269406,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991816,1991429,2269003,2269501,1991454,2269412,6449,'2269412-6449-2269406',1991437,2269406,100,0,true);

INSERT INTO dj_rfc_relation (rfc_id,release_id,ns_id,ci_relation_id,from_rfc_id,from_ci_id,relation_id,relation_goid,to_rfc_id,to_ci_id,action_id,execution_order,is_active_in_release) VALUES (1991817,1991429,2269003,2269502,NULL,2268357,1371,'2268357-1371-2269436',1991575,2269436,100,0,true);

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991434,1991432,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991433,1991432,6415,NULL,'1990743');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991436,1991435,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991440,1991438,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991439,1991438,6415,NULL,'1990741');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991442,1991441,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991451,1991449,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991450,1991449,6415,NULL,'1990221');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991453,1991452,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991462,1991460,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991461,1991460,6415,NULL,'1990221');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991464,1991463,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991486,1991484,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991485,1991484,6415,NULL,'1990347');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991488,1991487,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991502,1991500,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991501,1991500,6415,NULL,'1990583');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991504,1991503,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991526,1991524,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991525,1991524,6415,NULL,'1990347');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991528,1991527,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991538,1991536,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991537,1991536,6415,NULL,'1990543');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991540,1991539,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991549,1991547,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991548,1991547,6415,NULL,'1990611');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991551,1991550,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991560,1991558,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991559,1991558,6415,NULL,'1990611');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991562,1991561,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991572,1991570,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991571,1991570,6415,NULL,'1990543');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991574,1991573,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991585,1991583,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991584,1991583,6415,NULL,'1990594');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991587,1991586,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991642,1991640,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991641,1991640,6415,NULL,'1990278');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991644,1991643,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991699,1991697,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991698,1991697,6415,NULL,'1990278');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991701,1991700,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991722,1991720,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991721,1991720,6415,NULL,'1990242');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991724,1991723,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991745,1991743,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991744,1991743,6415,NULL,'1990242');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991747,1991746,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991757,1991755,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991756,1991755,6415,NULL,'1990382');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991759,1991758,1368,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991769,1991767,6416,NULL,'1');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991768,1991767,6415,NULL,'1990382');

INSERT INTO dj_rfc_relation_attributes (rfc_attr_id,rfc_id,attribute_id,old_attribute_value,new_attribute_value) VALUES (1991771,1991770,1368,NULL,'1');

INSERT INTO dj_deployment (deployment_id,ns_id,release_id,created_by,release_revision,state_id) VALUES (1991818,840155,1991429,'bannama',1,500);

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991919,1991818,10,1991817,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991918,1991818,10,1991816,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991917,1991818,10,1991815,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991916,1991818,10,1991814,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991915,1991818,10,1991813,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991914,1991818,10,1991812,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991913,1991818,10,1991811,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991912,1991818,10,1991810,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991911,1991818,10,1991809,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991910,1991818,10,1991808,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991909,1991818,10,1991807,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991908,1991818,10,1991806,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991907,1991818,10,1991805,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991906,1991818,10,1991804,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991905,1991818,10,1991803,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991904,1991818,10,1991802,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991903,1991818,10,1991801,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991902,1991818,10,1991800,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991901,1991818,10,1991799,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991900,1991818,10,1991798,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991899,1991818,10,1991797,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991898,1991818,10,1991796,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991897,1991818,10,1991795,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991896,1991818,10,1991794,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991895,1991818,10,1991793,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991894,1991818,10,1991792,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991893,1991818,10,1991791,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991892,1991818,10,1991790,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991891,1991818,10,1991789,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991890,1991818,10,1991788,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991889,1991818,10,1991787,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991888,1991818,10,1991786,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991887,1991818,10,1991785,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991886,1991818,10,1991784,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991885,1991818,10,1991783,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991884,1991818,10,1991782,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991883,1991818,10,1991781,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991882,1991818,10,1991780,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991881,1991818,10,1991779,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991880,1991818,10,1991778,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991879,1991818,10,1991777,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991878,1991818,10,1991776,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991877,1991818,10,1991775,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991876,1991818,10,1991774,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991875,1991818,10,1991773,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991874,1991818,10,1991772,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991873,1991818,10,1991770,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991872,1991818,10,1991767,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991871,1991818,10,1991758,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991870,1991818,10,1991755,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991869,1991818,10,1991746,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991868,1991818,10,1991743,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991867,1991818,10,1991723,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991866,1991818,10,1991720,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991865,1991818,10,1991700,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991864,1991818,10,1991697,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991863,1991818,10,1991643,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991862,1991818,10,1991640,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991861,1991818,10,1991586,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991860,1991818,10,1991583,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991859,1991818,10,1991573,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991858,1991818,10,1991570,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991857,1991818,10,1991561,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991856,1991818,10,1991558,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991855,1991818,10,1991550,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991854,1991818,10,1991547,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991853,1991818,10,1991539,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991852,1991818,10,1991536,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991851,1991818,10,1991527,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991850,1991818,10,1991524,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991849,1991818,10,1991503,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991848,1991818,10,1991500,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991847,1991818,10,1991487,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991846,1991818,10,1991484,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991845,1991818,10,1991463,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991844,1991818,10,1991460,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991843,1991818,10,1991452,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991842,1991818,10,1991449,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991841,1991818,200,1991441,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:21.899');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991840,1991818,200,1991438,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:21.899');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991839,1991818,10,1991435,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991838,1991818,10,1991432,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991837,1991818,10,1991760,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991836,1991818,10,1991748,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991835,1991818,10,1991725,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991834,1991818,10,1991702,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991833,1991818,10,1991645,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991832,1991818,10,1991588,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991831,1991818,10,1991575,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991830,1991818,10,1991563,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991829,1991818,10,1991541,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991828,1991818,10,1991529,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991827,1991818,10,1991552,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991826,1991818,10,1991505,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991825,1991818,10,1991489,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991824,1991818,10,1991465,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991823,1991818,10,1991443,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991822,1991818,10,1991454,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991821,1991818,10,1991430,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:18.984');

INSERT INTO dj_deployment_rfc (deployment_rfc_id,deployment_id,state_id,rfc_id,comments,ops,created,updated) VALUES (1991820,1991818,200,1991437,NULL,NULL,TIMESTAMP '2017-06-28 15:19:18.984',TIMESTAMP '2017-06-28 15:19:21.899');

