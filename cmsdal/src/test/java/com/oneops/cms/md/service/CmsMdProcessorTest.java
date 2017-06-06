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
package com.oneops.cms.md.service;

import com.oneops.cms.md.dal.ClazzMapper;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAction;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsMdValidator;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.List;

/**
 * The Class CmsMdProcessorTest.
 */
public class CmsMdProcessorTest {
	private static SqlSession session;
	private static ClazzMapper clazzMapper;
	private static CmsMdValidator mdValidator;
	private static CmsMdProcessor mdProcessor;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        System.out.println("Starting up CmsMdProcessor tests");
	    String resource = "mybatis-config.xml";
	    Reader reader = Resources.getResourceAsReader( resource );
	    SqlSessionFactory sf = new SqlSessionFactoryBuilder().build(reader);
        session = sf.openSession();
	    clazzMapper = session.getMapper( ClazzMapper.class );
	    mdValidator = new CmsMdValidator();
	    mdProcessor = new CmsMdProcessor();
	    mdProcessor.setClazzMapper( clazzMapper );
	    mdProcessor.setMdValidator( mdValidator );
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
        System.out.println("Closing down CmsMdProcessor tests");
    }

    /**
     * Gets the update clazz test.
     *
     * @return the update clazz test
     * @throws Exception the exception
     */
    @Test
	public void getUpdateClazzTest() throws Exception {
	    int clazzId = 1057;
	    CmsClazz clazz = clazzMapper.getClazzById( clazzId );
	    Assert.assertNotNull("Loaded Clazz is null", clazz);
        Assert.assertEquals( clazz.getClassId(), clazzId );

	    List<CmsClazzAction> actions = clazzMapper.getClazzActions( clazzId );
	    int initActionSize = actions.size();
	    actions.remove( actions.size()-1);

	    Assert.assertEquals( actions.size(), initActionSize - 1 );
	    clazz.setActions( actions );
	    clazzMapper.flushCache();
	    mdProcessor.updateClazz( clazz );

	    CmsClazz clazz1 = mdProcessor.getClazz( clazzId , true);
	    List<CmsClazzAction> actions1 = clazz1.getActions();
	    Assert.assertEquals( initActionSize - 1, actions1.size() );

    }


}
