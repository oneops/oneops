package com.oneops.transistor.service;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.transistor.domain.CatalogExport;

@Transactional
public interface DesignManager {
	public long generatePlatform(CmsRfcCI platRfc, long assemblyId, String userId, String scope);
	public long deletePlatform(long platformId, String userId, String scope);
	public long clonePlatform(CmsRfcCI platRfc, Long targetAssemblyId, long sourcePlatId, String userId, String scope);
	public long cloneAssembly(CmsCI assemblyCI, long sourceAssemblyId, String userId, String scope);
	public long saveAssemblyAsCatalog(CmsCI catalogCI, long sourceAssemblyId, String userId, String scope);
	public CatalogExport exportCatalog(long catalogCIid, String scope);
	public long importCatalog(CatalogExport catExp, String userId, String scope);
	
}

