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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.domain.IaasBindingTriplet;
import com.oneops.transistor.domain.IaasRequest;
import com.oneops.transistor.exceptions.TransistorException;

public class IaasManagerImpl implements IaasManager{

	static Logger logger = Logger.getLogger(IaasManagerImpl.class);
	
	
	private CmsCmProcessor cmProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private ManifestRfcBulkProcessor manifestRfcProcessor;
    private Gson gson = new Gson();
    private TransUtil trUtil;
	
	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}
    
	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setManifestRfcProcessor(ManifestRfcBulkProcessor manifestRfcProcessor) {
		this.manifestRfcProcessor = manifestRfcProcessor;
	}
	
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	@Override
	public long processPlatformIaas(IaasRequest request, long platformId, String userId) {		
		
		long iaasId = 0;
		
		CmsRfcCI platform = cmRfcMrgProcessor.getCiById(platformId, "dj");

		long envId = getEnvIdByPlatformId(platformId);
		if (envId == 0) {
			String errMsg = "Can not find environment by the platformId = " + platformId + ";"; 
			logger.error(errMsg);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_FIND_ENVIRONMENT, errMsg);
		}
		
		CmsCI env = cmProcessor.getCiById(envId);
		String nsPath = env.getNsPath() + "/" + env.getCiName() + "/manifest";
		trUtil.verifyAndCreateNS(nsPath);
		
		Map<Long, Set<String>> iaas2service = new HashMap<Long, Set<String>>();
		for (IaasBindingTriplet iaasBinding : request.getIaasList()){
			if (iaas2service.containsKey(iaasBinding.getTmplIaasId())) {
				if (!iaas2service.get(iaasBinding.getTmplIaasId()).contains(iaasBinding.getServiceType())) {
					iaas2service.get(iaasBinding.getTmplIaasId()).add(iaasBinding.getServiceType());
				}		
			} else {
				Set<String> serviceTypes = new HashSet<String>();
				serviceTypes.add(iaasBinding.getServiceType());
				iaas2service.put(iaasBinding.getTmplIaasId(), serviceTypes);
			}
		}
		
		Set<Long> manifestIaasIds = new HashSet<Long>();
		for (Long tmplIaasId : iaas2service.keySet()) {
			CmsRfcCI manifestIaas = upsertManifestIaas(tmplIaasId, envId, nsPath, userId);
			manifestIaasIds.add(manifestIaas.getCiId());
			CmsRfcRelation rel = cmRfcMrgProcessor.getExisitngRelationRfcMerged(platform.getCiId(), "manifest.ServicedBy", manifestIaas.getCiId(), "df");
			if (rel == null) {
				Set<String> attrs = new HashSet<String>();
				attrs.add("services");
				rel = manifestRfcProcessor.bootstrapRelationRfcWithAttrs(platform.getCiId(), manifestIaas.getCiId(), "manifest.ServicedBy", platform.getNsPath(), nsPath, attrs);
			}
			//need to convert Set -> List for jason compatibility
			rel.getAttribute("services").setNewValue(gson.toJson(new ArrayList<String>(iaas2service.get(tmplIaasId))));
			rel.setCreatedBy(userId);
			rel.setUpdatedBy(userId);
			CmsRfcRelation relRfc = cmRfcMrgProcessor.upsertRelationRfc(rel, "oneops-transistor", "dj");
			logger.info("created iaas relation " + relRfc.getRelationId() + " between " + platform.getCiName() + " and " + manifestIaas.getCiName());
			iaasId = manifestIaas.getCiId();
		}
		
		//lets get existing ServicedBy relations
		List<CmsRfcRelation> existingRels = cmRfcMrgProcessor.getFromCIRelationsNakedNoAttrs(platform.getCiId(), "manifest.ServicedBy", null, "manifest.Iaas");
		for (CmsRfcRelation existingRel : existingRels) {
			//if we don't have this iaas in the list lets remove the rel
			if (!manifestIaasIds.contains(existingRel.getToCiId())) {
				cmProcessor.deleteRelation(existingRel.getCiRelationId());
			}
		}
		return iaasId;
	}

	
	private CmsRfcCI upsertManifestIaas(long tmplIaasId, long envId, String nsPath, String userId) {
		
		CmsCI tmplIaas = cmProcessor.getCiById(tmplIaasId);
		String iaasNsPath = nsPath + "/" + tmplIaas.getCiName();
		List<CmsRfcCI> exisitngManifestIaasList = cmRfcMrgProcessor.getDfDjCi(iaasNsPath, "manifest.Iaas", tmplIaas.getCiName(), "df");
		if (exisitngManifestIaasList.size()>0) {
			return exisitngManifestIaasList.get(0);
		} else {
			return manifestRfcProcessor.processIaas(tmplIaas, envId, nsPath, userId); 
		}
	}
	
	private long getEnvIdByPlatformId(long platId) {
		
		List<CmsRfcRelation> envRels = cmRfcMrgProcessor.getToCIRelationsNakedNoAttrs(platId, "manifest.ComposedOf", null, "manifest.Environment");
		
		if (envRels.size()>0) {
			return envRels.get(0).getFromCiId();
		} else {
			return 0;
		}
	}
	
	


}
