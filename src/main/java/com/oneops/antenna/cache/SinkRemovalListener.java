/*
* Copyright 2013-2014 @WalmartLabs.
*
*/
package com.oneops.antenna.cache;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.oneops.antenna.domain.BasicSubscriber;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cache removal listener implementation for sink subscribers
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @version 1.0
 */
@Component
public class SinkRemovalListener implements RemovalListener<SinkKey, List<BasicSubscriber>> {

    /* Logger instance */
    private static Logger logger = Logger.getLogger(SinkRemovalListener.class);

    @Override
    public void onRemoval(RemovalNotification<SinkKey, List<BasicSubscriber>> notif) {
        logger.warn("Removing sink subscribers for " + notif.getKey().getNsPath() + " as it's " + notif.getCause());
    }
}
