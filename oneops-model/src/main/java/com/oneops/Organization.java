package com.oneops;

public class Organization {

	private String description;
	private String owner;
	private String full_name;
	private OrganizationTags tags;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public OrganizationTags getTags() {
		return tags;
	}

	public void setTags(OrganizationTags tags) {
		this.tags = tags;
	}

}
