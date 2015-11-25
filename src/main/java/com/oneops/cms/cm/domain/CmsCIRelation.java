package com.oneops.cms.cm.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsCIRelation.
 */
public class CmsCIRelation extends CmsCIRelationBasic implements Serializable {

	private static final long serialVersionUID = 1L;

	private int relationId;
	private int relationStateId;
	private long nsId;
	private CmsCI fromCi;
	private CmsCI toCi;
	private Map<String,CmsCIRelationAttribute> attributes = new HashMap<String,CmsCIRelationAttribute>();
	
	/**
	 * Gets the relation id.
	 *
	 * @return the relation id
	 */
	public int getRelationId() {
		return relationId;
	}
	
	/**
	 * Sets the relation id.
	 *
	 * @param relationId the new relation id
	 */
	public void setRelationId(int relationId) {
		this.relationId = relationId;
	}
	
	/**
	 * Gets the relation state id.
	 *
	 * @return the relation state id
	 */
	public int getRelationStateId() {
		return relationStateId;
	}
	
	/**
	 * Sets the relation state id.
	 *
	 * @param relationStateId the new relation state id
	 */
	public void setRelationStateId(int relationStateId) {
		this.relationStateId = relationStateId;
	}
	
	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, CmsCIRelationAttribute> getAttributes() {
		return attributes;
	}
	
	/**
	 * Gets the attribute.
	 *
	 * @param attrName the attr name
	 * @return the attribute
	 */
	public CmsCIRelationAttribute getAttribute(String attrName) {
		return attributes.get(attrName);
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attributes the attributes
	 */
	public void setAttributes(Map<String, CmsCIRelationAttribute> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Adds the attribute.
	 *
	 * @param attribute the attribute
	 */
	public void addAttribute(CmsCIRelationAttribute attribute) {
		this.attributes.put(attribute.getAttributeName(), attribute);
	}
	
	/**
	 * Gets the from ci.
	 *
	 * @return the from ci
	 */
	public CmsCI getFromCi() {
		return fromCi;
	}
	
	/**
	 * Sets the from ci.
	 *
	 * @param fromCi the new from ci
	 */
	public void setFromCi(CmsCI fromCi) {
		this.fromCi = fromCi;
	}
	
	/**
	 * Gets the to ci.
	 *
	 * @return the to ci
	 */
	public CmsCI getToCi() {
		return toCi;
	}
	
	/**
	 * Sets the to ci.
	 *
	 * @param toCi the new to ci
	 */
	public void setToCi(CmsCI toCi) {
		this.toCi = toCi;
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
	 * Gets the ns id.
	 *
	 * @return the ns id
	 */
	public long getNsId() {
		return nsId;
	}
	
}
