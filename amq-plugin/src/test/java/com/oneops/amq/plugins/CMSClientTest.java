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

import com.oneops.cms.simple.domain.CmsCISimple;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.mockito.Mockito.*;

public class CMSClientTest {

	private final CMSClient cmsClient = new CMSClient();
	//this is used in the setter, but we never do call out to http
	private final String SERVICE_URL = "http://localhost:8080/adapter/rest/";

	private final String CI_NAME = "foo";
	private final String NS_NAME = "bar";
	private final String KEY = "ci-key";
	private final String VALUE = "ci-value";

	private RestTemplate restTemplateThrows;


	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUp(){

		//a mocked rest template used when we need simulated exception response
		//it is used two times so lets init it here once
		restTemplateThrows=mock(RestTemplate.class);
		when(restTemplateThrows.getForObject(anyString(), any(Class.class),anyString(),anyString(),anyString())).thenThrow(new RestClientException(this.getClass().getName()));

		this.cmsClient.setServiceUrl(SERVICE_URL);

	}

	@Test(priority=1)
	/**
	 * first test to run. it runs the setter, and a constructor for kicks
	 */
	public void initializationTests(){
		this.cmsClient.setServiceUrl(SERVICE_URL);

		CmsAuthException tryConstructorResult = new CmsAuthException();
		Assert.assertNotNull(tryConstructorResult);

		CmsAuthException e = new CmsAuthException();
		Assert.assertNotNull(e);

	}

	@SuppressWarnings("unchecked")
	@Test(priority=2)

	/**
	 * test the getZone API , the rest template mock will give enough to be not null
	 */
	public void getZoneTest(){

		CmsCISimple cmsCISimple = new CmsCISimple();
		cmsCISimple.addCiAttribute(KEY, VALUE);

		CmsCISimple[] cisWith1 = new CmsCISimple[1];
		cisWith1[0]=cmsCISimple;

		RestTemplate restTemplateMockGives1=mock(RestTemplate.class);
		when(restTemplateMockGives1.getForObject(anyString(), any(Class.class), anyString(),anyString(),anyString() )).thenReturn(cisWith1);

		this.cmsClient.setRestTemplate(restTemplateMockGives1);

		CmsCISimple cis = this.cmsClient._getZoneCi(NS_NAME, CI_NAME);
		Assert.assertNotNull(cis);
	}

	@SuppressWarnings("unchecked")
	@Test(priority=3)
	/*
	 * tests getCloud method where a special rest client here will return 2
	 */
	public void getCloudTest(){
		CmsCISimple cmsCISimple = new CmsCISimple();
		cmsCISimple.addCiAttribute(KEY, VALUE);

		CmsCISimple[] cisWith2 = new CmsCISimple[2];
		cisWith2[0]=cmsCISimple;
		cisWith2[1]=cmsCISimple;

		RestTemplate restTemplateMockGives2=mock(RestTemplate.class);

		when(restTemplateMockGives2.getForObject(anyString(), any(Class.class), anyString(),anyString(),anyString() )).thenReturn(cisWith2);

		//and call again where res will have 2 mgmtClouds
		this.cmsClient.setRestTemplate(restTemplateMockGives2);

		this.cmsClient.getCloudCi(NS_NAME, CI_NAME);

	}

	@Test(priority=4,expectedExceptions = RestClientException.class)
	/** test to make sure we handle exception from rest client */
	public void getZoneTestRestException() {
		// and call again where bad restClient
		this.cmsClient.setRestTemplate(restTemplateThrows);
		this.cmsClient._getZoneCi(NS_NAME, CI_NAME);

	}

	@Test(priority=4,expectedExceptions = RestClientException.class)
	/** test to make sure we handle exception from rest client */
	public void getCloudTestRestException() {

		// and call again where bad restClient
		this.cmsClient.setRestTemplate(restTemplateThrows);
		this.cmsClient.getCloudCi(NS_NAME, CI_NAME);

	}

}
