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
package com.oneops.cms.util;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.*;
import org.testng.log4testng.Logger;

import static org.testng.Assert.*;


import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.simple.domain.CmsCISimple;


public class CmsUtilTest {


	private CmsUtil util = new CmsUtil();
	CmsCISimple ciSimple = new CmsCISimple();
	private Map<String, String> ciAttributes;

	private static final String CLASS_NAME = null;
	private static final long CI_ID = 100;
	private static final String CI_NAME = "Source";
	private static final String NS_PATH = "/public/main";
	private static final String CI_IMPL = "java.lang";
	private static final String DE_FACTO = "df";
	private static final String DE_JURE = "dj";
	private static final Logger logger = Logger.getLogger(CmsUtilTest.class);

	public CmsUtilTest() {
		this.ciAttributes = new HashMap<String, String>(3);
		this.ciAttributes.put("A", "1");
		this.ciAttributes.put("B", "2");
		this.ciAttributes.put("Z", "26");

		this.ciSimple.setCiClassName(CLASS_NAME);
		this.ciSimple.setCiId(CI_ID);
		this.ciSimple.setCiName(CI_NAME);
		this.ciSimple.setCreated(new Date());
		this.ciSimple.setImpl(CI_IMPL);
		this.ciSimple.setNsPath(NS_PATH);
		this.ciSimple.setCiAttributes(ciAttributes);
	}


	//TODO	@Test
	public void custCiSimpleToCi() {

		CmsCI ci = util.custCISimple2CI(this.ciSimple, DE_FACTO);
	}


	private void dumpCmsCIAttributes(CmsCI ci) {
		for (CmsCIAttribute manifestAttr : ci.getAttributes().values()) {
			System.out.println("~Ci_Dj :" + manifestAttr.getDjValue());
		}
	}

	private void dumpMaps(Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
		System.out.println("~CLOUDVARS  " + cloudVars);
		System.out.println("~GLOBALVARS " + globalVars);
		System.out.println("~LOCALVARS  " + localVars);
	}

