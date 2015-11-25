/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.jms;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsUtil;

/**
 * JMS publisher class which publishes both work-orders and action-orders
 * to the search stream queue.
 * 
 * @author ranand
 *
 */
public class WoPublisher {
	
	private static Logger logger = Logger.getLogger(WoPublisher.class);
	
	private Connection connection = null;
    private Session session = null; 
    final private Gson gson = new Gson();
    private String queueName;
    private boolean isPubEnabled;
    
    private final String SEARCH_FLAG = "IS_SEARCH_ENABLED";

    private ActiveMQConnectionFactory connFactory;
    private MessageProducer producer;
    
    /**
     *
     * @throws JMSException
     */
    public void init() throws JMSException {
        connection = connFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        logger.info(">>>>WOPublisher initalized...");
        initProducer();
        isPubEnabled = "true".equals(System.getenv(SEARCH_FLAG));
    }
    
    private void initProducer() throws JMSException {
    	 // Create the session
        Destination destination = session.createQueue(queueName);
        // Create the producer.
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }
    
    
    /**
     * 
     * @param workOrder
     * @throws JMSException
     */
    public void publishMessage(CmsWorkOrderSimpleBase cmsWoSimpleBase,String type,String id) throws JMSException {
    	if(isPubEnabled){
    		cmsWoSimpleBase = CmsUtil.maskSecuredFields(cmsWoSimpleBase,type);
	    	TextMessage message = session.createTextMessage(gson.toJson(cmsWoSimpleBase));
	    	message.setStringProperty("type", getType(type));
	    	message.setStringProperty("msgId", id);
	    	producer.send(message);
	    	if (cmsWoSimpleBase instanceof CmsWorkOrderSimple) {
	    		logger.info("WO published to search stream queue for RfcId: "+((CmsWorkOrderSimple)cmsWoSimpleBase).getRfcId());
	    	} else if (cmsWoSimpleBase instanceof CmsActionOrderSimple) {
	    		logger.info("AO published to search stream queue for procedureId/actionId: " 
	    				    + ((CmsActionOrderSimple)cmsWoSimpleBase).getProcedureId() + "/" 
	    				    + ((CmsActionOrderSimple)cmsWoSimpleBase).getActionId());
	    	}
	    	logger.debug("WO published to search stream queue: "+message.getText());
    	}
    }
    
    
	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getType(String type) {
    	if(CmsUtil.WORK_ORDER_TYPE.equals(type))
    		return "workorder";
    	else if(CmsUtil.ACTION_ORDER_TYPE.equals(type))
    		return "actionorder";
    	
		return null;
	}

