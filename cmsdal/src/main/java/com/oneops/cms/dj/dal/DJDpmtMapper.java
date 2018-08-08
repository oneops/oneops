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
package com.oneops.cms.dj.dal;

import com.oneops.cms.dj.domain.*;
import com.oneops.cms.util.TimelineQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The Interface DJDpmtMapper.
 */
public interface DJDpmtMapper {
	
	void createDeployment(CmsDeployment cmsDeployment);
	void createDpmtApproval(CmsDpmtApproval cmsDpmtApproval);
	void dpmtApprove(CmsDpmtApproval cmsDpmtApproval);
	void updDpmtApproval(CmsDpmtApproval cmsDpmtApproval);
	void retryDeployment(CmsDeployment cmsDeployment);
	void cancelDeployment(CmsDeployment cmsDeployment);
	void completeDeployment(CmsDeployment cmsDeployment);
	void updDeployment(CmsDeployment cmsDeployment);
	void updDpmtRecordState(CmsDpmtRecord cmsDpmtRecord);
	void resetFailedRecords(CmsDeployment cmsDeployment);

	CmsDeployment getDeployment(long deploymentId);
	CmsDeployment getDeploymentSimple(long deploymentId);
	List<CmsDeployment> findDeployment(@Param("nsPath") String nsPath,@Param("state") String state);
	List<CmsDeployment> findDeploymentRecursive(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("state") String state);
	List<CmsDeployment> findDeploymentsByTimePeriod(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("start") Date start, @Param("end") Date end);
	List<CmsDeployment> findLatestDeployment(@Param("nsPath") String nsPath,@Param("state") String state);
	List<CmsDeployment> findLatestDeploymentRecursive(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("state") String state);
	List<CmsDeployment> findDeploymentByReleaseId(@Param("releaseId") long releaseId, @Param("state") String state);
	List<CmsDeployment> findLatestDeploymentByReleaseId(@Param("releaseId") long releaseId, @Param("state") String state);
	long countDeployment(@Param("nsPath") String nsPath,@Param("state") String state);
	long countDeploymentRecursive(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("state") String state);
	List<Map<String,Object>> countDeploymentGroupByNs(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("state") String state);
	
	List<CmsDpmtRecord> getDeploymentRecords(long deploymentId);	
	CmsDpmtRecord getDeploymentRecord(long dpmtRfcId);
	List<CmsDpmtRecord> getDeploymentRecordCis(long deploymentId);
	List<CmsDpmtRecord> getDeploymentRecordCisByListOfIds(@Param("value")long deploymentId, @Param("list") List<Long> list);
	List<CmsDpmtRecord> getDeploymentRecordCisByRfcIds(@Param("value")long deploymentId, @Param("list") List<Long> list);
	List<CmsDpmtRecord> getDeploymentRecordRelations(long deploymentId);
	List<CmsDpmtRecord> getDeploymentRecordsByState(@Param("deploymentId") long deploymentId, @Param("state") String state, @Param("execOrder") Integer execOrder);
	List<CmsDpmtRecord> getDeploymentRecordsUpdatedAfter(@Param("deploymentId") long deploymentId, @Param("timestamp") Date timestamp);
	long getDeploymentRecordsCountByState(@Param("deploymentId") long deploymentId, @Param("state") String state, @Param("execOrder") Integer execOrder);
	List<CmsDpmtRecord> getDeploymentRecordsByCiId(@Param("ciId") long ciId, @Param("state") String state);
	List<CmsWorkOrder> getWorkOrders(@Param("deploymentId") long deploymentId, @Param("state") String state, @Param("execOrder") Integer execOrder);
	List<CmsWorkOrder> getWorkOrdersLimited(@Param("deploymentId") long deploymentId, @Param("state") String state, @Param("execOrder") Integer execOrder, @Param("limit") Integer limit);
	CmsWorkOrder getWorkOrder(@Param("dpmtRecordId") long dpmtRecordId, @Param("state") String state, @Param("execOrder") Integer execOrder);
	
	List<CmsRfcCI> getDeploymentRfcCIs(@Param("deploymentId") long deploymentId, @Param("state") String state, @Param("execOrder") Integer execOrder,@Param("classNames") String[] classNames,@Param("action") String action);

	List<CmsDpmtStateChangeEvent> getDeploymentStateHist(long deploymentId);
	
	List<Long> getToCiIdsForDeployment(@Param("deploymentId") long deploymentId, @Param("state") String state, @Param("relName") String relName);
	List<Long> getToCiIdsForRelease(@Param("releaseId") long deploymentId, @Param("relName") String relName);
	List<Long> getToCiIdsForReleasePending(@Param("releaseId") long deploymentId, @Param("relName") String relName);
	
	List<CmsDpmtApproval> getDpmtApprovals(long deploymentId);
	CmsDpmtApproval getDpmtApproval(long approvalId);
	List<TimelineDeployment> getDeploymentsByFilter(TimelineQueryParam queryParam);
	List<TimelineDeployment> getDeploymentsByNsPath(TimelineQueryParam queryParam);
	List<Map<String,Object>> getDeploymentRecordsCountByStates(@Param("deploymentId") long deploymentId, @Param("step") Integer step);
	void updateDeploymentCurrentStep(CmsDeployment cmsDeployment);
	void updateDeploymentExecInfo(CmsDeployment cmsDeployment);
	void getDpmtLock(long deploymentId);
	void createDeploymentExec(@Param("deploymentId") long deploymentId, @Param("step") int step, @Param("state") String state);
	int getAndUpdateStepState(@Param("deploymentId") long deploymentId, @Param("step") int step, @Param("newState") String newState);

}
