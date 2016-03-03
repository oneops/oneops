package com.oneops.transistor.export.domain;

import java.util.HashMap;
import java.util.Map;

public class ExportCi {
	private String name;
	private String type;
	private String comments;
	private String template;
	private Map<String,String> attributes;
	
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getName() {
		return name;
	}
	public void setName(String ciName) {
		this.name = ciName;
	}
	public String getType() {
		return type;
	}
	public void setType(String ciClassName) {
		this.type = ciClassName;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(String name, String value) {
		if (this.attributes == null) {
			this.attributes = new HashMap<String,String>();
		}
		this.attributes.put(name, value);
	}
	
}
