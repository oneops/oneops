package com.oneops.cms.dj.dal;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Test for CmsDpmtProcessorTest;
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext-test.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class DjMapperTest {

    @Autowired
    private DJMapper djMapper;
    private String nsPath = "/test/";


    @Test
    public void testInsertRfcCIAttribute() throws Exception {
        CmsRfcAttribute attr = new CmsRfcAttribute();
        attr.setRfcAttributeId(1);
        attr.setRfcId(1001);
        attr.setAttributeId(101);
        attr.setAttributeName("testAttribute");
        attr.setNewValue("testValue");
        djMapper.insertRfcCIAttribute(attr);
        List<CmsRfcAttribute> list = djMapper.getRfcCIAttributes(1001);
        
        assertEquals(1, list.size());
        assertEquals(attr.getAttributeName(), list.get(0).getAttributeName());
        assertEquals(attr.getNewValue(), list.get(0).getNewValue());
    }


    @Test
    public void testAltNs() throws Exception {
        
        assertNull(djMapper.getTagId("test"));
        djMapper.createTag("test");
        assertNotNull(djMapper.getTagId("test"));


        List<CmsAltNs> list = djMapper.getAltNsBy(1);
        assertEquals(list.size(), 0);
        
        djMapper.createAltNs(1, djMapper.getTagId("test"), 1);
        
        list = djMapper.getAltNsBy(1);
        assertEquals(list.size(), 1);
        
        djMapper.deleteAltNs(1, 1);
        
        list = djMapper.getAltNsBy(1);
        assertEquals(list.size(), 0);

    }
}
