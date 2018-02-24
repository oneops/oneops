package com.oneops.crawler.plugins.hadr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.oneops.Cloud;
import com.oneops.Organization;

public class PlatformHADRRecord implements Serializable {

	private static final long serialVersionUID = 1L;
	private int totalCores;
	private int totalComputes;
	private String nsPath;
	private String platform;
	private String ooUrl;
	private String assembly;
	private String sClouds;
	private Date createdTS;
	private String env;
	private String pack;
	private String org;
	private String packVersion;
	private boolean isDR;
	private String source;
	private String sourcePack;
	private boolean isHA;

	private List<String> activeClouds= new ArrayList<String>();
	private List<String> offlineClouds= new ArrayList<String>();
	private List<String> primaryClouds= new ArrayList<String>();
	private List<String> secondaryClouds= new ArrayList<String>();
	private transient Map<String,Cloud> cloudsMap;
	private List<Cloud> clouds;
	private Organization	organization;
	


	public String getNsPath() {
		return nsPath;
	}

	public void setNsPath(String nsPath) {
		this.nsPath = nsPath;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getOoUrl() {
		return ooUrl;
	}

	public void setOoUrl(String ooUrl) {
		this.ooUrl = ooUrl;
	}

	public String getAssembly() {
		return assembly;
	}

	public void setAssembly(String assembly) {
		this.assembly = assembly;
	}

	public String getSClouds() {
		return sClouds;
	}

	public void setSClouds(String sClouds) {
		this.sClouds = sClouds;
	}

	public Date getCreatedTS() {
		return createdTS;
	}

	public void setCreatedTS(Date createdTS) {
		this.createdTS = createdTS;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getPack() {
		return pack;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getPackVersion() {
		return packVersion;
	}

	public void setPackVersion(String packVersion) {
		this.packVersion = packVersion;
	}

	public boolean getIsDR() {
		return isDR;
	}

	public void setIsDR(boolean isDR) {
		this.isDR = isDR;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourcePack() {
		return sourcePack;
	}

	public void setSourcePack(String sourcePack) {
		this.sourcePack = sourcePack;
	}

	public boolean getIsHA() {
		return isHA;
	}

	public void setIsHA(boolean isHA) {
		this.isHA = isHA;
	}

	public int getTotalCores() {
		return totalCores;
	}

	public void setTotalCores(int totalCores) {
		this.totalCores = totalCores;
	}

	public int getTotalComputes() {
		return totalComputes;
	}

	public void setTotalComputes(int totalComputes) {
		this.totalComputes = totalComputes;
	}



	public List<String> getActiveClouds() {
		return activeClouds;
	}

	public void setActiveClouds(List<String> activeClouds) {
		this.activeClouds = activeClouds;
	}

	public List<String> getPrimaryClouds() {
		return primaryClouds;
	}

	public void setPrimaryClouds(List<String> primaryClouds) {
		this.primaryClouds = primaryClouds;
	}

	public List<String> getSecondaryClouds() {
		return secondaryClouds;
	}

	public void setSecondaryClouds(List<String> secondaryClouds) {
		this.secondaryClouds = secondaryClouds;
	}

	public List<String> getOfflineClouds() {
		return offlineClouds;
	}

	public void setOfflineClouds(List<String> offlineClouds) {
		this.offlineClouds = offlineClouds;
	}

	public Map<String, Cloud> getCloudsMap() {
		return cloudsMap;
	}

	public void setCloudsMap(Map<String, Cloud> cloudsMap) {
		this.cloudsMap = cloudsMap;
	}

	public List<Cloud> getClouds() {
		return clouds;
	}

	public void setClouds(List<Cloud> clouds) {
		this.clouds = clouds;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	
}