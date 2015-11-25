package com.oneops.cms.dj.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsDpmtRecord.
 */
public class CmsDpmtRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	private long dpmtRecordId;
	private long deploymentId;
	private long rfcId;
	private String dpmtRecordState;
	private String comments;
	private String ops;
	private Date created;
	private Date updated;
	
	/**
	 * Gets the dpmt record id.
	 *
	 * @return the dpmt record id
	 */
	public long getDpmtRecordId() {
		return dpmtRecordId;
	}
	
	/**
	 * Sets the dpmt record id.
	 *
	 * @param dpmtRecordId the new dpmt record id
	 */
	public void setDpmtRecordId(long dpmtRecordId) {
		this.dpmtRecordId = dpmtRecordId;
	}
	
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
	 * Gets the rfc id.
	 *
	 * @return the rfc id
	 */
	public long getRfcId() {
		return rfcId;
	}
	
	/**
	 * Sets the rfc id.
	 *
	 * @param rfcId the new rfc id
	 */
	public void setRfcId(long rfcId) {
		this.rfcId = rfcId;
	}
	
	/**
	 * Gets the dpmt record state.
	 *
	 * @return the dpmt record state
	 */
	public String getDpmtRecordState() {
		return dpmtRecordState;
	}
	
	/**
	 * Sets the dpmt record state.
	 *
	 * @param dpmtRecordState the new dpmt record state
	 */
	public void setDpmtRecordState(String dpmtRecordState) {
		this.dpmtRecordState = dpmtRecordState;
	}
	
	/**
	 * Gets the comments.
	 *
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	
	/**
	 * Sets the comments.
	 *
	 * @param comments the new comments
	 */
	public void setComments(String comments) {
		this.comments = comments;
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

	public String getOps() {
		return ops;
	}

	public void setOps(String ops) {
		this.ops = ops;
	}
	
}
