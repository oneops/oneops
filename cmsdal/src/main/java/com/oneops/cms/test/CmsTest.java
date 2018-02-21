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
package com.oneops.cms.test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oneops.cms.ns.service.CmsNsProcessor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.google.gson.Gson;
import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.service.CmsCmManagerImpl;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.md.dal.ClazzMapper;
import com.oneops.cms.md.dal.RelationMapper;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsClazzRelation;
import com.oneops.cms.md.service.CmsMdManagerImpl;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.dal.NSMapper;
import com.oneops.cms.ns.service.CmsNsManagerImpl;
import com.oneops.cms.service.OneopsCmsManager;
import com.oneops.cms.util.CmsCmValidator;
import com.oneops.cms.util.domain.AttrQueryCondition;

/**
 * The Class CmsTest.
 */
public class CmsTest {

	private static CmsCmProcessor cmMan = new CmsCmProcessor();
	private static CmsCmValidator cmValidator = new CmsCmValidator();
	private static CmsNsProcessor nsMan = new CmsNsProcessor();
	private static CmsMdProcessor mdMan = new CmsMdProcessor();
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SQLException the sQL exception
	 */
	public static void main(String[] args) throws IOException, SQLException {
		testProcedure();
	}	

	private static void testProcedure() {
		CmsOpsProcedure proc = new CmsOpsProcedure();
		proc.setCiId(123);
		proc.setProcedureCiId(456);
		proc.setArglist("arg1:value1,arg2:value2");
		Gson gson = new Gson();
		System.out.println(gson.toJson(proc));
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static void _main(String[] args) throws IOException, SQLException {
		String resource = "mybatis-config.xml";
		Reader reader = Resources.getResourceAsReader(resource);
		SqlSessionFactory sqlsf = new SqlSessionFactoryBuilder().build(reader);

		SqlSession session = sqlsf.openSession();
		CIMapper ciMapper = session.getMapper(CIMapper.class);
		ClazzMapper clMapper = session.getMapper(ClazzMapper.class);
		NSMapper nsMapper = session.getMapper(NSMapper.class);
		RelationMapper rlMapper = session.getMapper(RelationMapper.class);
		CmsCmProcessor cmProcessor = new CmsCmProcessor();
		
		cmProcessor.setCiMapper(ciMapper);
		cmProcessor.setCmValidator(cmValidator);
		cmProcessor.setCmsNsProcessor(nsMan);
		
		cmValidator.setCmsMdProcessor(mdMan);
		cmValidator.setCmsNsProcessor(nsMan);
		
		nsMan.setNsMapper(nsMapper);
		
		CmsMdProcessor mdProcessor = new CmsMdProcessor();
		mdProcessor.setClazzMapper(clMapper);
		mdProcessor.setRelationMapper(rlMapper);
		
		
		
		try {
			testQueryAttrs(ciMapper);
			//testState(sqlsf);
			//testClazzes(sqlsf);
			//testCreateOrg();
			//testCreateAssembly();
			//testCreatPlatform(sqlsf);
			//int newRelId = testCreateRelation(sqlsf, 1048, 1052);
			//testGetRelation(sqlsf, newRelId);
			//testGetToFromRelation(sqlsf, 1048);
			//testUpdateCI(sqlsf);
			//testDeleteCi(sqlsf);
			//testGetCiById(sqlsf);
			//testGetCi(sqlsf);
			//testCreateClazz(sqlsf);
			//testGetClazz(sqlsf);
			session.commit();
		} finally {
			session.close();
		}
	}

	private static void testQueryAttrs(CIMapper ciMapper) {
		AttrQueryCondition attr1 = new AttrQueryCondition();
		attr1.setAttributeName("tag");
		attr1.setAvalue("master2");
		attr1.setCondition("eq");

		List<AttrQueryCondition> attrList = new ArrayList<AttrQueryCondition>();
		attrList.add(attr1);
		Gson gson = new Gson();
		List <CmsCI> cis = ciMapper.getCIbyAttributes("/oneops/Assembly1", "catalog.Software", null, attrList);
		System.out.println(gson.toJson(cis));
		cis = ciMapper.getCIbyAttributes("/oneops/Assembly1", null, "Software", attrList);
		System.out.println(gson.toJson(cis));
	}
	
	@SuppressWarnings("unused")
	private static void testState(SqlSessionFactory sqlsf) {
		SqlSession session = sqlsf.openSession();
		DJMapper djMapper = session.getMapper(DJMapper.class);

		try {
			Integer stateId = djMapper.getReleaseStateId("qqqq");
		} finally {
			session.close();
		}
	
	}

	@SuppressWarnings("unused")
	private static void testClazzes(SqlSessionFactory sqlsf) {
		OneopsCmsManager cmsman = new OneopsCmsManager();
		cmsman.setSqlSessionFactory(sqlsf);
		for (CmsClazz clazz : cmsman.getClazzes()) {
			System.out.println(clazz.getClassName());
			for (CmsClazzAttribute attr : clazz.getMdAttributes()) {
				System.out.println("--->" + attr.getAttributeName());
			}
			System.out.println("From Relations:");
			for (CmsClazzRelation rel : clazz.getFromRelations()) {
				System.out.println(">>>>>" + rel.getRelationName());
			}
			System.out.println("To Relations:");
			for (CmsClazzRelation rel : clazz.getToRelations()) {
				System.out.println(">>>>>" + rel.getRelationName());
			}
		}
	}

	@SuppressWarnings("unused")
	private static long testCreateAssembly() {
		CmsCI ci = new CmsCI();
		
		ci.setCiClassName("account.Assembly");
		ci.setCiName("ZikaSoftAssembly5");
		ci.setCiState("default");
		ci.setComments("Zika's assembly");
		ci.setNsPath("/ZikaSoft");
	
		CmsCIAttribute desc = new CmsCIAttribute();
		desc.setAttributeName("description");
		desc.setDfValue("df value");
		desc.setComments("desc comments");
		
		ci.addAttribute(desc);
		
		cmMan.createCI(ci);
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(ci));
		return ci.getCiId();
	}

