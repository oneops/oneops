package com.oneops.cms.dj.domain;

public class CIRelative {

	private String relationName;
	private long relationId;
	private long ciRelationId;
	private long relCiId;
	private String type;

	public String getRelationName() {
		return relationName;
	}
	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}
	public long getRelationId() {
		return relationId;
	}
	public void setRelationId(long relationId) {
		this.relationId = relationId;
	}
	public long getRelCiId() {
		return relCiId;
	}
	public void setRelCiId(long relCiId) {
		this.relCiId = relCiId;
	}
	public long getCiRelationId() {
		return ciRelationId;
	}
	public void setCiRelationId(long ciRelationId) {
		this.ciRelationId = ciRelationId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
