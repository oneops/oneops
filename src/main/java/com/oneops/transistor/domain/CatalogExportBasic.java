package com.oneops.transistor.domain;

public abstract class CatalogExportBasic {
	
	private String catalogName;
	private String description;
	private long catalogId;
	
	public String getCatalogName() {
		return catalogName;
	}
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}
	public void setCatalogId(long catalogId) {
		this.catalogId = catalogId;
	}
	public long getCatalogId() {
		return catalogId;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}

}
