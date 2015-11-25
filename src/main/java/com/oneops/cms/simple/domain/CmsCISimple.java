package com.oneops.cms.simple.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.oneops.cms.cm.domain.CmsCIBasic;

/**
 * The Class CmsCISimple.
 */
public class CmsCISimple extends CmsCIBasic implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private long nsId;
	private Map<String,String> ciAttributes = new HashMap<String,String>();
	private Map<String,Map<String,String>> attrProps = new HashMap<String,Map<String,String>>();
	
	
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
	 * Gets the ci attributes.
	 *
	 * @return the ci attributes
	 */
	public Map<String, String> getCiAttributes() {
		return ciAttributes;
	}
	
	/**
	 * Sets the ci attributes.
	 *
	 * @param ciAttributes the ci attributes
	 */
	public void setCiAttributes(Map<String, String> ciAttributes) {
		this.ciAttributes = ciAttributes;
	}
	
	/**
	 * Adds the ci attribute.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addCiAttribute(String key, String value) {
		this.ciAttributes.put(key, value);
	}

	/**
	 * @return the attrProps
	 */
	public Map<String, Map<String, String>> getAttrProps() {
		return attrProps;
	}

	/**
	 * @param attrProps the attrProps to set
	 */
	public void setAttrProps(Map<String, Map<String, String>> attrProps) {
		this.attrProps = attrProps;
	}
	
	/**
	 * 
	 */
	/**
	 * Adds the CI attribute property.
	 *
	 * @param attrName the attribute name
	 *  @param propName the prop name
	 * @param value the value
	 */
	public void addAttrProps(String propName, String attrName, String value) {
		if (attrProps.get(propName) == null) {
			attrProps.put(propName, new HashMap<String,String>());
		}
		if (attrName != null && value != null) {
			attrProps.get(propName).put(attrName, value);
		}
	}

}