	@SuppressWarnings("unused")
	private static long testCreatPlatform() {
		
		CmsCI ci = new CmsCI();
		
		ci.setCiClassName("design.Platform");
		ci.setCiName("ZikaPlatform");
		ci.setCiState("default");
		ci.setComments("Zika's platform");
		ci.setNsPath("/ZikaSoft/ZikaSoftAssembly5");
		
		CmsCIAttribute desc = new CmsCIAttribute();
		desc.setAttributeName("description");
		desc.setDfValue("df value");
		desc.setComments("desc comments");
		
		ci.addAttribute(desc);
		
		cmMan.createCI(ci);

		Gson gson = new Gson();
		System.out.println(gson.toJson(ci));
		return ci.getCiId();
	}

	@SuppressWarnings("unused")
	private static long testCreateRelation(int fromCiId, int toCiId) {
		
		CmsCIRelation ciRel = new CmsCIRelation();
		
		ciRel.setFromCiId(fromCiId);
		ciRel.setComments("testRelation");
		ciRel.setRelationName("account.ComposedOf");
		ciRel.setRelationState("default");
		ciRel.setToCiId(toCiId);
		
		CmsCIRelationAttribute enabled = new CmsCIRelationAttribute();
		enabled.setAttributeName("enabled");
		enabled.setComments("test_rel_attr");
		enabled.setDfValue("df value");
		
		ciRel.addAttribute(enabled);
		
		CmsCIRelation newRel = cmMan.createRelation(ciRel);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRel));
		return newRel.getCiRelationId();
	}
	
	@SuppressWarnings("unused")
	private static void testUpdateCI() {
		
		CmsCI ci = new CmsCI();
		
		ci.setCiId(1048);
		ci.setCiClassName("account.Assembly");
		ci.setCiName("ZikaSoftAssembly5");
		ci.setCiState("default");
		ci.setComments("Zika's assembly new  ");
		ci.setNsPath("/ZikaSoft");
		
		CmsCIAttribute label = new CmsCIAttribute();
		label.setAttributeName("label");
		label.setDfValue("df updated");
		label.setComments("comments");
	
		CmsCIAttribute desc = new CmsCIAttribute();
		desc.setAttributeName("description");
		desc.setDfValue("df updated");
		desc.setComments("desc comments");
		
		ci.addAttribute(label);
		ci.addAttribute(desc);
		
		cmMan.updateCI(ci);

		Gson gson = new Gson();
		System.out.println(gson.toJson(ci));
	}
	
	@SuppressWarnings("unused")
	private static void testCreateOrg() {
		CmsCI ci = new CmsCI();
		
		ci.setCiClassName("account.Organization");
		ci.setCiName("ZikaSoft");
		ci.setCiState("default");
		ci.setComments("Zika's org");
		ci.setNsPath("/");
		
		cmMan.createCI(ci);

		Gson gson = new Gson();
		System.out.println(gson.toJson(ci));
		
	}

	@SuppressWarnings("unused")
	private static void testDeleteCi() throws SQLException {

		cmMan.deleteCI(1040, "TestUser");

		System.out.println("Deleted");
	}
	
	
	@SuppressWarnings("unused")
	private static void testGetCi() throws SQLException {
		
		List<CmsCI> ciList = null;
		ciList = cmMan.getCiBy3("/ZikaSoft", "account.Assembly", "ZikaSoftAssembly");

		Gson gson = new Gson();
		System.out.println(gson.toJson(ciList));
	}

	@SuppressWarnings("unused")
	private static void testGetRelation(int relId) throws SQLException {
		CmsCIRelation rel = cmMan.getRelationById(relId);
		Gson gson = new Gson();
		System.out.println(gson.toJson(rel));
	}

	@SuppressWarnings("unused")
	private static void testGetToFromRelation(int ciId) throws SQLException {
		
		List<CmsCIRelation> fromList = null;
		List<CmsCIRelation> toList = null;

		fromList = cmMan.getFromCIRelations(ciId, "account.ComposedOf", "design.Platform");
		toList = cmMan.getToCIRelations(ciId, "account.ComposedOf", "account.Assembly");

		Gson gson = new Gson();
		System.out.println(gson.toJson(fromList));
		System.out.println(gson.toJson(toList));
		
	}
	
	
	@SuppressWarnings("unused")
	private static void testGetCiById() throws SQLException {
				
		CmsCI ci = cmMan.getCiById(1038);
		Gson gson = new Gson();
		System.out.println(gson.toJson(ci));
	
	}
	
}
