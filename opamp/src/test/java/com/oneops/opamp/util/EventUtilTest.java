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
package com.oneops.opamp.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.opamp.cache.WatchedByAttributeCache;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.Notifications;
import com.oneops.opamp.util.EventUtil;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.ops.events.Status;
import com.oneops.ops.states.CiOpsState;

public class EventUtilTest {

	private static final String NOTIFY_ONLY_ON_STATE_CHANGE = "notifyOnlyOnStateChange";
	EventUtil eventUtil = new EventUtil();

	@BeforeClass
	public void setup() {
		eventUtil.setGson(new Gson());
		eventUtil.setNotifier(mock(Notifications.class));
	}

	@Test
	public void getKeyWithValidManifestId() throws OpampException {
		OpsBaseEvent event = mock(OpsBaseEvent.class);
		Long l = 35677l;
		when(event.getManifestId()).thenReturn(l);
		String source = "p1-compute-load";
		when(event.getSource()).thenReturn(source);
		String attributeName = NOTIFY_ONLY_ON_STATE_CHANGE;
		String key = l.toString() + ":" + source + ":" + attributeName;
		Assert.assertEquals(key, eventUtil.getKey(event));
	}

	@Test(expectedExceptions = OpampException.class)
	public void getKeyWithInValidManifestIdThrowsException() throws OpampException {
		OpsBaseEvent event = mock(OpsBaseEvent.class);
		// event has manifestId as zero
		Long l = 0l;
		when(event.getManifestId()).thenReturn(l);

		//
		String source = "p1-compute-load";
		when(event.getSource()).thenReturn(source);
		Long manifestId = 23456l;
		when(event.getCiId()).thenReturn(manifestId);

		CmsCmProcessor cmProcessor = mock(CmsCmProcessor.class);
		eventUtil.setCmProcessor(cmProcessor);

		when(cmProcessor.getToCIRelationsNakedNoAttrs(23456, CmsConstants.BASE_REALIZED_AS, null, null)).thenReturn(Collections.<CmsCIRelation> emptyList());
		eventUtil.getKey(event);
		String key = l.toString() + ":" + source + ":" + NOTIFY_ONLY_ON_STATE_CHANGE;

		Assert.assertEquals(key, eventUtil.getKey(event));

	}

