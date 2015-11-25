package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.transistor.domain.CatalogExport;

public class DesignManagerImpl implements DesignManager {

	private DesignRfcProcessor designRfcProcessor;
	private CatalogProcessor catalogProcessor;

	public void setDesignRfcProcessor(DesignRfcProcessor designRfcProcessor) {
		this.designRfcProcessor = designRfcProcessor;
	}

	public void setCatalogProcessor(CatalogProcessor catalogProcessor) {
		this.catalogProcessor = catalogProcessor;
	}

	@Override
	public long generatePlatform(CmsRfcCI platRfc, long assemblyId,
			String userId, String scope) {
		return designRfcProcessor.generatePlatFromTmpl(platRfc, assemblyId, userId, scope);
	}

	@Override
	public long clonePlatform(CmsRfcCI platRfc, Long targetAssemblyId,
			long sourcePlatId, String userId, String scope) {
		return designRfcProcessor.clonePlatform(platRfc, targetAssemblyId, sourcePlatId, userId, scope);
	}

	@Override
	public long cloneAssembly(CmsCI assemblyCI, 
			long sourceAssemblyId, String userId, String scope) {
		return designRfcProcessor.cloneAssembly(assemblyCI, sourceAssemblyId, userId, scope);
	}

	@Override
	public long saveAssemblyAsCatalog(CmsCI catalogCI, 
			long sourceAssemblyId, String userId, String scope) {
		return catalogProcessor.saveAssemblyAsCatalog(catalogCI, sourceAssemblyId, userId, scope);
	}

	@Override
	public CatalogExport exportCatalog(long catalogCIid, String scope) {
		return catalogProcessor.exportCatalog(catalogCIid, scope);
	}

	@Override
	public long importCatalog(CatalogExport catExp, String userId,
			String scope) {
		return catalogProcessor.importCatalog(catExp, userId, scope);
	}

	@Override
	public long deletePlatform(long platformId, String userId, String scope) {
		return designRfcProcessor.deletePlatform(platformId, userId, scope);
	}


}
