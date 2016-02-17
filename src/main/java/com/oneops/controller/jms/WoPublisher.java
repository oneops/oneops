/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.util.AsyncSearchPublisher;
import com.oneops.util.MessageData;

/**
 * JMS publisher class which publishes both work-orders and action-orders
 * to the search stream queue.
 * 
 * @author ranand
 *
 */
public class WoPublisher {
	
	private static Logger logger = Logger.getLogger(WoPublisher.class);
	
	private AsyncSearchPublisher asyncSearchPublisher; 
    final private Gson gson = new Gson();

    private boolean isPubEnabled;
    
    private final String SEARCH_FLAG = "IS_SEARCH_ENABLED";
    
    /**
     *
     * @throws JMSException
     */
    public void init() throws JMSException {
        isPubEnabled = "true".equals(System.getenv(SEARCH_FLAG));
        logger.info(">>>>WOPublisher initalized...");
    }
    
    /**
     * 
     * @param workOrder
     * @throws JMSException
     */
    public void publishMessage(CmsWorkOrderSimpleBase cmsWoSimpleBase,String type,String id) throws JMSException {
    	if(isPubEnabled){
			cmsWoSimpleBase = CmsUtil.maskSecuredFields(cmsWoSimpleBase, type);
			String payload = gson.toJson(cmsWoSimpleBase);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("type", getType(type));
			headers.put("msgId", id);
			MessageData data = new MessageData(payload, headers);
			asyncSearchPublisher.publishAsync(data);
			if (cmsWoSimpleBase instanceof CmsWorkOrderSimple) {
				logger.info("WO published to search stream queue for RfcId: "
						+ ((CmsWorkOrderSimple) cmsWoSimpleBase).getRfcId());
			} else if (cmsWoSimpleBase instanceof CmsActionOrderSimple) {
				logger.info("AO published to search stream queue for procedureId/actionId: "
						+ ((CmsActionOrderSimple) cmsWoSimpleBase).getProcedureId() + "/"
						+ ((CmsActionOrderSimple) cmsWoSimpleBase).getActionId());
			}
    	}
    }
    
    
	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getType(String type) {
    	if(CmsUtil.WORK_ORDER_TYPE.equals(type))
    		return "workorder";
    	else if(CmsUtil.ACTION_ORDER_TYPE.equals(type))
    		return "actionorder";
    	
		return null;
	}

	public void setAsyncSearchPublisher(AsyncSearchPublisher asyncSearchPublisher) {
		this.asyncSearchPublisher = asyncSearchPublisher;
	}

}
