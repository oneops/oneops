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
package com.oneops.sensor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class SensorHeartBeat.
 */
public class SensorHeartBeat {
	
	private ConcurrentHashMap<String, ChannelState> heartBeat = new ConcurrentHashMap<>();
	
	/**
	 * Time stamp it.
	 *
	 * @param channel the channel name
	 */
	public void timeStampIt(String channel) {
		if (heartBeat.containsKey(channel)) {
			ChannelState cs = heartBeat.get(channel);
			cs.setUp(true);
			cs.setLastProcessed(System.currentTimeMillis());
			if (cs.getUpSince() == 0) {
				cs.setUpSince(System.currentTimeMillis());
			}
		} else {
			ChannelState cs = new ChannelState();
			cs.setUp(true);
			cs.setLastProcessed(System.currentTimeMillis());
			cs.setUpSince(System.currentTimeMillis());
			heartBeat.put(channel, cs);
		}
	}
	
	public Map<String, ChannelState> getChannelsStatus() {
		Map<String, ChannelState> result = new HashMap<>();
		for (Entry<String, ChannelState> entry : heartBeat.entrySet()) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public long getNumChannelsDown() {
		return heartBeat.entrySet().stream().filter(e->e.getValue().isDown()).count();
	}
	public long getTotalChannels() {
		return heartBeat.size();
	}
	/**
	 * Gets the latest hear beat time.
	 *
	 * @param channel for which latest heart beat needs to be found.
	 * @return the latest heart beat time
	 */
	public long getLatestHearBeatTime(String channel) {
		if (heartBeat.containsKey(channel)) {
			return heartBeat.get(channel).getLastProcessed(); 
		} else {
			return 0;
		}
	}

	/**
	 * Gets the time since channel is up.
	 *
	 * @param channel the hb name
	 * @return the time since channel is up
	 */
	public long getUpSince(String channel) {
		if (heartBeat.containsKey(channel) && heartBeat.get(channel).isUp()) {
			return heartBeat.get(channel).getUpSince(); 
		} else {
			return 0;
		}
	}

	/**
	 * Gets the time since channel is up.
	 *
	 * @param channel the channel name
	 * @return the time since channel is up
	 */
	public long getDownSince(String channel) {
		if (heartBeat.containsKey(channel) && !heartBeat.get(channel).isUp()) {
			return heartBeat.get(channel).getDownSince(); 
		} else {
			return 0;
		}
	}
	
	public void markDown(String channel) {
		if (heartBeat.containsKey(channel)) {
			ChannelState cs = heartBeat.get(channel);
			cs.setUp(false);
			cs.setDownSince(System.currentTimeMillis());
			cs.setUpSince(0);
		} else {
			ChannelState cs = new ChannelState();
			cs.setUp(false);
			cs.setDownSince(System.currentTimeMillis());
			cs.setUpSince(0);
			heartBeat.put(channel, cs);
		}
	}
	
	public boolean isUp(String channel) {
		if (heartBeat.containsKey(channel)) {
			return heartBeat.get(channel).isUp();
		} else {
			return false;
		}
	}
}
