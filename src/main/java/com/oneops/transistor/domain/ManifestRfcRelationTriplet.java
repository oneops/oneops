package com.oneops.transistor.domain;

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

public class ManifestRfcRelationTriplet {

	private CmsRfcCI toRfcCI;
	private CmsRfcCI fromRfcCI;
	private CmsRfcRelation rfcRelation;
	
	public CmsRfcCI getToRfcCI() {
		return toRfcCI;
	}
	public void setToRfcCI(CmsRfcCI toRfcCI) {
		this.toRfcCI = toRfcCI;
	}
	public CmsRfcCI getFromRfcCI() {
		return fromRfcCI;
	}
	public void setFromRfcCI(CmsRfcCI fromRfcCI) {
		this.fromRfcCI = fromRfcCI;
	}
	public CmsRfcRelation getRfcRelation() {
		return rfcRelation;
	}
	public void setRfcRelation(CmsRfcRelation rfcRelation) {
		this.rfcRelation = rfcRelation;
	}
	
	
}
