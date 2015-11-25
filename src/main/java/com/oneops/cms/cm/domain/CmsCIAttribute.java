package com.oneops.cms.cm.domain;

import java.io.Serializable;

/**
 * The Class CmsCIAttribute.
 */
public class CmsCIAttribute extends CmsBasicAttribute implements Serializable {

	private static final long serialVersionUID = 1L;

	private long ciAttributeId;
	private long ciId;

	/**
	 * Gets the ci attribute id.
	 *
	 * @return the ci attribute id
	 */
	public long getCiAttributeId() {
		return ciAttributeId;
	}
	
	/**
	 * Sets the ci attribute id.
	 *
	 * @param ciAttributeId the new ci attribute id
	 */
	public void setCiAttributeId(long ciAttributeId) {
		this.ciAttributeId = ciAttributeId;
	}
	
	/**
	 * Gets the ci id.
	 *
	 * @return the ci id
	 */
	public long getCiId() {
		return ciId;
	}
	
	/**
	 * Sets the ci id.
	 *
	 * @param ciId the new ci id
	 */
	public void setCiId(long ciId) {
		this.ciId = ciId;
	}

}
