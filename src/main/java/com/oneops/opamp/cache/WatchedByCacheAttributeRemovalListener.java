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
