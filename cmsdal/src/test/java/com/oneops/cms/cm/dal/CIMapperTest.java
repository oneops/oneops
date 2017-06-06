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
package com.oneops.cms.cm.dal;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;

/**
 * The Class CIMapperTest.
 */
public class CIMapperTest {

	private static CIMapper ciMapper;
	private static SqlSession session; 

    private int classId = 1051;
    private int ciId = 100001;
    private int relId = 100001;
    private String nsPath = "/public";
    private String ciName = "ci test 1";

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("Starting up CIMapper tests");

		String resource = "mybatis-config.xml";
		Reader reader = Resources.getResourceAsReader(resource);
		SqlSessionFactory sf = new SqlSessionFactoryBuilder().build(reader);
        session = sf.openSession();
		ciMapper = session.getMapper(CIMapper.class);

	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@AfterClass 
	public static void tearDown() throws Exception {
		session.rollback();
		session.close();
		System.out.println("Closing down CIMapper tests");
	} 


    /**
     * Creates the and get ci test.
     *
     * @throws Exception the exception
     */
    @Test
	public void createAndGetCITest() throws Exception{
        int nsId = 1000;
        int ciStateId = 100;
        String goId = "ci-go-id";
        String comments = "ci comments test";

        CmsCI ci = new CmsCI();
        ci.setCiId(ciId);
		ci.setCiName(ciName);
        ci.setCiStateId(ciStateId);
        ci.setCiGoid(goId);
        ci.setComments(comments);
        ci.setCiClassId(classId);
        ci.setNsId(nsId);
        ci.setNsPath(nsPath);

		ciMapper.createCI(ci);

        CmsCI ci1 = ciMapper.getCIById(ciId);
		Assert.assertNotNull("Created CI is null", ci1);
        Assert.assertEquals(ci1.getCiId(), ciId);
		Assert.assertEquals(ci1.getCiName(), ciName);
        Assert.assertEquals(ci1.getCiStateId(), ciStateId);
        Assert.assertEquals(ci1.getCiClassId(), classId);
        Assert.assertEquals(ci1.getComments(), comments);
        Assert.assertNotNull("Created date CI is null", ci1.getCreated());
	}

    /**
     * Creates the and get ci attribute test.
     *
     * @throws Exception the exception
     */
    @Test
	public void createAndGetCIAttributeTest() throws Exception{
        int attributeId = 1028;
        int attributeId1 = 1048;
        String owner = "owner test";
        String comments = "ci attr comments test";
        String djValue = "dj value";
        String dfValue = "df value";


        CmsCIAttribute attribute = new CmsCIAttribute();
        attribute.setCiId(ciId);
		attribute.setAttributeId(attributeId);
        attribute.setOwner(owner);
        attribute.setComments(comments);
        attribute.setDfValue(dfValue);
        attribute.setDjValue(djValue);

		ciMapper.addCIAttribute(attribute);

        CmsCIAttribute attribute1 = new CmsCIAttribute();
        attribute1.setCiId(ciId);
		attribute1.setAttributeId(attributeId1);
        attribute1.setOwner(owner);
        attribute1.setComments(comments);
        attribute1.setDfValue(dfValue);
        attribute1.setDjValue(djValue);
        ciMapper.addCIAttributeAndPublish(attribute1);

        List<CmsCIAttribute> attrs = ciMapper.getCIAttrs(ciId);
        Assert.assertNotNull("Loaded CI attribute map is null", attrs);
        Assert.assertEquals(attrs.size(), 2);

        for (CmsCIAttribute a: attrs) {
            Assert.assertEquals(a.getComments(), comments);
            Assert.assertEquals(a.getDfValue(), dfValue);
            Assert.assertEquals(a.getDjValue(), djValue);
        }

	}


    /**
     * Update and get ci attribute test.
     *
     * @throws Exception the exception
     */
    @Test
	public void updateAndGetCIAttributeTest() throws Exception{
        String comments = "ci attr comments test new";
        String djValue = "dj value new";
        String dfValue = "df value new";


        List<CmsCIAttribute> attrs = ciMapper.getCIAttrs(ciId);
        Assert.assertNotNull("Loaded CI attribute map is null", attrs);
        Assert.assertEquals(attrs.size(), 2);

        for (CmsCIAttribute a: attrs) {
            a.setDfValue(dfValue);
            a.setDjValue(djValue);
            a.setComments(comments);
            ciMapper.updateCIAttribute(a);
        }

        attrs = ciMapper.getCIAttrs(ciId);
        Assert.assertNotNull("Loaded CI attribute map is null", attrs);
        Assert.assertEquals(attrs.size(), 2);

        for (CmsCIAttribute a: attrs) {
            Assert.assertEquals(a.getComments(), comments);
            Assert.assertEquals(a.getDfValue(), dfValue);
            Assert.assertEquals(a.getDjValue(), djValue);
        }

	}

    /**
     * Update and get ci test.
     *
     * @throws Exception the exception
     */
    @Test
	public void updateAndGetCITest() throws Exception{
        int ciStateId = 200;
        String comments = "ci comments test new";

        CmsCI ci = ciMapper.getCIById(ciId);
        ci.setCiStateId(ciStateId);
        ci.setComments(comments);

		ciMapper.updateCI(ci);

        CmsCI ci1 = ciMapper.getCIById(ciId);
		Assert.assertNotNull("Created CI is null", ci1);
        Assert.assertEquals(ci1.getCiId(), ciId);
		Assert.assertEquals(ci1.getCiName(), ciName);
        Assert.assertEquals(ci1.getCiStateId(), ciStateId);
        Assert.assertEquals(ci1.getComments(), comments);
        Assert.assertNotNull("Updated date CI is null", ci1.getUpdated());
	}


