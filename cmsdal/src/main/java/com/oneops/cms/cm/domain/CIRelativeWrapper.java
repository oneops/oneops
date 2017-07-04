package com.oneops.cms.cm.domain;

import java.util.Date;
import java.util.Map;

public class CIRelativeWrapper extends CmsCI {

	private static final long serialVersionUID = 1L;

	private String relationName;

	private int relationId;

	private CmsCI relCi;
	
	public CIRelativeWrapper() {
		relCi = new CmsCI();
	}

	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	public int getRelationId() {
		return relationId;
	}

	public void setRelationId(int relationId) {
		this.relationId = relationId;
	}

	public CmsCI getRelCi() {
		return relCi;
	}

	public void setRelCi(CmsCI relCi) {
		this.relCi = relCi;
	}

	public void setCiClassId(int ciClassId) {
		relCi.setCiClassId(ciClassId);
	}

	@Override
	public void setNsId(long nsId) {
		relCi.setNsId(nsId);
	}

	@Override
	public void setCiStateId(int ciStateId) {
		relCi.setCiStateId(ciStateId);
	}

	@Override
	public void setAttributes(Map<String, CmsCIAttribute> attributes) {
		relCi.setAttributes(attributes);
	}

	@Override
	public void setCiId(long ciId) {
		relCi.setCiId(ciId);
	}

	@Override
	public void setCiName(String ciName) {
		relCi.setCiName(ciName);
	}

	@Override
	public void setCiClassName(String ciClassName) {
		relCi.setCiClassName(ciClassName);
	}

	@Override
	public void setNsPath(String nsPath) {
		relCi.setNsPath(nsPath);
	}

	@Override
	public void setCiGoid(String ciGoid) {
		relCi.setCiGoid(ciGoid);
	}

	@Override
	public void setComments(String comments) {
		relCi.setComments(comments);
	}

	@Override
	public void setCiState(String ciState) {
		relCi.setCiState(ciState);
	}

	@Override
	public void setLastAppliedRfcId(long lastAppliedRfcId) {
		relCi.setLastAppliedRfcId(lastAppliedRfcId);
	}

	@Override
	public void setCreatedBy(String createdBy) {
		relCi.setCreatedBy(createdBy);
	}

	@Override
	public void setUpdatedBy(String updatedBy) {
		relCi.setUpdatedBy(updatedBy);
	}

	@Override
	public void setCreated(Date created) {
		relCi.setCreated(created);
	}

	@Override
	public void setUpdated(Date updated) {
		relCi.setUpdated(updated);
	}

	@Override
	public void setImpl(String impl) {
		relCi.setImpl(impl);
	}

	@Override
	public void addAttribute(CmsCIAttribute attribute) {
		relCi.addAttribute(attribute);
	}
}
