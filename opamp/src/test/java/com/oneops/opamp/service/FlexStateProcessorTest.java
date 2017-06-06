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
package com.oneops.opamp.service;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oneops.ops.events.CiChangeStateEvent;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.ops.CiOpsProcessor;


public class FlexStateProcessorTest {
	
	private CmsCmProcessor cmProcessorMock;
	private EnvPropsProcessor envProcessorMock;
//	private CiOpsProcessor coProcessorMock;
	
	private Notifications notifierMock;
	
	private CmsRfcProcessor rfcProcessorMock;
	private RestTemplate restTemplateMock;
//    private String transistorUrlMock;
//    private long coolOffPeriodMili = 180000;
	
	private static final long ID_WITH_AUTO_REPAIR = 11;
	private static final long ID_WITHOUT_AUTO_REPAIR = 10;
	private static final long ID_WITH_AUTO_SCALING = 21;
	private static final long ID_WITHOUT_AUTO_SCALING = 20;
	private static final long ID_NOT_FOUND = -1;
	private static Logger logger = Logger.getLogger(FlexStateProcessor.class);

	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setup(){
			
		notifierMock = mock(Notifications.class);
		restTemplateMock = mock(RestTemplate.class);
		when(restTemplateMock.getForObject(anyString(), eq(Long.class),  anyMap())).thenReturn(54321L);		
		
		envProcessorMock = mock(EnvPropsProcessor.class);
		CmsCI cmsCIWithAutocFeatures = new CmsCI();
		cmsCIWithAutocFeatures.setCiName("mock-ci-name");
		cmsCIWithAutocFeatures.setNsPath("/mock/ns/path/abc");
		cmsCIWithAutocFeatures.setCiId(21);
		cmsCIWithAutocFeatures.setCiClassName("bom.Compute");

		when(envProcessorMock.isAutorepairEnabled(ID_WITH_AUTO_REPAIR)).thenReturn(true);
		when(envProcessorMock.isAutorepairEnabled(ID_WITHOUT_AUTO_REPAIR)).thenReturn(false);
		when(envProcessorMock.getEnv4Bom(ID_NOT_FOUND)).thenReturn(null);

        when(envProcessorMock.isAutoscaleEnabled(ID_WITH_AUTO_SCALING)).thenReturn(true);
        when(envProcessorMock.isAutoscaleEnabled(ID_WITHOUT_AUTO_SCALING)).thenReturn(false);

		
		List<CmsRelease> emptyReleaseList = new ArrayList<CmsRelease>();
		rfcProcessorMock = mock(CmsRfcProcessor.class);
		when(rfcProcessorMock.getLatestRelease(anyString(), anyString())).thenReturn(emptyReleaseList);
		
		CmsCIAttribute ciaAutoRepairTrue = new CmsCIAttribute();
		ciaAutoRepairTrue.setAttributeName("autorepair");
		ciaAutoRepairTrue.setDfValue("true");
		
		List<CmsCIRelation> manifestCiRels1 = new ArrayList<CmsCIRelation>();
		
		CmsCI cmsCIAutoRepairTrue = new CmsCI();
		Map<String, CmsCIAttribute> attributes = new HashMap<String, CmsCIAttribute> (1);
		CmsCIAttribute a5=new CmsCIAttribute();
		a5.setDjValue("100");
		attributes.put("pct_dpmt", a5);//100 percent
		CmsCIAttribute a6=new CmsCIAttribute();
		attributes.put("step_up", a6);
		
		cmsCIAutoRepairTrue.setAttributes(attributes);
		cmsCIAutoRepairTrue.setCiName("mock-ci-name");
		cmsCIAutoRepairTrue.setNsPath("/mock/ns/path/abc");
		cmsCIAutoRepairTrue.setCiId(ID_WITH_AUTO_REPAIR);
		cmsCIAutoRepairTrue.setCiClassName("bom.Compute");
		
		
		CmsCIRelation ciRelationAutoRepairTrue = new CmsCIRelation();
		ciRelationAutoRepairTrue.setFromCiId(21L);
		ciRelationAutoRepairTrue.setFromCi(cmsCIAutoRepairTrue);
		manifestCiRels1.add(ciRelationAutoRepairTrue);
		

		CmsCIRelationAttribute ccra = new CmsCIRelationAttribute();
		ccra.setDjValue("true");
		CmsCIRelationAttribute ccrb = new CmsCIRelationAttribute();
		ccrb.setDjValue("5");
    	CmsCIRelationAttribute ccrc = new CmsCIRelationAttribute();
		ccrc.setDjValue("7");
    	CmsCIRelationAttribute ccrd = new CmsCIRelationAttribute();
		ccrd.setDjValue("100"); //percent
    	CmsCIRelationAttribute ccre = new CmsCIRelationAttribute();
		ccre.setDjValue("11"); //
    	CmsCIRelationAttribute ccrf = new CmsCIRelationAttribute();
		ccrf.setDjValue("12"); //
		Map<String, CmsCIRelationAttribute> mymap = new HashMap<String, CmsCIRelationAttribute>(1);
		mymap.put("flex", ccra);
		mymap.put("current", ccrb);
		mymap.put("max", ccrc);
		mymap.put("pct_dpmt", ccrd);
		mymap.put("min", ccre);
		mymap.put("step_down", ccrf);

			
		CmsCIRelation ciRelationFlexTrue = new CmsCIRelation();
		ciRelationFlexTrue.setAttributes(mymap);
		List<CmsCIRelation> flexRelationList = new ArrayList<CmsCIRelation>(1);
		flexRelationList.add(ciRelationFlexTrue);
	
		
		this.cmProcessorMock= mock(CmsCmProcessor.class);
		when(cmProcessorMock.getCiById(1L)).thenReturn(cmsCIAutoRepairTrue);
		when(cmProcessorMock.getCiById(ID_WITH_AUTO_REPAIR)).thenReturn(cmsCIAutoRepairTrue);
		when(cmProcessorMock.getCiById(ID_WITH_AUTO_SCALING)).thenReturn(cmsCIAutoRepairTrue);


		when(cmProcessorMock.getFromCIRelationsNaked(ID_WITH_AUTO_REPAIR, "base.RealizedAs","bom.ManagedVia","bom.Compute")).thenReturn(manifestCiRels1);
		when(cmProcessorMock.getFromCIRelationsNaked(ID_WITH_AUTO_REPAIR, "base.RealizedAs", "manifest.Compute")).thenReturn(manifestCiRels1);
		when(cmProcessorMock.getToCIRelationsNaked(ID_WITH_AUTO_REPAIR, "base.RealizedAs", "manifest.Compute")).thenReturn(manifestCiRels1);
		
		//used in findFlexRelation()
		when(cmProcessorMock.getFromCIRelationsNaked(ID_WITH_AUTO_REPAIR, "manifest.DependsOn", null)).thenReturn(flexRelationList);
		when(cmProcessorMock.getToCIRelationsNaked(ID_WITH_AUTO_REPAIR, "manifest.DependsOn", null)).thenReturn(flexRelationList);

		when(cmProcessorMock.getToCIRelationsNaked(ID_WITH_AUTO_SCALING, "manifest.DependsOn", null)).thenReturn(flexRelationList);

		
		
//		when(cmProcessorMock.getToCIRelationsNakedNoAttrs(ID_WITH_AUTO_REPAIR, "manifest.Requires",null, "manifest.Platform")).thenReturn(manifestCiRels1);//manifestPlatRels1);
//		when(cmProcessorMock.getToCIRelations(ID_WITH_AUTO_REPAIR, "manifest.ComposedOf",null, "manifest.Environment")).thenReturn(manifestCiRels1);
	
	}

