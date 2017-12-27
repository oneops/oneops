package com.oneops.cms.dj.service;

import static org.junit.Assert.*;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import org.testng.annotations.Test;

@Test
public class CmsRfcUtilTest {
    @Test
    public void testMergeRfcAndCi() {
        //create ci and rfc
        CmsRfcCI rfc = new CmsRfcCI();
        CmsRfcAttribute rfcAttribute_1 = createRfcAttribute("1");
        rfc.addAttribute(rfcAttribute_1);

        CmsRfcAttribute rfcAttribute_2 = createRfcAttribute("2");
        rfc.addAttribute(rfcAttribute_2);

        CmsCI ci = new CmsCI();
        ci.setNsPath("/test/ns/path");
        CmsRfcUtil rfcUtil = new CmsRfcUtil();

        CmsCIAttribute ciAttribute_1 = createCiAttribute("1");
        ci.addAttribute(ciAttribute_1);

        CmsCIAttribute ciAttribute_2 = createCiAttribute("2");
        ci.addAttribute(ciAttribute_2);

        ciAttribute_1.setDjValue("dj_value_1");
        ciAttribute_2.setDjValue("dj_value_2");

        //test that old values do get set in the rfc
        CmsRfcCI result = rfcUtil.mergeRfcAndCi(rfc, ci, "df");
        assertEquals(result.getAttribute("name-1").getOldValue(), ciAttribute_1.getDfValue());
        assertEquals(result.getAttribute("name-2").getOldValue(), ciAttribute_2.getDfValue());

        //test with dj values

        result = rfcUtil.mergeRfcAndCi(rfc, ci, "dj");
        assertEquals(result.getAttribute("name-1").getOldValue(), ciAttribute_1.getDjValue());
        assertEquals(result.getAttribute("name-2").getOldValue(), ciAttribute_2.getDjValue());

        //test if the attribute is not present in the rfc, it gets added
        CmsCIAttribute ciAttribute_3 = createCiAttribute("3");
        ci.addAttribute(ciAttribute_3);

        result = rfcUtil.mergeRfcAndCi(rfc, ci, "df");

        assertEquals(result.getAttribute("name-3").getNewValue(), ciAttribute_3.getDfValue());

        //test if the rfc is of type "add", all of ci's attributes do get set to rfc with "new value" as ci's attribute value
        // also test if the attribtue is not in rfc, it does get added in this case

        rfc.setRfcActionId(100);
        rfcAttribute_1.setNewValue("");
        rfc.addAttribute(rfcAttribute_1);

        result = rfcUtil.mergeRfcAndCi(rfc, ci, "df");
        assertEquals(result.getAttribute("name-1").getNewValue(), ciAttribute_1.getDfValue());

        CmsCIAttribute ciAttribute_4 = createCiAttribute("4");
        ci.addAttribute(ciAttribute_4);

        result = rfcUtil.mergeRfcAndCi(rfc, ci, "df");

        assertEquals(result.getAttribute("name-4").getNewValue(), ciAttribute_4.getDfValue());
    }

    private CmsCIAttribute createCiAttribute(String suffix) {
        CmsCIAttribute ciAttribute = new CmsCIAttribute();
        ciAttribute.setAttributeName("name-" + suffix);
        ciAttribute.setDfValue("df_value_" + suffix);
        return ciAttribute;
    }

    private CmsRfcAttribute createRfcAttribute(String suffix) {
        CmsRfcAttribute rfcAttribute = new CmsRfcAttribute();
        rfcAttribute.setAttributeName("name-" + suffix);
        rfcAttribute.setNewValue("new-value-" + suffix);
        return rfcAttribute;
    }
}