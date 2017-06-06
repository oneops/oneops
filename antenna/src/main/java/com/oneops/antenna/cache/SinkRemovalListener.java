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
