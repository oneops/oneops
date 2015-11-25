package com.oneops.cms.cm.domain;

import java.io.Serializable;

/**
 * The Class CmsCIRelationAttribute.
 */
public class CmsCIRelationAttribute extends CmsBasicAttribute implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long ciRelationAttributeId;
	private long ciRelationId;

	/**
	 * Gets the ci relation attribute id.
	 *
	 * @return the ci relation attribute id
	 */
	public long getCiRelationAttributeId() {
		return ciRelationAttributeId;
	}
	
	/**
	 * Sets the ci relation attribute id.
	 *
	 * @param ciRelationAttributeId the new ci relation attribute id
	 */
	public void setCiRelationAttributeId(long ciRelationAttributeId) {
		this.ciRelationAttributeId = ciRelationAttributeId;
	}
	
	/**
	 * Gets the ci relation id.
	 *
	 * @return the ci relation id
	 */
	public long getCiRelationId() {
		return ciRelationId;
	}
	
	/**
	 * Sets the ci relation id.
	 *
	 * @param ciRelationId the new ci relation id
	 */
	public void setCiRelationId(long ciRelationId) {
		this.ciRelationId = ciRelationId;
	}
	
}
