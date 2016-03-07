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
package com.oneops.cms.transmitter;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.util.AsyncSearchPublisher;
import com.oneops.util.MessageData;

public class SearchPublisher {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AsyncSearchPublisher asyncSearchPublisher;
	private Gson gson = new Gson();

	protected String getHeaders(CMSEvent event) {
		return gson.toJson(event.getHeaders());
	}

	public void publishMessage(CMSEvent event) throws JMSException {
		sendAsync(event);
		if (logger.isDebugEnabled()) {
			logger.debug("Submitted msg for publishing to search.stream " + getHeaders(event));
		}
	}

	private void sendAsync(CMSEvent event) {
		String payload = gson.toJson(event.getPayload());
		MessageData data = new MessageData(payload, event.getHeaders());
		asyncSearchPublisher.publishAsync(data);
	}

	public void setAsyncSearchPublisher(AsyncSearchPublisher publisher) {
		this.asyncSearchPublisher = publisher;
	}

}