package com.oneops.sensor.client;

public class SensorClientException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public SensorClientException(Exception e) {
		super(e);
	}

	public SensorClientException(String error) {
		super(error);
	}
	
}
