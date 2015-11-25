package com.oneops.cms.dj.domain;

import java.io.Serializable;

/**
 * The Class CmsRelease.
 */
public class CmsRelease extends CmsReleaseBasic implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long nsId;
	private int releaseStateId;
	private long ciRfcCount;
	private long relationRfcCount;
	
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
	 * Gets the release state id.
	 *
	 * @return the release state id
	 */
	public int getReleaseStateId() {
		return releaseStateId;
	}
	
	/**
	 * Sets the release state id.
	 *
	 * @param releaseStateId the new release state id
	 */
	public void setReleaseStateId(int releaseStateId) {
		this.releaseStateId = releaseStateId;
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

}
