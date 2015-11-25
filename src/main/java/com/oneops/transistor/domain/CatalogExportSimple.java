package com.oneops.transistor.domain;

import java.util.List;

import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;

public class CatalogExportSimple extends CatalogExportBasic {
	private List<CmsCISimple> cis;
	private List<CmsCIRelationSimple> relations;

	public List<CmsCISimple> getCis() {
		return cis;
	}
	public void setCis(List<CmsCISimple> cis) {
		this.cis = cis;
	}
	public List<CmsCIRelationSimple> getRelations() {
		return relations;
	}
	public void setRelations(List<CmsCIRelationSimple> relations) {
		this.relations = relations;
	}
}
