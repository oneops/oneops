package com.oneops.ops;

public class OrphanCloseEvent {

	private long ciId;
	private long manifestId;
	private String name;
	private String openEventPayload;
	
	public long getCiId() {
		return ciId;
	}
	public void setCiId(long ciId) {
		this.ciId = ciId;
	}
	public long getManifestId() {
		return manifestId;
	}
	public void setManifestId(long manifestId) {
		this.manifestId = manifestId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOpenEventPayload() {
		return openEventPayload;
	}
	public void setOpenEventPayload(String openEventPayload) {
		this.openEventPayload = openEventPayload;
	}
	
}
