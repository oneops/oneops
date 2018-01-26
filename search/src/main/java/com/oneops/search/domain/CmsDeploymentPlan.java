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
package com.oneops.search.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "cms")
public class CmsDeploymentPlan implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String nsPath;
	private String createdBy;
	private long ciId;
	private long releaseId;
	private long ciRfcCount;
	private long relationRfcCount;
	private double planGenerationTime;
	private String mode;
	private Boolean manifestCommit;
	private Boolean autoDeploy;

	private Date created;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNsPath() {
		return nsPath;
	}

	public void setNsPath(String nsPath) {
		this.nsPath = nsPath;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getCiId() {
		return ciId;
	}

	public void setCiId(long ciId) {
		this.ciId = ciId;
	}

	public long getReleaseId() {
		return releaseId;
	}

	public void setReleaseId(long releaseId) {
		this.releaseId = releaseId;
	}

	public long getCiRfcCount() {
		return ciRfcCount;
	}

	public void setCiRfcCount(long ciRfcCount) {
		this.ciRfcCount = ciRfcCount;
	}

	public long getRelationRfcCount() {
		return relationRfcCount;
	}

	public void setRelationRfcCount(long relationRfcCount) {
		this.relationRfcCount = relationRfcCount;
	}

	public double getPlanGenerationTime() {
		return planGenerationTime;
	}

	public void setPlanGenerationTime(double planGenerationTime) {
		this.planGenerationTime = planGenerationTime;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Boolean getManifestCommit() {
		return manifestCommit;
	}

	public void setManifestCommit(Boolean manifestCommit) {
		this.manifestCommit = manifestCommit;
	}

	public Boolean getAutoDeploy() {
		return autoDeploy;
	}

	public void setAutoDeploy(Boolean autoDeploy) {
		this.autoDeploy = autoDeploy;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
}
