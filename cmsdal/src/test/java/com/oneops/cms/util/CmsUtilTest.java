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

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.crypto.CmsCryptoDES;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import org.apache.log4j.Logger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.Map.Entry;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;


public class CmsUtilTest {


  private static final String CLASS_NAME = null;
  private static final long CI_ID = 100;
  private static final String CI_NAME = "Source";
  private static final String NS_PATH = "/public/main";
  private static final String CI_IMPL = "java.lang";
  private static final Logger logger = Logger.getLogger(CmsUtilTest.class);
  CmsCISimple ciSimple = new CmsCISimple();
  private CmsUtil util = new CmsUtil();
  private Map<String, String> ciAttributes;

  public CmsUtilTest() {
    this.ciAttributes = new HashMap<>(3);
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


  protected void dumpCmsCIAttributes(CmsCI ci) {
    for (CmsCIAttribute manifestAttr : ci.getAttributes().values()) {
      System.out.println("~Ci_Dj :" + manifestAttr.getDjValue());
    }
  }

  protected void dumpMaps(Map<String, String> cloudVars, Map<String, String> globalVars,
      Map<String, String> localVars) {
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
    Map<String, String> cloudVars = getVariablesMap(cloudValues);
    logger.info("cloud values have been set too: " + cloudVars);

    Map<String, String> globalVars = getVariablesMap(globalValues);
    logger.info("globalVars values have been set too: " + globalVars);

    Map<String, String> localVars = getVariablesMap(localValues);
    logger.info("local values have been set too: " + localVars);

    CmsCI ci = new CmsCI();
    ci.setCiId(4444);
    ci.setCiName("processAllVarsTest");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(cloudValues.length * 3);
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

    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("after>" + djKey + "->" + djAfter);
      if (djKey.startsWith("cloud_")) {
        String expected = attributesBefore.get(djKey).getDjValue().replace("OO_CLOUD{", "")
            .replace("}", "").toUpperCase();
        assertEquals(djAfter, expected, "did not measure up for key " + djKey);
      } else {
        if (djKey.startsWith("global_")) {
          String expected = attributesBefore.get(djKey).getDjValue().replace("OO_GLOBAL{", "")
              .replace("}", "").toUpperCase();
          assertEquals(djAfter, expected, "did not measure up for key " + djKey);
        } else {
          if (djKey.startsWith("local_")) {
            String expected = attributesBefore.get(djKey).getDjValue().replace("OO_LOCAL{", "")
                .replace("}", "").toUpperCase();
            assertEquals(djAfter, expected, "did not measure up for key " + djKey);
          }
        }
      }
    }
  }

  protected CmsUtil getCmsUtil() {
    CmsUtil util = new CmsUtil();
    CmsCrypto crypto = mock(CmsCrypto.class);
    try {
      doAnswer(new Answer<String>() {
        public String answer(InvocationOnMock invocation) {
          return invocation.getArguments()[0].toString();
        }
      }).when(crypto).decrypt(anyString());
    } catch (GeneralSecurityException e) {
      //for mocking
    }
    util.setCmsCrypto(crypto);
    return util;
  }

  private Map<String, String> getVariablesMap(String[] varValues) {
    Map<String, String> cloudVars = new HashMap<>(3);
    for (String val : varValues) {
      cloudVars.put(val, val.toUpperCase());
    }
    return cloudVars;
  }

