/**
 * 
 */
package com.oneops.opamp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.opamp.service.EnvPropsProcessor;

/**
 * Testing class for EnvPropsProcessor
 *
 */
public class EnvPropsProcessorTest {
	private static Logger LOGGER = Logger.getLogger(EnvPropsProcessorTest.class);
	
	private CmsCmProcessor cmProcessorMockRepairs;
	private CmsCmProcessor cmProcessorMockScales;

//	private EnvPropsProcessor envPropsProcessor;
		
	private List<CmsCIRelation> manifestCiReps = new ArrayList<CmsCIRelation>();
	private List<CmsCIRelation> manifestCiScals= new ArrayList<CmsCIRelation>();//TODO
	private List<CmsCIRelation> envRels1 = new ArrayList<CmsCIRelation>();//TODO

	private static final long ID_WITH_AUTO_REPAIR = 11;
	private static final long ID_WITHOUT_AUTO_REPAIR = 10;
	private static final long ID_WITH_AUTO_SCALING = 21;
	private static final long ID_WITHOUT_AUTO_SCALING = 20;
	private static final long ID_NOT_FOUND = -1;
	
	@BeforeClass
	public void initFields(){
		
		CmsCIAttribute ciaAutoRepairTrue = new CmsCIAttribute();
		ciaAutoRepairTrue.setAttributeName("autorepair");
		ciaAutoRepairTrue.setDfValue("true");
		
		CmsCIAttribute ciaAutoScaleTrue = new CmsCIAttribute();
		ciaAutoScaleTrue.setAttributeName("autoscale");
		ciaAutoScaleTrue.setDfValue("true");
		
		
		CmsCI cmsCIAutoRepairTrue = new CmsCI();
		Map<String, CmsCIAttribute> attributesAr = new HashMap<String, CmsCIAttribute> (1);
		attributesAr.put("autorepair", ciaAutoRepairTrue);
		
		cmsCIAutoRepairTrue.setAttributes(attributesAr);
		
		CmsCI cmsCIAutoScaleTrue = new CmsCI();
		Map<String, CmsCIAttribute> attributesAs = new HashMap<String, CmsCIAttribute> (1);
		attributesAs.put("autoscale", ciaAutoScaleTrue);
		cmsCIAutoScaleTrue.setAttributes(attributesAs);
		
		cmsCIAutoScaleTrue.setAttributes(attributesAs);

		
		CmsCIRelation ciRelationAutoRepairTrue = new CmsCIRelation();
		ciRelationAutoRepairTrue.setFromCiId(ID_WITH_AUTO_REPAIR);
		ciRelationAutoRepairTrue.setFromCi(cmsCIAutoRepairTrue);
		manifestCiReps.add(ciRelationAutoRepairTrue);
		
		CmsCIRelation ciRelationAutoScaleTrue = new CmsCIRelation();
		ciRelationAutoScaleTrue.setFromCiId(ID_WITH_AUTO_SCALING);
		ciRelationAutoScaleTrue.setFromCi(cmsCIAutoScaleTrue);
		manifestCiScals.add(ciRelationAutoScaleTrue);
		
		
		this.cmProcessorMockRepairs= mock(CmsCmProcessor.class);
		when(cmProcessorMockRepairs.getToCIRelationsNakedNoAttrs(ID_WITH_AUTO_REPAIR, "base.RealizedAs", null, null)).thenReturn(manifestCiReps);
		when(cmProcessorMockRepairs.getToCIRelationsNakedNoAttrs(ID_WITH_AUTO_REPAIR, "manifest.Requires",null, "manifest.Platform")).thenReturn(manifestCiReps);
		when(cmProcessorMockRepairs.getToCIRelations(ID_WITH_AUTO_REPAIR, "manifest.ComposedOf",null, "manifest.Environment")).thenReturn(manifestCiReps);
		//
		this.cmProcessorMockScales=mock(CmsCmProcessor.class);
		when(cmProcessorMockScales.getToCIRelationsNakedNoAttrs(ID_WITH_AUTO_SCALING, "base.RealizedAs", null, null)).thenReturn(manifestCiScals);
		when(cmProcessorMockScales.getToCIRelationsNakedNoAttrs(ID_WITH_AUTO_SCALING, "manifest.Requires",null, "manifest.Platform")).thenReturn(manifestCiScals);
		when(cmProcessorMockScales.getToCIRelations(ID_WITH_AUTO_SCALING, "manifest.ComposedOf",null, "manifest.Environment")).thenReturn(manifestCiScals);
		
	}
	
	
	@Test
	public void repairAutoTest(){
		EnvPropsProcessor envPropsProcessor=new EnvPropsProcessor();

		envPropsProcessor.setCmProcessor(cmProcessorMockRepairs);

		CmsCI ciA = envPropsProcessor.isAutorepairEnbaled4bom(ID_WITH_AUTO_REPAIR);
		assertNotNull(ciA);
		assertEquals("true",ciA.getAttribute("autorepair").getDfValue());
			
		LOGGER.info("AUTOR*EPAIR ATT " +ciA.getAttribute("autorepair").getDfValue());
	}

	@Test 
	public void scaleAutoTest(){
		EnvPropsProcessor envPropsProcessor=new EnvPropsProcessor();

		envPropsProcessor.setCmProcessor(cmProcessorMockScales);

		CmsCI ciB = envPropsProcessor.isAutoScalingEnbaled4bom(ID_WITH_AUTO_SCALING);
		assertNotNull(ciB);
		LOGGER.warn("AUTO*SCALE ATT " +ciB.getAttribute("autoscale").getDfValue());
		
	}
	
	
	@Test
	public void m2(){
		EnvPropsProcessor envPropsProcessor=new EnvPropsProcessor();

		envPropsProcessor.setCmProcessor(cmProcessorMockRepairs);

		CmsCI ciC = envPropsProcessor.isAutorepairEnbaled4bom(ID_WITHOUT_AUTO_REPAIR);
		assertNull(ciC);
		
		CmsCI ciD = envPropsProcessor.isAutoScalingEnbaled4bom(ID_WITHOUT_AUTO_SCALING);
		assertNull(ciD);

		CmsCI ci1 =  envPropsProcessor.isAutorepairEnbaled4bom(ID_NOT_FOUND);
		assertNull(ci1);

		CmsCI ci2 = envPropsProcessor.isAutoScalingEnbaled4bom(ID_NOT_FOUND);
		assertNull(ci2);
		
	}

}
