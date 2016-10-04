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
package com.oneops.search.msg.processor;

import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsOpsProcedureSearch;
import org.apache.log4j.Logger;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryString;

public class OpsProcMessageProcessor {

	private static Logger logger = Logger.getLogger(OpsProcMessageProcessor.class);
	private ElasticsearchTemplate template;
	
	
	/**
	 * 
	 * @param procedure
	 * @return
	 */
	public CmsOpsProcedureSearch processOpsProcMsg(CmsOpsProcedureSearch procedure) {
		
		CmsOpsProcedureSearch esProcedure = null;
		try {
			esProcedure = fetchprocedureRecord(procedure.getProcedureId());

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
			if(isFinalState(procedure.getProcedureState().getName())){
				
				if(esProcedure!=null && OpsProcedureState.canceled.getName().equalsIgnoreCase(procedure.getProcedureState().getName())) {
					if(OpsProcedureState.failed.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())){
						esProcedure.setFailedEndTS(simpleDateFormat.format(new Date()));

						double failedDuration = esProcedure.getFailedDuration() +
								((((Long) simpleDateFormat.parse(esProcedure.getFailedEndTS()).getTime()) - ((Long) simpleDateFormat.parse(esProcedure.getFailedStartTS()).getTime()))/1000.0); 
						esProcedure.setFailedDuration(Math.round(failedDuration * 1000.0)/1000.0);
					}
				}else if(esProcedure!=null && OpsProcedureState.complete.getName().equalsIgnoreCase(procedure.getProcedureState().getName())){
					if(OpsProcedureState.active.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())){
						esProcedure.setActiveEndTS(simpleDateFormat.format(new Date()));

						double activeDuration = esProcedure.getActiveDuration() +
								((((Long) simpleDateFormat.parse((esProcedure.getActiveEndTS())).getTime()) - ((Long) simpleDateFormat.parse(esProcedure.getActiveStartTS()).getTime())) / 1000.0);
						esProcedure.setActiveDuration(Math.round(activeDuration * 1000.0)/1000.0);
					}
				}
				esProcedure.setProcedureState(procedure.getProcedureState());
				esProcedure.setTotalTime(((System.currentTimeMillis()) - (esProcedure.getCreated().getTime()))/1000.0);
			}
			else if(OpsProcedureState.active.getName().equalsIgnoreCase(procedure.getProcedureState().getName())){

				if(esProcedure!=null){ 
					esProcedure.setActiveStartTS(simpleDateFormat.format(new Date()));
					if(OpsProcedureState.failed.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())){
						esProcedure.setRetryCount(esProcedure.getRetryCount()+1);
						esProcedure.setFailedEndTS(simpleDateFormat.format(new Date()));

						double failedDuration = esProcedure.getFailedDuration() +
								((((Long) simpleDateFormat.parse(esProcedure.getFailedEndTS()).getTime()) - ((Long) simpleDateFormat.parse(esProcedure.getFailedStartTS()).getTime()))/1000.0);
						esProcedure.setFailedDuration(Math.round(failedDuration * 1000.0)/1000.0);
						esProcedure.setProcedureState(procedure.getProcedureState());
					}
				}
				else{
					procedure.setActiveStartTS(simpleDateFormat.format(new Date()));
				}
			}
			else if(OpsProcedureState.failed.getName().equalsIgnoreCase(procedure.getProcedureState().getName())){
				esProcedure.setFailureCnt(esProcedure.getFailureCnt() + 1);
				esProcedure.setFailedStartTS(simpleDateFormat.format(new Date()));
				if(OpsProcedureState.active.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())){
					esProcedure.setActiveEndTS(simpleDateFormat.format(new Date()));

					double activeDuration = esProcedure.getActiveDuration() +
							((((Long) simpleDateFormat.parse((esProcedure.getActiveEndTS())).getTime()) - ((Long) simpleDateFormat.parse(esProcedure.getActiveStartTS()).getTime())) / 1000.0);
					esProcedure.setActiveDuration(Math.round(activeDuration * 1000.0)/1000.0);
				}
				esProcedure.setProcedureState(procedure.getProcedureState());
			}
		} catch (Exception e) {
			logger.error("Error in processing ops-procedure message "+ e.getMessage());
		}
		
		return esProcedure!=null?esProcedure:procedure;
	}
	
	
	private CmsOpsProcedureSearch fetchprocedureRecord(long procedureId) {
		SearchQuery searchQuery = new NativeSearchQueryBuilder()  // todo: index name
				.withTypes("opsprocedure").withQuery(queryString(String.valueOf(procedureId)).field("procedureId"))
				.build();
		
		List<CmsOpsProcedureSearch> esProcedureList = template.queryForList(searchQuery, CmsOpsProcedureSearch.class);
		return !esProcedureList.isEmpty()?esProcedureList.get(0):null;
	}


	private boolean isFinalState(String state){
		return OpsProcedureState.complete.getName().equalsIgnoreCase(state) || OpsProcedureState.canceled.getName().equalsIgnoreCase(state);
	}
	

	public ElasticsearchTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ElasticsearchTemplate template) {
		this.template = template;
	}
}
