package com.oneops.cms.cm.domain;

public class ClassRelationPair {

	private String clazzName;
	private String relationName;

	public ClassRelationPair(String clazzName, String relationName) {
		this.clazzName = clazzName;
		this.relationName = relationName;
	}

	public String getClazzName() {
		return clazzName;
	}
	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}
	public String getRelationName() {
		return relationName;
	}
	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}
}
