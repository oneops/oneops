package com.oneops.cms.simple.domain;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.oneops.cms.cm.domain.CmsCIRelationBasic;

/**
 * The Class CmsCIRelationSimple.
 */
public class CmsCIRelationSimple extends CmsCIRelationBasic implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String,String> relationAttributes = new HashMap<String,String>();
	
	private CmsCISimple fromCi;
	private CmsCISimple toCi;
	
	
	/**
	 * Gets the relation attributes.
	 *
	 * @return the relation attributes
	 */
	public Map<String, String> getRelationAttributes() {
		return relationAttributes;
	}
	
	/**
	 * Sets the relation attributes.
	 *
	 * @param relationAttributes the relation attributes
	 */
	public void setRelationAttributes(Map<String, String> relationAttributes) {
		this.relationAttributes = relationAttributes;
	}
	
	/**
	 * Adds the relation attribute.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addRelationAttribute(String key, String value) {
		this.relationAttributes.put(key, value);
	}

	/**
	 * Gets the from ci.
	 *
	 * @return the from ci
	 */
	public CmsCISimple getFromCi() {
		return fromCi;
	}
	
	/**
	 * Sets the from ci.
	 *
	 * @param fromCi the new from ci
	 */
	public void setFromCi(CmsCISimple fromCi) {
		this.fromCi = fromCi;
	}
	
	/**
	 * Gets the to ci.
	 *
	 * @return the to ci
	 */
	public CmsCISimple getToCi() {
		return toCi;
	}
	
	/**
	 * Sets the to ci.
	 *
	 * @param toCi the new to ci
	 */
	public void setToCi(CmsCISimple toCi) {
		this.toCi = toCi;
	}
	
}
