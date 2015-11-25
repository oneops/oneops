package com.oneops.ops;
/**
 * Base class with common fields and getter , setters
 */
public class LogDataRequest extends DataRequest {

	private long zone_ci_id;

	/**
	 * Gets the zone_ci_id.
	 *
	 * @return the zone_ci_id
	 */
	public long getZone_ci_id() {
		return zone_ci_id;
	}

	/**
	 * Sets the zone_ci_id.
	 *
	 * @param zone_ci_id the new zone_ci_id
	 */
	public void setZone_ci_id(long zone_ci_id) {
		this.zone_ci_id = zone_ci_id;
	}
}