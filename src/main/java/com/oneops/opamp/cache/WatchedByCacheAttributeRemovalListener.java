package com.oneops.opamp.cache;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

@Component
public class WatchedByCacheAttributeRemovalListener implements RemovalListener<String, String >{

	 /* Logger instance */
    private static Logger logger = Logger.getLogger(WatchedByCacheAttributeRemovalListener.class);

	@Override
	public void onRemoval(RemovalNotification<String, String> notif) {
        logger.info("Removing attribute(notifyOnlyifStatechanges) for " + notif.getKey()+ " as it's " + notif.getCause());
	}
	
}
