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
import java.util.List;

import com.oneops.cms.ns.service.CmsNsProcessor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.google.gson.Gson;
import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmDjManagerImpl;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsDjManagerImpl;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.md.dal.ClazzMapper;
import com.oneops.cms.md.dal.RelationMapper;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.dal.NSMapper;
import com.oneops.cms.util.CmsCmValidator;
import com.oneops.cms.util.CmsDJValidator;

/**
 * The Class DJTests.
 */
public class DJTests {

	private static CmsCmValidator cmValidator = new CmsCmValidator();
	private static CmsDJValidator djValidator = new CmsDJValidator();
	private static CmsNsProcessor nsProc = new CmsNsProcessor();
	private static CmsMdProcessor mdProc = new CmsMdProcessor();
	private static CmsDjManagerImpl djMan = new CmsDjManagerImpl();
	private static CmsCmDjManagerImpl cmdjMan = new CmsCmDjManagerImpl();
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException {
		String resource = "mybatis-config.xml";
		Reader reader = Resources.getResourceAsReader(resource);
		SqlSessionFactory sqlsf = new SqlSessionFactoryBuilder().build(reader);

		SqlSession session = sqlsf.openSession();
		CIMapper ciMapper = session.getMapper(CIMapper.class);
		ClazzMapper clMapper = session.getMapper(ClazzMapper.class);
		NSMapper nsMapper = session.getMapper(NSMapper.class);
		RelationMapper rlMapper = session.getMapper(RelationMapper.class);
		DJMapper djMapper = session.getMapper(DJMapper.class);
		CmsRfcProcessor rfcProcessor = new CmsRfcProcessor();
		CmsCmProcessor cmProcessor = new CmsCmProcessor();
		
		//cmMan.setCiMapper(ciMapper);
		//cmMan.setCmValidator(cmValidator);
		//cmMan.setNsManager(nsProc);
		
		cmProcessor.setCiMapper(ciMapper);
		cmProcessor.setCmValidator(cmValidator);
		cmProcessor.setCmsNsProcessor(nsProc);

		cmValidator.setCmsMdProcessor(mdProc);
		cmValidator.setCmsNsProcessor(nsProc);
		
		djValidator.setCmsMdProcessor(mdProc);
		djValidator.setCmsNsProcessor(nsProc);
		
		nsProc.setNsMapper(nsMapper);
		
		CmsMdProcessor mdProcessor = new CmsMdProcessor();
		mdProcessor.setClazzMapper(clMapper);
		mdProcessor.setRelationMapper(rlMapper);
		
	
		rfcProcessor.setDjMapper(djMapper);
		rfcProcessor.setCmsNsProcessor(nsProc);
		rfcProcessor.setDjValidator(djValidator);
		rfcProcessor.setCiMapper(ciMapper);
		
		djMan.setRfcProcessor(rfcProcessor);
		
		
		CmsCmRfcMrgProcessor cmrfcProcessor = new CmsCmRfcMrgProcessor();
		cmrfcProcessor.setCmProcessor(cmProcessor);
		cmrfcProcessor.setRfcProcessor(rfcProcessor);
		cmrfcProcessor.setDjValidator(djValidator);
		
		cmdjMan.setCmRfcMrgProcessor(cmrfcProcessor);
		
		try {
			//testCreateRelease();
			//testUpdateRelease();
			//testCreateRrfcCi();
			//testUpdateRrfcCi();
			//testRmRrfcCi();
			//testGetRfcCi();
			//testCreateRrfcRelation();
			//testUpdateRrfcRelation();
			testGetRfcCI(); 
			session.commit();
		} finally {
			session.close();
		}
	}

	private static void testGetRfcCI() {
		CmsRfcCI rfcCi = cmdjMan.getCiById(1129, "df");
		Gson gson = new Gson();
		System.out.println(gson.toJson(rfcCi));
	}

