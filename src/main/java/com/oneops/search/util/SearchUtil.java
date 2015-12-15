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
package com.oneops.search.util;

import java.util.Arrays;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;

import com.oneops.cms.util.CmsConstants;

public class SearchUtil {
	
	private static Logger logger = Logger.getLogger(SearchUtil.class);

	public static Long getTimefromDate(String date){
		try {
			return DateUtil.parseDate(date,Arrays.asList(new String[]{CmsConstants.SEARCH_TS_PATTERN})).getTime();
		} catch (DateParseException e) {
			logger.error("Exception occured while parsing date "+e);
		}
		return null;
	}
}