  @Test
  /** test that a global value can refer to a cloud variable */
  public void processGlobalValueWithCloudValueResolution() {
    String[] cloudValues = new String[]{"czat", "czitCCC"};
    String[] globalValues = new String[]{"czit"};//map to czitCCC in cloud

    //-cloud variables are just the variable name set to upper case
    Map<String, String> cloudVars = getVariablesMap(cloudValues);
    logger.info("cloud values have been set too: " + cloudVars);

    Map<String, String> globalVars = new HashMap<>(3);
    for (String val : globalValues) {
      globalVars.put(val,
          "$OO_CLOUD{" + val + "CCC}" //is a reference to a cloud variable
      );
    }
    logger.info("globalVars values have been set too: " + globalVars);

    CmsCI ci = new CmsCI();
    ci.setCiId(9876);
    ci.setCiName("processGlobalValueWithCloudValueResolution");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(cloudValues.length * 3);
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

    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, null);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, new HashMap<>());
    dumpCmsCIAttributes(ci);

    for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("after>" + djKey + "->" + djAfter);
      if (djKey.startsWith("cloud_")) {
        String expected = attributesBefore.get(djKey).getDjValue().replace("OO_CLOUD{", "")
            .replace("}", "").toUpperCase();
        assertEquals(djAfter, expected, "did not measure up for key " + djKey);
      } else {
        if (djKey.startsWith("global_")) {
          String expected = attributesBefore.get(djKey).getDjValue().replace("OO_GLOBAL{", "")
              .replace("}", "").toUpperCase();
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
    Map<String, String> globalVars = getVariablesMap(globalValues);
    System.out.println("globalVars values have been set to : " + globalVars);

    Map<String, String> localVars = new LinkedHashMap<>(2);
    localVars.put("myLocalScalar", "abcdefg");

    localVars.put("myLocalVariable",        //one local variable refers to global
        "$OO_GLOBAL{ggyp-Global}" //is a reference to the global variable
    );
    System.out.println("localVars values have been set too: " + localVars);

    CmsCI ci = new CmsCI();
    ci.setCiId(135791113);
    ci.setCiName("processLocalValueWithGlobalValueResolution");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
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
    System.out
        .println("*****CI Attributes have been set into the ci, attrs: " + attributes.values());
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }

    CmsUtil util = getCmsUtil();
    dumpMaps(null, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, new HashMap<>(), globalVars, localVars);
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
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("justX", "ca");
    cloudVars.put("p2", "cb");

    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("justX", "ga");
    globalVars.put("p2", "$OO_CLOUD{p2}");

    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("l1", "$OO_GLOBAL{p2}");//want 'ca'
    localVars.put("p2", "$OO_GLOBAL{justX}");//want 'ga' not 'ca'

    CmsCI ci = new CmsCI();
    ci.setCiId(89);
    ci.setCiName("processLocalValueWithGlobalAndCloudValueResolution");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
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
    System.out
        .println("*****CI Attributes have been set into the ci, attrs: " + attributes.values());
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("common", "duolc");

    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("common", "labolg");

    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("common", "lacol");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("processLocalVarChoiceGlobalVsCloud");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_CLOUD{common}");
    String nameOfAttribute = "my-one-attr";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("mylocal", "123456");

    CmsCI ci = new CmsCI();
    ci.setCiId(95);
    ci.setCiName("processLocalVarMid");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("/preamble/$OO_LOCAL{mylocal}");
    String nameOfAttribute = "my-only-attr";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("mylocal", "123456");

    CmsCI ci = new CmsCI();
    ci.setCiId(98);
    ci.setCiName("processLocalVarDuo");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("/preamble/$OO_LOCAL{mylocal}/middle/$OO_LOCAL{mylocal}");
    String nameOfAttribute = "my-only-attr";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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

  @Test(priority = 105)
  /** test local var which in turn references both cloud and global variables  */
  public void processLocalVarUsingBothCloudGlobal() {
    //Variables for cloud, global, and locals
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("common", "cl1");
    cloudVars.put("store", "st1");

    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("common", "gl1");
    globalVars.put("version", "1.0");
    globalVars.put("cldstr", "$OO_CLOUD{store}");
    globalVars.put("dir", "up");

    Map<String, String> localVars = new HashMap<>(3);
    //combination of cloud and global variables
    localVars.put("common",
        "lc1-$OO_GLOBAL{common}-ver-$OO_CLOUD{common}-1-$OO_CLOUD{store}-v1-$OO_CLOUD{common}-$OO_GLOBAL{version}.SNAPSHOT");
    //references global variable which in turn uses cloud variable
    localVars.put("store-name", "commons-$OO_GLOBAL{cldstr}-$OO_CLOUD{common}-$OO_GLOBAL{dir}");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("processLocalVarUsingBothCloudGlobal");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);

    CmsCIAttribute attr1 = new CmsCIAttribute();
    attr1.setDjValue("$OO_LOCAL{common}");
    String attr1Name = "attr1";
    attributes.put(attr1Name, attr1);

    CmsCIAttribute attr2 = new CmsCIAttribute();
    attr2.setDjValue("$OO_LOCAL{store-name}");
    String attr2Name = "attr2";
    attributes.put(attr2Name, attr2);

    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals(attr1Name)) {
        assertEquals(djAfter, "lc1-gl1-ver-cl1-1-st1-v1-cl1-1.0.SNAPSHOT");
      } else if (djKey.equals(attr2Name)) {
        assertEquals(djAfter, "commons-st1-cl1-up");
      }
    }

  }

  @Test(priority = 111)
  /** test a composite var , one of each layer*/
  public void processLocalVarMixed() {
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("myCloud", "987654");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("myGlobal", "1212");
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("myLocal", "8989");

    CmsCI ci = new CmsCI();
    ci.setCiId(111);
    ci.setCiName("processLocalVarMixed");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue(
        "/preamble/$OO_CLOUD{myCloud}/middle/$OO_GLOBAL{myGlobal}/sss/$OO_LOCAL{myLocal}");
    String nameOfAttribute = "myMixedAttribute";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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

    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("groupId", "esb");
    localVars.put("artifactId", "service");
    localVars.put("extension", "html");

    CmsCI ci = new CmsCI();
    ci.setCiId(115);
    ci.setCiName("processLocalVarTrips");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}");
    String nameOfAttribute = "myArtifactGav";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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

    Map<String, String> localVars = new HashMap<>(1);
    localVars.put("groupId", "barrrrrr");

    CmsCI ci = new CmsCI();
    ci.setCiId(122);
    ci.setCiName("processLocalVarNegativeSpelling");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_NOOCAL{groupId}");//typo in place of $OO_LOCAL
    String nameOfAttribute = "irrelevantname";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, String> localVars = new HashMap<>(1);
    localVars.put("aaaaaa", "ignore-me");
    CmsCI ci = new CmsCI();
    ci.setCiId(130);
    ci.setCiName("processLocalVarNegativeMissingLocal");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_LOCAL{this-is-not-resolved}");
    String nameOfAttribute = "trouble";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_LOCAL{this-is-not-resolved}");
    String nameOfAttribute = "trouble";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, null);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, Collections.EMPTY_MAP, Collections.EMPTY_MAP,
        Collections.EMPTY_MAP);//this time all map empty
    CmsCrypto crypto = mock(CmsCrypto.class);
    try {
      doAnswer(new Answer<String>() {
        public String answer(InvocationOnMock invocation) {

          return invocation.getArguments()[0].toString();
        }
      }).when(crypto).decrypt(anyString());
    } catch (GeneralSecurityException e) {
      //for mocking
    }
    util.setCmsCrypto(crypto);
    dumpCmsCIAttributes(ci);

  }

  @Test(priority = 132, expectedExceptions = CIValidationException.class)
  /** test variable that user did not set, it is exception
   * bad reference */
  public void processLocalVarNegativeMissingGlobal() {
    CmsCI ci = new CmsCI();
    ci.setCiId(132);
    ci.setCiName("processLocalVarNegativeMissingGlobal");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_GLOBAL{where-is-it}");
    String nameOfAttribute = "nowhere";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, null);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    dumpCmsCIAttributes(ci);
  }

  @Test(priority = 132, expectedExceptions = CIValidationException.class)
  /** test variable that user did not set, it is exception
   * bad reference */
  public void processLocalVarNegativeMissingGlobal2() {
    Map<String, String> globalVars = new HashMap<>(1);
    globalVars.put("cc", "ignore-distraction");
    CmsCI ci = new CmsCI();
    ci.setCiId(132);
    ci.setCiName("processLocalVarNegativeMissingGlobal2");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_GLOBAL{where?}");
    String nameOfAttribute = "nowh---ere";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, globalVars, null);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, globalVars, null);
    dumpCmsCIAttributes(ci);

  }

  @Test(priority = 133, expectedExceptions = CIValidationException.class)
  /** test variable that user did not set, it is exception
   * bad reference */
  public void processLocalVarNegativeMissingCloud1() {
    Map<String, String> cloudVars = new HashMap<>(1);
    cloudVars.put("bb", "ignore-bb");
    CmsCI ci = new CmsCI();
    ci.setCiId(133);
    ci.setCiName("processLocalVarNegativeMissingCloud1");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_CLOUD{not-cloudy}");
    String nameOfAttribute = "sun";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_CLOUD{not-cloudy}");
    String nameOfAttribute = "sun";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, null);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    dumpCmsCIAttributes(ci);

  }

  @Test(priority = 140)
  /** test multi resolution as in artifacts usage
   * 	$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}
   * */
  public void processLocalVarDemo() {
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("version", "2.0");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("version", "$OO_CLOUD{version}");
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("groupId", "esb");
    localVars.put("artifactId", "service");

    CmsCI ci = new CmsCI();
    ci.setCiId(140);
    ci.setCiName("processLocalVarDemo");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_GLOBAL{version}");
    String nameOfAttribute = "myArtifactGav";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("FOO", "");
    CmsCI ci = new CmsCI();
    ci.setCiId(150);
    ci.setCiName("processLocalVarBlankValue");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(1);
    CmsCIAttribute attrL = new CmsCIAttribute();
    attrL.setDjValue("$OO_CLOUD{FOO}");
    String nameOfAttribute = "testingABlank";
    attributes.put(nameOfAttribute, attrL);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
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

  @Test(priority = 105)
  /** 2. When a global var uses another global var along with a cloud var, it does not result into a correct value.
   * Meaning the global var does not resolve both cloud and another global variable together.**/
  public void processGlobalVarUsingBothCloudGlobal() {
    //Variables for cloud, global, and locals
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("common", "cl1");
    cloudVars.put("store", "st1");
    cloudVars.put("common", "glcloud");

    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("common", "gl1");
    globalVars.put("version", "1.0");
    globalVars.put("cldstr", "$OO_CLOUD{store}");
    globalVars.put("dir", "up");
    globalVars
        .put("globalcloudvar1", "Global var $OO_GLOBAL{common} has cloud var $OO_CLOUD{common}");

    Map<String, String> localVars = new HashMap<>(3);
    //combination of cloud and global variables
    localVars.put("common",
        "lc1-$OO_GLOBAL{common}-ver-$OO_CLOUD{common}-1-$OO_CLOUD{store}-v1-$OO_CLOUD{common}-$OO_GLOBAL{version}.SNAPSHOT");
    //references global variable which in turn uses cloud variable
    localVars.put("store-name", "commons-$OO_GLOBAL{cldstr}-$OO_CLOUD{common}-$OO_GLOBAL{dir}");
    //localVars.put("globalcloudvar", "Global var $OO_GLOBAL{common} has cloud var $OO_CLOUD{common}");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("processLocalVarUsingBothCloudGlobal");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);

    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("$OO_GLOBAL{globalcloudvar1}");
    String attr4Name = "attr4";
    attributes.put(attr4Name, attr4);

    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals(attr4Name)) {
        assertEquals(djAfter, "Global var gl1 has cloud var glcloud");
      }
      /*else if (djKey.equals(attr2Name)) {
				assertEquals(djAfter, "commons-st1-cl1-up");
			}*/
    }

  }

  @Test
  public void testUsingVarsInEncryptedAttributes() {
//Variables for cloud, global, and locals
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("common", "cl1");
    cloudVars.put("store", "st1");
    cloudVars.put("common", "glcloud");

    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("common", "gl1");
    globalVars.put("version", "1.0");
    globalVars.put("cldstr", "$OO_CLOUD{store}");
    globalVars.put("dir", "up");
    globalVars
        .put("globalcloudvar1", "Global var $OO_GLOBAL{common} has cloud var $OO_CLOUD{common}");

    Map<String, String> localVars = new HashMap<>(3);
    //combination of cloud and global variables
    localVars.put("common",
        "lc1-$OO_GLOBAL{common}-ver-$OO_CLOUD{common}-1-$OO_CLOUD{store}-v1-$OO_CLOUD{common}-$OO_GLOBAL{version}.SNAPSHOT");
    //references global variable which in turn uses cloud variable
    localVars.put("store-name", "commons-$OO_GLOBAL{cldstr}-$OO_CLOUD{common}-$OO_GLOBAL{dir}");
    //localVars.put("globalcloudvar", "Global var $OO_GLOBAL{common} has cloud var $OO_CLOUD{common}");

    CmsCI ci = new CmsCI();
    CmsCryptoDES crypto = getCrypto();

    ci.setCiId(90);
    ci.setCiName("processLocalVarUsingBothCloudGlobal");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);

    CmsCIAttribute attr4 = new CmsCIAttribute();
    //set var in encrypted attribute
    attr4.setDjValue(encrypt(crypto));
    String attr4Name = "attr4";
    attributes.put(attr4Name, attr4);

    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    util.setCmsCrypto(crypto);
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();

      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals(attr4Name)) {
        String djAfterDec = null;
        try {
          djAfterDec = crypto.decrypt(a.getValue().getDjValue());
        } catch (GeneralSecurityException e) {
          e.printStackTrace();
        }
        assertEquals(djAfterDec, "Global var gl1 has cloud var glcloud");
      }

    }

  }


  private String encrypt(CmsCryptoDES crypto) {
    try {
      return crypto.encrypt("$OO_GLOBAL{globalcloudvar1}");
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return null;
  }

  private CmsCryptoDES getCrypto() {
    CmsCryptoDES crypto = new CmsCryptoDES();
    try {
      crypto.init(getClass().getResource("/oo.key").getFile());

    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }
    return crypto;
  }

  //	• Local variable refers to another local variable

  @Test
  public void localVarRefersAnotherLVar() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "localVar1");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("$OO_LOCAL{lv2}");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, null, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "localVar1");
      }

    }

  }

  @Test
  public void localVarRefersAnotherLVarWithPrefix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "localVar1");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv2}");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, null, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix localVar1");
      }

    }

  }

  @Test
  public void localVarRefersAnotherLVarWithSuffix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "localVar1");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, null, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "localVar1Suffix");
      }

    }

  }

  @Test
  public void localVarRefersAnotherLVarWithPrefixSuffix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "localVar1");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, null, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "PrefixlocalVar1Suffix");
      }

    }

  }

  @Test
  public void localVarRefersAnotherGVarWithPrefixSuffix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "PrefixglobalVar1Suffix");
      }

    }

  }

  @Test
  public void localVarRefersAnotherGVarWithPrefix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "PrefixglobalVar1");
      }

    }

  }

  @Test
  public void localVarRefersAnotherCVarWithPrefix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_CLOUD{cv1}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "PrefixcloudVar1");
      }

    }

  }

  @Test
  public void localVarRefersAnotherCVarWithSuffixPrefix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_CLOUD{cv1}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv2} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix cloudVar1 Suffix");
      }

    }

  }


  @Test
  public void localVarRefersToGV() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv1} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix globalVar1 Suffix");
      }

    }

  }

  @Test
  public void localVarRefersToGVPrefix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv1}");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix globalVar1");
      }

    }

  }

  @Test
  public void localVarRefersToGVSuffix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("$OO_LOCAL{lv1} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "globalVar1 Suffix");
      }

    }

  }

  //Local Variable refers to Global Var indirection

  @Test
  public void localVarRefersToGV2() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "$OO_GLOBAL{gv2}");
    globalVars.put("gv2", "globalVar2");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv1} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix globalVar2 Suffix");
      }

    }

  }

  //Local Variable refers to Global -Cloud Var indirection

  @Test
  public void localVarRefersToGVCV() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "$OO_CLOUD{cv1}");
    globalVars.put("gv2", "globalVar2");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv1} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix cloudVar1 Suffix");
      }

    }

  }


  @Test
  public void localVarRefersToCV() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_CLOUD{cv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "$OO_CLOUD{cv1}");
    globalVars.put("gv2", "globalVar2");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv1} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix cloudVar1 Suffix");
      }

    }

  }

  @Test
  public void localVarRefersToCV2() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_CLOUD{cv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "$OO_CLOUD{cv1}");
    globalVars.put("gv2", "globalVar2");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "$OO_CLOUD{cv2}");
    cloudVars.put("cv2", "cloudVar2");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix $OO_LOCAL{lv1} Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "Prefix cloudVar2 Suffix");
      }

    }

  }

  @Test
  public void localVarRefersMixed() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_CLOUD{cv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "$OO_CLOUD{cv1}");
    globalVars.put("gv2", "globalVar2");

    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "$OO_CLOUD{cv2}");
    cloudVars.put("cv2", "cloudVar2");

    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherGVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("$OO_LOCAL{lv1}/$OO_GLOBAL{gv1}/$OO_CLOUD{cv2}");
    //cloudVar2 cloudVar2 cloudVar2
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "cloudVar2/cloudVar2/cloudVar2");
      }

    }

  }

  @Test(expectedExceptions = {
      CIValidationException.class}, expectedExceptionsMessageRegExp = ".* cyclic reference.*")
  public void localVarRefersAnotherGVarCvarWithPrefixSuffixCycleGlobal() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv2}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1-$OO_GLOBAL{gv2}");
    globalVars.put("gv2", "$OO_GLOBAL{gv1}");
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
  }

  @Test(expectedExceptions = {
      CIValidationException.class}, expectedExceptionsMessageRegExp = ".* cyclic reference.*")
  public void localVarRefersAnotherGVarCvarWithPrefixSuffixCycleCloud() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv1}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1-$OO_GLOBAL{gv2}");
    globalVars.put("gv2", "$OO_CLOUD{cv1}");
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "$OO_CLOUD{cv1}");
    CmsCI ci = new CmsCI();

    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
  }

  @Test(expectedExceptions = {
      CIValidationException.class}, expectedExceptionsMessageRegExp = ".* cyclic reference.*")
  public void localVarRefersAnotherGVarCvarWithPrefixSuffixCycleLocalItself() {
    Map<String, String> localVars = new HashMap<>(3);
    //localVars.put("lv1", "$OO_LOCAL{lv2}");
    localVars.put("lv2", "$OO_LOCAL{lv2}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1-$OO_GLOBAL{gv2}");
    globalVars.put("gv2", "$OO_GLOBAL{gv1}");
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
  }

  @Test(expectedExceptions = {
      CIValidationException.class}, expectedExceptionsMessageRegExp = ".* cyclic reference.*")
  public void localVarRefersAnotherGVarCvarWithPrefixSuffixCycleLocal() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_LOCAL{lv2}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1-$OO_GLOBAL{gv2}");
    globalVars.put("gv2", "$OO_GLOBAL{gv1}");
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
  }

  @Test
  public void localVarRefersAnotherGVarCvarWithPrefixSuffix() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "$OO_GLOBAL{gv2}");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    Map<String, String> globalVars = new HashMap<>(3);
    globalVars.put("gv1", "globalVar1-$OO_CLOUD{cv1}");
    globalVars.put("gv2", "$OO_GLOBAL{gv1}");
    Map<String, String> cloudVars = new HashMap<>(3);
    cloudVars.put("cv1", "cloudVar1");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("Prefix$OO_LOCAL{lv2}Suffix");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, cloudVars, globalVars, localVars);
    dumpCmsCIAttributes(ci);
    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "PrefixglobalVar1-cloudVar1Suffix");
      }

    }

  }


  @Test(expectedExceptions = {
      CIValidationException.class}, expectedExceptionsMessageRegExp = ".* variable syntax.*")
  public void localVarRefersAnotherLVarInvalidSyntax() {
    Map<String, String> localVars = new HashMap<>(3);
    localVars.put("lv1", "localVar1");
    localVars.put("lv2", "$OO_LOCAL{lv1}");
    CmsCI ci = new CmsCI();
    ci.setCiId(90);
    ci.setCiName("localVarRefersAnotherLVar");
    Map<String, CmsCIAttribute> attributes = new LinkedHashMap<>(2);
    CmsCIAttribute attr4 = new CmsCIAttribute();
    attr4.setDjValue("$OO_LOCAL{lv2");
    attributes.put("localVarRefersAnotherLVar", attr4);
    ci.setAttributes(attributes);
    Map<String, CmsCIAttribute> attributesBefore = ci.getAttributes();
    for (Map.Entry<String, CmsCIAttribute> e : attributesBefore.entrySet()) {
      System.out.println("*- b4   |" + e.getKey() + "->" + e.getValue().getDjValue());
    }
    CmsUtil util = getCmsUtil();
    dumpMaps(null, null, localVars);
    dumpCmsCIAttributes(ci);
    util.processAllVars(ci, null, null, localVars);
    dumpCmsCIAttributes(ci);

    for (Map.Entry<String, CmsCIAttribute> a : ci.getAttributes().entrySet()) {
      String djKey = a.getKey();
      String djAfter = a.getValue().getDjValue();
      System.out.println("*after k>" + djKey + " v->" + djAfter);
      if (djKey.equals("localVarRefersAnotherLVar")) {
        assertEquals(djAfter, "localVar1");
      }

    }

  }

  @Test
  public void nsPathOrgCheck() {
    assertTrue(CmsUtil.isOrgLevel("/test-org/"));
    assertTrue(CmsUtil.isOrgLevel("/test-org"));
    assertFalse(CmsUtil.isOrgLevel("/test-org/a1"));
    assertFalse(CmsUtil.isOrgLevel("/test-org/a2/"));
    assertFalse(CmsUtil.isOrgLevel("/test-org/a2/dev"));
    assertFalse(CmsUtil.isOrgLevel("/test-org/a2/dev/"));
    assertFalse(CmsUtil.isOrgLevel("test-org"));
  }

 @Test
  public void testMaskSecuredAttributes(){
   CmsRfcCISimple aRfc = new CmsRfcCISimple();
   Map<String,String> rfcAttributes = new HashMap<>();
   rfcAttributes.put("secureAtrribute1","secureValue");
   rfcAttributes.put("attrib2","attrib2");
   aRfc.setCiAttributes(rfcAttributes);

   Map<String,String> baseAttributes = new HashMap<>();
   baseAttributes.put("secureBaseAtrribute1","secureBaseAtrribute1");
   baseAttributes.put("baseAtrribute1","baseAtrribute1");
   aRfc.setCiBaseAttributes(baseAttributes);
   Map<String, Map<String, String>> secureAttributeMap = new HashMap<>();
   Map<String, String> attributeNames = new HashMap<>();
   attributeNames.put("secureAtrribute1","true");
   attributeNames.put("secureBaseAtrribute1","true");
   secureAttributeMap.put(CmsConstants.SECURED_ATTRIBUTE,attributeNames);
   aRfc.setCiAttrProps(secureAttributeMap);
   CmsUtil.maskSecure(aRfc);
   assertEquals(aRfc.getCiAttributes().get("secureAtrribute1"),CmsUtil.MASK);
   assertEquals(aRfc.getCiAttributes().get("attrib2"),"attrib2");
   assertEquals(aRfc.getCiBaseAttributes().get("secureBaseAtrribute1"),CmsUtil.MASK);
   assertEquals(aRfc.getCiBaseAttributes().get("baseAtrribute1"),"baseAtrribute1");
  }




}
