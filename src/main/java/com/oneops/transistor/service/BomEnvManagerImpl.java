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
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.ns.service.CmsNsManager;


public class BomEnvManagerImpl implements BomEnvManager  {

	static Logger logger = Logger.getLogger(BomEnvManagerImpl.class);

	private CmsCmProcessor cmProcessor;
	private CmsNsManager nsManager;
	private CmsRfcProcessor rfcProcessor;
	private CmsDpmtProcessor dpmtProcessor;

	private BomManager bomManager;
	
	public void setDpmtProcessor(CmsDpmtProcessor dpmtProcessor) {
		this.dpmtProcessor = dpmtProcessor;
	}
	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setNsManager(CmsNsManager nsManager) {
		this.nsManager = nsManager;
	}

	public void setBomManager(BomManager bomManager) {
		this.bomManager = bomManager;
	}
	
	@Override
	public void cleanEnvBom(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = env.getNsPath() + "/" + env.getCiName() + "/bom";
		nsManager.deleteNs(bomNsPath);
		logger.info("cleaned bom nspath: " + bomNsPath);
	}
	
	
	@Override
	public void takeEnvSnapshot(long envId) {
		// TODO Auto-generated method stub
	}

	@Override
	public long discardEnvBom(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = env.getNsPath() + "/" + env.getCiName() + "/bom";
		bomManager.check4openDeployment(bomNsPath);
		long bomReleaseId = 0;
		List<CmsRelease> bomReleases = rfcProcessor.getReleaseBy3(bomNsPath, null, "open");
		for (CmsRelease bomRel : bomReleases) {
			bomRel.setReleaseState("canceled");
			rfcProcessor.updateRelease(bomRel);
			bomReleaseId = bomRel.getReleaseId();
		}
		return bomReleaseId;
	}
	
	@Override
	public long discardEnvManifest(long envId, String userId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String manifestNsPath = env.getNsPath() + "/" + env.getCiName() + "/manifest";
		long manifestReleaseId = 0;
		List<CmsRelease> manifestReleases = rfcProcessor.getReleaseBy3(manifestNsPath, null, "open");
		for (CmsRelease manifestRel : manifestReleases) {
			manifestRel.setReleaseState("canceled");
			manifestRel.setCommitedBy(userId);
			rfcProcessor.updateRelease(manifestRel);
			env.setComments("");
			cmProcessor.updateCI(env);
			manifestReleaseId = manifestRel.getReleaseId();
		}
		return manifestReleaseId;
	}



}
