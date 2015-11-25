package com.oneops.transistor.domain;

import java.util.ArrayList;
import java.util.List;

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

public class ManifestRootRfcContainer {
	private List<Long> templateCis = new ArrayList<>();
	private CmsRfcCI rfcCI;
	private List<CmsRfcRelation> toRfcRelation = new ArrayList<>();
	private List<CmsRfcRelation> fromRfcRelation = new ArrayList<>();


	public List<Long> getTemplateCis() {
		return templateCis;
	}

	public void setTemplateCis(List<Long> templateCis) {
		this.templateCis = templateCis;
	}

	public CmsRfcCI getRfcCI() {
		return rfcCI;
	}

	public void setRfcCI(CmsRfcCI rfcCI) {
		this.rfcCI = rfcCI;
	}

	public List<CmsRfcRelation> getToRfcRelation() {
		return toRfcRelation;
	}

	public void setToRfcRelation(List<CmsRfcRelation> toRfcRelation) {
		this.toRfcRelation = toRfcRelation;
	}

	public List<CmsRfcRelation> getFromRfcRelation() {
		return fromRfcRelation;
	}

	public void setFromRfcRelation(List<CmsRfcRelation> fromRfcRelation) {
		this.fromRfcRelation = fromRfcRelation;
	}

}
