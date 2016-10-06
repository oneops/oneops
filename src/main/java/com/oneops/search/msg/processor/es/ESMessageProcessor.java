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
package com.oneops.search.msg.processor.es;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsUtil;
import com.oneops.search.domain.CmsDeploymentSearch;
import com.oneops.search.domain.CmsNotificationSearch;
import com.oneops.search.domain.CmsOpsProcedureSearch;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.index.impl.ESIndexer;
import com.oneops.search.msg.processor.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Elastic Search message processor. Indexes the
 * work orders ,action orders, deployments, CMS releases ,
 * CMS CIs , procedures and notifications on elastic search.
 *
 * @author ranand
 *
 */
public class ESMessageProcessor implements MessageProcessor {
	
	private static Logger logger = Logger.getLogger(ESMessageProcessor.class);
	private static final String SUCCESS_PREFIX = "SUCCESS:";
	private Indexer indexer;
	private CmsUtil cmsUtil;
	DozerBeanMapper mapper;
	DpmtMessageProcessor dpmtProcessor;
	OpsProcMessageProcessor opsProcProcessor;
	CIMessageProcessor ciMessageProcessor;
	NSMessageProcessor nsMessageProcessor;
	RelationMsgProcessor relationMsgProcessor;
	PolicyMessageProcessor policyMessageProcessor;
	OfferingsMessageProcessor offeringMessageProcessor;
	final private Gson gson = new Gson();
	final private Gson searchGson = new GsonBuilder().setDateFormat(CmsConstants.SEARCH_TS_PATTERN).create();

