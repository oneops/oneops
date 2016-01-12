/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.amq.plugins;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.oneops.cms.simple.domain.CmsCISimple;

/**
 * The Class CMSClient.
 */
public class CMSClient {
	private static Logger logger = Logger.getLogger(CMSClient.class);
	
	private RestTemplate restTemplate;
    private String serviceUrl; // = "http://localhost:8080/adapter/rest/";
    private final String zoneClass = "account.provider.Zone"; 
    private final String mgmtCloud = "mgmt.Cloud";
    private final String acctCloud = "account.Cloud";
    
	/**
	 * Sets the rest template.
	 *
	 * @param restTemplate the new rest template
	 */
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * Sets the service url.
	 *
	 * @param serviceUrl the new service url
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * _get zone ci.
	 *
	 * @param ns the ns
	 * @param ciName the ci name
	 * @return the cms ci simple
	 */
	public CmsCISimple _getZoneCi(String ns, String ciName) {
		
		try {
			CmsCISimple[] cis = restTemplate.getForObject(serviceUrl + "cm/simple/cis?nsPath={nsPath}&ciClassName={zoneClass}&ciName={ciName}", new CmsCISimple[0].getClass(), ns, zoneClass, ciName);
			if (cis.length > 0) {
				return cis[0];
			};
			return null;
		} catch (RestClientException ce) {
			logger.error("Broker can not connect to cms api to authenticate the user:" + ce.getMessage());
			throw ce;
		}
	}

	/**
	 * Gets the cloud ci.
	 *
	 * @param ns the ns
	 * @param ciName the ci name
	 * @return the cloud ci
	 */
	public CmsCISimple getCloudCi(String ns, String ciName) {
		
		try {
			CmsCISimple[] mgmtClouds = restTemplate.getForObject(serviceUrl + "cm/simple/cis?nsPath={nsPath}&ciClassName={mgmtCloud}&ciName={ciName}", new CmsCISimple[0].getClass(), ns, mgmtCloud, ciName);
			if (mgmtClouds.length > 0) {
				return mgmtClouds[0];
			} 
			CmsCISimple[] acctClouds = restTemplate.getForObject(serviceUrl + "cm/simple/cis?nsPath={nsPath}&ciClassName={acctCloud}&ciName={ciName}", new CmsCISimple[0].getClass(), ns, acctCloud, ciName);
			if (acctClouds.length > 0) {
				return acctClouds[0];
			} 
			
			return null;
		} catch (RestClientException ce) {
			logger.error("Broker can not connect to cms api to authenticate the user:" + ce.getMessage());
			throw ce;
		}
	}
	
}
