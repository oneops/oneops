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
