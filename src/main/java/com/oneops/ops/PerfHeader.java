package com.oneops.ops;

import java.util.Map;

import org.apache.log4j.Logger;

public class PerfHeader {
	
	//private static String DS_TYPE = "dstype";
	//private static String GAUGE = "gauge";
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PerfHeader.class);
	
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public int getRraCount() {
		return rraCount;
	}
	public void setRraCount(int rraCount) {
		this.rraCount = rraCount;
	}
	public long getUpdated() {
		return updated;
	}
	public void setUpdated(long updated) {
		this.updated = updated;
	}
	public Map<String, PerfDatasource> getDsMap() {
		return dsMap;
	}
	public void setDsMap(Map<String, PerfDatasource> dsMap) {
		this.dsMap = dsMap;
	}
	public Map<String, PerfArchive> getRraMap() {
		return rraMap;
	}
	public void setRraMap(Map<String, PerfArchive> rraMap) {
		this.rraMap = rraMap;
	}
	public Map<String, Double> getCdpMap() {
		return cdpMap;
	}
	public void setCdpMap(Map<String, Double> cdpMap) {
		this.cdpMap = cdpMap;
	}

	public Map<Long, Long> getZoneMap() {
		return zoneMap;
	}
	public void setZoneMap(Map<Long, Long> zoneMap) {
		this.zoneMap = zoneMap;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

	
	public String toLogString() {
		String dss = toLogStringDs();
		String rras = toLogStringRra();
		String cdps = toLogStringCdp();
		
		return "{updated:"+updated+" step:"+step+", rraCount:"+rraCount+ dss + rras + cdps + "}";
	}
	
	private String toLogStringDs() {
		String str = "";
		for (String key : dsMap.keySet() ) {
			PerfDatasource ds = dsMap.get(key);
			str += "{ds: "+key+" type:"+ds.getType()+ " hearbeat:"+ds.getHeartbeat()+ " pdp:"+ds.getPdp() +"}";
		}
		return str;
	}
	private String toLogStringRra() {
		String str = "";
		for (String key : rraMap.keySet() ) {
			PerfArchive rra = rraMap.get(key);
			str += rra.toLogString();
		}
		return str;
	}
	private String toLogStringCdp() {
		String str = "";
		for (String key : cdpMap.keySet() ) {
			Double val = cdpMap.get(key);
			str += "{cdp: "+key+":"+ val +"}";
		}
		return str;
	}		
	
	
	private int step;
	private int rraCount;
	private long updated;
	private String ip;
	private Map<String,PerfDatasource> dsMap;
	private Map<String,PerfArchive> rraMap;
	private Map<String,Double> cdpMap;
	private Map<Long,Long> zoneMap;

}
