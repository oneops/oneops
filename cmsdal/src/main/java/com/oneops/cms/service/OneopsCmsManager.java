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
package com.oneops.cms.service;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.oneops.cms.md.dal.ClazzMapper;
import com.oneops.cms.md.domain.CmsClazz;



/**
 * The Class OneopsCmsManager.
 */
public class OneopsCmsManager {
	private SqlSessionFactory sqlSessionFactory;
	
	/**
	 * Test it.
	 *
	 * @param id the id
	 * @return the string
	 */
	public String testIt(int id) {
		SqlSession session = sqlSessionFactory.openSession();
		String clazzName;
		try {
			ClazzMapper mapper = session.getMapper(ClazzMapper.class);
			CmsClazz cl = mapper.getClazz("CI");
			clazzName = cl.getClassName();
		} finally {
			session.close();
		}
		return clazzName;
	}

	/**
	 * Gets the clazzes.
	 *
	 * @return the clazzes
	 */
	public List<CmsClazz> getClazzes() {
		SqlSession session = sqlSessionFactory.openSession();
		List<CmsClazz> clazzes;
		try {
			ClazzMapper mapper = session.getMapper(ClazzMapper.class);
			clazzes = mapper.getClazzes();
			for(CmsClazz clazz : clazzes) {
				clazz.setMdAttributes(mapper.getClazzAttrs(clazz.getClassId()));
				clazz.setFromRelations(mapper.getFromClazzRelations(clazz.getClassId()));
				clazz.setToRelations(mapper.getToClazzRelations(clazz.getClassId()));
			}
		} finally {
			session.close();
		}
		return clazzes;
	}
	
	
	/**
	 * Gets the sql session factory.
	 *
	 * @return the sql session factory
	 */
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	/**
	 * Sets the sql session factory.
	 *
	 * @param sqlSessionFactory the new sql session factory
	 */
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}
}
