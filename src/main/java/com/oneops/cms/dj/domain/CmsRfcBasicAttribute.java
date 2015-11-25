package com.oneops.cms.dj.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsRfcBasicAttribute.
 */
public class CmsRfcBasicAttribute implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int attributeId;
	private String attributeName;
	private String oldValue;
	private String newValue;
	private String comments;
	private String owner;
	private Date created;

	/**
	 * Gets the attribute id.
	 *
	 * @return the attribute id
	 */
	public int getAttributeId() {
		return attributeId;
	}
	
	/**
	 * Sets the attribute id.
	 *
	 * @param attributeId the new attribute id
	 */
	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}
	
	/**
	 * Gets the attribute name.
	 *
	 * @return the attribute name
	 */
	public String getAttributeName() {
		return attributeName;
	}
	
	/**
	 * Sets the attribute name.
	 *
	 * @param attributeName the new attribute name
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	/**
	 * Gets the old value.
	 *
	 * @return the old value
	 */
	public String getOldValue() {
		return oldValue;
	}
	
	/**
	 * Sets the old value.
	 *
	 * @param oldValue the new old value
	 */
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	
	/**
	 * Gets the new value.
	 *
	 * @return the new value
	 */
	public String getNewValue() {
		return newValue;
	}
	
	/**
	 * Sets the new value.
	 *
	 * @param newValue the new new value
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
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
	 * Sets the owner.
	 *
	 * @param owner the new owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * Gets the owner.
	 *
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

}
