/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.cms.util;

public final class CmsConstants {

	//Cloud active state
	public static final String CLOUD_STATE_ACTIVE = "active";
	public static final String CLOUD_STATE_OFFLINE = "offline";
	
	public static final String SECURED_ATTRIBUTE = "IS_SECURED";
	public static final String ENCRYPTED_ATTR_VALUE = "ENC_VALUE";
	
	public static final String REQUEST_ENQUE_TS = "requestEnqueTS";
	public static final String REQUEST_DEQUE_TS = "requestDequeTS";
	public static final String RESPONSE_ENQUE_TS = "responseEnqueTS";
	public static final String RESPONSE_DEQUE_TS = "responseDequeTS";
	public static final String QUEUE_TIME = "queueTime";
	public static final String CLOSE_TIME = "closeTime";
	public static final String EXECUTION_TIME = "executionTime";
	public static final String LOCAL_WAIT_TIME = "localWaitTime";
	public static final String TOTAL_TIME = "totalTime";
	public static final String SEARCH_TS_PATTERN =  "yyyy-MM-dd'T'HH:mm:ss.SSS";
	
	//starting to put some relation constants 
	public static final String BASE_REALIZED_AS = "base.RealizedAs";
	public static final String BASE_COMPLIES_WITH = "base.CompliesWith";
	public static final String DEPLOYED_TO = "base.DeployedTo";
	public static final String MANIFEST_WATCHED_BY = "manifest.WatchedBy";

	public static final String ENTRYPOINT = "Entrypoint";

	public static final String ATTR_VALUE_TYPE_DF = "df";
	
	public static final String ATTR_NAME_ENABLED = "enabled";
	public static final String ATTR_NAME_AUTO_COMPLY = "autocomply";
	
	public static final String CI_STATE_PENDING_DELETION = "pending_deletion";

	public static final int SECONDARY_CLOUD_STATUS = 2;
	public static final int PRIMARY_CLOUD_STATUS = 1;
	
	public static final String MONITOR_CLASS = "manifest.Monitor";
}
