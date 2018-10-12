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

public interface InductorConstants {

  String ERROR_RESPONSE_CODE = "500";
  String OK_RESPONSE_CODE = "200";
  String MONITOR = "monitor";
  String LOG = "log";
  String ATTACHMENT = "attachment";
  String BEFORE_ATTACHMENT = "before_";
  String AFTER_ATTACHMENT = "after_";

  String WORK_ORDER_TYPE = "deploybom";
  String ACTION_ORDER_TYPE = "opsprocedure";

  String ENVIRONMENT = "Environment";
  String ORGANIZATION = "Organization";
  String KEYPAIR = "Keypair";

  String WATCHED_BY = "WatchedBy";
  String LOGGED_BY = "LoggedBy";
  String EXTRA_RUN_LIST = "ExtraRunList";

  String COMPLETE = "complete";
  String COMPUTE = "compute";
  String DOMAIN = "domain";
  String REMOTE = "remote";
  String FAILED = "failed";
  String SHARED_IP = "shared_ip";
  String PUBLIC_IP = "public_ip";
  String PRIVATE_IP = "private_ip";
  String PRIVATE = "private";
  String PRIVATE_KEY = "private_key";

  String ADD = "add";
  String UPDATE = "update";
  String REPLACE = "replace";
  String DELETE = "delete";
  String ADD_FAIL_CLEAN = "add_fail_clean";
  String KNOWN = "KNOWN";

  String DEFAULT_DOMAIN = "oneops.me";
  String ONEOPS_USER = "oneops";

  List<String> SEARCH_TS_FORMATS = Collections.singletonList(SEARCH_TS_PATTERN);

  String CLOUD_CONFIG_FILE_PATH =  "/cloud-conf.json";
  String KEYWHIZ_BASE_PATH = "/secrets";
  String KEYWHIZ_PREFIX = "keywhiz.lookup(";

}