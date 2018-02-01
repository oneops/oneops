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
import com.oneops.opamp.service.ComputeService;

public class AzureEventsHandler {

	private static Logger logger = Logger.getLogger(AzureEventsHandler.class);

	private String oneopsSearchNsPath = "/";
	private boolean oneopsSearchNsPathRecursively = true;
	private String oneopsSearchNsPathByAttributeName = "instance_id";
	private String oneopsSearchNsPathByAttributeCondition = "eq";
	private CmsCmManager cmManager;
	private ComputeService computeService;
	private ObjectMapper objectMapper;

	public void submitEventAction(String event) throws AzureEventsHandlerException {
		logger.info("Starting to process event data...");
		String resourceId;
		try {
			resourceId = parseResourceIdFromEvent(event);

			List<CmsCI> ciList = getCidForAzureResourceID(resourceId);
			logger.info("ciList: " + ciList);

			if (ciList.size() < 1) {
				
				logger.error("No matching compute instance_id found for AzureEvent resourceId: " + resourceId);

			} else {
				logger.info("Replace Compute for resourceId: " + resourceId + " ,Cid: " + ciList.get(0).getCiId());
				Map<String, Integer> computeServiceRespMap= computeService.replaceComputeByCid(ciList.get(0).getCiId());
				
				logger.info("computeService Response : "+computeServiceRespMap.get("deploymentId") + " for resourceId: "+resourceId +" CiId: "+ciList.get(0).getCiId());
				if (computeServiceRespMap.get("deploymentId") == Integer.valueOf(0)) {
					logger.info("Compute was replacement request was submitted successfully  for resourceId: "+resourceId +" CiId: "+ciList.get(0).getCiId());
				} else {
					logger.error("ComputeService unable to replace compute for resourceId: "+resourceId +" CiId: "+ciList.get(0).getCiId());
					
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

	public String parseResourceIdFromEvent(String event) throws JsonProcessingException, IOException {

		JsonNode rootNode = objectMapper.readTree(event);
		String resourceId = rootNode.get("resourceId").asText();
		logger.info("resourceId: " + resourceId);
		return resourceId;

	}

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
		System.out.println("cmManager : "+cmManager);
		System.out.println("attrConds hash: "+attrConds.hashCode());
		List<CmsCI> ciList = cmManager.getCiByAttributes(oneopsSearchNsPath, null, attrConds, oneopsSearchNsPathRecursively);
		return ciList;

	}

	public CmsCmManager getCmManager() {
		return cmManager;
	}

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	public ComputeService getComputeService() {
		return computeService;
	}

	public void setComputeService(ComputeService computeService) {
		this.computeService = computeService;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
