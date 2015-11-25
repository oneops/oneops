package com.oneops.transistor.domain;

import java.util.List;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;

public class CatalogExport extends CatalogExportBasic{
	private List<CmsCI> cis;
	private List<CmsCIRelation> relations;

	public List<CmsCI> getCis() {
		return cis;
	}
	public void setCis(List<CmsCI> cis) {
		this.cis = cis;
	}
	public List<CmsCIRelation> getRelations() {
		return relations;
	}
	public void setRelations(List<CmsCIRelation> relations) {
		this.relations = relations;
	}
}