	/**
	 * Elastic Search message processor. Indexes events and 
	 * different message types.
	 * 
	 */
	@Override
	public void processMessage(String message, String msgType, String msgId) {

		try {
			if("deployment".equals(msgType)){
				String dpmtEventMessage = searchGson.toJson(gson.fromJson(message, CmsDeployment.class));
				indexer.indexEvent("deployment", dpmtEventMessage);
				CmsDeployment od = gson.fromJson(message, CmsDeployment.class);
				CmsDeploymentSearch deployment = mapper.map(od,CmsDeploymentSearch.class);
				deployment = dpmtProcessor.processDeploymentMsg(deployment);
				msgId = String.valueOf(deployment.getDeploymentId());
				message = searchGson.toJson(deployment);
				indexer.index(msgId, msgType, message);
			}
			else if("opsprocedure".equals(msgType)){
				String procEventMessage = searchGson.toJson(gson.fromJson(message, CmsOpsProcedure.class));
				indexer.indexEvent("opsprocedure", procEventMessage);
				CmsOpsProcedure op = gson.fromJson(message, CmsOpsProcedure.class);
				CmsOpsProcedureSearch procedure = mapper.map(op,CmsOpsProcedureSearch.class);
				procedure = opsProcProcessor.processOpsProcMsg(procedure);
				msgId = String.valueOf(procedure.getProcedureId());
				message = searchGson.toJson(procedure);
				indexer.index(msgId, msgType, message);
			}
			else if("cm_ci".equals(msgType)){
				CmsCI cmsCI = gson.fromJson(message, CmsCI.class);
				CmsCISimple simpleCI = cmsUtil.custCI2CISimple(cmsCI, "df");
				String ciMessage = searchGson.toJson(simpleCI);
				indexer.indexEvent("ci", ciMessage);
				CmsCI ci = gson.fromJson(message, CmsCI.class);
				simpleCI = cmsUtil.custCI2CISimple(ci, "df");
				//For plan generation metrics
				if("manifest.Environment".equals(ci.getCiClassName()) &&
						StringUtils.isNotEmpty(ci.getComments()) && ci.getComments().startsWith(SUCCESS_PREFIX)){
					ciMessageProcessor.processDeploymentPlanMsg(ci,indexer,searchGson);
				}else if("account.Policy".equals(ci.getCiClassName()) || "mgmt.manifest.Policy".equals(ci.getCiClassName())){
					policyMessageProcessor.processMessage(simpleCI);
				}else if("cloud.Offering".equals(ci.getCiClassName())){
					offeringMessageProcessor.processMessage(simpleCI);
				}
				msgId = String.valueOf(ci.getCiId());
				//add wo to all bom cis
				if(ci.getCiClassName().startsWith("bom")){
					message = ciMessageProcessor.processCIMsg(simpleCI,searchGson);
				}else{
					message = searchGson.toJson(simpleCI);
				}
				indexer.index(msgId, "ci", message);
				relationMsgProcessor.processRelationForCi(message,indexer);
			}
			else if("cm_ci_rel".equals(msgType)){
				Thread.sleep(3000);//wait for CI events to get processed
				CmsCIRelationSimple relation = cmsUtil.custCIRelation2CIRelationSimple(gson.fromJson(message, CmsCIRelation.class), "df", false);
				relation = relationMsgProcessor.processRelationMsg(relation,((ESIndexer)indexer).getIndexName());
				String releaseMsg = searchGson.toJson(relation);
				indexer.indexEvent("relation", releaseMsg);
				indexer.index(String.valueOf(relation.getCiRelationId()), "relation",releaseMsg);
			}
			else if("release".equals(msgType)){
				CmsRelease release = gson.fromJson(message, CmsRelease.class);
				String releaseMsg = searchGson.toJson(release);
				indexer.indexEvent("release", releaseMsg);
				indexer.index(String.valueOf(release.getReleaseId()), "release",releaseMsg);
			}
			else if("notification".equals(msgType)){
				NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
				CmsNotificationSearch notificationSearch = mapper.map(notification,CmsNotificationSearch.class);
				notificationSearch.setPayload(notification.getPayload());
				String notificationTS = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN).format(new Date(notification.getTimestamp()));
				notificationSearch.setTs(notificationTS);
				if("ops".equals(notification.getSource())){
					ciMessageProcessor.processNotificationMsg(notificationSearch,indexer,searchGson);
				}
				String notificationMsg = searchGson.toJson(notificationSearch);
				indexer.index("notification",notificationMsg);
			}
			else{
				indexer.index(msgId, msgType, message);
			}
			 
		} catch (Exception e) {
			logger.error(">>>>>>>>Error in ESMessageProcessor for type :" + msgType + "::" +  ExceptionUtils.getMessage(e));
		}
	}
	
	@Override
	public void deleteMessage(String msgType, String msgId) {
		ESIndexer esIndexer = ((ESIndexer)indexer);
		
		if("namespace".equals(msgType)){
			nsMessageProcessor.processNSDeleteMsg(msgId, indexer);
		}
		else {
			if("cm_ci".equals(msgType)){
				msgType = "ci";
			}
				
			esIndexer.getTemplate().delete(esIndexer.getIndexName(), msgType, msgId);
			logger.info("Deleted message with id::"+ msgId +" and type::"+ msgType +" from ES.");
			
			if("ci".equals(msgType)){
				//Delete all relation docs for given ci 
				relationMsgProcessor.processRelationDeleteMsg(msgId, esIndexer);
				//TEMP code: Till ciClassName is available try to delete all ciIds from percolator type also
				esIndexer.getTemplate().delete(esIndexer.getIndexName(), ".percolator", msgId);
			}
		}
	}

	public void setMapper(DozerBeanMapper mapper) {
		this.mapper = mapper;
	}

	public Indexer getIndexer() {
		return indexer;
	}

	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public void setDpmtProcessor(DpmtMessageProcessor dpmtProcessor) {
		this.dpmtProcessor = dpmtProcessor;
	}

	public void setOpsProcProcessor(OpsProcMessageProcessor opsProcProcessor) {
		this.opsProcProcessor = opsProcProcessor;
	}

	public CIMessageProcessor getCiMessageProcessor() {
		return ciMessageProcessor;
	}

	public void setCiMessageProcessor(CIMessageProcessor ciMessageProcessor) {
		this.ciMessageProcessor = ciMessageProcessor;
	}

	public void setNsMessageProcessor(NSMessageProcessor nsMessageProcessor) {
		this.nsMessageProcessor = nsMessageProcessor;
	}

	public PolicyMessageProcessor getPolicyMessageProcessor() {
		return policyMessageProcessor;
	}

	public void setPolicyMessageProcessor(
			PolicyMessageProcessor policyMessageProcessor) {
		this.policyMessageProcessor = policyMessageProcessor;
	}
	
	@Autowired
    public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public RelationMsgProcessor getRelationMsgProcessor() {
		return relationMsgProcessor;
	}

	public void setRelationMsgProcessor(RelationMsgProcessor relationMsgProcessor) {
		this.relationMsgProcessor = relationMsgProcessor;
	}

	public OfferingsMessageProcessor getOfferingMessageProcessor() {
		return offeringMessageProcessor;
	}

	public void setOfferingMessageProcessor(
			OfferingsMessageProcessor offeringMessageProcessor) {
		this.offeringMessageProcessor = offeringMessageProcessor;
	}

}
