package com.oneops.cms.dj.dal;

import com.oneops.cms.dj.domain.CmsRfcAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;


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
}
