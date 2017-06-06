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
package com.oneops.sensor.ws;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.sensor.Sensor;
import com.oneops.sensor.ws.SensorWsController;

public class SensorWsControllerTest {

	private static final String OUTAGE_EVENT = "outage-event";
	private static final long CI_ID_A = 123;
	private static final String CI_ID_AVAL = "16";

	private SensorWsController sensorWsController;

	/**
	 * initialize the mocks and their mock behaviors
	 */
	@BeforeClass
	public void setMock() {
		sensorWsController = new SensorWsController();
		this.sensorWsController.setSensor(mock(Sensor.class));

		CiOpsProcessor coProcessor = mock(CiOpsProcessor.class);
		List<Long> states0 = new ArrayList<Long>();
		states0.add(CI_ID_A);
		states0.add(20L);
		states0.add(30L);
		Map<Long, String> statesMap0 = new HashMap<Long, String>();
		statesMap0.put(CI_ID_A, "soloState");

		List<Long> states1 = new ArrayList<Long>();
		states1.add(CI_ID_A);
		states1.add(20L);
		states1.add(30L);
		Map<Long, String> statesMap1 = new HashMap<Long, String>();
		statesMap1.put(CI_ID_A, CI_ID_AVAL);
		statesMap1.put(20L, "StateB");
		statesMap1.put(30L, "TransitionC");

		when(coProcessor.getCisStates(states0)).thenReturn(statesMap0);
		when(coProcessor.getCisStates(states1)).thenReturn(statesMap1);
		when(coProcessor.getCIstate(CI_ID_A)).thenReturn(CI_ID_AVAL);
		this.sensorWsController.setCoProcessor(coProcessor);

		OpsEventDao opDaoMock = mock(OpsEventDao.class);
		List<CiOpenEvent> ciOpenEventsList = new ArrayList<CiOpenEvent>(2);
		CiOpenEvent ev1 = new CiOpenEvent();
		ev1.setName("event:1:m");
		ev1.setState("open");
		ev1.setTimestamp(123456789);
		CiOpenEvent ev2 = new CiOpenEvent();
		ev2.setName("event:2:n");
		ev2.setState("flapping");
		ev2.setTimestamp(123456888);
		ciOpenEventsList.add(ev1);
		ciOpenEventsList.add(ev2);

		when(opDaoMock.getCiOpenEvents(anyLong())).thenReturn(ciOpenEventsList);

		this.sensorWsController.setOeDao(opDaoMock);
	}

	@Test
	/** startTracking method is to return String success*/
	public void startTrackingTest() {

		String scope = "";
		CmsWorkOrderSimple woSimple = new CmsWorkOrderSimple();

		CmsRfcCISimple rfcCi = new CmsRfcCISimple();
		rfcCi.setCiName("mocked");
		rfcCi.setCiId(0);
		rfcCi.setRfcAction("update");
		woSimple.setRfcCi(rfcCi);

		Map<String, List<CmsRfcCISimple>> payLoad = new HashMap<String, List<CmsRfcCISimple>>();
		List<CmsRfcCISimple> wb = new ArrayList<CmsRfcCISimple>();
		CmsRfcCISimple sim1 = new CmsRfcCISimple();
		sim1.setCiId(1);
		sim1.setCiName("ci-named");
		wb.add(sim1);
		payLoad.put("WatchedBy", wb);

		List<CmsRfcCISimple> ra = new ArrayList<CmsRfcCISimple>();
		CmsRfcCISimple sim2 = new CmsRfcCISimple();
		sim2.setCiId(1);
		sim2.setCiName("ci-named");
		ra.add(sim2);
		payLoad.put("RealizedAs", ra);

		woSimple.setPayLoad(payLoad);
		sensorWsController.startTracking(woSimple, scope);

		String res = this.sensorWsController.startTracking(woSimple, scope);
		assertEquals(res, "{\"success\"}");
	}

	@Test
	/** examine /ops/states List getCisStates*/
	public void getStates() {

		String ciIdsStr = CI_ID_A + ",20,30";

		List<Map<String, String>> states = this.sensorWsController
				.getCisStatesGet(ciIdsStr);

		assert (states.size() == 3);
		// ==[{id=20, state=StateB}, {id=10, state=valueA}, {id=30,
		// state=TransitionC}]
		List<String> gottenKeys = new ArrayList<String>(3);
		for (Map<String, String> m : states) {
			gottenKeys.add(m.get("id"));
		}

		String[] expected = new String[] { String.valueOf(CI_ID_A), "20", "30" };

		assert (gottenKeys.containsAll(Arrays.asList(expected)));
	}

	@Test
	/** get one of our states to see it is right, based on this class' constants */
	public void getCIState() {

		Map<String, String> stateMap = new HashMap<String, String>(1);
		stateMap = this.sensorWsController.getCIstate(CI_ID_A);
		assert (CI_ID_AVAL.equals(stateMap.get("state")));

	}

	@Test
	/** empty event and response check*/
	public void emptyEvent() {

		String wasSent = this.sensorWsController.sendEmptyEvent(100, "mocker",
				200);
		assertEquals(wasSent, "Sent");
	}

	@Test
	public void getCiOpens() {
		// getCIOpenEvents
		// oeDao.getCiOpenEvents(ciId);
		Map<Long, Map<String, List<CiOpenEvent>>> outMap = sensorWsController
				.getCIOpenEvents(CI_ID_A);

		assert (outMap.size() == 1);
	}

	@Test
	public void getOpens() {
		List<Map<Long, Map<String, List<CiOpenEvent>>>> thoseOpen = this.sensorWsController
				.getOpenEvents(CI_ID_AVAL + "," + CI_ID_AVAL + "1,"
						+ CI_ID_AVAL + "2");
		assert (thoseOpen.size() == 0);
	}

	@Test
	public void closeEventTest() {
		List<CiOpenEvent> closed = this.sensorWsController.closeCIevent(
				CI_ID_A, OUTAGE_EVENT);
		assert (closed == null || !closed.contains(CI_ID_A));
	}

}