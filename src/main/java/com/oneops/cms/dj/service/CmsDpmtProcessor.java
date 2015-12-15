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
package com.oneops.cms.dj.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtApproval;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.domain.CmsDpmtStateChangeEvent;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.ns.dal.NSMapper;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.ListUtils;

/**
 * The Class CmsDpmtProcessor.
 */
public class CmsDpmtProcessor {

	static Logger logger = Logger.getLogger(CmsDpmtProcessor.class);
	
	private DJDpmtMapper dpmtMapper;
	private NSMapper nsMapper;
	private CmsCmProcessor cmProcessor;
	private CmsRfcProcessor rfcProcessor;
	private Gson gson = new Gson();

	private static final String DPMT_STATE_PENDING = "pending";
	private static final String DPMT_STATE_ACTIVE = "active";
	private static final String DPMT_STATE_CANCELED = "canceled";
	private static final String DPMT_STATE_FAILED = "failed";
	private static final String DPMT_STATE_PAUSED = "paused";
	private static final String DPMT_STATE_COMPLETE = "complete";
	
	private static final String APPROVAL_STATE_PENDING = "pending";
	private static final String APPROVAL_STATE_APPROVED = "approved";
	private static final String APPROVAL_STATE_REJECTED = "rejected";	
	//private static final String APPROVAL_STATE_EXPIRED = "expired";	
	
	
	private static final String DPMT_RECORD_STATE_INPROGRESS = "inprogress";
	private static final String RELEASE_STATE_OPEN = "open";
	private static final String MANIFEST_PLATFORM_CLASS = "manifest.Platform";
	private static final String ONEOPS_AUTOREPLACE_USER = "oneops-autoreplace";
	private static final int BOM_RELASE_NSPATH_LENGTH = 5;

	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	/**
	 * Sets the dpmt mapper.
	 *
	 * @param dpmtMapper the new dpmt mapper
	 */
	public void setDpmtMapper(DJDpmtMapper dpmtMapper) {
		this.dpmtMapper = dpmtMapper;
	}

