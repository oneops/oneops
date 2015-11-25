package com.oneops.search.msg.processor;

import static org.elasticsearch.index.query.QueryBuilders.queryString;

import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsDeploymentSearch;
import com.oneops.search.util.SearchConstants;
import com.oneops.search.util.SearchUtil;

public class DpmtMessageProcessor {

	private static Logger logger = Logger.getLogger(DpmtMessageProcessor.class);
	private ElasticsearchTemplate template;
	
	
	/**
	 * 
	 * @param deployment
	 * @return
	 */
	public CmsDeploymentSearch processDeploymentMsg(CmsDeploymentSearch deployment) {
		
		CmsDeploymentSearch esDeployment = null;
		try {
			esDeployment = fetchDeploymentRecord(deployment.getDeploymentId());
			
			if(isFinalState(deployment.getDeploymentState())){
				
				if(esDeployment!=null && SearchConstants.DPMT_STATE_CANCELED.equalsIgnoreCase(deployment.getDeploymentState())) {
					if(SearchConstants.DPMT_STATE_PAUSED.equalsIgnoreCase(esDeployment.getDeploymentState())){
						esDeployment.setPausedEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double pausedDuration = esDeployment.getPausedDuration() +
								(((SearchUtil.getTimefromDate(esDeployment.getPausedEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getPausedStartTS())))/1000.0);
						esDeployment.setPausedDuration(Math.round(pausedDuration * 1000.0) / 1000.0);
						
					}else if(SearchConstants.DPMT_STATE_PENDING.equalsIgnoreCase(esDeployment.getDeploymentState())){
						esDeployment.setPendingEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double pendingDuration = esDeployment.getPendingDuration() +
								(((SearchUtil.getTimefromDate(esDeployment.getPendingEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getPendingStartTS())))/1000.0);
						esDeployment.setPendingDuration(Math.round(pendingDuration * 1000.0) / 1000.0);
					}
					else if(SearchConstants.DPMT_STATE_FAILED.equalsIgnoreCase(esDeployment.getDeploymentState())){
						esDeployment.setFailedEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double failedDuration = esDeployment.getFailedDuration() +
								(((SearchUtil.getTimefromDate(esDeployment.getFailedEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getFailedStartTS())))/1000.0);
						esDeployment.setFailedDuration(Math.round(failedDuration * 1000.0) / 1000.0);
					}
					}else if(esDeployment!=null && SearchConstants.DPMT_STATE_COMPLETE.equalsIgnoreCase(deployment.getDeploymentState())){
						esDeployment.setActiveEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double activeDuration = esDeployment.getActiveDuration() +
								(((SearchUtil.getTimefromDate(esDeployment.getActiveEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getActiveStartTS()))) / 1000.0);
						esDeployment.setActiveDuration(Math.round(activeDuration * 1000.0) / 1000.0);
					}
					esDeployment.setDeploymentState(deployment.getDeploymentState());
					esDeployment.setTotalTime((System.currentTimeMillis() - esDeployment.getCreated().getTime())/1000.0);
			}
			else if(SearchConstants.DPMT_STATE_ACTIVE.equalsIgnoreCase(deployment.getDeploymentState())){

				if(esDeployment!=null){
					esDeployment.setActiveStartTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
					if(SearchConstants.DPMT_STATE_FAILED.equalsIgnoreCase(esDeployment.getDeploymentState())){
						esDeployment.setRetryCount(esDeployment.getRetryCount()+1);
						esDeployment.setFailedEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double failedDuration = esDeployment.getFailedDuration() +
								(((SearchUtil.getTimefromDate(esDeployment.getFailedEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getFailedStartTS())))/1000.0);
						esDeployment.setFailedDuration(Math.round(failedDuration * 1000.0) / 1000.0);
						esDeployment.setDeploymentState(deployment.getDeploymentState());
					}
					else if(SearchConstants.DPMT_STATE_PAUSED.equalsIgnoreCase(esDeployment.getDeploymentState())){
						esDeployment.setPausedEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double pausedDuration = esDeployment.getPausedDuration() + 
								(((SearchUtil.getTimefromDate(esDeployment.getPausedEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getPausedStartTS())))/1000.0);
						esDeployment.setPausedDuration(Math.round(pausedDuration * 1000.0) / 1000.0);
						esDeployment.setDeploymentState(deployment.getDeploymentState());
					}
					else if(SearchConstants.DPMT_STATE_PENDING.equalsIgnoreCase(esDeployment.getDeploymentState())){
						esDeployment.setPendingEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
						double pendingDuration = esDeployment.getPendingDuration() + 
								(((SearchUtil.getTimefromDate(esDeployment.getPendingEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getPendingStartTS())))/1000.0);
						esDeployment.setPendingDuration(Math.round(pendingDuration * 1000.0) / 1000.0);
						esDeployment.setDeploymentState(deployment.getDeploymentState());
					}
					updateTotalTime(esDeployment);
				}
				else{
					deployment.setActiveStartTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
				}
			}
			else if(SearchConstants.DPMT_STATE_PENDING.equalsIgnoreCase(deployment.getDeploymentState())){
				if(esDeployment == null){esDeployment=deployment;} 
				esDeployment.setPendingStartTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
				esDeployment.setDeploymentState(deployment.getDeploymentState());
			}
			else if(SearchConstants.DPMT_STATE_PAUSED.equalsIgnoreCase(deployment.getDeploymentState())){
				esDeployment.setPauseCnt(deployment.getPauseCnt() + 1);
				esDeployment.setPausedStartTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
				if(SearchConstants.DPMT_STATE_ACTIVE.equalsIgnoreCase(esDeployment.getDeploymentState())){
					esDeployment.setActiveEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
					double activeDuration = esDeployment.getActiveDuration() +
							(((SearchUtil.getTimefromDate(esDeployment.getActiveEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getActiveStartTS()))) / 1000.0);
					esDeployment.setActiveDuration(Math.round(activeDuration * 1000.0) / 1000.0);
				}
				esDeployment.setDeploymentState(deployment.getDeploymentState());
				updateTotalTime(esDeployment);
			}	
			else if(SearchConstants.DPMT_STATE_FAILED.equalsIgnoreCase(deployment.getDeploymentState())){
				esDeployment.setFailureCnt(esDeployment.getFailureCnt() + 1);
				esDeployment.setFailedStartTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
				if(SearchConstants.DPMT_STATE_ACTIVE.equalsIgnoreCase(esDeployment.getDeploymentState())){
					esDeployment.setActiveEndTS(DateUtil.formatDate(new Date(), CmsConstants.SEARCH_TS_PATTERN));
					double activeDuration = esDeployment.getActiveDuration() +
							(((SearchUtil.getTimefromDate(esDeployment.getActiveEndTS())) - (SearchUtil.getTimefromDate(esDeployment.getActiveStartTS()))) / 1000.0);
					esDeployment.setActiveDuration(Math.round(activeDuration * 1000.0) / 1000.0);
				}
				esDeployment.setDeploymentState(deployment.getDeploymentState());
				updateTotalTime(esDeployment);
			}
		} catch (Exception e) {
			logger.error("Error in processing deployment message "+ e.getMessage());
		}
		
		return esDeployment!=null?esDeployment:deployment;
	}
	
	private CmsDeploymentSearch fetchDeploymentRecord(long deploymentId) {
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withTypes("deployment").withQuery(queryString(String.valueOf(deploymentId)).field("deploymentId"))
        .build();
		
		List<CmsDeploymentSearch> esDeploymentList = template.queryForList(searchQuery, CmsDeploymentSearch.class);
		return !esDeploymentList.isEmpty()?esDeploymentList.get(0):null;
	}
	
	
	/**
	 * Update the total time taken by the deployment before it reaches a terminal state
	 * 
	 * @param esDeployment
	 */
	private void updateTotalTime(CmsDeploymentSearch esDeployment){
		double tt = esDeployment.getActiveDuration() + esDeployment.getFailedDuration() + esDeployment.getPausedDuration() + esDeployment.getPendingDuration();
		 esDeployment.setTotalTime(Math.round(tt * 1000.0) / 1000.0);
	}


	private boolean isFinalState(String state){
		return SearchConstants.DPMT_STATE_COMPLETE.equalsIgnoreCase(state) || SearchConstants.DPMT_STATE_CANCELED.equalsIgnoreCase(state);
	}
	

	public ElasticsearchTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ElasticsearchTemplate template) {
		this.template = template;
	}
}
