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
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.dj.domain.CmsRfcRelation;

@Transactional
public interface ManifestManager {
	public long generateEnvManifest(long envId, String userId, Map<String, String> platModes);
	public long activatePlatform(long platId, String userId);
	public long disablePlatform(long platId, String userId);
	public long enablePlatform(long platId, String userId);
	public void updateCloudAdminStatus(long cloudId, long envId, String adminstatus, String userId);
	public long updateEnvClouds(long envId, List<CmsCIRelation> cloudRels, String userId);
	public void updatePlatformCloud(CmsRfcRelation cloudRel, String userId);
}