package com.oneops.opamp.exceptions;

public class AzureEventsHandlerException  extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new AzureEventsHandlerException exception.
	 */
	public AzureEventsHandlerException() {
		super();
	}

	/**
	 * Instantiates a new AzureEventsHandlerException exception.
	 *
	 * @param e the e
	 */
	public AzureEventsHandlerException(Exception e) {
		super(e);
	}
	
	/**
	 * Instantiates a new AzureEventsHandlerException exception.
	 *
	 * @param errMsg the err msg
	 */
	public AzureEventsHandlerException(String errMsg) {
		super(errMsg);
	}
	
	
}
