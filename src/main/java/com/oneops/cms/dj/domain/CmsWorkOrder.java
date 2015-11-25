package com.oneops.cms.dj.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.domain.CmsWorkOrderBase;

/**
 * The Class CmsWorkOrder.
 */
public class CmsWorkOrder extends CmsDpmtRecord implements CmsWorkOrderBase {
	private static final long serialVersionUID = 1L;

	private CmsRfcCI rfcCi;
	private CmsCI cloud;
	private CmsCI resultCi;
	private CmsCI box;
	private Map<String, List<CmsRfcCI>> payLoad;
	private Map<String,Map<String, CmsCI>> services;
	
	/**
	 * Gets the cloud.
	 *
	 * @return the cloud
	 */
	public CmsCI getCloud() {
		return cloud;
	}
	
	/**
	 * Sets the cloud.
	 *
	 * @param cloud the new cloud
	 */
	public void setCloud(CmsCI cloud) {
		this.cloud = cloud;
	}
	
	/**
	 * Gets the services.
	 *
	 * @return the services
	 */
	public Map<String, Map<String, CmsCI>> getServices() {
		return services;
	}
	
	/**
	 * Sets the services.
	 *
	 * @param services the services
	 */
	public void setServices(Map<String, Map<String, CmsCI>> services) {
		this.services = services;
	}
	
	/**
	 * Gets the box.
	 *
	 * @return the box
	 */
	public CmsCI getBox() {
		return box;
	}
	
	/**
	 * Sets the box.
	 *
	 * @param box the new box
	 */
	public void setBox(CmsCI box) {
		this.box = box;
	}
	
	/**
	 * Gets the result ci.
	 *
	 * @return the result ci
	 */
	public CmsCI getResultCi() {
		return resultCi;
	}
	
	/**
	 * Sets the result ci.
	 *
	 * @param resultCi the new result ci
	 */
	public void setResultCi(CmsCI resultCi) {
		this.resultCi = resultCi;
	}
	
	/**
	 * Gets the pay load.
	 *
	 * @return the pay load
	 */
	public Map<String, List<CmsRfcCI>> getPayLoad() {
		return payLoad;
	}
	
	/**
	 * Sets the pay load.
	 *
	 * @param payLoad the pay load
	 */
	public void setPayLoad(Map<String, List<CmsRfcCI>> payLoad) {
		this.payLoad = payLoad;
	}
	
	/**
	 * Put pay load entry.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void putPayLoadEntry(String key, List<CmsRfcCI> value) {
		if (value != null) {
			if (this.payLoad == null) {
				this.payLoad = new HashMap<String, List<CmsRfcCI>>();
			}
			this.payLoad.put(key, value);
		}
	}

	/**
	 * Adds the pay load entry.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addPayLoadEntry(String key, CmsRfcCI value) {
		if (value != null) {
			if (this.payLoad == null) {
				this.payLoad = new HashMap<String, List<CmsRfcCI>>();
				this.payLoad.put(key, new ArrayList<CmsRfcCI>());
			}
			this.payLoad.get(key).add(value);
		}
	}
	
	/**
	 * Gets the rfc ci.
	 *
	 * @return the rfc ci
	 */
	public CmsRfcCI getRfcCi() {
		return rfcCi;
	}
	
	/**
	 * Sets the rfc ci.
	 *
	 * @param rfcCi the new rfc ci
	 */
	public void setRfcCi(CmsRfcCI rfcCi) {
		this.rfcCi = rfcCi;
	}
}
