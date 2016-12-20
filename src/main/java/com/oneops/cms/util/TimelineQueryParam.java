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
	
	private String envNs;
	private String manifestNsLike;
	private String bomNsLike;
	private String type;
	
	private String filter;
	private String wildcardFilter;
	private String manifestClassFilter;
	private String bomClassFilter;
	private String manifestNsLikeWithFilter;
	private String bomNsLikeWithFilter;

	private Long dpmtOffset;
	private Long releaseOffset;
	private Long endRelId;
	private List<Long> releaseList;
	
	private List<Long> excludeReleaseList;
	
	private QueryOrder order;
	private Integer limit;

	public TimelineQueryParam(String envNs, String filter, String type, QueryOrder order, Long releaseOffset, Long dpmtOffset, Integer limit) {
		this.envNs = envNs;
		this.order = order;
		this.type = type;
		this.filter = filter;
		this.dpmtOffset = dpmtOffset;
		this.releaseOffset = releaseOffset;
		this.limit = limit;
	}

	public String getBomNsLike() {
		return bomNsLike;
	}

	public void setBomNsLike(String envNsLike) {
		this.bomNsLike = envNsLike;
	}

	public String getWildcardFilter() {
		return wildcardFilter;
	}

	public void setWildcardFilter(String ciFilter) {
		this.wildcardFilter = ciFilter;
	}

	public String getManifestNsLike() {
		return manifestNsLike;
	}

	public void setManifestNsLike(String manifestNsLike) {
		this.manifestNsLike = manifestNsLike;
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

	public String getEnvNs() {
		return envNs;
	}

	public void setEnvNs(String envNs) {
		this.envNs = envNs;
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

	public String getManifestClassFilter() {
		return manifestClassFilter;
	}

	public void setManifestClassFilter(String manifestClassFilter) {
		this.manifestClassFilter = manifestClassFilter;
	}

	public String getBomClassFilter() {
		return bomClassFilter;
	}

	public void setBomClassFilter(String bomClassFilter) {
		this.bomClassFilter = bomClassFilter;
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

	public String getManifestNsLikeWithFilter() {
		return manifestNsLikeWithFilter;
	}

	public void setManifestNsLikeWithFilter(String manifestNsFilter) {
		this.manifestNsLikeWithFilter = manifestNsFilter;
	}

	public String getBomNsLikeWithFilter() {
		return bomNsLikeWithFilter;
	}

	public void setBomNsLikeWithFilter(String bomNsFilter) {
		this.bomNsLikeWithFilter = bomNsFilter;
	}

	public List<Long> getExcludeReleaseList() {
		return excludeReleaseList;
	}

	public void setExcludeReleaseList(List<Long> exclusionReleaseList) {
		this.excludeReleaseList = exclusionReleaseList;
	}

}
