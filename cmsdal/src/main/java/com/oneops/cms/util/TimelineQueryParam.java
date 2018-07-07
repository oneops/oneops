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
	private String releaseNs;
	private String dpmtNs;
	private boolean isDesignNamespace;

	private String type;
	
	private String filter;
	private String wildcardFilter;
	private String rfcNsFilter;

	private Long dpmtOffset;
	private Long releaseOffset;
	private Long endRelId;

	private List<Long> excludeReleaseList;
	
	private QueryOrder order;
	private Integer limit;

	public TimelineQueryParam(String nsPath, String filter, String type, QueryOrder order, Long releaseOffset, Long dpmtOffset, Integer limit) {
		this.nsPath = nsPath;
		this.order = order;
		this.type = type;
		this.filter = filter;
		this.dpmtOffset = dpmtOffset;
		this.releaseOffset = releaseOffset;
		this.limit = limit;

		this.isDesignNamespace = this.nsPath.contains("/_design");
		this.dpmtNs = CmsUtil.appendToNs(this.nsPath, CmsConstants.BOM);
		this.releaseNs = isDesignNamespace() ?
				nsPath.substring(0, nsPath.indexOf("/_design")) :
				CmsUtil.appendToNs(nsPath, CmsConstants.MANIFEST);
	}

	public String getWildcardFilter() {
		return wildcardFilter;
	}

	public void setWildcardFilter(String ciFilter) {
		this.wildcardFilter = ciFilter;
	}

	public Long getEndRelId() {
		return endRelId;
	}

	public void setEndRelId(Long endRelId) {
		this.endRelId = endRelId;
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

	public String getRfcNsFilter() {
		return rfcNsFilter;
	}

	public void setRfcNsFilter(String rfcNsFilter) {
		this.rfcNsFilter = rfcNsFilter;
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

	public String getDpmtNs() {
		return dpmtNs;
	}

	public void setDpmtNs(String dpmtNs) {
		this.dpmtNs = dpmtNs;
	}
}