	/**
     * Sets the conn factory.
     *
     * @param connFactory the new conn factory
     */
    public void setConnFactory(ActiveMQConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
//	public static void main(String[] args) {
//		Gson gson = new Gson();
//		String msgText ="{\"ci\":{\"ciAttributes\":{\"ostype\":\"redhat-6.2\",\"public_ip\":\"10.63.64.134\",\"vm_state\":\"active\",\"instance_name\":\"app-edc-sg-test-local-398715\",\"cores\":\"2\",\"osname\":\"redhat-6.2 Linux 2.6.32-220.el6.x86_64 #1 SMP Wed Nov 9 08:03:13 EST 2011 x86_64 x86_64 GNU/Linux\",\"allow_loopback\":\"true\",\"deny_rules\":\"[]\",\"allow_rules\":\"[\\\"-p tcp --dport 22\\\"]\",\"timezone\":\"UTC\",\"host_id\":\"0dd4c34019fe804d49a1281fef6976712e495e9f7c320dc5f3b202db\",\"additional_search_domains\":\"[]\",\"hosts\":\"{}\",\"sysctl\":\"{\\\"net.ipv4.tcp_mem\\\":\\\"1529280 2039040 3058560\\\",\\\"net.ipv4.udp_mem\\\":\\\"1529280 2039040 3058560\\\",\\\"fs.file-max\\\":\\\"1611021\\\"}\",\"metadata\":\"{\\\"owner\\\":\\\"r@n.com\\\",\\\"mgmt_url\\\":\\\"https://http://localhost:3000\\\",\\\"organization\\\":\\\"local\\\",\\\"assembly\\\":\\\"sg-test\\\",\\\"environment\\\":\\\"edc\\\",\\\"platform\\\":\\\"app\\\",\\\"component\\\":\\\"397280\\\",\\\"instance\\\":\\\"398715\\\"}\",\"instance_id\":\"95b2fd2b-0f27-4b23-bdd1-967060594e8d\",\"limits\":\"{}\",\"instance_state\":\"ACTIVE\",\"iptables_enabled\":\"false\",\"hostname\":\"app-396892-1-398715\",\"image_id\":\"\",\"size\":\"M\",\"drop_policy\":\"true\",\"nat_rules\":\"[]\",\"private_ip\":\"10.63.64.134\",\"proxy_map\":\"{}\",\"repo_list\":\"[]\",\"ram\":\"3831.25\",\"dns_record\":\"10.63.64.134\"},\"ciId\":398715,\"ciName\":\"compute-396892-1\",\"ciClassName\":\"bom.Compute\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/sg-test/edc/bom/app/1\",\"ciGoid\":\"398453-1920-398715\",\"ciState\":\"default\",\"lastAppliedRfcId\":91938,\"createdBy\":\"ranand\",\"created\":\"Apr 22, 2014 5:06:26 AM\",\"updated\":\"May 1, 2014 8:46:53 AM\"},\"cloud\":{\"ciAttributes\":{\"location\":\"/local/_clouds/prod-edc\",\"description\":\"\",\"priority\":\"1\",\"auth\":\"prod-edc\"},\"ciId\":396892,\"ciName\":\"prod-edc\",\"ciClassName\":\"account.Cloud\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/_clouds\",\"ciGoid\":\"43604-1849-396892\",\"comments\":\"\",\"ciState\":\"default\",\"lastAppliedRfcId\":0,\"createdBy\":\"ranand\",\"created\":\"Apr 18, 2014 5:27:30 AM\",\"updated\":\"Apr 18, 2014 5:27:30 AM\"},\"box\":{\"ciAttributes\":{\"source\":\"walmartlabs\",\"is_active\":\"true\",\"description\":\"\",\"major_version\":\"1\",\"pack\":\"tomcat\",\"availability\":\"single\",\"version\":\"1\"},\"ciId\":397269,\"ciName\":\"app\",\"ciClassName\":\"manifest.Platform\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/sg-test/edc/manifest/app/1\",\"ciGoid\":\"397268-3152-397269\",\"ciState\":\"default\",\"lastAppliedRfcId\":90834,\"createdBy\":\"ranand\",\"created\":\"Apr 18, 2014 12:49:08 PM\",\"updated\":\"Apr 18, 2014 12:49:08 PM\"},\"payLoad\":{\"Environment\":[{\"ciAttributes\":{\"autorepair\":\"true\",\"monitoring\":\"true\",\"description\":\"\",\"dpmtdelay\":\"60\",\"subdomain\":\"edc.sg-test.local\",\"codpmt\":\"false\",\"debug\":\"false\",\"global_dns\":\"false\",\"autoscale\":\"false\",\"availability\":\"single\"},\"ciId\":397252,\"ciName\":\"edc\",\"ciClassName\":\"manifest.Environment\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/sg-test\",\"ciGoid\":\"396920-2219-397252\",\"comments\":\"\",\"ciState\":\"default\",\"lastAppliedRfcId\":0,\"createdBy\":\"ranand\",\"created\":\"Apr 18, 2014 6:34:56 AM\",\"updated\":\"May 1, 2014 9:54:39 AM\"}],\"RealizedAs\":[{\"ciAttributes\":{\"limits\":\"{}\",\"ostype\":\"redhat-6.2\",\"iptables_enabled\":\"false\",\"image_id\":\"\",\"size\":\"M\",\"allow_loopback\":\"true\",\"drop_policy\":\"true\",\"timezone\":\"UTC\",\"deny_rules\":\"[]\",\"allow_rules\":\"[\\\"-p tcp --dport 22\\\"]\",\"additional_search_domains\":\"[]\",\"nat_rules\":\"[]\",\"repo_list\":\"[]\",\"proxy_map\":\"{}\",\"hosts\":\"{}\",\"required_availability_zone\":\"\",\"sysctl\":\"{\\\"net.ipv4.tcp_mem\\\":\\\"1529280 2039040 3058560\\\",\\\"net.ipv4.udp_mem\\\":\\\"1529280 2039040 3058560\\\",\\\"fs.file-max\\\":\\\"1611021\\\"}\"},\"ciId\":397280,\"ciName\":\"compute\",\"ciClassName\":\"manifest.Compute\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/sg-test/edc/manifest/app/1\",\"ciGoid\":\"397268-1914-397280\",\"ciState\":\"default\",\"lastAppliedRfcId\":93042,\"createdBy\":\"ranand\",\"updatedBy\":\"ranand\",\"created\":\"Apr 18, 2014 12:49:08 PM\",\"updated\":\"Apr 27, 2014 1:43:15 PM\"}],\"SecuredBy\":[{\"ciAttributes\":{\"public\":\"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDAys0tz59NBzy/QKdgIBAiJMPbVE1yXluaJLpwseYqfoKUAuXGkEGv55F6fu/i1H+dsAAWV1fIgJfkC1GTCmihUEj/B8D527g015ROZW6BG/VR9Nuh1BXrAAIBrD7hetc07vSFOd0s3wXyrNsJvLTGoBkYvhIZOPIkljbB+kWnhyPzycGZZHNOvOn+MHJrFnJSp6sKj8d1LCkao/rYHk6o6M/pzASZgFGEKME4Go5xh1VVcZjtONOy6Wv2dKg1qwqb21CJQ8UwlvDdUj8qG3KGU4RxQm2Ajqt2HjvfgPSlVsrQb7O6XtXNj0v2D/KY7ZSgylrG/SF76OPUp5KiTGQN oneops\",\"private\":\"-----BEGIN RSA PRIVATE KEY-----\\nMIIEowIBAAKCAQEAwMrNLc+fTQc8v0CnYCAQIiTD21RNcl5bmiS6cLHmKn6ClALl\\nxpBBr+eRen7v4tR/nbAAFldXyICX5AtRkwpooVBI/wfA+du4NNeUTmVugRv1UfTb\\nodQV6wACAaw+4XrXNO70hTndLN8F8qzbCby0xqAZGL4SGTjyJJY2wfpFp4cj88nB\\nmWRzTrzp/jByaxZyUqerCo/HdSwpGqP62B5OqOjP6cwEmYBRhCjBOBqOcYdVVXGY\\n7TjTsulr9nSoNasKm9tQiUPFMJbw3VI/KhtyhlOEcUJtgI6rdh4734D0pVbK0G+z\\nul7VzY9L9g/ymO2UoMpaxv0he+jj1KeSokxkDQIDAQABAoIBAQCUJsRJYfVuwhMC\\nfXKhwcHkGxCPKVdiffQynerP00WmfhVmHFvqnMx9mpwG4CkDzHxM9wT6JiCFKZyP\\nV8tWDXeeDw4pPRIkb548rTWAkR28BXodguaMk2hMcHMaSf0AVN+wA7jP8Elww+nU\\nNNUVxHe+8ySWCOnrjAE38F8f7W09cJyHyiWQljZZPTtra43uNVaAlZ+pydRauwO5\\ndHsSUVt5K7Zc5Fxa8gyBuw+56wJsSjJrzNSzsNecrue+5ulcXdhyqPUtpN+bcHNl\\ngShlt84lnbrov+fFS2VOoWjlsksg0Bb/1fJeybwjRcevCNXFgfDikGiFRpbweimE\\nMPJYxZ5ZAoGBAOevkB3hcLTN5rCtPDBhBDKVPG4gBtPhQnwRPetnYZ3eZkyQ4auH\\n1zwX3Rja/VsgQj9R+B1RELdWB4RgeZ8iXdGMoPPHUx9MdwjEoMZfdygCFMZszu06\\npwzWEBoVAZ737+FMjc031l4H9fuhETCmV6hqJQs4sxJKkGT+QF4MOTy/AoGBANUG\\nVCWHj6vsAL8ffVroVS5QqxeBvgonjWngB9NIIVv69PLJzKVCbTdw32NpBnyr66Nw\\nNTu4wc9VnvohXmPqcmrnGq9GcDrbkwwGVHXB7ZoFTt+n0i2Va5322iyIAMyt9xKW\\neNHMFh8oDPjV+9X0DRIkR1ZWof9g6zeHU5oUKzYzAoGANbXMijDOKQwms8SlVG3Z\\nRES2iq0gCBSLjimNGjqQNcHuI0ffGR3XttWtauqxm2OCGB3PkDRP1MC742NeLpcP\\n+tlIItuNmI7odX7n6rUM8Zhx6oIlAnf7bA1gFDvUcV73HFrb8LxGFOxxiREQ5B9b\\n6O4aglx1nwLsMI2ErIjW6A8CgYBUdAPcNRgycogBPXy3E7Dhgb2yvg8Xe4LG3Lh9\\nfPUswBtoPbTSN7mQ50DI6pRVzNrImu2f1amQh0NVqvD+762Y0DJ2Fpjb3L05YTEO\\n1mlaTIxugtwCkmBt6bWpPeoDO1zek6Uml6CVjY1HAZ6rveuIq7VUd1gWJNhb1VzB\\nkwlmnQKBgErdNHb/tbTISRvvPzJTrOLZ26iT04E+0PNoXaI9cDGYenoKclSQT6Zy\\npkBHrrqafsHX7L5eNCixoaPoXGMxpdsTtnerqCT73w8oGJdvmPI3+9wuopsb0C/a\\nRnaWs7PcUs+K5/CuDPzaZRNxLRMxKOd6AV2isX7yqzhpr9aj2Ewf\\n-----END RSA PRIVATE KEY-----\"},\"ciId\":398712,\"ciName\":\"sshkeys-396892-1\",\"ciClassName\":\"bom.Keypair\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/sg-test/edc/bom/app/1\",\"ciGoid\":\"398453-2583-398712\",\"ciState\":\"default\",\"lastAppliedRfcId\":91930,\"createdBy\":\"ranand\",\"created\":\"Apr 22, 2014 4:48:04 AM\",\"updated\":\"May 1, 2014 9:55:15 AM\"}],\"DependsOn\":[{\"ciAttributes\":{\"group_name\":\"app-edc-sg-test-local-398709\",\"description\":\"\",\"group_id\":\"d0f15bd0-02d7-4196-acca-608f7af93d33\",\"inbound\":\"[\\\"2522 2662 tcp 0.0.0.0/0\\\"]\"},\"ciId\":398709,\"ciName\":\"secgroup-396892-1\",\"ciClassName\":\"bom.Secgroup\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/sg-test/edc/bom/app/1\",\"ciGoid\":\"398453-3456-398709\",\"comments\":\"\",\"ciState\":\"default\",\"lastAppliedRfcId\":91922,\"createdBy\":\"ranand\",\"created\":\"Apr 22, 2014 4:53:21 AM\",\"updated\":\"Apr 22, 2014 4:53:21 AM\"}]},\"services\":{\"dns\":{\"prod-edc\":{\"ciAttributes\":{\"username\":\"iaassvc\",\"host\":\"infoblox-api.walmart.com\",\"password\":\"@ut@m@t3\",\"cloud_dns_id\":\"\",\"zone\":\"prod.walmart.com\"},\"ciId\":398438,\"ciName\":\"infoblox-prod\",\"ciClassName\":\"cloud.service.Infoblox\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/_clouds/prod-edc\",\"ciGoid\":\"396896-2437-398438\",\"comments\":\"\",\"ciState\":\"default\",\"lastAppliedRfcId\":0,\"createdBy\":\"ranand\",\"created\":\"Apr 18, 2014 12:52:11 PM\",\"updated\":\"Apr 18, 2014 12:52:11 PM\"}},\"compute\":{\"prod-edc\":{\"ciAttributes\":{\"region\":\"RegionOne\",\"max_instances\":\"\",\"repo_map\":\"{ \\r\\n              \\\"redhat-6.2\\\":\\\"yum-config-manager --add-repo http://edc-satproxy.walmart.com/base/redhat/6.2/ ; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/edc-satproxy.walmart.com_base_redhat_6.2_.repo ; yum-config-manager --add-repo http://edc-satproxy.walmart.com/epel/6/ ; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/edc-satproxy.walmart.com_epel_6_.repo ;  yum-config-manager --add-repo http://edc-satproxy.walmart.com/optional/ ; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/edc-satproxy.walmart.com_optional_.repo ; yum -q makecache\\\", \\r\\n              \\\"centos-6.4\\\":\\\"echo \\\\\\\"[base]\\\\\\\" \\u003e /etc/yum.repos.d/base.repo; echo name\\u003dbase \\u003e\\u003e /etc/yum.repos.d/base.repo; echo baseurl\\u003dhttp://edc-satproxy.walmart.com/base/centos/6.4/ \\u003e\\u003e /etc/yum.repos.d/base.repo; echo enabled\\u003d1 \\u003e\\u003e /etc/yum.repos.d/base.repo; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/base.repo; yum -d0 -e0 -y install rsync yum-utils; yum-config-manager --add-repo http://edc-satproxy.walmart.com/epel/6/ ; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/edc-satproxy.walmart.com_epel_6_.repo ;  yum -q makecache\\\",\\r\\n              \\\"default-cloud\\\":\\\"echo \\\\\\\"[base]\\\\\\\" \\u003e /etc/yum.repos.d/base.repo; echo name\\u003dbase \\u003e\\u003e /etc/yum.repos.d/base.repo; echo baseurl\\u003dhttp://edc-satproxy.walmart.com/base/centos/6.4/ \\u003e\\u003e /etc/yum.repos.d/base.repo; echo enabled\\u003d1 \\u003e\\u003e /etc/yum.repos.d/base.repo; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/base.repo; yum -d0 -e0 -y install rsync yum-utils; yum-config-manager --add-repo http://edc-satproxy.walmart.com/epel/6/ ; echo gpgcheck\\u003d0 \\u003e\\u003e /etc/yum.repos.d/edc-satproxy.walmart.com_epel_6_.repo ;  yum -q makecache\\\"\\r\\n              }\",\"ostype\":\"centos-6.4\",\"max_ram\":\"\",\"max_cores\":\"\",\"sizemap\":\"{ \\\"XS\\\":\\\"1\\\",\\\"S\\\":\\\"2\\\",\\\"M\\\":\\\"3\\\",\\\"L\\\":\\\"4\\\",\\\"XL\\\":\\\"5\\\" }\",\"password\":\"Ooc6ahque4imaht8zai0Eyeeh\",\"imagemap\":\"{\\r\\n                            \\\"redhat-6.2\\\":\\\"0f50dbb5-ac18-4f78-bfae-d763452ebedf\\\",\\r\\n                            \\\"centos-6.4\\\":\\\"da658a37-757e-4a88-bd7b-1c1e969de4a1\\\"\\r\\n                          }\",\"endpoint\":\"http://edc-openstack-ctrl1.prod.walmart.com:5000/v2.0/tokens\",\"availability_zones\":\"[]\",\"username\":\"platform\",\"subnet\":\"Primary_External_Net\",\"tenant\":\"platform\",\"max_keypairs\":\"\",\"env_vars\":\"{ \\\"rubygems\\\":\\\"http://ndc-satproxy.walmart.com/gemrepo/\\\",\\\"misc\\\":\\\"http://ndc-satproxy.walmart.com/mirrored-assets/apache.mirrors.pair.com/\\\" }\",\"max_secgroups\":\"\"},\"ciId\":396897,\"ciName\":\"edc1\",\"ciClassName\":\"cloud.service.Openstack\",\"impl\":\"oo::chef-11.4.0\",\"nsPath\":\"/local/_clouds/prod-edc\",\"ciGoid\":\"396896-3072-396897\",\"comments\":\"\",\"ciState\":\"default\",\"lastAppliedRfcId\":0,\"createdBy\":\"ranand\",\"created\":\"Apr 18, 2014 5:37:57 AM\",\"updated\":\"Apr 18, 2014 5:37:57 AM\"}}},\"reqEnqueTs\":\"1355533433443\",\"queueTime\":0,\"executionTime\":0,\"totalTime\":0,\"actionId\":94084,\"procId\":94083,\"actionName\":\"status\",\"ciId\":398715,\"actionState\":\"pending\",\"execOrder\":1,\"isCritical\":true,\"arglist\":\"\",\"created\":\"May 1, 2014 10:02:50 AM\"}";
//		JsonReader reader = new JsonReader(new StringReader(msgText));
//    	//reader.setLenient(true);
//    	CmsWorkOrderSimpleBase wo = gson.fromJson(reader, CmsWorkOrderSimple.class);
//    	CmsCISimple simple = wo.getServices().get("dns").get("prod-edc");
//	}


}
