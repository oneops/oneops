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

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.transistor.domain.CatalogExport;
import com.oneops.transistor.export.domain.DesignExportSimple;

import java.util.List;
import java.util.Map;

@Transactional
public interface DesignManager {
	public long generatePlatform(CmsRfcCI platRfc, long assemblyId, String userId, String scope);
	public long deletePlatform(long platformId, String userId, String scope);
	public long clonePlatform(CmsRfcCI platRfc, Long targetAssemblyId, long sourcePlatId, String userId, String scope);
	public Map<String, List<?>> getPlatformRfcs(long platId, String userId, String scope);
	public long discardReleaseForPlatform(long platId, String userId);
	public long commitReleaseForPlatform(long platId, String desc, String userId);
	public long cloneAssembly(CmsCI assemblyCI, long sourceAssemblyId, String userId, String scope);
	public long saveAssemblyAsCatalog(CmsCI catalogCI, long sourceAssemblyId, String userId, String scope);
	public CatalogExport exportCatalog(long catalogCIid, String scope);
	public long importCatalog(CatalogExport catExp, String userId, String scope);
	public DesignExportSimple exportDesign(long assemblyId, Long[] platformIds, String scope);
	public long importDesign(long assemblyId, String userId, String scope, DesignExportSimple des);
	public void updateOwner(long assemblyId);
    public long refreshPack(long platformId, String userId, String scope);
	
}

