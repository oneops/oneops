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
package com.oneops.daq;

import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.daq.domain.Chart;
import com.oneops.daq.domain.Series;
import com.oneops.ops.PerfDataRequest;
import com.oneops.ops.PerfHeader;
import com.oneops.ops.dao.PerfDataAccessor;
import com.oneops.ops.dao.PerfHeaderDao;
import com.oneops.sensor.events.PerfEvent;
import com.oneops.sensor.events.PerfEventPayload;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * The Class PerfDaoTest.
 */
public class PerfDaoTest {

	private static PerfHeaderDao phd = new PerfHeaderDao();
	private static PerfWriter pd = new PerfWriter();
	private static PerfDataAccessor pdAccessor = new PerfDataAccessor();

	private static Logger logger = Logger.getLogger(PerfHeaderDao.class);
	
	private long ciId = 9;
	private String ciName = "a1";
	private String key = ciId+":"+ciName;	
	private String ip = "127.0.0.1";
	private int step = 60;
	private long timestamp = 2000000L;
	
	private static void init() {
		ClusterBootstrap daqCluster = new ClusterBootstrap();
		daqCluster.setHostPort("localhost:9160");				
		phd.setClusterBootstrap(daqCluster);

		phd.setClusterName("TestCluster");
		phd.setKeyspaceName("mdb");
		phd.init();

		pdAccessor.setClusterBootstrap(daqCluster);
		pdAccessor.setClusterName("TestCluster");
		pdAccessor.setKeyspaceName("mdb");
		pdAccessor.setTestMode(true);
		pdAccessor.init();
	}
	