	@Test
	/** substitute cloud , global and local variables where each is simply a variable
	 * easy test to pass */
	public void processAllVarsTest() {
		String[] cloudValues = new String[]{"cbar", "cfoo", "cbaz"};
		String[] globalValues = new String[]{"gbat", "gfee", "gbiz"};
		String[] localValues = new String[]{"lbaz", "lfuu", "lbuz"};

		//-cloud variables are just the variable name set to upper case
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		for (String val : cloudValues) {
			cloudVars.put(val, val.toUpperCase());
		}
		logger.info("cloud values have been set too: " + cloudVars);

		Map<String, String> globalVars = new HashMap<String, String>(3);
		for (String val : globalValues) {
			globalVars.put(val, val.toUpperCase());
		}
		logger.info("globalVars values have been set too: " + globalVars);

		Map<String, String> localVars = new HashMap<String, String>(3);
		for (String val : localValues) {
			localVars.put(val, val.toUpperCase());
		}
		logger.info("local values have been set too: " + localVars);

		CmsCI ci = new CmsCI();
		ci.setCiId(4444);
		ci.setCiName("processAllVarsTest");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(cloudValues.length * 3);
		///set up the ci with attributes which each need replacing...
		int i = 0;
		for (String cloudVariable : cloudValues) {//3 cloud vars
			CmsCIAttribute attrC = new CmsCIAttribute();
			attrC.setDjValue("$OO_CLOUD{" + cloudVariable + "}");
			attrC.setAttributeName("pav1");
			attributes.put("cloud_" + (++i), attrC);
		}
		for (String globalVariable : globalValues) {//3 globals
			CmsCIAttribute attrH = new CmsCIAttribute();
			attrH.setDjValue("$OO_GLOBAL{" + globalVariable + "}");
			attrH.setAttributeName("pav2");

			attributes.put("global_" + (++i), attrH);
		}
		for (String localVariable : localValues) {//3 locals
			CmsCIAttribute attrI = new CmsCIAttribute();
			attrI.setDjValue("$OO_LOCAL{" + localVariable + "}");
			attrI.setAttributeName("pav3");

			attributes.put("local_" + (++i), attrI);
		}
		ci.setAttributes(attributes);
		logger.info("CI Attributes have been set into the ci, attrs: " + attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}

		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);

		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("after>" + djKey + "->" + djAfter);
			if (djKey.startsWith("cloud_")) {
				String expected = attributesBefore.get(djKey).getDjValue().replace("OO_CLOUD{", "").replace("}", "").toUpperCase();
				assertEquals(djAfter, expected, "did not measure up for key " + djKey);
			} else {
				if (djKey.startsWith("global_")) {
					String expected = attributesBefore.get(djKey).getDjValue().replace("OO_GLOBAL{", "").replace("}", "").toUpperCase();
					assertEquals(djAfter, expected, "did not measure up for key " + djKey);
				} else {
					if (djKey.startsWith("local_")) {
						String expected = attributesBefore.get(djKey).getDjValue().replace("OO_LOCAL{", "").replace("}", "").toUpperCase();
						assertEquals(djAfter, expected, "did not measure up for key " + djKey);
					}
				}
			}
		}
	}

	@Test
	/** test that a global value can refer to a cloud variable */
	public void processGlobalValueWithCloudValueResolution() {
		String[] cloudValues = new String[]{"czat", "czitCCC"};
		String[] globalValues = new String[]{"czit"};//map to czitCCC in cloud

		//-cloud variables are just the variable name set to upper case
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		for (String val : cloudValues) {
			cloudVars.put(val, val.toUpperCase());
		}
		logger.info("cloud values have been set too: " + cloudVars);

		Map<String, String> globalVars = new HashMap<String, String>(3);
		for (String val : globalValues) {
			globalVars.put(val,
					"$OO_CLOUD{" + val + "CCC}" //is a reference to a cloud variable
			);
		}
		logger.info("globalVars values have been set too: " + globalVars);

		CmsCI ci = new CmsCI();
		ci.setCiId(9876);
		ci.setCiName("processGlobalValueWithCloudValueResolution");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(cloudValues.length * 3);
		///set up the ci with attributes which each need replacing...
		int i = 0;
		for (String cloudVariable : cloudValues) {//3 cloud vars
			CmsCIAttribute attrC = new CmsCIAttribute();
			attrC.setDjValue("$OO_CLOUD{" + cloudVariable + "}");
			attributes.put("cloud_" + (++i), attrC);
		}
		for (String globalVariable : globalValues) {//3 globals
			CmsCIAttribute attrG = new CmsCIAttribute();
			attrG.setDjValue("$OO_GLOBAL{" + globalVariable + "}");
			attrG.setAttributeName("djdj");
			attributes.put("global_" + (++i), attrG);
		}

		ci.setAttributes(attributes);
		logger.info("CI Attributes have been set into the ci, attrs: " + attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}

		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, globalVars, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, globalVars, new HashMap<String, String>());
		dumpCmsCIAttributes(ci);

		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("after>" + djKey + "->" + djAfter);
			if (djKey.startsWith("cloud_")) {
				String expected = attributesBefore.get(djKey).getDjValue().replace("OO_CLOUD{", "").replace("}", "").toUpperCase();
				assertEquals(djAfter, expected, "did not measure up for key " + djKey);
			} else {
				if (djKey.startsWith("global_")) {
					String expected = attributesBefore.get(djKey).getDjValue().replace("OO_GLOBAL{", "").replace("}", "").toUpperCase();
					assertEquals(djAfter, expected, "did not measure up for key " + djKey);

				}
			}
		}
	}


	@Test
	/** test that local variable can refer to a global variable value  */
	public void processLocalValueWithGlobalValueResolution() {
		String[] globalValues = new String[]{"ggyp-Global"};

		//- global variables' values are just the variable name set to upper case
		Map<String, String> globalVars = new HashMap<String, String>(3);
		for (String val : globalValues) {
			globalVars.put(val, val.toUpperCase());
		}
		System.out.println("globalVars values have been set to : " + globalVars);

		Map<String, String> localVars = new LinkedHashMap<String, String>(2);
		localVars.put("myLocalScalar", "abcdefg");

		localVars.put("myLocalVariable",        //one local variable refers to global
				"$OO_GLOBAL{ggyp-Global}" //is a reference to the global variable
		);
		System.out.println("localVars values have been set too: " + localVars);

		CmsCI ci = new CmsCI();
		ci.setCiId(135791113);
		ci.setCiName("processLocalValueWithGlobalValueResolution");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		///set up the ci with attributes which each need replacing...
		int i = 0;

		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_LOCAL{myLocalVariable}");
		attrL.setAttributeName("pl11");
		attributes.put("l81_" + (++i), attrL);
		CmsCIAttribute attrL2 = new CmsCIAttribute();
		attrL2.setDjValue("$OO_LOCAL{myLocalScalar}");
		attrL2.setAttributeName("pl22");
		attributes.put("l82_" + (++i), attrL2);

		ci.setAttributes(attributes);
		System.out.println("*****CI Attributes have been set into the ci, attrs: " + attributes.values());
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}

		CmsUtil util = new CmsUtil();
		dumpMaps(null, globalVars, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, new HashMap<String, String>(), globalVars, localVars);
		dumpCmsCIAttributes(ci);


		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("after>" + djKey + "->" + djAfter);

			if (djKey.startsWith("global_")) {
				String expected = attributesBefore.get(djKey).getDjValue()
						.replace("OO_GLOBAL{", "").replace("}", "")
						.toUpperCase();
				assertEquals(djAfter, expected, "did not measure up for key "
						+ djKey);
			} else {
				if (djKey.startsWith("local_")) {
					String expected = attributesBefore.get(djKey).getDjValue()
							.replace("OO_LOCAL{", "").replace("}", "")
							.toUpperCase();
					assertEquals(djAfter, expected,
							"did not measure up for key " + djKey);
				}
			}
		}

	}


	@Test(priority = 89)
	/** test that 1 local variable can refer to a global variable (g2) which
	 * in turn refers to a Cloud variable (c2)
	 * while another (justX) is both in Cloud and Global variable  */
	public void processLocalValueWithGlobalAndCloudValueResolution() {
		//Variables for cloud, global, and locals
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		cloudVars.put("justX", "ca");
		cloudVars.put("p2", "cb");

		Map<String, String> globalVars = new HashMap<String, String>(3);
		globalVars.put("justX", "ga");
		globalVars.put("p2", "$OO_CLOUD{p2}");

		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("l1", "$OO_GLOBAL{p2}");//want 'ca'
		localVars.put("p2", "$OO_GLOBAL{justX}");//want 'ga' not 'ca'

		CmsCI ci = new CmsCI();
		ci.setCiId(89);
		ci.setCiName("processLocalValueWithGlobalAndCloudValueResolution");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_LOCAL{l1}");
		attrL.setAttributeName("fo");
		attributes.put("firstOne", attrL);

		CmsCIAttribute attrL2 = new CmsCIAttribute();
		attrL2.setDjValue("$OO_LOCAL{p2}");
		attrL2.setAttributeName("SO");
		attributes.put("secondOne", attrL2);

		CmsCIAttribute attrL3 = new CmsCIAttribute();
		attrL3.setDjValue("$OO_GLOBAL{p2}");
		attrL3.setAttributeName("TO");
		attributes.put("thirdOne", attrL3);

		ci.setAttributes(attributes);
		System.out.println("*****CI Attributes have been set into the ci, attrs: " + attributes.values());
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);


		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals("firstOne")) {
				assertEquals(djAfter, "cb", "firstOne was not resolved correctly, local>global>cloud");
			}
			if (djKey.equals("secondOne")) {
				assertEquals(djAfter, "ga", "secondOne was not resolved correctly, local>global");
			}
			if (djKey.equals("thirdOne")) {
				assertEquals(djAfter, "cb", "thirdOne was not resolved correctly, global>cloud");
			}
		}

	}


	@Test(priority = 90)
	/** test local var where both Cloud and Global have that variable name
	 * and the one we want is from the Cloud  */
	public void processLocalVarChoiceGlobalVsCloud() {
		//Variables for cloud, global, and locals
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		cloudVars.put("common", "duolc");

		Map<String, String> globalVars = new HashMap<String, String>(3);
		globalVars.put("common", "labolg");

		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("common", "lacol");

		CmsCI ci = new CmsCI();
		ci.setCiId(90);
		ci.setCiName("processLocalVarChoiceGlobalVsCloud");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		int i = 0;
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_CLOUD{common}");
		String nameOfAttribute = "my-one-attr";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);

		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "duolc");
			}
		}

	}


	@Test(priority = 95)
	/** test local var where regex matches in middle not start */
	public void processLocalVarMid() {
		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("mylocal", "123456");

		CmsCI ci = new CmsCI();
		ci.setCiId(95);
		ci.setCiName("processLocalVarMid");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		int i = 0;
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("/preamble/$OO_LOCAL{mylocal}");
		String nameOfAttribute = "my-only-attr";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, localVars);
		dumpCmsCIAttributes(ci);

		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "/preamble/123456");
			}
		}

	}

	@Test(priority = 98)
	/** test local var where regex matches in middle not start */
	public void processLocalVarDuo() {
		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("mylocal", "123456");

		CmsCI ci = new CmsCI();
		ci.setCiId(98);
		ci.setCiName("processLocalVarDuo");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		int i = 0;
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("/preamble/$OO_LOCAL{mylocal}/middle/$OO_LOCAL{mylocal}");
		String nameOfAttribute = "my-only-attr";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, localVars);
		dumpCmsCIAttributes(ci);

		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "/preamble/123456/middle/123456");
			}
		}

	}

	@Test(priority = 111)
	/** test a composite var , one of each layer*/
	public void processLocalVarMixed() {
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		cloudVars.put("myCloud", "987654");
		Map<String, String> globalVars = new HashMap<String, String>(3);
		globalVars.put("myGlobal", "1212");
		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("myLocal", "8989");

		CmsCI ci = new CmsCI();
		ci.setCiId(111);
		ci.setCiName("processLocalVarMixed");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("/preamble/$OO_CLOUD{myCloud}/middle/$OO_GLOBAL{myGlobal}/sss/$OO_LOCAL{myLocal}");
		String nameOfAttribute = "myMixedAttribute";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);


		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "/preamble/987654/middle/1212/sss/8989");
			}
		}

	}

	@Test(priority = 115)
	/** test multi resolution as in artifacts usage
	 * 	$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}
	 * */
	public void processLocalVarTrips() {

		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("groupId", "esb");
		localVars.put("artifactId", "service");
		localVars.put("extension", "html");

		CmsCI ci = new CmsCI();
		ci.setCiId(115);
		ci.setCiName("processLocalVarTrips");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}");
		String nameOfAttribute = "myArtifactGav";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, localVars);
		dumpCmsCIAttributes(ci);


		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "esb:service:html");
			}
		}

	}

	@Test(priority = 122)
	/** test variable that is not one of our OO_CLOUD/LOCAL/GLOBAL varieties */
	public void processLocalVarNegativeSpelling() {

		Map<String, String> localVars = new HashMap<String, String>(1);
		localVars.put("groupId", "barrrrrr");

		CmsCI ci = new CmsCI();
		ci.setCiId(122);
		ci.setCiName("processLocalVarNegativeSpelling");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_NOOCAL{groupId}");//typo in place of $OO_LOCAL
		String nameOfAttribute = "irrelevantname";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, localVars);
		dumpCmsCIAttributes(ci);


		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "$OO_NOOCAL{groupId}");//outcome is it stays as it
			}
		}

	}

	@Test(priority = 130, expectedExceptions = CIValidationException.class)
	/** test variable that user did not set, it is exception 
	 * bad reference */
	public void processLocalVarNegativeMissingLocal() {
		Map<String, String> localVars = new HashMap<String, String>(1);
		localVars.put("aaaaaa", "ignore-me");
		CmsCI ci = new CmsCI();
		ci.setCiId(130);
		ci.setCiName("processLocalVarNegativeMissingLocal");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_LOCAL{this-is-not-resolved}");
		String nameOfAttribute = "trouble";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, localVars);
		dumpCmsCIAttributes(ci);

	}

	@Test(priority = 131, expectedExceptions = CIValidationException.class)
	/** test variable that user did not set, it is exception
	 * bad reference */
	public void processLocalVarNegativeMissingLocal2() {
		CmsCI ci = new CmsCI();
		ci.setCiId(132);
		ci.setCiName("processLocalVarNegativeMissingLocal2");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_LOCAL{this-is-not-resolved}");
		String nameOfAttribute = "trouble";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, null);//this time all map empty
		dumpCmsCIAttributes(ci);

	}

	@Test(priority = 132, expectedExceptions = CIValidationException.class)
	/** test variable that user did not set, it is exception
	 * bad reference */
	public void processLocalVarNegativeMissingGlobal() {
		CmsCI ci = new CmsCI();
		ci.setCiId(132);
		ci.setCiName("processLocalVarNegativeMissingGlobal");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_GLOBAL{where-is-it}");
		String nameOfAttribute = "nowhere";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, null);
		dumpCmsCIAttributes(ci);
	}

	@Test(priority = 132, expectedExceptions = CIValidationException.class)
	/** test variable that user did not set, it is exception 
	 * bad reference */
	public void processLocalVarNegativeMissingGlobal2() {
		Map<String, String> globalVars = new HashMap<String, String>(1);
		globalVars.put("cc", "ignore-distraction");
		CmsCI ci = new CmsCI();
		ci.setCiId(132);
		ci.setCiName("processLocalVarNegativeMissingGlobal2");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_GLOBAL{where?}");
		String nameOfAttribute = "nowh---ere";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, globalVars, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, globalVars, null);
		dumpCmsCIAttributes(ci);

	}

	@Test(priority = 133, expectedExceptions = CIValidationException.class)
	/** test variable that user did not set, it is exception
	 * bad reference */
	public void processLocalVarNegativeMissingCloud1() {
		Map<String, String> cloudVars = new HashMap<String, String>(1);
		cloudVars.put("bb", "ignore-bb");
		CmsCI ci = new CmsCI();
		ci.setCiId(133);
		ci.setCiName("processLocalVarNegativeMissingCloud1");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_CLOUD{not-cloudy}");
		String nameOfAttribute = "sun";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, null, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, null, null);
		dumpCmsCIAttributes(ci);
	}

	@Test(priority = 134, expectedExceptions = CIValidationException.class)
	/** test variable that user did not set, it is exception
	 * bad reference */
	public void processLocalVarNegativeMissingCloud() {
		CmsCI ci = new CmsCI();
		ci.setCiId(134);
		ci.setCiName("processLocalVarNegativeMissingCloud");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_CLOUD{not-cloudy}");
		String nameOfAttribute = "sun";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, null, null, null);
		dumpCmsCIAttributes(ci);

	}

	@Test(priority = 140)
	/** test multi resolution as in artifacts usage
	 * 	$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}
	 * */
	public void processLocalVarDemo() {
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		cloudVars.put("version", "2.0");
		Map<String, String> globalVars = new HashMap<String, String>(3);
		globalVars.put("version", "$OO_CLOUD{version}");
		Map<String, String> localVars = new HashMap<String, String>(3);
		localVars.put("groupId", "esb");
		localVars.put("artifactId", "service");

		CmsCI ci = new CmsCI();
		ci.setCiId(140);
		ci.setCiName("processLocalVarDemo");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(2);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_GLOBAL{version}");
		String nameOfAttribute = "myArtifactGav";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, globalVars, localVars);
		dumpCmsCIAttributes(ci);

		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "esb:service:2.0");
			}
		}

	}

	@Test(priority = 150)
	/** test variable that user did not set, instead it is empty string ""
	 * this is NOT a bad reference */
	public void processLocalVarBlankValue() {
		Map<String, String> cloudVars = new HashMap<String, String>(3);
		cloudVars.put("FOO", "");
		CmsCI ci = new CmsCI();
		ci.setCiId(150);
		ci.setCiName("processLocalVarBlankValue");
		Map<String, CmsCIAttribute> attributes = new LinkedHashMap<String, CmsCIAttribute>(1);
		CmsCIAttribute attrL = new CmsCIAttribute();
		attrL.setDjValue("$OO_CLOUD{FOO}");
		String nameOfAttribute = "testingABlank";
		attributes.put(nameOfAttribute, attrL);
		ci.setAttributes(attributes);
		Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
		for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
			System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
		}
		CmsUtil util = new CmsUtil();
		dumpMaps(null, null, null);
		dumpCmsCIAttributes(ci);
		util.processAllVars(ci, cloudVars, null, null);
		dumpCmsCIAttributes(ci);
		for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
			String djKey = a.getKey();
			String djAfter = a.getValue().getDjValue();
			System.out.println("*after k>" + djKey + " v->" + djAfter);
			if (djKey.equals(nameOfAttribute)) {
				assertEquals(djAfter, "", "this is not a empty string as expected");
			}
		}
	}

	@Test
	public void testErrorMessage(){
		String errorMessage="CI tomcat[/p1/1], attribute: pre_shutdown_command is using invalid or missing local variable <DEPLOYCONTEXT>! Value=null";
		assertEquals(util.getErrorMessage("tomcat","/LOCAL2/A1/testEnv2/manifest/p1/1","pre_shutdown_command", null, "DEPLOYCONTEXT", "\\$OO_LOCAL\\{"),errorMessage);
	}

}
