package com.oneops.opamp.exceptions;

/**
 * custom domain Exception inherting from Exception
 *
 */
public class OpampException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new opamp exception.
	 */
	public OpampException() {
		super();
	}

	/**
	 * Instantiates a new opamp exception.
	 *
	 * @param e the e
	 */
	public OpampException(Exception e) {
		super(e);
	}
	
	/**
	 * Instantiates a new opamp exception.
	 *
	 * @param errMsg the err msg
	 */
	public OpampException(String errMsg) {
		super(errMsg);
	}
}
