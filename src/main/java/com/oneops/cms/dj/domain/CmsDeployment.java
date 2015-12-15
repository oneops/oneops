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
package com.oneops.cms.dj.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsDeployment.
 */
public class CmsDeployment implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private long deploymentId;
	private long releaseId;
	private int maxExecOrder;
	private String nsPath;
	private String deploymentState;
	private String processId;
	private String createdBy;
	private String updatedBy;
	private String description;
	private String comments;
	private String ops;
	private Date created;
	private Date updated;

	/**
	 * Gets the deployment id.
	 *
	 * @return the deployment id
	 */
	public long getDeploymentId() {
		return deploymentId;
	}
	
	/**
	 * Sets the deployment id.
	 *
	 * @param deploymentId the new deployment id
	 */
	public void setDeploymentId(long deploymentId) {
		this.deploymentId = deploymentId;
	}
	
	/**
	 * Gets the release id.
	 *
	 * @return the release id
	 */
	public long getReleaseId() {
		return releaseId;
	}
	
	/**
	 * Sets the release id.
	 *
	 * @param releaseId the new release id
	 */
	public void setReleaseId(long releaseId) {
		this.releaseId = releaseId;
	}
	
	/**
	 * Gets the max exec order.
	 *
	 * @return the max exec order
	 */
	public int getMaxExecOrder() {
		return maxExecOrder;
	}
	
	/**
	 * Sets the max exec order.
	 *
	 * @param maxExecOrder the new max exec order
	 */
	public void setMaxExecOrder(int maxExecOrder) {
		this.maxExecOrder = maxExecOrder;
	}
	
	/**
	 * Gets the ns path.
	 *
	 * @return the ns path
	 */
	public String getNsPath() {
		return nsPath;
	}
	
	/**
	 * Sets the ns path.
	 *
	 * @param nsPath the new ns path
	 */
	public void setNsPath(String nsPath) {
		this.nsPath = nsPath;
	}
	
	/**
	 * Gets the deployment state.
	 *
	 * @return the deployment state
	 */
	public String getDeploymentState() {
		return deploymentState;
	}
	
	/**
	 * Sets the deployment state.
	 *
	 * @param deploymentState the new deployment state
	 */
	public void setDeploymentState(String deploymentState) {
		this.deploymentState = deploymentState;
	}
	
	/**
	 * Gets the created by.
	 *
	 * @return the created by
	 */
	public String getCreatedBy() {
		return createdBy;
	}
	
	/**
	 * Sets the created by.
	 *
	 * @param createdBy the new created by
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	/**
	 * Gets the updated by.
	 *
	 * @return the updated by
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}
	
	/**
	 * Sets the updated by.
	 *
	 * @param updatedBy the new updated by
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the created.
	 *
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}
	
	/**
	 * Sets the created.
	 *
	 * @param created the new created
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
	
	/**
	 * Gets the updated.
	 *
	 * @return the updated
	 */
	public Date getUpdated() {
		return updated;
	}
	
	/**
	 * Sets the updated.
	 *
	 * @param updated the new updated
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
	/**
	 * Gets the process id.
	 *
	 * @return the process id
	 */
	public String getProcessId() {
		return processId;
	}
	
	/**
	 * Sets the process id.
	 *
	 * @param processId the new process id
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getOps() {
		return ops;
	}

	public void setOps(String ops) {
		this.ops = ops;
	}
	
}
