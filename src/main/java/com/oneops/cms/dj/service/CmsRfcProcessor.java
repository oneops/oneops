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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcAction;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcBasicAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcLink;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsManager;
import com.oneops.cms.util.CIValidationResult;
import com.oneops.cms.util.CmsDJValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;

/**
 * The Class CmsRfcProcessor.
 */
public class CmsRfcProcessor {
	static Logger logger = Logger.getLogger(CmsRfcProcessor.class);

	private static final String RFCNAMEREGEX = "[a-zA-Z0-9\\-]*";
    private static Pattern rfcNamePattern = Pattern.compile(RFCNAMEREGEX);	
    private static final int CHUNK_SIZE = 100;
	private DJMapper djMapper;
	private CmsNsManager nsManager;
	private CmsDJValidator djValidator;
	private CIMapper ciMapper;
	private CmsRfcUtil rfcUtil;
	private Gson gson = new Gson();

	/**
	 * Sets the dj validator.
	 *
	 * @param djValidator the new dj validator
	 */
	public void setDjValidator(CmsDJValidator djValidator) {
		this.djValidator = djValidator;
	}
	
	/**
	 * Sets the dj mapper.
	 *
	 * @param djMapper the new dj mapper
	 */
	public void setDjMapper(DJMapper djMapper) {
		this.djMapper = djMapper;
	}

	/**
	 * Sets the ns manager.
	 *
	 * @param nsManager the new ns manager
	 */
	public void setNsManager(CmsNsManager nsManager) {
		this.nsManager = nsManager;
	}

	/**
	 * Sets the ci mapper.
	 *
	 * @param ciMapper the new ci mapper
	 */
	public void setCiMapper(CIMapper ciMapper) {
		this.ciMapper = ciMapper;
	}

	/**
	 * Sets the rfc util.
	 *
	 * @param rfcUtil the new rfc util
	 */
	public void setRfcUtil(CmsRfcUtil rfcUtil) {
		this.rfcUtil = rfcUtil;
	}
	
	/**
	 * Brush exec order.
	 *
	 * @param releaseId the release id
	 */
	public void brushExecOrder(long releaseId) {
		djMapper.brushReleaseExecOrder(releaseId);
	}
	
	/**
	 * Update release.
	 *
	 * @param release the release
	 * @return the cms release
	 */
	public CmsRelease updateRelease(CmsRelease release) {
		
		Integer stateId = djMapper.getReleaseStateId(release.getReleaseState());
		if (stateId == null) {
			String err = "Can not resolve release state " + release.getReleaseState();
			logger.error(err);
			throw new DJException(CmsError.DJ_CANT_RESOLVE_RELEASE_STATE_ERROR, err);
		}
		release.setReleaseStateId(stateId.intValue());
		
		long updated = djMapper.updateRelease(release);
		logger.info("Updated " + updated + " " + release.getReleaseId() + " release");
		return djMapper.getReleaseById(release.getReleaseId());
	}
	
	/**
	 * Commit release.
	 *
	 * @param releaseId the release id
	 * @param setDfValue the set df value
	 * @param newCiState the new ci state
	 * @param userId the user id
	 * @param desc the desc
	 */
	public void commitRelease(long releaseId,Boolean setDfValue, String newCiState, String userId, String desc) {
		commitRelease(releaseId, setDfValue, newCiState, true, userId, desc);
	}	
	
	/**
	 * Commit release.
	 *
	 * @param releaseId the release id
	 * @param setDfValue the set df value
	 * @param newCiState the new ci state
	 * @param delete4real the delete4real
	 * @param userId the user id
	 * @param desc the desc
	 */
	public void commitRelease(long releaseId,Boolean setDfValue, String newCiState, boolean delete4real, String userId, String desc) {
		Integer ciState = null;
		if (newCiState != null) {
			ciState = ciMapper.getCiStateId(newCiState);
			if (ciState == null) {
				String errorMsg = "Can not resolve new ci state " + newCiState;
				logger.error(errorMsg);
				throw new DJException(CmsError.DJ_CANT_RESOLVE_CI_STATE_ERROR, errorMsg);
			}
		}	
		//lets process flex relations and adjust current value (this is for DependsOn relations only
		List<CmsRfcRelation> relations = getRfcRelationByReleaseAndClass(releaseId, null, "DependsOn");
		for (CmsRfcRelation rel : relations) {
			if (!"delete".equalsIgnoreCase(rel.getRfcAction())) {
				//if it's update we need to get ci and merge to compute the current
				if ("update".equalsIgnoreCase(rel.getRfcAction())) {
					CmsCIRelation ciRel = getRelationById(rel.getCiRelationId());
					rel = rfcUtil.mergeRfcRelAndCiRel(rel, ciRel, "dj");
				}
				if (rel.getAttribute("flex") != null && "true".equalsIgnoreCase(rel.getAttribute("flex").getNewValue())) {
					setFlexCurrentOnRelation(rel);
				}
			}
		}
		djMapper.commitRelease(releaseId, setDfValue, ciState, delete4real, userId, desc);
	}

	private CmsCIRelation getRelationById(long relId) {
		CmsCIRelation rel = ciMapper.getCIRelation(relId);
		if (rel != null) {
			for (CmsCIRelationAttribute attr : ciMapper.getCIRelationAttrs(rel.getCiRelationId())) {
				rel.addAttribute(attr);
			}
		}
		return rel;
	}
	

	private void setFlexCurrentOnRelation(CmsRfcRelation rel) {
		
		int min = Integer.valueOf(rel.getAttribute("min").getNewValue());
		int max = Integer.valueOf(rel.getAttribute("max").getNewValue());
		int current = Integer.valueOf(rel.getAttribute("current").getNewValue());
		int newcurrent = (min>current) ? min : current;
		newcurrent = (max<newcurrent) ? max : newcurrent;
		if (newcurrent != current) {
			CmsRfcAttribute attr = rel.getAttribute("current");
			attr.setNewValue(String.valueOf(newcurrent));
			attr.setRfcId(rel.getRfcId());
			djMapper.upsertRfcRelationAttribute(rel.getAttribute("current"));
		}
	}
	
	private List<CmsRfcRelation> getRfcRelationByReleaseAndClass(long releaseId, String relationName, String shortRelName) {
			
		List<CmsRfcRelation> relList = djMapper.getRfcRelationByReleaseAndClass(releaseId, relationName, shortRelName);
		populateRfcRelationAttributes(relList);
		return relList;	
		
	}
	
	
	/**
	 * Creates the release.
	 *
	 * @param release the release
	 * @return the cms release
	 */
	public CmsRelease createRelease(CmsRelease release) {
		
		List<CmsRelease> existingReleases = getReleaseBy3(release.getNsPath(), null, "open");
		if (existingReleases.size() > 0) {
			return existingReleases.get(0);
		}
		
		long releaseId = djMapper.getNextDjId();
		release.setReleaseId(releaseId);

		CmsNamespace ns = nsManager.getNs(release.getNsPath());
		if (ns == null) {
			String err = "Can not resolve name space";
			logger.error(err);
			throw new DJException(CmsError.DJ_CANT_RESOLVE_NAMESPACE_ERROR, err);
		}
		release.setNsId(ns.getNsId());
		
		Integer stateId = djMapper.getReleaseStateId(release.getReleaseState());
		if (stateId == null) {
			String err = "Can not resolve release state " + release.getReleaseState();
			logger.error(err);
			throw new DJException(CmsError.DJ_CANT_RESOLVE_RELEASE_STATE_ERROR, err);
		}
		release.setReleaseStateId(stateId.intValue());
		
		if (release.getRevision() == 0) release.setRevision(1);
		
		release.setReleaseName(release.getNsPath() + String.valueOf(releaseId));
		
		djMapper.createRelease(release);
		
		return djMapper.getReleaseById(releaseId);
	}

