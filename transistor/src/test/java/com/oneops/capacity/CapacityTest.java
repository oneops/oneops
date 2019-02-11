package com.oneops.capacity;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.oneops.capacity.CapacityProcessor;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.service.BomAsyncProcessor;
import com.oneops.transistor.service.BomEnvManager;
import com.oneops.transistor.service.CostData;
import com.oneops.transistor.service.DesignManager;
import com.oneops.transistor.service.IaasManager;
import com.oneops.transistor.service.ManifestAsyncProcessor;
import com.oneops.transistor.service.ManifestManager;
import com.oneops.transistor.service.SnapshotManager;
import com.oneops.transistor.service.peristenceless.BomData;
import com.oneops.transistor.service.peristenceless.InMemoryBomProcessor;
import com.oneops.transistor.ws.rest.TransistorRestController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:test-capacity-context.xml")
@Ignore
public class CapacityTest {
	private static final Logger logger = Logger.getLogger(CapacityTest.class);
	
	@Autowired
	CapacityProcessor capacityProcessor;
	
	@Autowired
	private ManifestManager manifestManager;
	
	@Autowired
	private BomEnvManager envManager;
	
	@Autowired
	private IaasManager iaasManager;
	
	@Autowired
	private DesignManager dManager;
	
	@Autowired
	private BomAsyncProcessor baProcessor;
	
	@Autowired
	private InMemoryBomProcessor imBomProcessor;
	
	@Autowired
	private ManifestAsyncProcessor maProcessor;
	
	@Autowired
	private CmsUtil util;
	
	@Autowired
	private SnapshotManager snapshotManager;

	
	private long envId = 279697;
	private Boolean commit = false;
	private String description = null;
	private String userId="igort";
	
	
	@Test
	public void testEstimate() {
		System.out.println(getLog4jConfig());
		try {
			BomData bomData = imBomProcessor.compileEnv(envId, userId, null, description, commit == null ? false : commit);
			logger.info("capacity ="+envManager.estimateDeploymentCapacity(bomData));
		} catch (CmsBaseException te) {
			logger.error(te);
			te.printStackTrace();
			throw te;
		}		
	}
	
	@Test
	public void  testLoadCis() {
		try {
			BomData bomData = imBomProcessor.compileEnv(envId, userId, null, description, commit == null ? false : commit);
//			Collection<CmsRfcRelation> deployedToRelations = bomData.getRelations().stream().filter(r -> r.getRelationName().equals("base.DeployedTo")).collect(toList());
			
			
			//delta
			List<CmsRfcCI> rfcCis = CapacityProcessor.getUpdateOrReplaceCi(bomData.getCis());
			
			logger.info("update cis size="+rfcCis.size() +" original="+bomData.getCis().size());
			
			// original cis map
			List<CmsCI> cis = capacityProcessor.loadCis(rfcCis);
			logger.info("cmsCis="+cis);
	
			List<CmsRfcCI> originalRfcs = CapacityProcessor.toRfcCis(cis);
			logger.info("originalState="+originalRfcs);
			
			List<CmsRfcCI> finalRfcs = CapacityProcessor.createFinalSate(rfcCis, cis);
			logger.info("finalState="+finalRfcs);
			
			
/*			List<Map<String, Integer>> originalCapacity = capacityProcessor.calculateCapacityByCi(originalRfcs);
			logger.info("original capacity="+originalCapacity);
			
			List<Map<String, Integer>> finalCapacity = capacityProcessor.calculateCapacityByCi(finalRfcs);
			logger.info("final    capacity="+finalCapacity);
			
			
			
			List<Map<String, Integer>> deltaCapacity = CapacityProcessor.calculateDeltaCapacity(originalCapacity, finalCapacity);
			logger.info("delta    capacity="+deltaCapacity); */
			
		} catch (CmsBaseException te) {
			logger.error(te);
			te.printStackTrace();
			throw te;
		}		
	} 
	
	public static URL getLog4jConfig() {
	    String override = OptionConverter.getSystemProperty("log4j.defaultInitOverride", null);
	    if (override == null || "false".equalsIgnoreCase(override)) {
	      String configurationOptionStr = OptionConverter.getSystemProperty("log4j.configuration", null);

	      URL url;

	      if (configurationOptionStr == null) {
	        url = Loader.getResource("log4j.xml");
	        if (url == null) {
	          url = Loader.getResource("log4j.properties");
	        }
	      } else {
	        try {
	          url = new URL(configurationOptionStr);
	        } catch (MalformedURLException ex) {
	          url = Loader.getResource(configurationOptionStr);
	        }
	      }
	      return url;
	    } else {
	      return null;
	    }
	  }

}
