package com.oneops.cms.ns.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsNamespace.
 */
public class CmsNamespace implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private long nsId;
	private String nsPath;
	private Date created;

	/**
	 * Gets the ns id.
	 *
	 * @return the ns id
	 */
	public long getNsId() {
		return nsId;
	}
	
	/**
	 * Sets the ns id.
	 *
	 * @param nsId the new ns id
	 */
	public void setNsId(long nsId) {
		this.nsId = nsId;
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
	
}