	@Test
	public void getKeyWithZeroValueOfManifestIdInEvent() throws OpampException {
		OpsBaseEvent event = mock(OpsBaseEvent.class);
		// event has manifestId as zero
		Long l = 0l;
		when(event.getManifestId()).thenReturn(l);

		//
		String source = "p1-compute-load";
		when(event.getSource()).thenReturn(source);

		when(event.getCiId()).thenReturn(23456l);

		CmsCmProcessor cmProcessor = mock(CmsCmProcessor.class);
		eventUtil.setCmProcessor(cmProcessor);

		List<CmsCIRelation> realizedList = new ArrayList<CmsCIRelation>();
		CmsCIRelation relation = new CmsCIRelation();
		Long expectedManifestId = 34567l;
		relation.setFromCiId(expectedManifestId);
		realizedList.add(relation);

		String key = expectedManifestId.toString() + ":" + source + ":" + NOTIFY_ONLY_ON_STATE_CHANGE;

		when(cmProcessor.getToCIRelationsNakedNoAttrs(23456, CmsConstants.BASE_REALIZED_AS, null, null)).thenReturn(realizedList);
		Assert.assertEquals(eventUtil.getKey(event), key);

	}

	
	@Test
	public void shouldNotifyTrueWithAttribSendNotificationOnlyIfStateChangeFalse() throws OpampException {
		CiChangeStateEvent ciEvent = new CiChangeStateEvent();
		ciEvent.setCiId(12345);
		ciEvent.setNewState(CiOpsState.notify.getName());
		ciEvent.setOldState(CiOpsState.notify.getName());
		OpsBaseEvent obe = new OpsBaseEvent();
		obe.setCiId(12345);
		obe.setManifestId(6789);
		obe.setBucket("mockBucket");
		obe.setSource("p1-compute-load");
		obe.setStatus(Status.NEW);
		Gson gson = new Gson();
		ciEvent.setPayLoad(gson.toJson(obe));

		WatchedByAttributeCache cacheWithNotifyOnlyOnStateChangeTrue = mock(WatchedByAttributeCache.class);
		LoadingCache<String, String> cache = mock(LoadingCache.class);
		eventUtil.setCache(cacheWithNotifyOnlyOnStateChangeTrue);

		try {
			when(cache.get(eventUtil.getKey(obe))).thenReturn(String.valueOf("false"));
			when(cacheWithNotifyOnlyOnStateChangeTrue.instance()).thenReturn(cache);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		boolean actualValue = eventUtil.shouldNotify(ciEvent, obe);
		Assert.assertEquals(actualValue, true);
	}
	
	@Test
	public void shouldNotifyFalseWithOldStatusCIAndAttribSendNotificationOnlyIfStateChangeTrue() throws OpampException {
		CiChangeStateEvent ciEvent = new CiChangeStateEvent();
		ciEvent.setCiId(12345);
		ciEvent.setNewState(CiOpsState.notify.getName());
		ciEvent.setOldState(CiOpsState.notify.getName());
		OpsBaseEvent obe = new OpsBaseEvent();
		obe.setCiId(12345);
		obe.setManifestId(6789);
		obe.setBucket("mockBucket");
		obe.setSource("p1-compute-load");
		obe.setStatus(Status.EXISTING);
		Gson gson = new Gson();
		ciEvent.setPayLoad(gson.toJson(obe));

		WatchedByAttributeCache cacheWithNotifyOnlyOnStateChangeTrue = mock(WatchedByAttributeCache.class);
		LoadingCache<String, String> cache = mock(LoadingCache.class);
		eventUtil.setCache(cacheWithNotifyOnlyOnStateChangeTrue);

		try {
			when(cache.get(eventUtil.getKey(obe))).thenReturn(String.valueOf("true"));
			when(cacheWithNotifyOnlyOnStateChangeTrue.instance()).thenReturn(cache);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		boolean actualValue = eventUtil.shouldNotify(ciEvent, obe);
		Assert.assertEquals(actualValue, false);
	}
	
	@Test
	public void shouldNotifyFalseAndExceptionInGettingAttribValue() throws OpampException {
		CiChangeStateEvent ciEvent = new CiChangeStateEvent();
		ciEvent.setCiId(12345);
		ciEvent.setNewState(CiOpsState.notify.getName());
		ciEvent.setOldState(CiOpsState.notify.getName());
		OpsBaseEvent obe = new OpsBaseEvent();
		obe.setCiId(12345);
		obe.setManifestId(6789);
		obe.setBucket("mockBucket");
		obe.setSource("p1-compute-load");
		obe.setStatus(Status.EXISTING);
		Gson gson = new Gson();
		ciEvent.setPayLoad(gson.toJson(obe));

		WatchedByAttributeCache cacheWithNotifyOnlyOnStateChangeTrue = mock(WatchedByAttributeCache.class);
		LoadingCache<String, String> cache = mock(LoadingCache.class);
		eventUtil.setCache(cacheWithNotifyOnlyOnStateChangeTrue);

		try {
			when(cache.get(eventUtil.getKey(obe))).thenThrow(new ExecutionException(null));
			when(cacheWithNotifyOnlyOnStateChangeTrue.instance()).thenReturn(cache);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		boolean actualValue = eventUtil.shouldNotify(ciEvent, obe);
		Assert.assertEquals(actualValue, false);
	}
	@Test
	public void shouldNotifyFalseWithNewStateNotifyAndOldStateHealthyAndAttribSendNotificationOnlyIfStateChangeTrue() throws OpampException {
		CiChangeStateEvent ciEvent = new CiChangeStateEvent();
		ciEvent.setCiId(12345);
		ciEvent.setNewState(CiOpsState.notify.getName());
		ciEvent.setOldState("healthy");
		OpsBaseEvent obe = new OpsBaseEvent();
		obe.setCiId(12345);
		obe.setManifestId(6789);
		obe.setBucket("mockBucket");
		obe.setSource("p1-compute-load");
		obe.setStatus(Status.NEW);
		Gson gson = new Gson();
		ciEvent.setPayLoad(gson.toJson(obe));

		WatchedByAttributeCache cacheWithNotifyOnlyOnStateChangeTrue = mock(WatchedByAttributeCache.class);
		LoadingCache<String, String> cache = mock(LoadingCache.class);
		eventUtil.setCache(cacheWithNotifyOnlyOnStateChangeTrue);

		try {
			when(cache.get(eventUtil.getKey(obe))).thenReturn(String.valueOf("true"));
			when(cacheWithNotifyOnlyOnStateChangeTrue.instance()).thenReturn(cache);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		boolean actualValue = eventUtil.shouldNotify(ciEvent, obe);
		Assert.assertEquals(actualValue, true);
	}
//	
	
	
	
	
}