	@Test
	public void logRraCountsTest() {
		init();
		pdAccessor.reportMetricCounts();
	}

	
	@Test
	public void purgeMetricsTest() {
		init();
		
		pdAccessor.purgeMetrics(1383781793L);
	}
		
	
    /**
     * Test set header.
     */
    @Test
	public void testaSetHeader() {
		String metricsJson = "{\"deriveTestMetric\":{\"display\":true,\"unit\":\"Per Second\",\"dstype\":\"DERIVE\",\"description\":\"Test\"}," 
				+"\"gaugeTestMetric\":{\"display\":true,\"unit\":\"Test Unit\",\"dstype\":\"GAUGE\",\"description\":\"Test\"}"
				+"}";
		init();		
		try {
			phd.createSingleHeader(key, new Integer(60).toString(), metricsJson);
			phd.createSingleHeader(key, new Integer(300).toString(), metricsJson);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
    /**
     * Test get header.
     */
    @Test	
	public void testaGetHeader() {		
    	init();
		PerfHeader ph = phd.getHeader(key);				
		assertEquals(ph.getStep(), step);

	}	

    /**
     * Test set data.
     */
    @Test	
    public void testSetData() {
		
		long sampleTimestamp = timestamp;
		long sampleIncrement = 60;
		double initialValue = 100;
		double incrementPerSample = 10;
		double value = initialValue;
		
		double[] deriveSamples = new double[10];
		double[] gaugeSamples = new double[10];
		deriveSamples[0] = 10.0;
		deriveSamples[1] = 70.0;	
		deriveSamples[2] = 130.0;
		deriveSamples[3] = 190.0;
		deriveSamples[4] = 250.0;
		deriveSamples[5] = 250.0;		
		deriveSamples[6] = 250.0;		
		deriveSamples[7] = 250.0;		
		deriveSamples[8] = 250.0;		
		deriveSamples[9] = 250.0;		
		
		gaugeSamples = deriveSamples;
		
		int currentSample = 0;
		while ( currentSample<deriveSamples.length) {

			PerfEvent perfEvent = new PerfEvent();
			perfEvent.setCiId(ciId);
			perfEvent.setTimestamp(sampleTimestamp);
			perfEvent.setGrouping(ciName);
			

			PerfEventPayload metrics = new PerfEventPayload();			
			metrics.addAvg("deriveTestMetric", deriveSamples[currentSample]);
			metrics.addAvg("gaugeTestMetric", gaugeSamples[currentSample]);
			perfEvent.setMetrics(metrics);
			try {
				pd.process(perfEvent, ip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
									
			value += incrementPerSample;
			sampleTimestamp += sampleIncrement;
			currentSample++;
		}
		assertTrue(value>initialValue);
    	
	}

    
	
    /**
     * Test get data.
     */
    @Test	
    public void testGetData() {
    	init();
    	PerfDataRequest req = new PerfDataRequest();
    	req.setCi_id(ciId);
    	
    	String metricString = ciName+":deriveTestMetric,"+ciName+":gaugeTestMetric";
    	String[] metrics = metricString.split(",");
    	req.setMetrics(metrics);
    	
    	req.setStart(timestamp-120);
    	req.setEnd(timestamp+(60*10));
    	req.setStep(step);
    	req.setStat_function("average");
    	
    	String jsonResult = pdAccessor.getPerfDataSeries(req);
    	logger.info(jsonResult);
    	
    	assertTrue(jsonResult.length() > 0);
	
	}
	
    
    /**
     * Test set data.
     */
    @Test	
    public void testSetData2() {
				
		long[] timestamps = new long[15];
		// real world in-consistant sample rates
		//1383781500-5 -- 98.4
		//1383781560
		timestamps[0] = 1383781613L; // 0:00 98.4
		timestamps[1] = 1383781643L; // 0:30 99.1
		//1383781620 
		timestamps[2] = 1383781673L; // 0:30 98.91
		timestamps[3] = 1383781703L; // 0:30 98.8
		//1383781680
		timestamps[4] = 1383781733L; // 0:30 98.9
		//1383781720
		timestamps[5] = 1383781763L; // 0:30 26.62
		timestamps[6] = 1383781793L; // 0:30 0.0
		//1383781780
		timestamps[7] = 1383781824L; // 0:31 45.65
		//1383781800-5 
		timestamps[8] = 1383781884L; // 0:60 98.50
		//1383781840
		timestamps[9] = 1383781944L; // 0:60 98.8
		//1383781900
		timestamps[10] = 1383782004L;// 0:60 98.8		
		//1383781960
		timestamps[11] = 1383782064L;// 0:60 98.8	
		//1383782020
		timestamps[12] = 1383782124L;// 0:60 98.8	
		//1383782080
		//1383782100-5		
		timestamps[13] = 1383782184L;// 0:60 98.8	
		//1383782140
		timestamps[14] = 1383782244L;// 0:60 98.8	
		//1383782200
		
		double[] samples = new double[15];
		samples[0] = 98.40;
		samples[1] = 99.10;	
		samples[2] = 98.91;
		samples[3] = 98.80;
		samples[4] = 98.90;
		samples[5] = 26.62;		
		samples[6] = 0.0;		
		samples[7] = 0.0;		
		samples[8] = 0.0;				
		samples[9] = 0.0;		
//		samples[7] = 45.65;		
//		samples[8] = 98.50;		
//		samples[9] = 98.80;		
		samples[10] = 98.80;		
		samples[11] = 98.80;		
		samples[12] = 98.80;		
		samples[13] = 98.80;		
		samples[14] = 98.80;				
		
		int currentSample = 0;
		while ( currentSample<samples.length) {

			PerfEvent perfEvent = new PerfEvent();
			perfEvent.setCiId(ciId);
			perfEvent.setTimestamp(timestamps[currentSample]);
			perfEvent.setGrouping(ciName);
			

			PerfEventPayload metrics = new PerfEventPayload();			
			metrics.addAvg("gaugeTestMetric", samples[currentSample]);
			perfEvent.setMetrics(metrics);
			try {
				pd.process(perfEvent, ip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
									
			currentSample++;
		}
		assertTrue(currentSample>0);
    	
	}

    
	
    /**
     * Test get data.
     */
    @Test	
    public void testGetData2() {
    	init();
    	PerfDataRequest req = new PerfDataRequest();
    	req.setCi_id(ciId);
    	
    	String metricString = ciName+":gaugeTestMetric";
    	String[] metrics = metricString.split(",");
    	req.setMetrics(metrics);
    	
    	req.setStart(1383781600L-60);
    	req.setEnd(1383781613L+(60*10));
    	req.setStep(60);
    	req.setStat_function("average");
    	
    	String jsonResult = pdAccessor.getPerfDataSeries(req);
    	logger.info(jsonResult);
    	
    	req.setStep(300);
    	jsonResult = pdAccessor.getPerfDataSeries(req);
    	logger.info(jsonResult);
    	
    	assertTrue(jsonResult.length() > 0);
	
	}    
    
    
	/**
	 * Test delete header.
	 */
	@Test
	public void testDeleteHeader() {
    	init();
		phd.removeHeader(key);			
	}

	/**
	 * Test set chart.
	 */
	@Test
	public void testSetChart() {
    	init();
    	Chart chart = new Chart();
    	chart.setName("testChart");
    	chart.setStep("auto");
    	chart.setDescription("testDesc");
    	chart.setCreated("");
    	chart.setCreator("");
    	chart.setEnd("");
    	chart.setStart("");
    	chart.setHeight("");
    	chart.setKey("testChart");
    	chart.setTheme("");
    	chart.setTitle("");
    	chart.setType("");
    	chart.setUpdated("");
    	chart.setWidth("");
    	chart.setYmax("");
    	chart.setYmin("");
    	Map<String,Series> series = new HashMap<String,Series>();
    	chart.setSeriesMap(series);
		pd.setChart(chart);	
		assertTrue(chart != null);
	}
	
	/**
	 * Test get perf data table.
	 */
	@Test
	public void testGetPerfDataTable() {
    	init();
    	
    	PerfDataRequest req = new PerfDataRequest();
    	req.setCi_id(ciId);
    	
    	String metricString = ciName+":deriveTestMetric,"+ciName+":gaugeTestMetric";
    	String[] metrics = metricString.split(",");
    	req.setMetrics(metrics);
    	
    	req.setStart(timestamp-120);
    	req.setEnd(timestamp+(60*10));
    	req.setStep(step);
    	req.setStat_function("average");
    	String table = pdAccessor.getPerfDataTable(req);	
		assertTrue(table != null);
		
	}
	
	
	/**
	 * Test get chart.
	 */
	@Test
	public void testGetChart() {
    	init();
		Chart chart = pd.getChart("testChart");
		assertTrue(chart != null);
	}
	
}