	/**
	 * Gets the release by id.
	 *
	 * @param releaseId the release id
	 * @return the release by id
	 */
	public CmsRelease getReleaseById(long releaseId) {
		return djMapper.getReleaseById(releaseId);
	}

	/**
	 * Gets the release by3.
	 *
	 * @param nsPath the ns path
	 * @param releaseName the release name
	 * @param releaseState the release state
	 * @return the release by3
	 */
	public List<CmsRelease> getReleaseBy3(String nsPath, String releaseName,
			String releaseState) {
		return djMapper.getReleaseBy3(nsPath, releaseName, releaseState);
	}

	/**
	 * Gets the latest release.
	 *
	 * @param nsPath the ns path
	 * @param releaseState the release state
	 * @return the latest release
	 */
	public List<CmsRelease> getLatestRelease(String nsPath, String releaseState) {
		return djMapper.getLatestRelease(nsPath, releaseState);
	}

	/**
	 * Returns latest open release.
	 * or creates one
	 * @param nsPath the ns path
	 * @param releaseType the release type
	 * @return the latest release
	 */
	public long getOpenReleaseIdByNs(String nsPath, String releaseType, String createdBy) {
		List<CmsRelease> existingReleases = getReleaseBy3(nsPath, null, "open");
		if (existingReleases.size() > 0) {
			CmsRelease existingRelease = existingReleases.get(0);
			if ((releaseType == null && existingRelease.getReleaseType() == null)
				|| (releaseType != null && releaseType.equals(existingRelease.getReleaseType()))) {
				return existingRelease.getReleaseId();
			} else {
				String errorMsg = "There is an open release with different release type, release_id = " + existingRelease.getReleaseId();
				logger.error(errorMsg);
				throw new DJException(CmsError.DJ_OPEN_RELEASE_WRONG_TYPE_ERROR, errorMsg);
			}
		} else {
			if ("oneops::autodeploy".equals(releaseType)) {
				//lets check if we have open bom release
				String bomNsPath = nsPath.substring(0, nsPath.lastIndexOf("/")) + "/bom";
				List<CmsRelease> bomReleases = getReleaseBy3(bomNsPath, null, "open");
				if (bomReleases.size()>0) {
					String errorMsg = "There is an open bom release for nsPath: " + bomNsPath;
					logger.error(errorMsg);
					throw new DJException(CmsError.DJ_OPEN_RELEASE_WRONG_TYPE_ERROR, errorMsg);
				}
			}
			
			CmsRelease release = new CmsRelease();
			release.setCreatedBy(createdBy);
			release.setNsPath(nsPath);
			release.setReleaseState("open");
			release.setRevision(1);
			release.setReleaseType(releaseType);
			return createRelease(release).getReleaseId();
		}
	}
	
	
	/**
	 * Delete release.
	 *
	 * @param releaseId the release id
	 * @return the long
	 */
	public long deleteRelease(long releaseId) {
		return djMapper.deleteRelease(releaseId);
	}
	
	/**
	 * Creates the rfc ci.
	 *
	 * @param rfcCi the rfc ci
	 * @return the long
	 */
	public long createRfcCI(CmsRfcCI rfcCi) {
		return createRfcCI(rfcCi, null);
	}
	
