package com.oneops.transistor.export.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DesignExportSimple {
	private String name;
	private String description;
	private Map<String,String> variables;
	private List<PlatformExport> platforms;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String,String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String,String> globalVars) {
		this.variables = globalVars;
	}

	public void addVariable(String name, String value) {
		if (this.variables == null) {
			this.variables = new HashMap<String,String>();
		}
		this.variables.put(name, value);
	}
	
	
	public List<PlatformExport> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<PlatformExport> platforms) {
		this.platforms = platforms;
	}
	
	public void addPlatformExport(PlatformExport platform) {
		if (this.platforms == null) {
			this.platforms = new ArrayList<PlatformExport>();
		}
		this.platforms.add(platform);
	}
	
}

