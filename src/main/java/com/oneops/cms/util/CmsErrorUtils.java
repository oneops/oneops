package com.oneops.cms.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsErrorUtils.
 */
public class CmsErrorUtils {

    private static Map<Integer,String> messages = new HashMap<Integer, String>();
    static {
        messages.put(3001,"Could not find superClass name: %s");
    }

    /**
     * Gets the message.
     *
     * @param errorCode the error code
     * @param params the params
     * @return the message
     */
    public static String getMessage(int errorCode, Object params) {
        String format = messages.get(errorCode);
        if(params == null) {
            return format;
        } else {
            return String.format(format, params);
        }
    }
}