    /**
     * Delete ci test.
     *
     * @throws Exception the exception
     */
    @Test
	public void deleteCITest() throws Exception{
        int ciStateId = 200;
		ciMapper.deleteCI(ciId, false, "TestUser");

        CmsCI ci = ciMapper.getCIById(ciId);
		Assert.assertNotNull("Deleted CI is null", ci);
        Assert.assertEquals(ci.getCiStateId(), ciStateId);

        ciMapper.deleteCI(ciId, true, "TestUser");
        ci = ciMapper.getCIById(ciId);
		Assert.assertNull("Deleted CI is not null", ci);
	}


    /**
     * Creates the and get relation test.
     *
     * @throws Exception the exception
     */
    @Test
	public void createAndGetRelationTest() throws Exception{
        int nsId = 1000;
        int relationId = 2016;
        int ciStateId = 100;
        int fromci = 11784;
        int toci = 11854;
        String goId = "rel-go-id";
        String comments = "relation comments test";

        CmsCIRelation rel = new CmsCIRelation();
        rel.setCiRelationId(relId);
        rel.setRelationId(relationId);
        rel.setRelationStateId(ciStateId);
        rel.setRelationGoid(goId);
        rel.setComments(comments);
        rel.setNsId(nsId);
        rel.setNsPath(nsPath);
        rel.setFromCiId(fromci);
        rel.setToCiId(toci);

		ciMapper.createRelation(rel);

        CmsCIRelation rel1 = ciMapper.getCIRelation(relId);
		Assert.assertNotNull("Created Relation is null", rel1);
        Assert.assertEquals(rel1.getCiRelationId(), relId);
        Assert.assertEquals(rel1.getRelationId(), relationId);
        Assert.assertEquals(rel1.getRelationStateId(), ciStateId);
        Assert.assertEquals(rel1.getComments(), comments);
        Assert.assertEquals(rel1.getFromCiId(), fromci);
        Assert.assertEquals(rel1.getToCiId(), toci);
        Assert.assertNotNull("Created date Realtion is null", rel1.getCreated());
	}

    /**
     * Creates the and get relation attribute test.
     *
     * @throws Exception the exception
     */
    @Test
	public void createAndGetRelationAttributeTest() throws Exception{
        int attributeId = 1560;
        int attributeId1 = 1657;
        String owner = "owner test";
        String comments = "rel attr comments test";
        String djValue = "dj value";
        String dfValue = "df value";


        CmsCIRelationAttribute attribute = new CmsCIRelationAttribute();
        attribute.setCiRelationId(relId);
		attribute.setAttributeId(attributeId);
        attribute.setOwner(owner);
        attribute.setComments(comments);
        attribute.setDfValue(dfValue);
        attribute.setDjValue(djValue);

		ciMapper.addRelationAttribute(attribute);

        CmsCIRelationAttribute attribute1 = new CmsCIRelationAttribute();
        attribute1.setCiRelationId(relId);
		attribute1.setAttributeId(attributeId1);
        attribute1.setOwner(owner);
        attribute1.setComments(comments);
        attribute1.setDfValue(dfValue);
        attribute1.setDjValue(djValue);

		ciMapper.addRelationAttributeAndPublish(attribute1);

        List<CmsCIRelationAttribute> attrs = ciMapper.getCIRelationAttrs(relId);
        Assert.assertNotNull("Loaded Relation attribute map is null", attrs);
        Assert.assertEquals(attrs.size(), 2);

        for (CmsCIRelationAttribute a: attrs) {
            Assert.assertEquals(a.getComments(), comments);
            Assert.assertEquals(a.getDfValue(), dfValue);
            Assert.assertEquals(a.getDjValue(), djValue);
        }

	}

    /**
     * Update and get relation attribute test.
     *
     * @throws Exception the exception
     */
    @Test
	public void updateAndGetRelationAttributeTest() throws Exception{
        String comments = "ci attr comments test new";
        String djValue = "dj value new";
        String dfValue = "df value new";


        List<CmsCIRelationAttribute> attrs = ciMapper.getCIRelationAttrs(relId);
        Assert.assertNotNull("Loaded Relation attribute map is null", attrs);
        Assert.assertEquals(attrs.size(), 2);

        for (CmsCIRelationAttribute a: attrs) {
            a.setDfValue(dfValue);
            a.setDjValue(djValue);
            a.setComments(comments);
            ciMapper.updateCIRelationAttribute(a);
        }

        attrs = ciMapper.getCIRelationAttrs(relId);
        Assert.assertNotNull("Loaded Relation attribute map is null", attrs);
        Assert.assertEquals(attrs.size(), 2);

        for (CmsCIRelationAttribute a: attrs) {
            Assert.assertEquals(a.getComments(), comments);
            Assert.assertEquals(a.getDfValue(), dfValue);
            Assert.assertEquals(a.getDjValue(), djValue);
        }

	}

    /**
     * Delete relation test.
     *
     * @throws Exception the exception
     */
    @Test
	public void deleteRelationTest() throws Exception{
        int ciStateId = 200;
		ciMapper.deleteRelation(relId, false);

        CmsCIRelation rel = ciMapper.getCIRelation(relId);
		Assert.assertNotNull("Deleted Relation is null", rel);
        Assert.assertEquals(rel.getRelationStateId(), ciStateId);

        ciMapper.deleteRelation(relId, true);
        rel = ciMapper.getCIRelation(relId);
		Assert.assertNull("Deleted CI is not null", rel);
	}


}
