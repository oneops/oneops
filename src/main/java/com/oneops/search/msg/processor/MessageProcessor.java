package com.oneops.search.msg.processor;

/**
 * 
 * @author ranand
 *
 */
public interface MessageProcessor {
	
	/**
	 * 
	 * @param msg
	 * @param msgType
	 * @param msgId
	 */
	public void processMessage(String msg,String msgType,String msgId);
	
	/**
	 * 
	 * @param msg
	 * @param msgType
	 * @param msgId
	 */
	public void deleteMessage(String msgType,String msgId);

}
