package com.oneops.cms.util;

import java.util.List;

public class ReleaseQueryParam {
	
	private String envNsLike;

	private String ciFilter;

	private String classFilter;

	private String manifestNsLike;

	private Long offset;

	private Long endRelId;
	
	private List<Long> releaseList;
	
	public ReleaseQueryParam(String envNsLike, String ciFilter, String classFilter, String manifestNsLike, Long offset) {
		this.envNsLike = envNsLike;
		this.ciFilter = ciFilter;
		this.classFilter = classFilter;
		this.manifestNsLike = manifestNsLike;
		this.offset = offset;
	}

	public String getEnvNsLike() {
		return envNsLike;
	}

	public void setEnvNsLike(String envNsLike) {
		this.envNsLike = envNsLike;
	}

	public String getCiFilter() {
		return ciFilter;
	}

	public void setCiFilter(String ciFilter) {
		this.ciFilter = ciFilter;
	}

	public String getClassFilter() {
		return classFilter;
	}

	public void setClassFilter(String classFilter) {
		this.classFilter = classFilter;
	}

	public String getManifestNsLike() {
		return manifestNsLike;
	}

	public void setManifestNsLike(String manifestNsLike) {
		this.manifestNsLike = manifestNsLike;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Long getEndRelId() {
		return endRelId;
	}

	public void setEndRelId(Long endRelId) {
		this.endRelId = endRelId;
	}

	public List<Long> getReleaseList() {
		return releaseList;
	}

	public void setReleaseList(List<Long> releaseList) {
		this.releaseList = releaseList;
	}

}
