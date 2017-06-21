package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.crypto.CmsCryptoDES;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.util.CmsUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

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
    public void setup(){
        CmsUtil cmsUtil = new CmsUtil();
        cmsUtil.setCmsCrypto(new CmsCryptoDES());
        cmsUtil.setCountOfErrorsToReport(5);
        proc = new BomRfcBulkProcessor();
        proc.setCmsUtil(cmsUtil);
    }
    
    @Test
    public void processAndValidateVars(){
        
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
        } catch (CIValidationException exception){
            String message = exception.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("testAttribute1") && message.contains("testAttribute2") && message.contains("test1") && message.contains("test2"));
        } catch (Exception e){
            fail(); // fail if any other exception thrown
        }
    }

}