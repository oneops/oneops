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
import com.oneops.daq.domain.GetLogDataByIdRequest;
import com.oneops.daq.domain.GetLogDataByIdResponse;
import com.oneops.daq.domain.GetLogDataRequest;
import com.oneops.daq.domain.GetLogDataResponse;
import com.oneops.ops.dao.PerfHeaderDao;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * The Class LogDaoTest.
 */
public class LogDaoTest {
		
	private static LogWriter ld = new LogWriter();
	private static Logger logger = Logger.getLogger(PerfHeaderDao.class);
	
	private long ciId = 123;
	private String ciName = "a1";
	//private String ip = "127.0.0.1";
	private String key = ciId+":info:"+ciName;	
	private long timestamp = 2000000L;
	
	private static void init() {
		ClusterBootstrap daqCluster = new ClusterBootstrap();
		daqCluster.setHostPort("localhost:9160");				
		ld.setClusterBootstrap(daqCluster);
		
		ld.setClusterName("TestCluster");
		ld.setKeyspaceName("ldb");
		ld.init();
	}
	
	
	/**
	 * Test process.
	 */
	@Test
	public void testProcess()
	{
		
		init();
		
		try {
			ld.process(timestamp, key, "test123", "123");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}
	
	/**
	 * Test get log data.
	 */
	@Test
	public void testGetLogData() {
		init();
		GetLogDataRequest req = new GetLogDataRequest();
		req.setCi_id(123);
		GetLogDataResponse resp = ld.getLogData(req);
		assertTrue(resp != null);
	}
	
	/**
	 * Test get log data by action or workorder.
	 */
	@Test
	public void testGetLogDataByActionOrWorkorder() {
		init();
		GetLogDataByIdRequest req = new GetLogDataByIdRequest();
		req.setId(123);
		GetLogDataByIdResponse resp = ld.getLogDataByActionOrWorkorder(req);
		assertTrue(resp != null);		
	}
	
	/**
	 * Gets the available log types.
	 *
	 * @return the available log types
	 */
	@Test
	public void getAvailableLogTypes() {
		init();
		String[] ci = {"123"};
		String types = ld.getAvailableLogTypes(ci);
		assertTrue(types != null);
	}
	

	
}
