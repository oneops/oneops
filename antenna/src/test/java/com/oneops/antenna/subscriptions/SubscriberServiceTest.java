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
package com.oneops.antenna.subscriptions;


import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.crypto.CmsCrypto;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * SubscriberService test. These tests are not functional due to latest changes.
 * ToDo - Need to re-work it completely.
 */
public class SubscriberServiceTest {

    private CmsCmProcessor cmProcessor;
    private CmsCrypto cmsCrypto;
    private static final String NSPATH = "/a/b/c";
    private static final String NOTFOUND = "deadbeef";
    private static final long CI_ID = 10;


    @BeforeClass
    public void setUp() {
        cmProcessor = mock(CmsCmProcessor.class);
        when(cmProcessor.getEnvByNS(NOTFOUND)).thenReturn(null);

        CmsCI subsCi = new CmsCI();
        subsCi.setCiId(CI_ID);
        subsCi.setCiClassName("account.notification.sns.Sink");//TODO see can have others

        Map<String, CmsCIAttribute> attributes = new HashMap<String, CmsCIAttribute>();
        CmsCIAttribute ciAttr = new CmsCIAttribute();
        ciAttr.setDfValue(CmsCrypto.ENC_PREFIX);
        attributes.put("some-key", ciAttr);
        subsCi.setAttributes(attributes);

        CmsCIRelation rel = new CmsCIRelation();
        rel.setFromCiId(CI_ID);
        CmsCI toCi = new CmsCI();
        toCi.setCiId(CI_ID);
        toCi.setCiClassName("account.notification.sns.Sink");//TODO see can have others

        Map<String, CmsCIAttribute> attMap = new HashMap<>();
        CmsCIAttribute c1 = new CmsCIAttribute();
        c1.setDfValue("http://X");
        CmsCIAttribute c2 = new CmsCIAttribute();
        c2.setDfValue("nobody");
        CmsCIAttribute c3 = new CmsCIAttribute();
        c3.setDfValue("blahblah");
        CmsCIAttribute c4 = new CmsCIAttribute();
        c4.setDfValue("unlimited");
        CmsCIAttribute c5 = new CmsCIAttribute();
        c5.setDfValue("garden");
        attMap.put("service_url", c1);
        attMap.put("user", c2);
        attMap.put("password", c3);
        attMap.put("access", c4);
        attMap.put("secret", c5);

        toCi.setAttributes(attMap);
        rel.setToCi(toCi);


        when(cmProcessor.getEnvByNS(NSPATH)).thenReturn(subsCi);
        List<CmsCIRelation> assemblys = new ArrayList<>();
        assemblys.add(rel);

        when(cmProcessor.getToCIRelationsNaked(CI_ID, "base.RealizedIn", "account.Assembly")).thenReturn(assemblys);
        List<CmsCIRelation> orgs = new ArrayList<>();
        when(cmProcessor.getToCIRelationsNaked(CI_ID, "base.Manages", "account.Organization")).thenReturn(assemblys);//orgs);

        List<CmsCIRelation> subRels = new ArrayList<>();
        when(cmProcessor.getFromCIRelations(CI_ID, "base.ForwardsTo", null)).thenReturn(assemblys);//subRels);

        cmsCrypto = mock(CmsCrypto.class);
        try {
            when(cmsCrypto.decrypt(anyString())).thenReturn("true");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        //this.subService.setCmProcessor(cmProcessor);
        //this.subService.setCmsCrypto(cmsCrypto);
    }

    /**
     * Gets the subscribers for ns test empty. Send as if we have empty result,
     * but expect the system subscribe to be the 1 returned only
     *
     * @return the subscribers for ns test empty
     */
    @Test
    public void getSubscribersForNsTestEmpty() {
        //this will log a not found message and yield empty result
        // List<BasicSubscriber> outList = this.subService.getSubscribersForNs(NOTFOUND);
        // assertTrue(outList.size()==1);
    }

    /**
     * Gets the subscribers for ns test. Again but this time with a legit nspath.
     *
     * @return the subscribers for ns test
     */
    @Test
    public void getSubscribersForNsTest() {
        //again, this time with result
        // List<BasicSubscriber> outList = this.subService.getSubscribersForNs(NSPATH);
        // assertTrue(outList.size() > 1);
    }

}
