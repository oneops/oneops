package com.oneops.transistor.export.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DesignExportSimple {
	private Map<String,String> variables;
	private List<PlatformExport> platforms;

	public Map<String,String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String,String> globalVars) {
		this.variables = globalVars;
	}

	public void addVariable(String name, String value) {
		if (value!=null) {
			if (this.variables == null) {
				this.variables = new HashMap<>();
			}
			this.variables.put(name, value);
		}
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

