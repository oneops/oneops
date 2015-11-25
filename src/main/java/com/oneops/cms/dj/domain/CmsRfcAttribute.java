package com.oneops.cms.dj.domain;

import java.io.Serializable;

/**
 * The Class CmsRfcAttribute.
 */
public class CmsRfcAttribute extends CmsRfcBasicAttribute implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private long rfcAttributeId;
	private long rfcId;

	/**
	 * Gets the rfc attribute id.
	 *
	 * @return the rfc attribute id
	 */
	public long getRfcAttributeId() {
		return rfcAttributeId;
	}
	
	/**
	 * Sets the rfc attribute id.
	 *
	 * @param rfcAttributeId the new rfc attribute id
	 */
	public void setRfcAttributeId(long rfcAttributeId) {
		this.rfcAttributeId = rfcAttributeId;
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
	
}