	/**
	 * Creates the rfc ci.
	 *
	 * @param rfcCi the rfc ci
	 * @param userId the user id
	 * @return the long
	 */
	public long createRfcCI(CmsRfcCI rfcCi, String userId) {
		
		if (rfcCi.getRfcId() > 0) {
			return updateRfcCI(rfcCi);
		}
		
		if (rfcCi.getReleaseId() == 0) {
			if (userId == null) {
				String errMsg = "You must specify userId if releaseId is null" + rfcCi.getCiId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MUST_SPECIFY_USER_ID_ERROR, errMsg);
			}
			long releaseId = getOpenReleaseIdByNs(rfcCi.getReleaseNsPath(), rfcCi.getReleaseType(), userId);
			rfcCi.setReleaseId(releaseId);
		} else {

			CmsRelease release = djMapper.getReleaseById(rfcCi.getReleaseId());
			
			if (!("open".equalsIgnoreCase(release.getReleaseState()))) {
				String errorMsg = "Rfc release is not open!";
				logger.error(errorMsg);
				throw new DJException(CmsError.DJ_RFC_RELEASE_NOT_OPEN_ERROR, errorMsg);
			}

		}
		
		
		if (rfcCi.getCiId() > 0) {
			List<CmsRfcCI> existingRfcs = getRfcCIBy3(rfcCi.getReleaseId(), true, rfcCi.getCiId()); 
			if (existingRfcs.size()>0) {
				CmsRfcCI existingRfc = existingRfcs.get(0);
				rfcCi.setRfcId(existingRfc.getRfcId());
				return updateRfcCIsimple(rfcCi,existingRfc);
			}	

			CmsCI existingCi = ciMapper.getCIById(rfcCi.getCiId()); 
			if (existingCi == null) {
				String errMsg = "There is no CI with ci_id = " + rfcCi.getCiId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_NO_CI_WITH_GIVEN_ID_ERROR, errMsg);
			}
			rfcCi.setCiClassName(existingCi.getCiClassName());
			rfcCi.setNsPath(existingCi.getNsPath());
		
		}	

		return createRfcCIsimple(rfcCi);
	}
	
	/**
	 * Creates the rfc ci no check.
	 *
	 * @param rfcCi the rfc ci
	 * @param userId the user id
	 * @return the long
	 */
	public long createRfcCINoCheck(CmsRfcCI rfcCi, String userId) {
		
		if (rfcCi.getRfcId() > 0) {
			return updateRfcCI(rfcCi);
		}
		
		if (rfcCi.getReleaseId() == 0) {
			if (userId == null) {
				String errMsg = "You must specify userId if releaseId is null" + rfcCi.getCiId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MUST_SPECIFY_USER_ID_ERROR, errMsg);
			}
			long releaseId = getOpenReleaseIdByNs(rfcCi.getReleaseNsPath(), rfcCi.getReleaseType(), userId);
			rfcCi.setCreatedBy(userId);
			rfcCi.setReleaseId(releaseId);
		}		

		return createRfcCIsimple(rfcCi);
	}
	
	public CmsRfcCI createAndfetchRfcCINoCheck(CmsRfcCI rfcCi, String userId) {

		if (rfcCi.getReleaseId() == 0) {
			if (userId == null) {
				String errMsg = "You must specify userId if releaseId is null" + rfcCi.getCiId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MUST_SPECIFY_USER_ID_ERROR, errMsg);
			}
			long releaseId = getOpenReleaseIdByNs(rfcCi.getReleaseNsPath(), rfcCi.getReleaseType(), userId);
			rfcCi.setCreatedBy(userId);
			rfcCi.setReleaseId(releaseId);
		}

		return buildRfcCIsimple(rfcCi);
	 }

	
	/**
	 * Creates the rfc c isimple.
	 *
	 * @param rfcCi the rfc ci
	 * @return the long
	 */
	public long createRfcCIsimple(CmsRfcCI rfcCi) {
		
		return buildRfcCIsimple(rfcCi).getRfcId();
	}
	
	/**
	 * 
	 * @param rfcCi
	 * @return
	 */
	private CmsRfcCI buildRfcCIsimple(CmsRfcCI rfcCi) {
		Matcher m = rfcNamePattern.matcher(rfcCi.getCiName());
		if (!m.matches()) 
			logger.error("The name format is invalid " + rfcCi.getCiName());
		CIValidationResult validation = djValidator.validateRfcCi(rfcCi);
		
		if (!validation.isValidated()) {
			logger.error(validation.getErrorMsg());
			throw new DJException(CmsError.DJ_VALIDATION_ERROR, validation.getErrorMsg());
		}

		if (rfcCi.getCiId() == 0) {
			rfcCi.setCiId(djMapper.getNextCiId());
		}	
		
		rfcCi.setRfcId(djMapper.getNextDjId());

		if (rfcCi.getCiGoid() == null) {
			rfcCi.setCiGoid(String.valueOf(rfcCi.getNsId()) + '-' + String.valueOf(rfcCi.getCiClassId()) + '-' + String.valueOf(rfcCi.getCiId()));
		}

		int rfcActionId = CmsRfcAction.valueOf(rfcCi.getRfcAction()).getId();
		rfcCi.setRfcActionId(rfcActionId);
		
		djMapper.createRfcCI(rfcCi);
		
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			if (attr.getNewValue() == null) {
				attr.setNewValue("");
			}	
			attr.setRfcId(rfcCi.getRfcId());
			djMapper.upsertRfcCIAttribute(attr);
			
		}
		return rfcCi;
	}

	/**
	 * Creates the Bom rfc plain no verification.
	 *
	 * @param rfcCi the rfc ci
	 * @return the long
	 */
	public long createBomRfc(CmsRfcCI rfcCi) {

		if (rfcCi.getCiId() == 0) {
			rfcCi.setCiId(djMapper.getNextCiId());
		}	
		
		rfcCi.setRfcId(djMapper.getNextDjId());

		if (rfcCi.getCiGoid() == null) {
			rfcCi.setCiGoid(String.valueOf(rfcCi.getNsId()) + '-' + String.valueOf(rfcCi.getCiClassId()) + '-' + String.valueOf(rfcCi.getCiId()));
		}

		int rfcActionId = CmsRfcAction.valueOf(rfcCi.getRfcAction()).getId();
		rfcCi.setRfcActionId(rfcActionId);
		
		djMapper.createRfcCI(rfcCi);
		
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			if (attr.getNewValue() == null) {
				attr.setNewValue("");
			}	
			attr.setRfcId(rfcCi.getRfcId());
			djMapper.upsertRfcCIAttribute(attr);
		}
		return rfcCi.getRfcId();
	}
	
	public long createBomRfcRelation(CmsRfcRelation rel) {
		
		//assumption here that client may already validate this relation so no need for double work
		if (rel.getRelationGoid() == null) {
			rel.setRelationGoid(String.valueOf(rel.getFromCiId()) + '-' + String.valueOf(rel.getRelationId()) + '-' +String.valueOf(rel.getToCiId()));
		}
		
		if (rel.getCiRelationId() == 0) {
			rel.setCiRelationId(djMapper.getNextCiId());
		}

		int rfcActionId = CmsRfcAction.valueOf(rel.getRfcAction()).getId();
		rel.setRfcActionId(rfcActionId);

		rel.setRfcId(djMapper.getNextDjId());
		djMapper.createRfcRelation(rel);
		
		for (CmsRfcAttribute attr : rel.getAttributes().values()){
			attr.setRfcId(rel.getRfcId());
			djMapper.upsertRfcRelationAttribute(attr);
		}
		return rel.getRfcId();
	}
	
	
	/**
	 * Update the Bom rfc no verification.
	 *
	 * @param rfcCi the rfc ci
	 * @return the long
	 */	
	
	public long updateBomRfc(CmsRfcCI rfcCi, CmsRfcCI existingRfcCi) {
		
		djMapper.updateRfcCI(rfcCi);
		
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			CmsRfcAttribute existingAttr = existingRfcCi.getAttribute(attr.getAttributeName());
			if(!(djValidator.rfcAttrsEqual(attr, existingAttr))) {
				attr.setRfcId(rfcCi.getRfcId());
				djMapper.upsertRfcCIAttribute(attr);
			}	
		}
		return rfcCi.getRfcId();
	}

	public long updateBomRfcRelation(CmsRfcRelation relation, CmsRfcRelation existingRelation) {
		
		djMapper.updateRfcRelation(relation);
		
		for (CmsRfcAttribute attr : relation.getAttributes().values()){
			CmsRfcAttribute existingAttr = existingRelation.getAttribute(attr.getAttributeName());
			if(!(djValidator.rfcAttrsEqual(attr, existingAttr))) {
				attr.setRfcId(relation.getRfcId());
				djMapper.upsertRfcRelationAttribute(attr);
			}	
		}
		return relation.getRfcId();
	}
	
	
	/**
	 * Update rfc ci.
	 *
	 * @param rfcCi the rfc ci
	 * @return the long
	 */
	public long updateRfcCI(CmsRfcCI rfcCi) {
		return updateRfcCIsimple(rfcCi, null);
	}
	
	/**
	 * Update rfc exec order.
	 *
	 * @param rfcCi the rfc ci
	 */
	public void updateRfcExecOrder(CmsRfcCI rfcCi) {
		djMapper.updateRfcCI(rfcCi);
	}
	
	private long updateRfcCIsimple(CmsRfcCI rfcCi, CmsRfcCI existingRfcCi) {
		//TODO validate rfcCi
		existingRfcCi = (existingRfcCi != null) ? existingRfcCi : getRfcCIById(rfcCi.getRfcId());

		if (existingRfcCi == null) {
			String errorMsg = "Rfc does not exists " + rfcCi.getRfcId();
			logger.error(errorMsg);
			throw new DJException(CmsError.DJ_RFC_DOESNT_EXIST_ERROR, errorMsg);
		}
		
		CmsRelease release = djMapper.getReleaseById(existingRfcCi.getReleaseId());
		
		if (!("open".equalsIgnoreCase(release.getReleaseState()))) {
			String errorMsg = "Rfc release is not open!";
			logger.error(errorMsg);
			throw new DJException(CmsError.DJ_RFC_RELEASE_NOT_OPEN_ERROR, errorMsg);
		}

		rfcCi.setCiClassName(existingRfcCi.getCiClassName());
		
		CIValidationResult validation = djValidator.validateRfcCiAttrs(rfcCi);
		
		if (!validation.isValidated()) {
			logger.error(validation.getErrorMsg());
			throw new DJException(CmsError.DJ_VALIDATION_ERROR, validation.getErrorMsg());
		}
		
		djMapper.updateRfcCI(rfcCi);
		
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			CmsRfcAttribute existingAttr = existingRfcCi.getAttribute(attr.getAttributeName());
			if(!(djValidator.rfcAttrsEqual(attr, existingAttr))) {
				attr.setRfcId(rfcCi.getRfcId());
				djMapper.upsertRfcCIAttribute(attr);
			}	
		}
		return rfcCi.getRfcId();
	}
	
	
	/**
	 * Rm rfc ci from release.
	 *
	 * @param rfcId the rfc id
	 * @return the long
	 */
	public long rmRfcCiFromRelease(long rfcId) {
		CmsRfcCI rfcCi = djMapper.getRfcCIById(rfcId);
		if (rfcCi != null) {
			List<Long> relRfcIds = djMapper.getLinkedRfcRelationId(rfcCi.getReleaseId(), true, rfcId);
			for (Long relRfcId : relRfcIds) {
				rmRfcRelationFromRelease(relRfcId.longValue());
			}
			return djMapper.rmRfcCIfromRelease(rfcId);
		} else {
			return 0;
		}
	}

	
	/**
	 * Gets the rfc ci by id.
	 *
	 * @param rfcId the rfc id
	 * @return the rfc ci by id
	 */
	public CmsRfcCI getRfcCIById(long rfcId) {
		CmsRfcCI rfcCi = djMapper.getRfcCIById(rfcId);
        populateRfcCIAttributes(rfcCi);
		return rfcCi;
	}

	/**
	 * Gets the rfc ci by id.
	 *
	 * @param rfcId the rfc id
	 * @return the rfc ci by id
	 */
	public CmsRfcCI getRfcCIByIdNoAttrs(long rfcId) {
		CmsRfcCI rfcCi = djMapper.getRfcCIById(rfcId);
		return rfcCi;
	}
	

	/**
	 * Gets the open rfc ci by ci id.
	 *
	 * @param ciId the ci id
	 * @return the open rfc ci by ci id
	 */
	public CmsRfcCI getOpenRfcCIByCiId(long ciId) {
		CmsRfcCI rfcCi = djMapper.getOpenRfcCIByCiId(ciId);
        populateRfcCIAttributes(rfcCi);
        return rfcCi;
	}
	
	/**
	 * Gets the open rfcs by ci id list.
	 * no attributes
	 * @param ids - List of the ci id
	 * @return the open rfc ci by ci id
	 */
	public List<CmsRfcCI> getOpenRfcCIByCiIdListNoAttrs(List<Long> ids) {
		List<CmsRfcCI> rfcs = new ArrayList<CmsRfcCI>();
		if (ids == null || ids.size() == 0) {
			return rfcs;
		}
		int fromIndex = 0;
		int toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
		List<Long> subList = ids.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			rfcs.addAll(djMapper.getOpenRfcCIByCiIdList(subList));
			fromIndex += CHUNK_SIZE;
			toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
			subList = ids.subList(fromIndex, toIndex);
		}
		if (subList.size() >0) {
			rfcs.addAll(djMapper.getOpenRfcCIByCiIdList(subList));
		}
		return rfcs;
	}

	/**
	 * Gets the open rfcs by ci id list.
	 * @param ids - List of the ci id
	 * @return the open rfc ci by ci id
	 */
	public List<CmsRfcCI> getOpenRfcCIByCiIdList(List<Long> ids) {
		List<CmsRfcCI> rfcs = new ArrayList<CmsRfcCI>();
		if (ids == null || ids.size() == 0) {
			return rfcs;
		}
		int fromIndex = 0;
		int toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
		List<Long> subList = ids.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			List<CmsRfcCI> chunk = djMapper.getOpenRfcCIByCiIdList(subList);
			populateRfcCIAttributesSimple(chunk);
			rfcs.addAll(chunk);
			fromIndex += CHUNK_SIZE;
			toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
			subList = ids.subList(fromIndex, toIndex);
		}
		if (subList.size() >0) {
			List<CmsRfcCI> chunk = djMapper.getOpenRfcCIByCiIdList(subList);
			populateRfcCIAttributesSimple(chunk);
			rfcs.addAll(chunk);
		}
		return rfcs;
	}
	
	/**
	 * Gets the open rfcs by ci id list.
	 * @param ids - List of the ci id
	 * @return the open rfc ci by ci id
	 */
	/*
	public List<CmsRfcCI> getOpenRfcCIByCiIdList(List<Long> ids) {
		List<CmsRfcCI> rfcs = getOpenRfcCIByCiIdListNoAttrs(ids);
		
		for (CmsRfcCI rfcCi : rfcs) {
			populateRfcCIAttributes(rfcCi);
		}
        return rfcs;
	}
	*/
	
	/*
	public List<CmsCI> getCiByIdListNaked(List<Long> ids) {
		List<CmsCI> cis = new ArrayList<CmsCI>();
		if (ids == null || ids.size() == 0) {
			return cis;
		}
		int fromIndex = 0;
		int toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
		List<Long> subList = ids.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			cis.addAll(ciMapper.getCIByIdList(subList));
			fromIndex += CHUNK_SIZE;
			toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
			subList = ids.subList(fromIndex, toIndex);
		}
		if (subList.size()>0) {
			cis.addAll(ciMapper.getCIByIdList(subList));
		}
		return cis;
	}
	*/
	
	
	/**
	 * Gets the rfc ci by3.
	 *
	 * @param releaseId the release id
	 * @param isActive the is active
	 * @param ciId the ci id
	 * @return the rfc ci by3
	 */
	public List<CmsRfcCI> getRfcCIBy3(long releaseId, Boolean isActive, Long ciId) {
		List<CmsRfcCI> rfcList = djMapper.getRfcCIBy3(releaseId, isActive, ciId);
		populateRfcCIAttributes(rfcList);
		return rfcList;
	}

	/**
	 * Gets the open rfc ci by clazz and name.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the open rfc ci by clazz and name
	 */
	public List<CmsRfcCI> getOpenRfcCIByClazzAndName(String nsPath, String clazzName, String ciName) {
		List<CmsRfcCI> rfcList = djMapper.getRfcCIByClazzAndName(nsPath, clazzName, ciName,  true, "open");
		populateRfcCIAttributes(rfcList);
		return rfcList;
	}

	/**
	 * Gets the open rfc ci by clazz and name lower no attr.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the open rfc ci by clazz and name lower no attr
	 */
	public List<CmsRfcCI> getOpenRfcCIByClazzAndNameLowerNoAttr(String nsPath, String clazzName, String ciName) {
		List<CmsRfcCI> rfcList = djMapper.getOpenRfcCIByClazzAndNameLower(nsPath, clazzName, ciName);
		return rfcList;
	}
	
	/**
	 * Gets the open rfc ci by clazz and name lower.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the open rfc ci by clazz and name lower
	 */
	public List<CmsRfcCI> getOpenRfcCIByClazzAndNameLower(String nsPath, String clazzName, String ciName) {
		List<CmsRfcCI> rfcList = djMapper.getOpenRfcCIByClazzAndNameLower(nsPath, clazzName, ciName);
		populateRfcCIAttributes(rfcList);
		return rfcList;
	}
	
	/**
	 * Gets the open rfc ci by ns like.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the open rfc ci by ns like
	 */
	public List<CmsRfcCI> getOpenRfcCIByNsLike(String nsPath, String clazzName, String ciName) {
		String nsLike = CmsUtil.likefyNsPath(nsPath);
		List<CmsRfcCI> rfcList = djMapper.getOpenRfcCIByNsLike(nsPath, nsLike, clazzName, ciName);
		populateRfcCIAttributes(rfcList);
		return rfcList;
	}
	
	
	/**
	 * Gets the open rfc ci by clazz and name no attrs.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the open rfc ci by clazz and name no attrs
	 */
	public List<CmsRfcCI> getOpenRfcCIByClazzAndNameNoAttrs(String nsPath, String clazzName, String ciName) {
		return djMapper.getRfcCIByClazzAndName(nsPath, clazzName, ciName,  true, "open");
	}
	
	/**
	 * Gets the open rfc ci by clazz and2 names no attrs.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param altCiName the alt ci name
	 * @return the open rfc ci by clazz and2 names no attrs
	 */
	public List<CmsRfcCI> getOpenRfcCIByClazzAnd2NamesNoAttrs(String nsPath, String clazzName, String ciName, String altCiName) {
		return djMapper.getOpenRfcCIByClazzAnd2Names(nsPath, clazzName, ciName, altCiName);
	}

	/**
	 * Gets the open rfc ci by clazz and2 names.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param altCiName the alt ci name
	 * @return the open rfc ci by clazz and2 names
	 */
	public List<CmsRfcCI> getOpenRfcCIByClazzAnd2Names(String nsPath, String clazzName, String ciName, String altCiName) {
		List<CmsRfcCI> rfcList = djMapper.getOpenRfcCIByClazzAnd2Names(nsPath, clazzName, ciName, altCiName);
		populateRfcCIAttributes(rfcList);
		return rfcList;
	}

    /**
     * Gets the active (default if isActive missing) or inactive rfc ci by ns path.
     * @param nsPath the ns path
     * @param isActive    is active              
     */               
    public List<CmsRfcCI> getRfcCIByNs(String nsPath, Boolean isActive) {
        return djMapper.getRfcCIByClazzAndName(nsPath, null, null, isActive, null);
    }

	
	/**
	 * Creates the rfc relation.
	 *
	 * @param rel the rel
	 * @return the long
	 */
	public long createRfcRelation(CmsRfcRelation rel) {
		return createRfcRelation(rel, null);
	}
	
	/**
	 * Creates the rfc relation.
	 *
	 * @param rel the rel
	 * @param userId the user id
	 * @return the long
	 */
	public long createRfcRelation(CmsRfcRelation rel, String userId) {
		
		rel.setCreatedBy(userId);
		rel.setUpdatedBy(userId);

		if (rel.getRfcId() > 0) {
			return updateRfcRelation(rel);
		}
		
		if (rel.getReleaseId() == 0) {
			if (userId == null) {
				String errMsg = "You must specify userId if releaseId is null" + rel.getCiRelationId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MUST_SPECIFY_USER_ID_ERROR, errMsg);
			}
			long releaseId = getOpenReleaseIdByNs(rel.getReleaseNsPath(), rel.getReleaseType(), userId);
			rel.setReleaseId(releaseId);
		} else {

			CmsRelease release = djMapper.getReleaseById(rel.getReleaseId());
			if (!("open".equalsIgnoreCase(release.getReleaseState()))) {
				String errorMsg = "Rfc release is not open!";
				logger.error(errorMsg);
				throw new DJException(CmsError.DJ_RFC_RELEASE_NOT_OPEN_ERROR, errorMsg);
			}

		}
		
		
		if (rel.getCiRelationId() > 0) {
			
			List<CmsRfcRelation> existingRfcRels = getRfcRelationBy3(rel.getReleaseId(), true, rel.getCiRelationId()); 
			if (existingRfcRels.size()>0) {
				CmsRfcRelation existingRel = existingRfcRels.get(0);
				rel.setRfcId(existingRel.getRfcId());
				return updateRfcRelationSimple(rel,existingRel);
			}	

			CmsCIRelation existingRel = ciMapper.getCIRelation(rel.getCiRelationId());
			if (existingRel == null) {
				String errMsg = "There is no Relation with ci_relation_id = " + rel.getCiRelationId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_NO_RELATION_WITH_GIVEN_ID_ERROR, errMsg);
			}
			rel.setRelationName(existingRel.getRelationName());
		} 	

		return createRfcRelationSimple(rel);
	}	

	/**
	 * Creates the rfc relation no check.
	 *
	 * @param rel the rel
	 * @param userId the user id
	 * @return the long
	 */
	public long createRfcRelationNoCheck(CmsRfcRelation rel, String userId) {
		
		rel.setCreatedBy(userId);
		rel.setUpdatedBy(userId);

		if (rel.getRfcId() > 0) {
			return updateRfcRelation(rel);
		}
		
		if (rel.getReleaseId() == 0) {
			if (userId == null) {
				String errMsg = "You must specify userId if releaseId is null" + rel.getCiRelationId();
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MUST_SPECIFY_USER_ID_ERROR, errMsg);
			}
			long releaseId = getOpenReleaseIdByNs(rel.getReleaseNsPath(), rel.getReleaseType(), userId);
			rel.setReleaseId(releaseId);
		}		

		return createRfcRelationSimple(rel);
	}	
	
	private long createRfcRelationSimple(CmsRfcRelation rel) {
		
		//assumption here that client may already validate this relation so no need for double work
		if (!rel.isValidated()) {
			CmsCI fromCi = checkRelationFromCI(rel);
			CmsCI toCi = checkRelationToCI(rel);
			
			CIValidationResult validation = djValidator.validateRfcRelation(rel, fromCi.getCiClassId(), toCi.getCiClassId());
			
			if (!validation.isValidated()) {
				logger.error(validation.getErrorMsg());
				throw new DJException(CmsError.DJ_VALIDATION_ERROR, validation.getErrorMsg());
			}
			
			rel.setComments(generateComments(fromCi, toCi));

		}
		if (rel.getRelationGoid() == null) {
			rel.setRelationGoid(String.valueOf(rel.getFromCiId()) + '-' + String.valueOf(rel.getRelationId()) + '-' +String.valueOf(rel.getToCiId()));
		}
		
		if (rel.getCiRelationId() == 0) {
			rel.setCiRelationId(djMapper.getNextCiId());
		}

		int rfcActionId = CmsRfcAction.valueOf(rel.getRfcAction()).getId();
		rel.setRfcActionId(rfcActionId);

		rel.setRfcId(djMapper.getNextDjId());
		djMapper.createRfcRelation(rel);
		
		for (CmsRfcAttribute attr : rel.getAttributes().values()){
			attr.setRfcId(rel.getRfcId());
			djMapper.upsertRfcRelationAttribute(attr);
		}
		return rel.getRfcId();
	}

	private String generateComments(CmsCI fromCi, CmsCI toCi) {
		Map<String, String> strMap = new HashMap<String, String>();
		strMap.put("fromCiName", fromCi.getCiName());
		strMap.put("fromCiClass", fromCi.getCiClassName());
		strMap.put("toCiName", toCi.getCiName());
		strMap.put("toCiClass", toCi.getCiClassName());
		return gson.toJson(strMap);
	}
	
	
	private CmsCI checkRelationFromCI(CmsRfcRelation rel) {

		CmsCI fromCi = ciMapper.getCIById(rel.getFromCiId());
		if (fromCi == null) {
			List<CmsRfcCI> rfcList = getRfcCIBy3(rel.getReleaseId(), true, rel.getFromCiId()); 
			if (rfcList.size()==0) {
				String errMsg = "there is no CI in the system with ci_id = " + rel.getFromCiId(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_NO_CI_WITH_GIVEN_ID_ERROR, errMsg);
			} else if (rfcList.size()>1) {
				String errMsg = "there is more then one rfc in thix release with ci_id = " + rel.getFromCiId(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MORE_THEN_ONE_RFC_IN_RELEASE_ERROR, errMsg);
			} else {
				CmsRfcCI rfcCi = rfcList.get(0);
				rel.setFromRfcId(rfcCi.getRfcId());
				fromCi = new CmsCI();
				fromCi.setCiClassId(rfcCi.getCiClassId());
				fromCi.setCiClassName(rfcCi.getCiClassName());
				fromCi.setCiName(rfcCi.getCiName());
				
			}
		} 
		return fromCi;
	}
	
	private CmsCI checkRelationToCI(CmsRfcRelation rel) {
		
		CmsCI toCi = ciMapper.getCIById(rel.getToCiId());
		if (toCi == null) {
			List<CmsRfcCI> rfcList = getRfcCIBy3(rel.getReleaseId(), true, rel.getToCiId()); 
			if (rfcList.size()==0) {
				String errMsg = "there is no CI in the system with ci_id = " + rel.getToCiId(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_NO_CI_WITH_GIVEN_ID_ERROR, errMsg);
			} else if (rfcList.size()>1) {
				String errMsg = "there is more then one rfc in thix release with ci_id = " + rel.getToCiId(); 
				logger.error(errMsg);
				throw new DJException(CmsError.DJ_MORE_THEN_ONE_RFC_IN_RELEASE_ERROR, errMsg);
			} else {
				CmsRfcCI rfcCi = rfcList.get(0);
				rel.setToRfcId(rfcCi.getRfcId());
				toCi = new CmsCI();
				toCi.setCiClassId(rfcCi.getCiClassId());
				toCi.setCiClassName(rfcCi.getCiClassName());
				toCi.setCiName(rfcCi.getCiName());
			}
		}
		return toCi;
	}
	
	
	/**
	 * Rm rfc relation from release.
	 *
	 * @param rfcId the rfc id
	 * @return the long
	 */
	public long rmRfcRelationFromRelease(long rfcId) {
		return djMapper.rmRfcRelationfromRelease(rfcId);
	}
	
	/**
	 * Gets the rfc relation by id.
	 *
	 * @param rfcId the rfc id
	 * @return the rfc relation by id
	 */
	public CmsRfcRelation getRfcRelationById(long rfcId) {
		
		CmsRfcRelation rfcRel = djMapper.getRfcRelationById(rfcId);
        populateRfcRelationAttributes(rfcRel);
        return rfcRel;
	}

	/**
	 * Gets the open rfc relation by ci rel id.
	 *
	 * @param ciRelationId the ci relation id
	 * @return the open rfc relation by ci rel id
	 */
	public CmsRfcRelation getOpenRfcRelationByCiRelId(long ciRelationId) {
		
		CmsRfcRelation rfcRel = djMapper.getOpenRfcRelationByCiRelId(ciRelationId);
        populateRfcRelationAttributes(rfcRel);
        return rfcRel;
	}
	
	
	/**
	 * Gets the rfc relation by release id.
	 *
	 * @param releaseId the release id
	 * @return the rfc relation by release id
	 */
	public List<CmsRfcRelation> getRfcRelationByReleaseId(long releaseId) {
		List<CmsRfcRelation> relList = djMapper.getRfcRelationByReleaseId(releaseId);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the rfc relation by4.
	 *
	 * @param releaseId the release id
	 * @param isActive the is active
	 * @param fromCiId the from ci id
	 * @param toCiId the to ci id
	 * @return the rfc relation by4
	 */
	public List<CmsRfcRelation> getRfcRelationBy4(long releaseId,
			Boolean isActive, Long fromCiId, Long toCiId) {
		List<CmsRfcRelation> relList = djMapper.getRfcRelationBy4(releaseId, isActive, fromCiId, toCiId);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open rfc relation by2.
	 *
	 * @param fromCiId the from ci id
	 * @param toCiId the to ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @return the open rfc relation by2
	 */
	public List<CmsRfcRelation> getOpenRfcRelationBy2(Long fromCiId, Long toCiId, String relName, String shortRelName) {
		List<CmsRfcRelation> relList = djMapper.getOpenRfcRelationBy2(fromCiId, toCiId, relName, shortRelName);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open from rfc relation by target clazz.
	 *
	 * @param fromCiId the from ci id
	 * @param relName the rel name
	 * @param targetClassName the target class name
	 * @return the open from rfc relation by target clazz
	 */
	public List<CmsRfcRelation> getOpenFromRfcRelationByTargetClazz(Long fromCiId, String relName, String targetClassName) {
		return getOpenFromRfcRelationByTargetClazz( fromCiId, relName, null,targetClassName);
	}

	/**
	 * Gets the open from rfc relation by target clazz.
	 *
	 * @param fromCiId the from ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @param targetClassName the target class name
	 * @return the open from rfc relation by target clazz
	 */
	public List<CmsRfcRelation> getOpenFromRfcRelationByTargetClazz(Long fromCiId, String relName, String shortRelName, String targetClassName) {
		List<CmsRfcRelation> relList = djMapper.getOpenFromRfcRelationByTargetClass(fromCiId, relName, shortRelName, targetClassName);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open from rfc relation by attrs.
	 *
	 * @param fromCiId the from ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @param targetClassName the target class name
	 * @param attrs the attrs
	 * @return the open from rfc relation by attrs
	 */
	public List<CmsRfcRelation> getOpenFromRfcRelationByAttrs
		(Long fromCiId, String relName, String shortRelName, String targetClassName, Map<String,String> attrs) {
		
		List<CmsRfcBasicAttribute> attrList = new ArrayList<CmsRfcBasicAttribute>();
		
		for (String attrName : attrs.keySet()) {
			CmsRfcBasicAttribute attr = new CmsRfcBasicAttribute();
			attr.setAttributeName(attrName);
			attr.setNewValue(attrs.get(attrName));
			attrList.add(attr);
		}
		
		List<CmsRfcRelation> relList = djMapper.getOpenFromRfcRelationByAttrs(fromCiId, relName, shortRelName, targetClassName, attrList);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open to rfc relation by attrs.
	 *
	 * @param toCiId the to ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @param targetClassName the target class name
	 * @param attrs the attrs
	 * @return the open to rfc relation by attrs
	 */
	public List<CmsRfcRelation> getOpenToRfcRelationByAttrs
	(Long toCiId, String relName, String shortRelName, String targetClassName, Map<String,String> attrs) {
	
	List<CmsRfcBasicAttribute> attrList = new ArrayList<CmsRfcBasicAttribute>();
	
	for (String attrName : attrs.keySet()) {
		CmsRfcBasicAttribute attr = new CmsRfcBasicAttribute();
		attr.setAttributeName(attrName);
		attr.setNewValue(attrs.get(attrName));
		attrList.add(attr);
	}
	
	List<CmsRfcRelation> relList = djMapper.getOpenToRfcRelationByAttrs(toCiId, relName, shortRelName, targetClassName, attrList);
	populateRfcRelationAttributes(relList);
	return relList;
}
	
	
	/**
	 * Gets the open from rfc relation by target clazz no attrs.
	 *
	 * @param fromCiId the from ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @param targetClassName the target class name
	 * @return the open from rfc relation by target clazz no attrs
	 */
	public List<CmsRfcRelation> getOpenFromRfcRelationByTargetClazzNoAttrs(Long fromCiId, String relName, String shortRelName, String targetClassName) {
		return djMapper.getOpenFromRfcRelationByTargetClass(fromCiId, relName, shortRelName, targetClassName);
	}

	
	/**
	 * Gets the open to rfc relation by target clazz.
	 *
	 * @param toCiId the to ci id
	 * @param relName the rel name
	 * @param targetClassName the target class name
	 * @return the open to rfc relation by target clazz
	 */
	public List<CmsRfcRelation> getOpenToRfcRelationByTargetClazz(Long toCiId, String relName, String targetClassName) {
		return getOpenToRfcRelationByTargetClazz( toCiId, relName, null, targetClassName); 
	}
	
	/**
	 * Gets the open to rfc relation by target clazz.
	 *
	 * @param toCiId the to ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @param targetClassName the target class name
	 * @return the open to rfc relation by target clazz
	 */
	public List<CmsRfcRelation> getOpenToRfcRelationByTargetClazz(Long toCiId, String relName, String shortRelName, String targetClassName) {
		List<CmsRfcRelation> relList = djMapper.getOpenToRfcRelationByTargetClass(toCiId, relName, shortRelName,targetClassName);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open to rfc relation by target clazz no attrs.
	 *
	 * @param toCiId the to ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @param targetClassName the target class name
	 * @return the open to rfc relation by target clazz no attrs
	 */
	public List<CmsRfcRelation> getOpenToRfcRelationByTargetClazzNoAttrs(Long toCiId, String relName, String shortRelName, String targetClassName) {
		return djMapper.getOpenToRfcRelationByTargetClass(toCiId, relName, shortRelName,targetClassName);
	}
	
	
	/**
	 * Gets the open rfc relation by2 no attrs.
	 *
	 * @param fromCiId the from ci id
	 * @param toCiId the to ci id
	 * @param relName the rel name
	 * @param shortRelName the short rel name
	 * @return the open rfc relation by2 no attrs
	 */
	public List<CmsRfcRelation> getOpenRfcRelationBy2NoAttrs(Long fromCiId, Long toCiId, String relName, String shortRelName) {
		return djMapper.getOpenRfcRelationBy2(fromCiId, toCiId, relName, shortRelName);
	}

	
	/**
	 * Gets the rfc relation by3.
	 *
	 * @param releaseId the release id
	 * @param isActive the is active
	 * @param ciRelationId the ci relation id
	 * @return the rfc relation by3
	 */
	public List<CmsRfcRelation> getRfcRelationBy3(long releaseId,
			Boolean isActive, Long ciRelationId) {
		List<CmsRfcRelation> relList = djMapper.getRfcRelationBy3(releaseId, isActive, ciRelationId);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open rfc relations naked.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the open rfc relations naked
	 */
	public List<CmsRfcRelation> getOpenRfcRelationsNaked(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName) {

		List<CmsRfcRelation> relList = getOpenRfcRelationsNakedNoAttrs(relationName, shortRelName, nsPath,fromClazzName, toClazzName);
		populateRfcRelationAttributes(relList);
		return relList;
	}	

	/**
	 * Gets the open rfc relations ns like naked.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the open rfc relations ns like naked
	 */
	public List<CmsRfcRelation> getOpenRfcRelationsNsLikeNaked(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName) {

		List<CmsRfcRelation> relList = getOpenRfcRelationsNsLikeNakedNoAttrs(relationName, shortRelName, nsPath,fromClazzName, toClazzName);
		populateRfcRelationAttributes(relList);
		return relList;
	}

    /**
     * Gets the open rfc relations by ns
     *
     * @param nsPath the ns path
     * @return the open rfc relations ns like naked
     */
    public List<CmsRfcRelation> getOpenRfcRelationsByNs(String nsPath) {
        return getRfcRelationsByNs(nsPath, true, "open");
    }


    /**
	 * Gets the rfc relations by ns
	 *
	 * @param nsPath the ns path
     * @param isActive is_active_in_release
     * @param state                
	 * @return the rfc relations with matching nsPath 
	 */
	public List<CmsRfcRelation> getRfcRelationsByNs(String nsPath, Boolean isActive, String state) {
		List<CmsRfcRelation> relList = djMapper.getRfcRelationsByNs(nsPath, isActive, state);
		populateRfcRelationAttributes(relList);
		return relList;
	}

	/**
	 * Gets the open rfc relations naked no attrs.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the open rfc relations naked no attrs
	 */
	public List<CmsRfcRelation> getOpenRfcRelationsNakedNoAttrs(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName) {
		return djMapper.getOpenRfcRelations(relationName, shortRelName, nsPath, fromClazzName, toClazzName);
	}
	
	/**
	 * Gets the open rfc relations ns like naked no attrs.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the open rfc relations ns like naked no attrs
	 */
	public List<CmsRfcRelation> getOpenRfcRelationsNsLikeNakedNoAttrs(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName) {
		String nsLike = CmsUtil.likefyNsPath(nsPath);
		return djMapper.getOpenRfcRelationsNsLike(relationName, shortRelName, nsPath, nsLike, fromClazzName, toClazzName);
	}
	
	/**
	 * Update rfc relation.
	 *
	 * @param rfcRelation the rfc relation
	 * @return the long
	 */
	public long updateRfcRelation(CmsRfcRelation rfcRelation) {
		return updateRfcRelationSimple(rfcRelation, null);
	}
	
	private long updateRfcRelationSimple(CmsRfcRelation relation, CmsRfcRelation existingRelation) {
		existingRelation = (existingRelation!= null) ? existingRelation : getRfcRelationById(relation.getRfcId());
		
		if (existingRelation == null) {
			String errorMsg = "Relation Rfc does not exists " + relation.getRfcId();
			logger.error(errorMsg);
			throw new DJException(CmsError.DJ_RELATION_RFC_DOESNT_EXIST_ERROR, errorMsg);
		}

		relation.setRelationName(existingRelation.getRelationName());
		
		CIValidationResult validation = djValidator.validateRfcRelationAttrs(relation);
		
		if (!validation.isValidated()) {
			logger.error(validation.getErrorMsg());
			throw new DJException(CmsError.DJ_VALIDATION_ERROR, validation.getErrorMsg());
		}
		
		long updated = djMapper.updateRfcRelation(relation);
		logger.debug(updated);
		
		for (CmsRfcAttribute attr : relation.getAttributes().values()){
			CmsRfcAttribute existingAttr = existingRelation.getAttribute(attr.getAttributeName());
			if(!(djValidator.rfcAttrsEqual(attr, existingAttr))) {
				attr.setRfcId(relation.getRfcId());
				djMapper.upsertRfcRelationAttribute(attr);
			}	
		}
		return relation.getRfcId();
	}

    /**
     * Gets the closed rfc ci by ci id.
     *
     * @param ciId the ci id
     * @return the closed rfc ci by ci id
     */
    public List<CmsRfcCI> getClosedRfcCIByCiId(long ciId) {
        List<CmsRfcCI> rfcList = djMapper.getClosedRfcCIByCiId(ciId);
        populateRfcCIAttributes(rfcList);
        return rfcList;
    }

    /**
     * Gets the closed relation rfcs by ciid (to or from).
     *
     * @param ciId the ci id
     * @return the closed relation rfc ci by ci id
     */
    public List<CmsRfcRelation> getClosedRelationRfcCIByCiId(long ciId) {
        List<CmsRfcRelation> rfcList = djMapper.getClosedRfcRelationByCiId(ciId);
        populateRfcRelationAttributes(rfcList);
        return rfcList;
    }
    
    
    /**
     * Gets the roll up rfc.
     *
     * @param ciId the ci id
     * @param rfcId the rfc id
     * @return the roll up rfc
     */
    public CmsRfcCI getRollUpRfc(long ciId, long rfcId) {
        List<CmsRfcCI> rfcCIList = djMapper.getRollUpRfc(ciId, rfcId);
        logger.debug(rfcCIList.size()+" RfcCi found fo aggregation");
        CmsRfcCI resultRfc = null;
        for(CmsRfcCI rfcCi : rfcCIList) {
            populateRfcCIAttributes(rfcCi);
            if(rfcCi.getRfcActionId() == 100) {
                if(resultRfc != null) {
                    logger.warn("Error in RfcCi: more one 'add' action!");
                }
                resultRfc =  rfcCi;
                resultRfc.setRfcId(0);
            } else if(rfcCi.getRfcActionId() == 200){
                if(resultRfc != null) {
                    Map<String,CmsRfcAttribute> attrs = rfcCi.getAttributes();
                    for(String key: attrs.keySet()) {
                        CmsRfcAttribute attr = attrs.get(key);
                        CmsRfcAttribute resultAttr = resultRfc.getAttribute(key);
                        if(resultAttr == null) {
                            attr.setRfcId(0);
                            resultRfc.addAttribute(attr);
                        } else {
                            resultAttr.setRfcId(0);
                            resultAttr.setOldValue(resultAttr.getNewValue());
                            resultAttr.setNewValue(attr.getNewValue());
                        }
                    }
                } else {
                    logger.warn("Error in RfcCi: 'update' action without 'add'!");
                }
            }
        }

        return null;
    }

    private CmsRfcCI populateRfcCIAttributes(CmsRfcCI rfcCi) {
        if (rfcCi == null) return null;
        for(CmsRfcAttribute attr : djMapper.getRfcCIAttributes(rfcCi.getRfcId())){
            rfcCi.addAttribute(attr);
        }
        return rfcCi;
    }

    private void populateRfcCIAttributesSimple(List<CmsRfcCI> rfcCis) {
    	if (rfcCis.size() == 0) {
    		return;
    	}
    	Map<Long, CmsRfcCI> rfcMap = new HashMap<Long, CmsRfcCI>();
        for (CmsRfcCI rfcCi : rfcCis) {
        	rfcMap.put(rfcCi.getRfcId(), rfcCi);
        }
        for(CmsRfcAttribute attr : djMapper.getRfcCIAttributesByRfcIdList(rfcMap.keySet())){
        	rfcMap.get(attr.getRfcId()).addAttribute(attr);
        }
    }

    private void populateRfcCIAttributes(List<CmsRfcCI> rfcCis) {
		int fromIndex = 0;
		int toIndex = rfcCis.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : rfcCis.size();
		List<CmsRfcCI> subList = rfcCis.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			populateRfcCIAttributesSimple(subList);
			fromIndex += CHUNK_SIZE;
			toIndex = rfcCis.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : rfcCis.size();
			subList = rfcCis.subList(fromIndex, toIndex);
		}
		if (subList.size() >0) {
			populateRfcCIAttributesSimple(subList);
		}
    }
    
    private CmsRfcRelation populateRfcRelationAttributes(CmsRfcRelation rfcRel) {
        if (rfcRel == null) return null;
        for(CmsRfcAttribute attr : djMapper.getRfcRelationAttributes(rfcRel.getRfcId())) {
            rfcRel.addAttribute(attr);
        }
        return rfcRel;
    }

    private void populateRfcRelationAttributesSimple(List<CmsRfcRelation> rfcRels) {
    	if (rfcRels.size() == 0) {
    		return;
    	}
        Map<Long, CmsRfcRelation> relRfcMap = new HashMap<Long, CmsRfcRelation>();
    	for (CmsRfcRelation rel : rfcRels) {
    		relRfcMap.put(rel.getRfcId(), rel);
    	}
        
        for(CmsRfcAttribute attr : djMapper.getRfcRelationAttributesByRfcIdList(relRfcMap.keySet())) {
        	relRfcMap.get(attr.getRfcId()).addAttribute(attr);
        }
    }

    private void populateRfcRelationAttributes(List<CmsRfcRelation> rfcRels) {
		int fromIndex = 0;
		int toIndex = rfcRels.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : rfcRels.size();
		List<CmsRfcRelation> subList = rfcRels.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			populateRfcRelationAttributesSimple(subList);
			fromIndex += CHUNK_SIZE;
			toIndex = rfcRels.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : rfcRels.size();
			subList = rfcRels.subList(fromIndex, toIndex);
		}
		if (subList.size() >0) {
			populateRfcRelationAttributesSimple(subList);
		}
    }
 
	/**
	 * get ci rfc links (simple call to get relations without extra info)
	 *
	 * @param  nsPath, relName
	 */
	public List<CmsRfcLink> getLinks(String nsPath, String relName) {
		return djMapper.getOpenRfcLinks(nsPath, relName);
	}
    
	/**
	 * get ci rfc links (simple call to get relations without extra info)
	 *
	 * @param  nsPath, relName
	 */
	public long getRfcCount(long nsPath) {
		return djMapper.countCiRfcByReleaseId(nsPath);
	}

	/**
	 * Get a count of open RFC CIs for a given namespace (not recursive).
	 *
	 * @param nsPath
	 * @return number of open RFC CIs
	 */
	public long getRfcCiCountByNs(String nsPath)
	{
		return djMapper.countOpenRfcCisByNs(nsPath);
	}

	/**
	 * Get a count of open RFC relations for a given namespace (not recursive).
	 *
	 * @param nsPath
	 * @return number of open RFC relations
	 */
	public long getRfcRelationCountByNs(String nsPath)
	{
		return djMapper.countOpenRfcRelationsByNs(nsPath);
	}

	/**
	 * Get a count of open RFCs (CIs + relations) for a given namespace (not recursive).
	 *
	 * @param nsPath
	 * @return number of open RFCs
	 */
	public long getRfcCountByNs(String nsPath)
	{
		return djMapper.countOpenRfcCisByNs(nsPath) + djMapper.countOpenRfcRelationsByNs(nsPath);
	}



    /**
     * Remove all rfcs and rfc relations for a given namespace  (not recursive).
     * this is using map as a workaround to transfer "nsPath" parameter in as well 
     * as "result" out (result contains stored procedure return value = number of rfcs deleted) 
     * because MyBatis doesn't allow to map simple return types from a stored procedures
     * 
     *
     * @param  nsPath
     */
    public long rmRfcs(String nsPath) {
        Map<String, Object> map = new HashMap<>();
        map.put("nsPath", nsPath);
        djMapper.rmRfcs(map);
        try {
            return Integer.parseInt(map.getOrDefault("result", "0").toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    /**
	 * get count of all Ci which have the given relation from the given ci id and not updated by the rfcId
	 */
	public long getCiCountNotUpdatedByRfc(long fromId, String relationName, String shortRelName, long rfcId) {
		return djMapper.countCiNotUpdatedByRfc(fromId, relationName, shortRelName, rfcId);
	}
}
