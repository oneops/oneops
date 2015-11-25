package com.oneops.sensor.exceptions;

public class SensorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SensorException(Exception e) {
		super(e);
	}

	public SensorException(String error) {
		super(error);
	}
	
}
