package com.oneops.transistor.util;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.transistor.exceptions.TransistorException;
import junit.framework.Assert;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oneops.cms.util.CmsConstants.ACCOUNT_CLOUD_CLASS;
import static com.oneops.cms.util.CmsConstants.MANIFEST_REQUIRES;
import static com.oneops.cms.util.CmsConstants.BASE_CONSUMES;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;


public class CloudUtilTest {


    @Test
    public void allServicesConfigured() {
        CmsCmProcessor cmsProcessor = mock(CmsCmProcessor.class);
        CmsCmRfcMrgProcessor cmRfcMrgProcessor = mock(CmsCmRfcMrgProcessor.class);
        CloudUtil cl = new CloudUtil();
        cl.setCmProcessor(cmsProcessor);
        cl.setCmRfcMrgProcessor(cmRfcMrgProcessor);
        //required services -s1,s2,s3
        //cloud services c1-s1,s2

        doAnswer(new Answer<List<CmsRfcRelation>>() {
            public List<CmsRfcRelation> answer(InvocationOnMock invocation) {
                return Stream.of(rfcRelation(Collections.singletonMap("services", "s1,s2,s3")))
                        .collect(toList());
            }
        }).when(cmRfcMrgProcessor).getFromCIRelationsNaked(anyLong(), eq(MANIFEST_REQUIRES), anyString(), anyString());

        doAnswer(new Answer<List<CmsCIRelation>>() {
            public List<CmsCIRelation> answer(InvocationOnMock invocation) {
                return Stream.of(relation(Collections.singletonMap("services", "s1,s2,s3")))
                        .collect(toList());
            }
        }).when(cmsProcessor).getFromCIRelationsNaked(anyLong(), eq("base.Provides"), anyString(), anyString());


        doAnswer(new Answer<List<CmsCIRelation>>() {
            public List<CmsCIRelation> answer(InvocationOnMock invocation) {
                return Stream.of(relation(null, "base.consumes"))
                        .collect(toList());
            }
        }).when(cmRfcMrgProcessor).getFromCIRelationsNaked(anyLong(), eq("base.Consumes"), eq("account.CloudUtil"), eq("dj"));

        cl.check4missingServices(Collections.singleton(1l));
    }

    @Test(expectedExceptions = {TransistorException.class})
    public void someMissingServicesCloud() throws Exception {
        CloudUtil cl = new CloudUtil();
        CmsCmProcessor cmsProcessor = mock(CmsCmProcessor.class);
        cl.setCmProcessor(cmsProcessor);
        CmsCmRfcMrgProcessor cmRfcMrgProcessor = mock(CmsCmRfcMrgProcessor.class);
        cl.setCmRfcMrgProcessor(cmRfcMrgProcessor);
        //required services -s1,s2,s3
        //cloud services c1-s1,s2

        String requiredServices = "s1,s2,s3";
        String configuredService = "s1";
        doAnswer(new Answer<List<CmsRfcRelation>>() {
            public List<CmsRfcRelation> answer(InvocationOnMock invocation) {
                return Stream.of(rfcRelation(Collections.singletonMap("services", requiredServices)))
                        .collect(toList());
            }
        }).when(cmRfcMrgProcessor).getFromCIRelationsNaked(anyLong(), eq("manifest.Requires"), anyString(), anyString());

        doAnswer(new Answer<List<CmsCIRelation>>() {
            public List<CmsCIRelation> answer(InvocationOnMock invocation) {
                return Stream.of(relation(Collections.singletonMap("services", configuredService)))
                        .collect(toList());
            }
        }).when(cmsProcessor).getFromCIRelationsNaked(anyLong(), eq("base.Provides"), anyString());

        doAnswer(new Answer<List<CmsRfcRelation>>() {
            public List<CmsRfcRelation> answer(InvocationOnMock invocation) {
                return Stream.of(rfcRelation(Collections.singletonMap("services", configuredService)))
                        .collect(toList());
            }
        }).when(cmRfcMrgProcessor).getFromCIRelations(anyLong(), eq(BASE_CONSUMES), eq(ACCOUNT_CLOUD_CLASS), eq("dj"));
        try {
            Set<Long> platforms = Arrays.asList(1l, 2l).stream().collect(Collectors.toSet());
            cl.check4missingServices(platforms);
        } catch (TransistorException e) {
            Assert.assertTrue(e.getMessage().contains("s2, s3"));
            throw e;
        }

    }


    private CmsCIRelation relation(CmsCI toCi, String relationName) {
        CmsCIRelation rel = new CmsCIRelation();
        rel.setToCi(toCi);
        rel.setRelationName(relationName);
        return rel;
    }


    CmsCIRelation relation(Map<String, String> attribs) {

        CmsCIRelation rel = new CmsCIRelation();
        CmsCIRelationAttribute attribute = new CmsCIRelationAttribute();
        attribute.setDjValue(attribs.get("services"));
        rel.setAttributes(Collections.singletonMap("service", attribute));
        return rel;
    }

    CmsRfcRelation rfcRelation(Map<String, String> m) {
        CmsRfcRelation relation = new CmsRfcRelation();
        relation.setToCiId(123l);
        relation.setAttributes(Collections.singletonMap("services", rfcAttribute(m.get("services"))));
        CmsRfcCI fromRfcCi = new CmsRfcCI();
        fromRfcCi.setCiName("c1");
        relation.setFromRfcCi(fromRfcCi);
        CmsRfcCI toRfcCi = new CmsRfcCI();
        toRfcCi.setCiName("c1");
        relation.setToRfcCi(toRfcCi);
        relation.setNsPath("/assembly/org/env/manifest/tc/1");
        return relation;
    }

    private CmsRfcAttribute rfcAttribute(String services) {
        CmsRfcAttribute attrib = new CmsRfcAttribute();
        attrib.setNewValue(services);
        return attrib;
    }

    private CmsRfcAttribute rfcAttribute() {
        return new CmsRfcAttribute();
    }

    @Test
    public void testGetMissingServices() throws Exception {


    }

}