	@Test
	//*where state is not either under or overutilized */
	public void processAlreadyHealthy(){
		CiOpsProcessor cop = mock(CiOpsProcessor.class);
		when(cop.getCIstate(0L)).thenReturn("unkown-state");
		//it is neither overutilized or underutilized state
		FlexStateProcessor fsp = new FlexStateProcessor();
		fsp.setCoProcessor(cop);		
		CiChangeStateEvent event = new CiChangeStateEvent();
		event.setCiId(ID_NOT_FOUND);

		try {
			//these will just remove keys from the Set; 
			//simple test; just make sure no exception
			fsp.processOverutilized(event, true);
			fsp.processUnderutilized(event, true, System.currentTimeMillis());
		} catch (OpampException e) {
			logger.warn("Not expected to catch here ",e);
			throw new RuntimeException(e);
		}
	}
	
	@Test
/** exercises processOverutilized() with an autoscale and a nonautoscale */
	public void processOverutilized(){
		CiOpsProcessor cop = mock(CiOpsProcessor.class);
		when(cop.getCIstate(ID_WITH_AUTO_SCALING)).thenReturn("overutilized");
		when(cop.getCIstate(ID_WITHOUT_AUTO_SCALING)).thenReturn("other-state");

		FlexStateProcessor fsp = new FlexStateProcessor();
		fsp.setCoProcessor(cop);	
		fsp.setEnvProcessor(envProcessorMock);
		fsp.setCmProcessor(cmProcessorMock);
		fsp.setNotifier(notifierMock);
		fsp.setRestTemplate(restTemplateMock);
		fsp.setTransistorUrl(null);		
	
		 try {
			 CiChangeStateEvent event = new CiChangeStateEvent();
			event.setCiId(ID_WITH_AUTO_SCALING);

			fsp.processOverutilized(event, true);
			//drives private method for growPool...
			event = new CiChangeStateEvent();
			event.setCiId(ID_WITHOUT_AUTO_SCALING);
			fsp.processOverutilized(event, true);
			event = new CiChangeStateEvent();
			event.setCiId(ID_WITHOUT_AUTO_REPAIR);
			fsp.processOverutilized(event, true);

		} catch (OpampException e) {
			logger.warn("Not expected to catch here ",e);
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void processUnderutilized(){
		CiOpsProcessor cop = mock(CiOpsProcessor.class);
		when(cop.getCIstate(ID_WITH_AUTO_SCALING)).thenReturn("underutilized");
		when(cop.getCIstate(0L)).thenReturn("other-state");

		FlexStateProcessor processor = new FlexStateProcessor();
		
		processor.setCoProcessor(cop);	
		processor.setEnvProcessor(envProcessorMock);
		
		System.out.println(" lets set up the mock ....."+ cmProcessorMock);
		processor.setNotifier(notifierMock);
		processor.setRestTemplate(restTemplateMock);
		processor.setTransistorUrl(null);
		processor.setCmProcessor(cmProcessorMock);

		try {
			CiChangeStateEvent event = new CiChangeStateEvent();
			event.setCiId(0L);

			processor.processUnderutilized(event, true, System.currentTimeMillis()); //actually not
			event = new CiChangeStateEvent();
			event.setCiId(ID_WITH_AUTO_REPAIR);
			processor.processUnderutilized(event, true, System.currentTimeMillis());

			event = new CiChangeStateEvent();
			event.setCiId(ID_WITH_AUTO_SCALING);
			processor.processUnderutilized(event, true, System.currentTimeMillis()); //is underutilized 
			
		} catch (OpampException e) {
			logger.warn("Not expected to catch here ",e);
			throw new RuntimeException(e);
		}
		
	}

}