	@SuppressWarnings("unused")
	private static void testCreateRelease() {
		CmsRelease release = new CmsRelease();
		release.setNsPath("/ZikaSoft/ZikaSoftAssembly5");
		release.setCreatedBy("Zika");
		release.setDescription("test release");
		release.setReleaseName("Zikas release");
		release.setReleaseState("open");
		release.setRevision(1);
		
		CmsRelease newRelease = djMan.createRelease(release);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRelease));
	}

	@SuppressWarnings("unused")
	private static void testUpdateRelease() {
		List<CmsRelease> releaseList = djMan.getReleaseBy3("/ZikaSoft/ZikaSoftAssembly5", null, null);
		for (CmsRelease release : releaseList) {
			release.setDescription("test release desc");
			release.setReleaseName("Zikas release updated");
			release.setReleaseState("design");
			release.setRevision(2);
			CmsRelease newRelease = djMan.updateRelease(release);
			Gson gson = new Gson();
			System.out.println(gson.toJson(newRelease));

		}
	}

	@SuppressWarnings("unused")
	private static void testCreateRrfcCi() {
		CmsRfcCI rfc = new CmsRfcCI();
		rfc.setCiClassName("catalog.Platform");
		rfc.setCiName("ZikasPlatform");
		rfc.setComments("My comments");
		rfc.setNsPath("/ZikaSoft/ZikaSoftAssembly5");
		rfc.setReleaseId(1033);
		rfc.setRfcAction("add");
		
		CmsRfcAttribute attr = new CmsRfcAttribute();
		attr.setAttributeName("description");
		attr.setNewValue("platform desc");
		rfc.addAttribute(attr);
		CmsRfcCI newRfc = djMan.createRfcCI(rfc);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRfc));
	}

	@SuppressWarnings("unused")
	private static void testUpdateRrfcCi() {
		CmsRfcCI rfc = new CmsRfcCI();
		rfc.setRfcId(1102);
		rfc.setComments("My comments updated");
		rfc.setNsPath("/ZikaSoft/ZikaSoftAssembly5");
		rfc.setReleaseId(1009);
		rfc.setRfcAction("add");
		
		CmsRfcAttribute attr = new CmsRfcAttribute();
		attr.setAttributeName("description");
		attr.setNewValue("updated platform desc");
		rfc.addAttribute(attr);
		CmsRfcCI newRfc = djMan.updateRfcCI(rfc);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRfc));
	}

	@SuppressWarnings("unused")
	private static void testRmRrfcCi() {
		long rmed = djMan.rmRfcCiFromRelease(1102);
		System.out.println(rmed);
	}

	@SuppressWarnings("unused")
	private static void testGetRfcCi() {
		CmsRfcCI newRfc = djMan.getRfcCIById(1101);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRfc));
	}
	
	@SuppressWarnings("unused")
	private static void testCreateRrfcRelation() {
		CmsRfcRelation relation = new CmsRfcRelation();
		relation.setComments("My comments");
		relation.setFromCiId(1022L);
		relation.setRelationName("account.ComposedOf");
		relation.setReleaseId(1009L);
		relation.setRfcAction("add");
		relation.setToCiId(1024L);
		
		CmsRfcAttribute attr = new CmsRfcAttribute();
		attr.setAttributeName("enabled");
		attr.setNewValue("yes");
		relation.addAttribute(attr);
		
		CmsRfcRelation newRelation = djMan.createRfcRelation(relation);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRelation));
	}

	@SuppressWarnings("unused")
	private static void testUpdateRrfcRelation() {
		CmsRfcRelation relation = new CmsRfcRelation();
		relation.setComments("My comments updated");
		relation.setRfcId(1113);
		
		CmsRfcAttribute attr = new CmsRfcAttribute();
		attr.setAttributeName("enabled");
		attr.setNewValue("no");
		relation.addAttribute(attr);
		
		CmsRfcRelation newRelation = djMan.updateRfcRelation(relation);
		Gson gson = new Gson();
		System.out.println(gson.toJson(newRelation));
	}
}