	public void setNsMapper(NSMapper nsMapper) {
		this.nsMapper = nsMapper;
	}

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}
	
	/**
	 * Deploy release.
	 *
	 * @param dpmt the dpmt
	 * @return the cms deployment
	 */
	public CmsDeployment deployRelease(CmsDeployment dpmt) {
		
		validateReleaseForDpmt(dpmt.getReleaseId());
		
		List<CmsDeployment> existingDpmts = dpmtMapper.findLatestDeploymentByReleaseId(dpmt.getReleaseId(), null);
		
		for (CmsDeployment existingDpmt : existingDpmts){
			if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState()) 
				|| DPMT_STATE_FAILED.equalsIgnoreCase(existingDpmt.getDeploymentState())
				|| DPMT_STATE_PENDING.equalsIgnoreCase(existingDpmt.getDeploymentState())
				|| DPMT_STATE_PAUSED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				String errMsg = "There is an " + existingDpmt.getDeploymentState() + " deployment already, returning this. dpmtId = " + existingDpmt.getDeploymentId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_STATE_ALREADY_DEPLOYMENT_ERROR, errMsg);
			}
		}
		
		if (ONEOPS_AUTOREPLACE_USER.equals(dpmt.getCreatedBy()))  {
			dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
			dpmtMapper.createDeployment(dpmt);
		} else  {
			if (needApprovalForNewDpmt(dpmt)) {
				dpmt.setDeploymentState(DPMT_STATE_PENDING);
				dpmtMapper.createDeployment(dpmt);
				needApproval(dpmt); // to cerate approval for this deployment
			} else {
				dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
				dpmtMapper.createDeployment(dpmt);
			}
		}
		logger.info("Created new deployment, dpmtId = " + dpmt.getDeploymentId());
		
		return dpmtMapper.getDeployment(dpmt.getDeploymentId());
	}
	
	private void validateReleaseForDpmt(long releaseId) {
		CmsRelease release = rfcProcessor.getReleaseById(releaseId);
		if (!release.getReleaseState().equals(RELEASE_STATE_OPEN)) {
			String errMsg = "The release with id " + releaseId + " is not open, Can't create deployment for it!";
			logger.error(errMsg);
			throw new DJException(CmsError.DJ_RFC_RELEASE_NOT_OPEN_ERROR, errMsg);
		}
		String[] nsParts = release.getNsPath().split("/");
		if (nsParts.length < BOM_RELASE_NSPATH_LENGTH || !"bom".equals(nsParts[nsParts.length-1])) {
			String errMsg = "The release with id " + releaseId + " is not a BOM release, Can't create deployment for it!";
			logger.error(errMsg);
			throw new DJException(CmsError.DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR, errMsg);
		}
	}
	
	public List<CmsDpmtApproval> updateApprovalList(List<CmsDpmtApproval> approvals) {
		
		List<CmsDpmtApproval> result = new ArrayList<CmsDpmtApproval>();
		ListUtils<Long> lu = new ListUtils<Long>();
		try {
			Map<Long, List<CmsDpmtApproval>> approvalMap = lu.toMapOfList(approvals, "deploymentId");
			for (Map.Entry<Long, List<CmsDpmtApproval>> dpmtApprovals : approvalMap.entrySet()) {
				long dpmtId = dpmtApprovals.getKey();
				String userId = null;
				String rejectComments = null;
				boolean rejected = false;
				for (CmsDpmtApproval approval : dpmtApprovals.getValue()) {
					userId = approval.getUpdatedBy();
					if (approval.getExpiresIn() == 0) {
						approval.setExpiresIn(1);
					}
					dpmtMapper.updDpmtApproval(approval);
					result.add(dpmtMapper.getDpmtApproval(approval.getApprovalId()));
					if (approval.getState().equals(APPROVAL_STATE_REJECTED)) {
						rejected = true;
						rejectComments = approval.getComments(); 
					}
				}
				CmsDeployment dpmt = new CmsDeployment();
				dpmt.setDeploymentId(dpmtId);
				dpmt.setUpdatedBy(userId);
				if (rejected) {
					dpmt.setDeploymentState(DPMT_STATE_CANCELED);
					dpmt.setComments(rejectComments);
				} else {
					dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
				}
				updateDeployment(dpmt);
			}
			return result;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
		}
	}
	
	/*
	public List<CmsDeployment> dpmtApprove(List<CmsDpmtApproval> approvals) {
		List<CmsDeployment> resultDpmts = new ArrayList<CmsDeployment>(); 
		ListUtils<Long> lu = new ListUtils<Long>();
		try {
			Map<Long, List<CmsDpmtApproval>> approvalMap = lu.toMapOfList(approvals, "deploymentId");
			for (Map.Entry<Long, List<CmsDpmtApproval>> dpmtApprovals : approvalMap.entrySet()) {
				long dpmtId = dpmtApprovals.getKey();
				String userId = null;
				for (CmsDpmtApproval approval : dpmtApprovals.getValue()) {
					userId = approval.getUpdatedBy();
					dpmtMapper.dpmtApprove(approval);
				}
				CmsDeployment dpmt = new CmsDeployment();
				dpmt.setDeploymentId(dpmtId);
				dpmt.setUpdatedBy(userId);
				dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
				resultDpmts.add(updateDeployment(dpmt));
			}
			return resultDpmts;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
		}
	}
	*/
	
	private boolean needApproval(CmsDeployment dpmt) {
		boolean needApproval = false;
		
		List<Long>	cloudIds = dpmtMapper.getToCiIdsForDeployment(dpmt.getDeploymentId(), "pending", "base.DeployedTo");
		
		List<CmsDpmtApproval> approvals = dpmtMapper.getDpmtApprovals(dpmt.getDeploymentId());
		//convert to map
		Map<Long,List<CmsDpmtApproval>> approvalMap = new HashMap<Long,List<CmsDpmtApproval>>();
		for (CmsDpmtApproval approval : approvals) {
			if (!approvalMap.containsKey(approval.getGovernCiId())) {
				approvalMap.put(approval.getGovernCiId(), new ArrayList<CmsDpmtApproval>());
			}
			approvalMap.get(approval.getGovernCiId()).add(approval);
		}
		
		
		List<CmsCI> governCIs = new ArrayList<CmsCI>();
		for (long cloudId : cloudIds) {
			for (CmsCIRelation supportRel : cmProcessor.getFromCIRelations(cloudId, "base.SupportedBy", null)) {
				governCIs.add(supportRel.getToCi());
			}
		}		
		for (long cloudId : cloudIds) {
			for (CmsCIRelation complianceRel : cmProcessor.getFromCIRelations(cloudId, "base.CompliesWith", null)) {
				governCIs.add(complianceRel.getToCi());
			}
		}		
		
		for (CmsCI governCi : governCIs) {
			if (governCi.getAttribute("enabled") != null
					&& Boolean.TRUE.toString().equals(governCi.getAttribute("enabled").getDjValue())
					&& governCi.getAttribute("approval") != null
					&&  Boolean.TRUE.toString().equals(governCi.getAttribute("approval").getDjValue())) {
				if (!approvalMap.containsKey(governCi.getCiId())) {
					createDpmtApproval(dpmt.getDeploymentId(), governCi);
					needApproval= true;
				} else {
					    CmsDpmtApproval approval =  getLatestApproval(approvalMap.get(governCi.getCiId())); 
						if (approval == null) {
							createDpmtApproval(dpmt.getDeploymentId(), governCi);
							needApproval=true;
						} else if (approval.getState().equals(APPROVAL_STATE_PENDING)) {
							needApproval=true;
						} else if (approval.getIsExpired() && approval.getState().equals(APPROVAL_STATE_APPROVED)){
							//approval.setState(APPROVAL_STATE_EXPIRED);
							//dpmtMapper.updDpmtApproval(approval);
							createDpmtApproval(dpmt.getDeploymentId(), governCi);
							needApproval=true;
						}
					
				}
			}
		}
		return needApproval;
	}
	
	private CmsDpmtApproval getLatestApproval(List<CmsDpmtApproval> approvals) {
		if (approvals.size() == 0) {
			return null;
		} else {
			Collections.sort(approvals, new Comparator<CmsDpmtApproval>() {
		        public int compare(CmsDpmtApproval a1, CmsDpmtApproval a2) {
		            //Sorts by 'ApprovalId' property desc
		            return (int)(a2.getApprovalId()-a1.getApprovalId());
		        }
		    });
			return approvals.get(0);
		}
	}

	private boolean needApprovalForNewDpmt(CmsDeployment dpmt) {
		boolean needApproval = false;
		List<Long> cloudIds = dpmtMapper.getToCiIdsForReleasePending(dpmt.getReleaseId(), "base.DeployedTo");
		List<CmsCI> governCIs = new ArrayList<CmsCI>();
		for (long cloudId : cloudIds) {
			for (CmsCIRelation supportRel : cmProcessor.getFromCIRelations(cloudId, "base.SupportedBy", null)) {
				governCIs.add(supportRel.getToCi());
			}
		}		
		for (long cloudId : cloudIds) {
			for (CmsCIRelation complianceRel : cmProcessor.getFromCIRelations(cloudId, "base.CompliesWith", null)) {
				governCIs.add(complianceRel.getToCi());
			}
		}		
		
		for (CmsCI governCi : governCIs) {
			if (governCi.getAttribute("enabled") != null
					&& Boolean.TRUE.toString().equals(governCi.getAttribute("enabled").getDjValue())
					&& governCi.getAttribute("approval") != null
					&&  Boolean.TRUE.toString().equals(governCi.getAttribute("approval").getDjValue())) {
					return true; //this domt need approval
			}
		}
		return needApproval;
	}
	
	
	
	private void createDpmtApproval(long dpmtId, CmsCI governCi) {
		CmsDpmtApproval approval = new CmsDpmtApproval();
		approval.setDeploymentId(dpmtId);
		approval.setGovernCiId(governCi.getCiId());
		approval.setGovernCiJson(gson.toJson(governCi));
		dpmtMapper.createDpmtApproval(approval);
		logger.info("Created approval for dpmtId = " + dpmtId + " : " + approval.getGovernCiJson());
	}
	
	
	@SuppressWarnings("unused")
	private boolean _envNeedApproval(CmsDeployment dpmt) {
		CmsCI env = cmProcessor.getEnvByNS(dpmt.getNsPath());
		if (env.getAttribute("approve") != null 
				&& env.getAttribute("approve").getDjValue() != null 
				&& env.getAttribute("approve").getDjValue().equalsIgnoreCase("true")){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Update deployment.
	 *
	 * @param dpmt the dpmt
	 * @return the cms deployment
	 */
	public CmsDeployment updateDeployment(CmsDeployment dpmt) {
		CmsDeployment existingDpmt = dpmtMapper.getDeployment(dpmt.getDeploymentId());
		//in case the new stae = old state just update the comments and timestamp
		//CMS_ALL event will not be generated
		if (existingDpmt.getDeploymentState().equals(dpmt.getDeploymentState())) {
			dpmt.setDeploymentState(null);
		}
		
		if (DPMT_STATE_CANCELED.equalsIgnoreCase(dpmt.getDeploymentState())) {
			if (dpmtMapper.getDeploymentRecordsCountByState(dpmt.getDeploymentId(), DPMT_RECORD_STATE_INPROGRESS, null) > 0) {
				String errMsg = "The deployment still have active work orders!"; 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_FAILED_ERROR, errMsg);
			}
			if (DPMT_STATE_FAILED.equalsIgnoreCase(existingDpmt.getDeploymentState())
				|| DPMT_STATE_PAUSED.equalsIgnoreCase(existingDpmt.getDeploymentState())
				|| DPMT_STATE_PENDING.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				dpmtMapper.cancelDeployment(dpmt);
				logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
			} else {
				String errMsg = "The deployment is not in failed/active state - " + existingDpmt.getDeploymentState(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_FAILED_ERROR, errMsg);
			}
		} else if (DPMT_STATE_ACTIVE.equalsIgnoreCase(dpmt.getDeploymentState())) {
			if (DPMT_STATE_PENDING.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				if (!needApproval(dpmt)) {
					dpmtMapper.updDeployment(dpmt);
					logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
				}
			} else if (DPMT_STATE_FAILED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				dpmtMapper.resetFailedRecords(dpmt);
				if (needApproval(dpmt)) {
					dpmt.setDeploymentState(DPMT_STATE_PENDING);
					dpmtMapper.updDeployment(dpmt);
					logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
				} else {
					dpmtMapper.retryDeployment(dpmt);
				}
			} else if(DPMT_STATE_PAUSED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				if (needApproval(dpmt)) {
					dpmt.setDeploymentState(DPMT_STATE_PENDING);
				}
				dpmtMapper.updDeployment(dpmt);
				logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
			} else {
				String errMsg = "The deployment is not in failed state - " + existingDpmt.getDeploymentState(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_FAILED_ERROR, errMsg);
			}
		} else if (DPMT_STATE_COMPLETE.equalsIgnoreCase(dpmt.getDeploymentState())) {
			if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState())
				|| DPMT_STATE_PAUSED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				//dpmtMapper.completeDeployment(dpmt);
				completeDeployment(dpmt);
				logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
			} else {
				String errMsg = "The deployment is not in active state - " + existingDpmt.getDeploymentState(); 
				logger.warn(errMsg);
				//throw new DJException(errMsg);
			}
		} else if (DPMT_STATE_FAILED.equalsIgnoreCase(dpmt.getDeploymentState())) {
			if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState())
				|| DPMT_STATE_PAUSED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				dpmtMapper.updDeployment(dpmt);
				logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
			} else {
				String errMsg = "The deployment is not in active state - " + existingDpmt.getDeploymentState(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_ERROR, errMsg);
			}
		} else if (DPMT_STATE_PAUSED.equalsIgnoreCase(dpmt.getDeploymentState())) {
			if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
				dpmtMapper.updDeployment(dpmt);
				logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
			} else {
				String errMsg = "The deployment is not in active state - " + existingDpmt.getDeploymentState(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_ERROR, errMsg);
			}
		} else if (dpmt.getDeploymentState() == null) {
				dpmtMapper.updDeployment(dpmt);
				logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());
		}
		else {
			String errMsg = "This state is not supported - " + dpmt.getDeploymentState(); 
			logger.error(errMsg);
			throw new DJException(CmsError.DJ_NOT_SUPPORTED_STATE_ERROR, errMsg);
		}

		

		return dpmtMapper.getDeployment(dpmt.getDeploymentId());
	}

	private void completeDeployment(CmsDeployment dpmt) {
		dpmtMapper.completeDeployment(dpmt);
		CmsRelease bomRelease = rfcProcessor.getReleaseById(dpmt.getReleaseId());
		CmsRelease manifestRelease = rfcProcessor.getReleaseById(bomRelease.getParentReleaseId());
		List<CmsCI> platforms = cmProcessor.getCiBy3NsLike(manifestRelease.getNsPath(), MANIFEST_PLATFORM_CLASS, null);
		for (CmsCI plat : platforms) {
			boolean vacuumAllowed = true;
			List<CmsCIRelation> platformCloudRels = cmProcessor.getFromCIRelationsNaked(plat.getCiId(), "base.Consumes", "account.Cloud");
			for (CmsCIRelation platformCloudRel : platformCloudRels) {
				if (platformCloudRel.getAttribute("adminstatus") != null
						&& !CmsConstants.CLOUD_STATE_ACTIVE.equals(platformCloudRel.getAttribute("adminstatus").getDjValue())) {
					String platBomNs = plat.getNsPath().replace("/manifest/", "/bom/");
					List<CmsCIRelation> deployedToRels = cmProcessor.getToCIRelationsByNsNaked(platformCloudRel.getToCiId(), "base.DeployedTo", null, null, platBomNs);
					if (deployedToRels.size() >0) {
						vacuumAllowed = false;
						break;
					}
				} else {
					List<CmsCI> componentsToDelet = cmProcessor.getCiByNsLikeByStateNaked(plat.getNsPath(), null, "pending_deletion");
					for (CmsCI component : componentsToDelet) {
						List<CmsCIRelation> realizedAsRels = cmProcessor.getFromCIRelationsNakedNoAttrs(component.getCiId(), "base.RealizedAs", null, null);
						if (realizedAsRels.size() > 0) {
							vacuumAllowed = false;
							break;
						}
					}
					if (!vacuumAllowed) {
						break;
					}
				}
			}
			if (vacuumAllowed) {
				nsMapper.vacuumNamespace(plat.getNsId());
			}
		}
		
		deleteGlobalVars(manifestRelease.getNsPath());
	}
	
	/**
	 * Delete global variables in pending_delete state
	 * 
	 * @param platforms
	 */
	private void deleteGlobalVars(String envNsPath) {
		List <CmsCI> toDelComponents = cmProcessor.getCiByNsLikeByStateNaked(envNsPath + "/", null, "pending_deletion");
		//if no components are marked for deletion then delete the global variables in pending_deletion state
		if(toDelComponents.isEmpty()){
			//delete global vars in pending_delete state
			for (CmsCI globalVar : cmProcessor.getCiByNsLikeByStateNaked(envNsPath, "manifest.Globalvar", "pending_deletion")) {
				cmProcessor.deleteCI(globalVar.getCiId(), true);
			}
		}
	}


	/**
	 * Update dpmt record.
	 *
	 * @param rec the rec
	 * @return the cms dpmt record
	 */
	public CmsDpmtRecord updateDpmtRecord(CmsDpmtRecord rec) {
		dpmtMapper.updDpmtRecordState(rec);
		return dpmtMapper.getDeploymentRecord(rec.getDpmtRecordId());
	}

	/**
	 * Complete work order.
	 *
	 * @param wo the wo
	 */
	public void completeWorkOrder(CmsWorkOrder wo) {
		dpmtMapper.updDpmtRecordState(wo);
		if (!wo.getRfcCi().getRfcAction().equalsIgnoreCase("delete")  &&  wo.getResultCi() != null) {
			cmProcessor.updateCI(wo.getResultCi());
		}
		//return dpmtMapper.getDeploymentRecord(wo.getDpmtRecordId());
	}
	
	/**
	 * Gets the deployment.
	 *
	 * @param dpmtId the dpmt id
	 * @return the deployment
	 */
	public CmsDeployment getDeployment(long dpmtId) {
		return dpmtMapper.getDeployment(dpmtId);
	}
	
	/**
	 * Find deployment.
	 *
	 * @param nsPath the ns path
	 * @param state the state
	 * @param recursive the recursive
	 * @return the list
	 */
	public List<CmsDeployment> findDeployment(String nsPath, String state, Boolean recursive) {
		if (recursive != null && recursive) {
			String nsLike = CmsUtil.likefyNsPath(nsPath);
			return dpmtMapper.findDeploymentRecursive(nsPath, nsLike, state);
		} else {
			return dpmtMapper.findDeployment(nsPath, state);
		}
	}

	/**
	 * Find latest deployment.
	 *
	 * @param nsPath the ns path
	 * @param state the state
	 * @param recursive the recursive
	 * @return the list
	 */
	public List<CmsDeployment> findLatestDeployment(String nsPath, String state, Boolean recursive) {
		if (recursive != null && recursive ) {
			String nsLike = CmsUtil.likefyNsPath(nsPath);
			return dpmtMapper.findLatestDeploymentRecursive(nsPath,nsLike, state);
		} else {
			return dpmtMapper.findLatestDeployment(nsPath, state);
		}
	}
	
	/**
	 * Count deployment.
	 *
	 * @param nsPath the ns path
	 * @param state the state
	 * @param recursive the recursive
	 * @return the long
	 */
	public long countDeployment(String nsPath, String state, Boolean recursive) {
		if (recursive != null && recursive ) {
			String nsLike = CmsUtil.likefyNsPath(nsPath);
			return dpmtMapper.countDeploymentRecursive(nsPath, nsLike, state);
		} else {
			return dpmtMapper.countDeployment(nsPath, state);
		}
	}

	/**
	 * Count deployment group by.
	 *
	 * @param nsPath the ns path
	 * @param state the state
	 * @return the map
	 */
	public Map<String, Long> countDeploymentGroupBy(String nsPath, String state) {
		Map<String, Long> result = new HashMap<String, Long>();
		String nsLike = CmsUtil.likefyNsPath(nsPath);
		List<Map<String,Object>> stats = dpmtMapper.countDeploymentGroupByNs(nsPath, nsLike, state);
		for (Map<String,Object> row : stats) {
			String nspath = (String)row.get("path");
			Long cnt = (Long)row.get("cnt");
			result.put(nspath, cnt);
		}
		
		return result;
	}
	
	
	/**
	 * Find deployment by release id.
	 *
	 * @param releaseId the release id
	 * @param state the state
	 * @return the list
	 */
	public List<CmsDeployment> findDeploymentByReleaseId(long releaseId, String state) {
		return dpmtMapper.findDeploymentByReleaseId(releaseId, state);
	}
	
	/**
	 * Find latest deployment by release id.
	 *
	 * @param releaseId the release id
	 * @param state the state
	 * @return the list
	 */
	public List<CmsDeployment> findLatestDeploymentByReleaseId(long releaseId, String state) {
		return dpmtMapper.findLatestDeploymentByReleaseId(releaseId, state);
	}
	
	/**
	 * Gets the deployment records.
	 *
	 * @param dpmtId the dpmt id
	 * @return the deployment records
	 */
	public List<CmsDpmtRecord> getDeploymentRecords(long dpmtId) {
		return dpmtMapper.getDeploymentRecords(dpmtId);
	}

	/**
	 * Gets the deployment approvals.
	 *
	 * @param dpmtId the dpmt id
	 * @return the deployment approvals
	 */
	public List<CmsDpmtApproval> getDeploymentApprovals(long dpmtId) {
		return dpmtMapper.getDpmtApprovals(dpmtId);
	}
	
	/**
	 * Gets the deployment approval.
	 *
	 * @param approvalId the approval id
	 * @return the deployment approval
	 */
	public CmsDpmtApproval getDeploymentApproval(long approvalId) {
		return dpmtMapper.getDpmtApproval(approvalId);
	}
	
	/**
	 * Gets the deployment record by dpmtRfcId.
	 *
	 * @param dpmtId the dpmtRfcId
	 * @return the deployment records
	 */
	public CmsDpmtRecord getDeploymentRecord(long dpmtRecordId) {
		return dpmtMapper.getDeploymentRecord(dpmtRecordId);
	}
	
	/**
	 * Gets the deployment record cis.
	 *
	 * @param dpmtId the dpmt id
	 * @return the deployment record cis
	 */
	public List<CmsDpmtRecord> getDeploymentRecordCis(long dpmtId) {
		return dpmtMapper.getDeploymentRecordCis(dpmtId);
	}

	/**
	 * Gets the deployment record cis.
	 *
	 * @param ciId the ciId
	 * @param record state 
	 * @return the deployment record cis
	 */
	public List<CmsDpmtRecord> getDeploymentRecordByCiId(long ciId, String state) {
		return dpmtMapper.getDeploymentRecordsByCiId(ciId, state);
	}
	
	/**
	 * Gets the deployment record cis by state.
	 *
	 * @param dpmtId the dpmt id
	 * @return the deployment record cis
	 */
	public List<CmsDpmtRecord> getDeploymentRecordCisByState(long dpmtId, String state, Integer execOrder) {
		return dpmtMapper.getDeploymentRecordsByState(dpmtId, state, execOrder);
	}
	
	/**
	 * Gets the deployment record relations.
	 *
	 * @param dpmtId the dpmt id
	 * @return the deployment record relations
	 */
	public List<CmsDpmtRecord> getDeploymentRecordRelations(long dpmtId) {
		return dpmtMapper.getDeploymentRecordRelations(dpmtId);
	}

	public long getDeploymentRecordCount(long dpmtId, String state, Integer execOrder) {
		return dpmtMapper.getDeploymentRecordsCountByState(dpmtId, state, execOrder);
	}

	public List<CmsDpmtStateChangeEvent> getDeploymentStateHist(long deploymentId) {
		return dpmtMapper.getDeploymentStateHist(deploymentId);
	}
	
}
