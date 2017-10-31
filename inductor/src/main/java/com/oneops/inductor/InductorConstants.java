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
package com.oneops.inductor;

import java.util.*;

import static com.oneops.cms.util.CmsConstants.SEARCH_TS_PATTERN;

public final class InductorConstants {

	public static final String ERROR_RESPONSE_CODE = "500";
	public static final String OK_RESPONSE_CODE = "200";
	public static final String MONITOR = "monitor";
	public static final String LOG = "log";
	public static final String ATTACHMENT = "attachment";
	public static final String BEFORE_ATTACHMENT = "before_";
	public static final String AFTER_ATTACHMENT = "after_";

	private InductorConstants() {
	} // Prevents instantiation

	public static final String WORK_ORDER_TYPE = "deploybom";
	public static final String ACTION_ORDER_TYPE = "opsprocedure";

	public static final String ENVIRONMENT = "Environment";
	public static final String ORGANIZATION = "Organization";
	public static final String KEYPAIR = "Keypair";

	public static final String WATCHED_BY = "WatchedBy";
	public static final String LOGGED_BY = "LoggedBy";
	public static final String EXTRA_RUN_LIST = "ExtraRunList";

	public static final String COMPLETE = "complete";
	public static final String COMPUTE = "compute";
	public static final String DOMAIN = "domain";
	public static final String REMOTE = "remote";
	public static final String FAILED = "failed";
	public static final String SHARED_IP = "shared_ip";
	public static final String PUBLIC_IP = "public_ip";
	public static final String PRIVATE_IP = "private_ip";
	public static final String PRIVATE = "private";
	public static final String PRIVATE_KEY = "private_key";

	public static final String ADD = "add";
	public static final String UPDATE = "update";
	public static final String REPLACE = "replace";	
	public static final String DELETE = "delete";
	public static final String ADD_FAIL_CLEAN = "add_fail_clean";
	public static final String KNOWN = "KNOWN";

	public static final String DEFAULT_DOMAIN = "oneops.me";
	public static final String TEST_HOST = "inductor-test-host";

	public static final String ONEOPS_USER = "oneops";

	protected static final List<String> SEARCH_TS_FORMATS = Collections.singletonList(SEARCH_TS_PATTERN);

}