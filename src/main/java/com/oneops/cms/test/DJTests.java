package com.oneops.cms.test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

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
import com.oneops.cms.md.service.CmsMdManagerImpl;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.dal.NSMapper;
import com.oneops.cms.ns.service.CmsNsManagerImpl;
import com.oneops.cms.util.CmsCmValidator;
import com.oneops.cms.util.CmsDJValidator;

/**
 * The Class DJTests.
 */
public class DJTests {

	private static CmsCmValidator cmValidator = new CmsCmValidator();
	private static CmsDJValidator djValidator = new CmsDJValidator();
	private static CmsNsManagerImpl nsMan = new CmsNsManagerImpl();
	private static CmsMdManagerImpl mdMan = new CmsMdManagerImpl();
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
		//cmMan.setNsManager(nsMan);
		
		cmProcessor.setCiMapper(ciMapper);
		cmProcessor.setCmValidator(cmValidator);
		cmProcessor.setNsManager(nsMan);

		cmValidator.setMdManager(mdMan);
		cmValidator.setNsManager(nsMan);
		
		djValidator.setMdManager(mdMan);
		djValidator.setNsManager(nsMan);
		
		nsMan.setNsMapper(nsMapper);
		
		CmsMdProcessor mdProcessor = new CmsMdProcessor();
		mdProcessor.setClazzMapper(clMapper);
		mdProcessor.setRelationMapper(rlMapper);
		
		mdMan.setMdProcessor(mdProcessor);

		rfcProcessor.setDjMapper(djMapper);
		rfcProcessor.setNsManager(nsMan);
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
