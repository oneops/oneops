package com.oneops.search.msg.index;

public interface Indexer {
	
	/**
	 * 
	 * @param id
	 * @param type
	 * @param message
	 */
	void index(String id,String type,String message);
	
	/**
	 * 
	 * @param type
	 * @param message
	 */
	void index(String type,String message);
	
	/**
	 * 
	 * @param type
	 * @param message
	 */
	void indexEvent(String type,String message);

}
