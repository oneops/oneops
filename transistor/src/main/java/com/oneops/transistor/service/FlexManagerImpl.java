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
package com.oneops.transistor.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsUtil;

import static com.oneops.cms.util.CmsConstants.BASE_CONSUMES;

public class FlexManagerImpl implements FlexManager {
	private static Logger logger = Logger.getLogger(FlexManagerImpl.class);

	private CmsCmProcessor cmProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsRfcProcessor rfcProcessor;
	private TransUtil trUtil;
	private BomRfcBulkProcessor bomGenerationProcessor;
	private CmsUtil cmsUtil;

	private BomManager bomManager;
	
	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setBomGenerationProcessor(BomRfcBulkProcessor bomGenerationProcessor) {
		this.bomGenerationProcessor = bomGenerationProcessor;
	}

	public void setBomManager(BomManager bomManager) {
		this.bomManager = bomManager;
	}

	@Override
	public long processFlex(long flexRelId, int step, boolean scaleUp, long envId) {

		String userId = "oneops-flex";

		CmsCIRelation flexRel = cmProcessor.getRelationById(flexRelId);
		long manifestReleaseId = processManifestRelation(flexRel, step, scaleUp, userId);
		//commit manifest release
		rfcProcessor.commitRelease(manifestReleaseId, true, null,false,userId, "Flex relation changed by oneops-flex system");
		CmsCI manPlat = getManifestPlatform(flexRel);
		long bomReleaseId = processBomRelease(envId, manPlat, manifestReleaseId, userId);
		bomManager.submitDeployment(bomReleaseId, userId, null);
		return bomReleaseId;
	}

	private CmsCI getManifestPlatform(CmsCIRelation flexRel) {
		
		List<CmsCIRelation> platRels = cmProcessor.getToCIRelations(flexRel.getFromCiId(), "manifest.Requires", "manifest.Platform");
		if (platRels.size()>0) {
			return platRels.get(0).getFromCi(); 
		} else {
			return null;
		}
	}

	private long processBomRelease(long envId, CmsCI platform, long manifestReleaseId, String userId) {
		EnvBomGenerationContext context = new EnvBomGenerationContext(envId, userId, cmProcessor, cmsUtil, rfcProcessor);
		context.load();
		String bomNsPath = context.getBomNsPath();
		trUtil.verifyAndCreateNS(bomNsPath);

		List<CmsCIRelation> cloudRels = cmProcessor.getFromCIRelations(platform.getCiId(), BASE_CONSUMES, "account.Cloud");
		for (CmsCIRelation cloudRel : cloudRels) {
			CmsCIRelationAttribute adminstatus = cloudRel.getAttribute("adminstatus");
			if (adminstatus == null || CmsConstants.CLOUD_STATE_ACTIVE.equals(adminstatus.getDjValue())) {
				bomGenerationProcessor.processManifestPlatform(context,
															   context.loadPlatformContext(platform),
															   cloudRel,
															   1,
															   false);
			}
		}
		
		return getPopulateParentAndGetReleaseId(bomNsPath, manifestReleaseId);
	}
	
	private long processManifestRelation(CmsCIRelation flexRel, int step, boolean scaleUp, String userId) {

		int current = Integer.valueOf(flexRel.getAttribute("current").getDjValue());
		int min = Integer.valueOf(flexRel.getAttribute("min").getDjValue());
		int max = Integer.valueOf(flexRel.getAttribute("max").getDjValue());
		if (scaleUp && current < max) {
			int newCurrent = current + step;
			if (newCurrent > max) {
				newCurrent = max;
			}
			flexRel.getAttribute("current").setDjValue(String.valueOf(newCurrent));
		} else if (!scaleUp && current > min) {
			int newCurrent = current - step;
			if (newCurrent < min) {
				newCurrent = min;
			}
			flexRel.getAttribute("current").setDjValue(String.valueOf(newCurrent));
		} else {
			logger.warn("Flex capacity limit was reached for relation - " + flexRel.getRelationGoid());
			return 0;
		}
		
		CmsRfcRelation relRfc = bootstrapRelationRfc(flexRel);
		CmsRfcRelation newRfc = cmRfcMrgProcessor.upsertRelationRfc(relRfc, userId);
		
		return newRfc.getReleaseId();
	}
	
	private CmsRfcRelation bootstrapRelationRfc(CmsCIRelation rel) {

		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(rel.getNsPath());
		newRfc.setCiRelationId(rel.getCiRelationId());
		newRfc.setRelationId(rel.getRelationId());
		newRfc.setRelationName(rel.getRelationName());
	    newRfc.setFromCiId(rel.getFromCiId());
	    newRfc.setToCiId(rel.getToCiId());
	    //newRfc.setRfcAction("update");
	    
	    CmsCIRelationAttribute flexAttr = rel.getAttribute("current");
		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();

		rfcAttr.setAttributeId(flexAttr.getAttributeId());
		rfcAttr.setAttributeName(flexAttr.getAttributeName());
		rfcAttr.setNewValue(flexAttr.getDjValue());
		newRfc.addAttribute(rfcAttr);
		
		return newRfc;
	}

	private long getPopulateParentAndGetReleaseId(String nsPath, long manifestReleaseId) {
		List<CmsRelease> releases = rfcProcessor.getLatestRelease(nsPath, "open"); 
		if (releases.size() >0) {
			CmsRelease bomRelease = releases.get(0);
			bomRelease.setParentReleaseId(manifestReleaseId); 
			rfcProcessor.updateRelease(bomRelease);
			return bomRelease.getReleaseId(); 
		}
		return 0;
	}
}
