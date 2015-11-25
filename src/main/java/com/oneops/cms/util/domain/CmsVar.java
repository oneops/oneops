package com.oneops.cms.util.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * CmsVar class : Will be used to store util configuration data 
 *
 */
public class CmsVar implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;			
	private String name;			
	private String value;			
	private Date created;
	private Date updated;
	
	/**
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * 
	 * @param vale
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * 
	 * @return
	 */
	public Date getCreated() {
		return created;
	}
	
	/**
	 * 
	 * @param created
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
	
	/**
	 * 
	 * @return
	 */
	public Date getUpdated() {
		return updated;
	}
	
	/**
	 * 
	 * @param updated
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	
}
