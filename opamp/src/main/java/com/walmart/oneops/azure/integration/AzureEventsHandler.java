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
package com.walmart.oneops.azure.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.opamp.exceptions.AzureEventsHandlerException;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.util.IConstants;

/**
 * @author dsing17
 *
 */
public class AzureEventsHandler {

	private static Logger logger = Logger.getLogger(AzureEventsHandler.class);

	private String oneopsSearchNsPath = "/";
	private boolean oneopsSearchNsPathRecursively = true;
	private String oneopsSearchNsPathByAttributeName = "instance_id";
	private String oneopsSearchNsPathByAttributeCondition = "eq";
	private CmsCmManager cmManager;
	private BadStateProcessor bsProcessor;
	private ObjectMapper objectMapper;

	/**
	 * @param event
	 * @throws AzureEventsHandlerException
	 */
	public void submitEventAction(String event) throws AzureEventsHandlerException {
		logger.info("Starting to process event data...");

		try {
			String resourceId = parseEventForEventAttribute(event,
					IConstants.AzureServiceBus_Event_attribute_resourceId);
			String status = parseEventForEventAttribute(event, IConstants.AzureServiceBus_Event_attribute_status);
			String resourceProviderName = parseEventForEventAttribute(event,
					IConstants.AzureServiceBus_Event_attribute_resourceProviderName);

			if (!status.equals(IConstants.AzureServiceBus_Event_attribute_status_failed) || !resourceProviderName
					.equals(IConstants.AzureServiceBus_Event_attribute_resourceProviderName_Value)) {
				logger.error("EventType not supported : " + event);
				return;

			}
			String userId = IConstants.ONEOPS_AUTOREPLACE_USER;
			logger.info("userId :" + userId);

			String description = "Auto-Replace triggered by Azure Service Bus";

			List<CmsCI> ciList = getCidForAzureResourceID(resourceId);
			logger.info("ciList: " + ciList);

			if (ciList.size() < 1) {

				logger.error("No matching ciId instance_id found for AzureEvent resourceId: " + resourceId);

			} else {
				logger.info("Replace ciId for resourceId: " + resourceId + " ,Cid: " + ciList.get(0).getCiId());
				Map<String, Integer> bsProcessorRespMap = bsProcessor.replaceByCid(ciList.get(0).getCiId(), userId,
						description);

				logger.info("Response : " + bsProcessorRespMap.get("deploymentId") + " for resourceId: " + resourceId
						+ " CiId: " + ciList.get(0).getCiId());
				if (bsProcessorRespMap.get("deploymentId") == Integer.valueOf(0)) {
					logger.info("CiId was replacement request was submitted successfully  for resourceId: " + resourceId
							+ " CiId: " + ciList.get(0).getCiId());
				} else {
					logger.error("unable to replace CiId for resourceId: " + resourceId + " CiId: "
							+ ciList.get(0).getCiId());

				}

			}
		} catch (JsonProcessingException e) {
			logger.error("Unable to convert EventData into JSON object: " + e);
			throw new AzureEventsHandlerException("Unable to convert EventData into JSON object: " + e);

		} catch (IOException e) {
			logger.error("Unable to parse EventData to get ResourceID: " + e);
			throw new AzureEventsHandlerException("Unable to parse EventData to get ResourceID: " + e);
		}
	}

	/**
	 * @param event
	 * @param attribute
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public String parseEventForEventAttribute(String event, String attribute)
			throws JsonProcessingException, IOException {

		JsonNode rootNode = objectMapper.readTree(event);
		String attributeValue = rootNode.get(attribute).asText();

		logger.info(attribute+" :" + attributeValue);
		return attributeValue;

	}

	/**
	 * @param resourceId
	 * @return
	 */
	public List<CmsCI> getCidForAzureResourceID(String resourceId) {
		logger.info("Initializing AttrQueryCondition....");
		AttrQueryCondition attrCondsObject = new AttrQueryCondition();
		attrCondsObject.setAttributeName(oneopsSearchNsPathByAttributeName);
		attrCondsObject.setCondition(oneopsSearchNsPathByAttributeCondition);
		attrCondsObject.setAvalue(resourceId);
		logger.info("Initialized AttrQueryCondition....");
		List<AttrQueryCondition> attrConds = new ArrayList<AttrQueryCondition>();
		attrConds.add(attrCondsObject);
		logger.info("retriving CMS data for Azure event resourceID...");

		List<CmsCI> ciList = cmManager.getCiByAttributes(oneopsSearchNsPath, null, attrConds,
				oneopsSearchNsPathRecursively);
		return ciList;

	}

	public CmsCmManager getCmManager() {
		return cmManager;
	}

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public BadStateProcessor getBsProcessor() {
		return bsProcessor;
	}

	public void setBsProcessor(BadStateProcessor bsProcessor) {
		this.bsProcessor = bsProcessor;
	}

}
