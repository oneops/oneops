package com.oneops.cms.cm.domain;

public class CmsLink {
	private long fromCiId;
	private String fromClazzName;
	private String relationName;
	private long toCiId;
	private String toClazzName;
	
	public long getFromCiId() {
		return fromCiId;
	}
	public void setFromCiId(long fromCiId) {
		this.fromCiId = fromCiId;
	}
	public String getFromClazzName() {
		return fromClazzName;
	}
	public void setFromClazzName(String fromClazzName) {
		this.fromClazzName = fromClazzName;
	}
	public String getRelationName() {
		return relationName;
	}
	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}
	public long getToCiId() {
		return toCiId;
	}
	public void setToCiId(long toCiId) {
		this.toCiId = toCiId;
	}
	public String getToClazzName() {
		return toClazzName;
	}
	public void setToClazzName(String toClazzName) {
		this.toClazzName = toClazzName;
	}
	
	
}
