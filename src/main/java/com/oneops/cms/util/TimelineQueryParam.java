/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.cms.util;

import java.util.List;

public class TimelineQueryParam {
	
	private String nsPath;
	private String releaseNsLike;
	private String releaseNs;
	private String dpmtNsLike;
	private String type;
	
	private String filter;
	private String wildcardFilter;
	private String releaseClassFilter;

	private List<Long> releaseScopeNsIds;
	private List<Long> nsIdsMatchingFilter;

	private String dpmtClassFilter;
	private String dpmtNsLikeWithFilter;

	private Long dpmtOffset;
	private Long releaseOffset;
	private Long endRelId;
	private List<Long> releaseList;
	
	private List<Long> excludeReleaseList;
	
	private QueryOrder order;
	private Integer limit;

	private boolean isDesignNamespace;

	public TimelineQueryParam(String nsPath, String filter, String type, QueryOrder order, Long releaseOffset, Long dpmtOffset, Integer limit) {
		this.nsPath = nsPath;
		this.order = order;
		this.type = type;
		this.filter = filter;
		this.dpmtOffset = dpmtOffset;
		this.releaseOffset = releaseOffset;
		this.limit = limit;
	}

	public String getDpmtNsLike() {
		return dpmtNsLike;
	}

	public void setDpmtNsLike(String envNsLike) {
		this.dpmtNsLike = envNsLike;
	}

	public String getWildcardFilter() {
		return wildcardFilter;
	}

	public void setWildcardFilter(String ciFilter) {
		this.wildcardFilter = ciFilter;
	}

	public String getReleaseNsLike() {
		return releaseNsLike;
	}

	public void setReleaseNsLike(String releaseNsLike) {
		this.releaseNsLike = releaseNsLike;
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

	public QueryOrder getOrder() {
		return order;
	}

	public void setOrder(QueryOrder order) {
		this.order = order;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getNsPath() {
		return nsPath;
	}

	public void setNsPath(String envNs) {
		this.nsPath = envNs;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Long getDpmtOffset() {
		return dpmtOffset;
	}

	public void setDpmtOffset(Long dpmtOffset) {
		this.dpmtOffset = dpmtOffset;
	}

	public String getReleaseClassFilter() {
		return releaseClassFilter;
	}

	public void setReleaseClassFilter(String releaseClassFilter) {
		this.releaseClassFilter = releaseClassFilter;
	}

	public String getDpmtClassFilter() {
		return dpmtClassFilter;
	}

	public void setDpmtClassFilter(String dpmtClassFilter) {
		this.dpmtClassFilter = dpmtClassFilter;
	}

	public Long getReleaseOffset() {
		return releaseOffset;
	}

	public void setReleaseOffset(Long releaseOffset) {
		this.releaseOffset = releaseOffset;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDpmtNsLikeWithFilter() {
		return dpmtNsLikeWithFilter;
	}

	public void setDpmtNsLikeWithFilter(String dpmtNsLikeWithFilter) {
		this.dpmtNsLikeWithFilter = dpmtNsLikeWithFilter;
	}

	public List<Long> getExcludeReleaseList() {
		return excludeReleaseList;
	}

	public void setExcludeReleaseList(List<Long> exclusionReleaseList) {
		this.excludeReleaseList = exclusionReleaseList;
	}

	public boolean isDesignNamespace() {
		return isDesignNamespace;
	}

	public void setDesignNamespace(boolean isDesignNamespace) {
		this.isDesignNamespace = isDesignNamespace;
	}

	public String getReleaseNs() {
		return releaseNs;
	}

	public void setReleaseNs(String releaseNs) {
		this.releaseNs = releaseNs;
	}

	public List<Long> getNsIdsMatchingFilter() {
		return nsIdsMatchingFilter;
	}

	public void setNsIdsMatchingFilter(List<Long> nsIdsMatchingFilter) {
		this.nsIdsMatchingFilter = nsIdsMatchingFilter;
	}

	public List<Long> getReleaseScopeNsIds() {
		return releaseScopeNsIds;
	}

	public void setReleaseScopeNsIds(List<Long> releaseScopeNsIds) {
		this.releaseScopeNsIds = releaseScopeNsIds;
	}

}
