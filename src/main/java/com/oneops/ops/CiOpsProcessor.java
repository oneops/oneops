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
package com.oneops.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.oneops.ops.dao.OpsCiStateDao;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.states.CiOpsStateMachine;
import com.oneops.sensor.thresholds.ThresholdsDao;

public class CiOpsProcessor {
	
	private static Logger logger = Logger.getLogger(CiOpsProcessor.class);
	
	private OpsEventDao oeDao;
	private OpsCiStateDao opsCiStateDao;
	private ThresholdsDao trDao;
	
	public void setTrDao(ThresholdsDao trDao) {
		this.trDao = trDao;
	}

	public void setOeDao(OpsEventDao oeDao) {
		this.oeDao = oeDao;
	}

	public void setOpsCiStateDao(OpsCiStateDao opsCiStateDao) {
		this.opsCiStateDao = opsCiStateDao;
	}

	public String getCIstate(long ciId) {
		List<CiOpenEvent> openEvents = oeDao.getCiOpenEvents(ciId);
		return getState(openEvents);
	}	

	public Map<Long,String> getCisStates(List<Long> ciIds) {
		
		Map<Long, List<CiOpenEvent>> openEvents = oeDao.getCiOpenEvents(ciIds);
		
		Map<Long,String> states = new HashMap<Long,String>();
		
		for (Long ciId : openEvents.keySet()) {
			states.put(ciId, getState(openEvents.get(ciId)));
		}
		return states;
	}	

	public Map<Long,Map<String,Long>> getComponentStates(List<Long> manifestIds) {
		return opsCiStateDao.getComponentStates(manifestIds);
	}

	public Map<Long,Map<String,Long>> getAllComponentStates() {
		List<Long> manifestIds = trDao.getAllManifestIds();
		int chunkSize = 1000;
		int startIndex = 0;
		Map<Long,Map<String,Long>> result = new HashMap<Long,Map<String,Long>>();
		while (true) {
			int endIndex = startIndex + chunkSize;
			if (endIndex > manifestIds.size()) {
				endIndex = manifestIds.size();     
			}
			List<Long> chunk = manifestIds.subList(startIndex, endIndex);
			result.putAll(opsCiStateDao.getComponentStates(chunk));
			if (chunk.size() < chunkSize) {
				break;
			}
			startIndex = endIndex;
		}
		return result;
	}
	
	
	public Map<Long, List<CiOpenEvent>> getCisOpenEvents(List<Long> ciIds) {
		
		return oeDao.getCiOpenEvents(ciIds);
	}
	
	public List<CiChangeStateEvent> getCiStateHistory(long ciId, Long startTime, Long endTime, Integer count) {
		return opsCiStateDao.getCiStateHistory(ciId, startTime, endTime, count);
	}	
	
	private String getState(List<CiOpenEvent> openEvents) {
		List<String> eventStates = new ArrayList<String>();
		for (CiOpenEvent event : openEvents) {
			eventStates.add(event.getState());
		}
		return CiOpsStateMachine.getCiState(eventStates);
	}

	public void resetManifestStates() {
		List<Long> manifestIds = trDao.getAllManifestIds();
		resetManifestStates(manifestIds);
	}	

	public void resetManifestStates(List<Long> manifestIds) {
		int cntr = 0;
		for (long manifestId : manifestIds) {
			cntr++;
			List<Long> bomIds = trDao.getManifestCiIds(manifestId);
			if (bomIds.size() == 0) {
				trDao.removeRealizedAsRow(manifestId);
				opsCiStateDao.resetComponentCountsToZero(manifestId);

				continue;
			}
			Map<String,Long> counters = new HashMap<String,Long>();
			counters.put("total", new Long(bomIds.size()));
			Map<Long,String> manifestStates = getCisStates(bomIds);
			for (String state : manifestStates.values()) {
				if (counters.containsKey(state)) {
					counters.put(state, counters.get(state) + 1);
				} else {
					counters.put(state, 1L);
				}
			}
			opsCiStateDao.setComponentsStates(manifestId, counters);
			if (cntr % 5000 == 0) {
				logger.info("Processed " + cntr + " components");
			}
		}
	}	
	
	public int removeManifestMap(long ciId, Long manifestId) {
		int remainingBoms = trDao.removeManifestMap(ciId, manifestId);
		if (remainingBoms >0) {
			resetManifestStates(Arrays.asList(manifestId));
		} else {
			opsCiStateDao.resetComponentCountsToZero(manifestId);
		}
		return remainingBoms;
	}
	
	public boolean isCiActive(long ciId) {
		return trDao.getManifestId(ciId) != null;
	}
	
    /**
     * Persist the ci to cassandra.
     *
     * @param ciId
     * @param chStateEvent
     * @param timestamp
     */
    public void persistCiStateChange(long ciId, long manifestId, CiChangeStateEvent chStateEvent, long timestamp) {
    	opsCiStateDao.persistCiStateChange(ciId, manifestId, chStateEvent, timestamp);
    	resetManifestStates(Arrays.asList(manifestId));
    }
    
}
