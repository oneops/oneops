package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.crypto.CmsCryptoDES;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.util.CmsUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.testng.Assert.*;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class BomRfcBulkProcessorTest {
    BomRfcBulkProcessor proc;

    @Before
    public void setup() {
        CmsUtil cmsUtil = new CmsUtil();
        cmsUtil.setCmsCrypto(new CmsCryptoDES());
        cmsUtil.setCountOfErrorsToReport(5);
        proc = new BomRfcBulkProcessor();
        proc.setCmsUtil(cmsUtil);
    }

    @Test
    public void processAndValidateVars() {

        ArrayList<CmsCI> cis = new ArrayList<>();
        CmsCI ci = new CmsCI();
        ci.setCiName("testCi");
        ci.setNsPath("/path");
        CmsCIAttribute attribute1 = new CmsCIAttribute();
        attribute1.setAttributeName("testAttribute1");
        attribute1.setDjValue("$OO_GLOBAL{test1}");
        attribute1.setDfValue("$OO_GLOBAL{test1}");
        CmsCIAttribute attribute2 = new CmsCIAttribute();
        attribute2.setAttributeName("testAttribute2");
        attribute2.setDjValue("$OO_GLOBAL{test2}");
        attribute2.setDfValue("$OO_GLOBAL{test2}");
        ci.addAttribute(attribute1);
        ci.addAttribute(attribute2);
        cis.add(ci);


        //assertThrows(CIValidationException.class,() -> proc.processAndValidateVars(cis, new HashMap<>(), new HashMap<>(), new HashMap<>()));
        try {
            proc.processAndValidateVars(cis, new HashMap<>(), new HashMap<>(), new HashMap<>());
            fail();
        } catch (CIValidationException exception) {
            String message = exception.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("testAttribute1") && message.contains("testAttribute2") && message.contains("test1") && message.contains("test2"));
        } catch (Exception e) {
            fail(); // fail if any other exception thrown
        }
    }

    @Test
    public void testBomCiOrderComparator() {
        ArrayList<CmsCI> bomCis = new ArrayList<>();
        CmsCI ci_1 = new CmsCI();
        CmsCI ci_2 = new CmsCI();
        CmsCI ci_3 = new CmsCI();
        CmsCI ci_4 = new CmsCI();
        CmsCI ci_5 = new CmsCI();
        CmsCI ci_6 = new CmsCI();
        CmsCI ci_7 = new CmsCI();
        ci_7.setCiName("compute-125389672-1");//order 0
        ci_6.setCiName("compute-125389672-8");//order 1
        ci_5.setCiName("compute-125389672-9");//order 2
        ci_3.setCiName("compute-125389672-11");//order 3
        ci_4.setCiName("compute-125389672-101");//order 5
        ci_2.setCiName("compute-125389672-501");//order 6
        ci_1.setCiName("compute-125389672-50");//order 4

        bomCis.add(ci_1);
        bomCis.add(ci_2);
        bomCis.add(ci_3);
        bomCis.add(ci_4);
        bomCis.add(ci_5);
        bomCis.add(ci_6);
        bomCis.add(ci_7);
        Collections.sort(bomCis, BomRfcBulkProcessor.bomCiComparatorByName);

        assertEquals(bomCis.get(0).getCiName(), ci_7.getCiName());
        assertEquals(bomCis.get(1).getCiName(), ci_6.getCiName());
        assertEquals(bomCis.get(2).getCiName(), ci_5.getCiName());
        assertEquals(bomCis.get(3).getCiName(), ci_3.getCiName());
        assertEquals(bomCis.get(4).getCiName(), ci_1.getCiName());
        assertEquals(bomCis.get(5).getCiName(), ci_4.getCiName());
        assertEquals(bomCis.get(6).getCiName(), ci_2.getCiName());
    }

    @Test
    public void testSufficientComputesCheckPass() {
        Map<String, List<CmsCI>> computesWithClouds = new HashMap<>();
       //cloud # 1 with at threshold # of computes
        ArrayList computes = new ArrayList();
        for (int i = 0; i <= BomRfcBulkProcessor.MIN_COMPUTES_SCALE; i++) {
            computes.add(new CmsCI());
        }
        computesWithClouds.put("cloud_1", computes);

        //cloud # 2 with above threshold # of computes
        computes = new ArrayList();
        for (int i = 0; i < BomRfcBulkProcessor.MIN_COMPUTES_SCALE + 2; i++) {
            computes.add(new CmsCI());
        }
        computesWithClouds.put("cloud_2", computes);
        assertTrue(new BomRfcBulkProcessor().hasSufficientComputes(computesWithClouds, 1, 3));
    }

    @Test
    public void testSufficientComputesCheckFail() {
        Map<String, List<CmsCI>> computesWithClouds = new HashMap<>();
        //cloud # 1 with at threshold # of computes
        ArrayList computes = new ArrayList();
        for (int i = 0; i <= BomRfcBulkProcessor.MIN_COMPUTES_SCALE; i++) {
            computes.add(new CmsCI());
        }
        computesWithClouds.put("cloud_1", computes);

        //cloud # 2 with below threshold # of computes
        computes = new ArrayList();
        for (int i = 0; i < BomRfcBulkProcessor.MIN_COMPUTES_SCALE - 1; i++) {
            computes.add(new CmsCI());
        }
        computesWithClouds.put("cloud_2", computes);
        assertFalse(new BomRfcBulkProcessor().hasSufficientComputes(computesWithClouds, 1, 3));

        //now add more computes to both clouds , still should fail because of the minComputesInEachCloud param value
        computes = new ArrayList();
        for (int i = 0; i < BomRfcBulkProcessor.MIN_COMPUTES_SCALE * 3; i++) {
            computes.add(new CmsCI());
        }
        computesWithClouds.get("cloud_1").addAll(computes);
        computesWithClouds.get("cloud_2").addAll(computes);

        assertFalse(new BomRfcBulkProcessor().hasSufficientComputes(computesWithClouds, 1,
                computesWithClouds.get("cloud_1").size()));
    }
}