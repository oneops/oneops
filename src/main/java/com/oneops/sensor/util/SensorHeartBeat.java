package com.oneops.sensor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class SensorHeartBeat.
 */
public class SensorHeartBeat {
	
	private ConcurrentHashMap<String, ChannelState> heartBeat = new ConcurrentHashMap<String, ChannelState>();
	
	/**
	 * Time stamp it.
	 *
	 * @param hbName the hb name
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
		Map<String, ChannelState> result = new HashMap<String, ChannelState>();
		for (Entry<String, ChannelState> entry : heartBeat.entrySet()) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Gets the latest hear beat time.
	 *
	 * @param hbName the hb name
	 * @return the latest hear beat time
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
	 * @param hbName the hb name
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
	 * @param hbName the hb name
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
