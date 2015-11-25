package com.oneops.ops.events;


/**
 * The enum to pass on whether the event is newly created 
 * or Existing. Can be extended in the future to pass oth
 * @author glall
 *
 */
public interface Status {
	//TODO revisit different statuses which can exist 
	//using lower case for json response.
	 String EXISTING = "existing";
	 String NEW = "new";
	 
}
