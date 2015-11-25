package com.oneops.sensor.domain;

/**
 * The Class SensorStatement.
 */
public class SensorStatement {
	private String stmtName;
	private String stmtText;
	private String listenerName;
	
	/**
	 * Instantiates a new sensor statement.
	 *
	 * @param stmtName the stmt name
	 * @param stmtText the stmt text
	 * @param listenerName the listener name
	 */
	public SensorStatement(String stmtName, String stmtText, String listenerName) {
		super();
		this.stmtName = stmtName;
		this.stmtText = stmtText;
		this.listenerName = listenerName;
	}

	/**
	 * Instantiates a new sensor statement.
	 */
	public SensorStatement() {
		super();
	}
	
	/**
	 * Gets the stmt name.
	 *
	 * @return the stmt name
	 */
	public String getStmtName() {
		return stmtName;
	}
	
	/**
	 * Sets the stmt name.
	 *
	 * @param stmtName the new stmt name
	 */
	public void setStmtName(String stmtName) {
		this.stmtName = stmtName;
	}
	
	/**
	 * Gets the stmt text.
	 *
	 * @return the stmt text
	 */
	public String getStmtText() {
		return stmtText;
	}
	
	/**
	 * Sets the stmt text.
	 *
	 * @param stmtText the new stmt text
	 */
	public void setStmtText(String stmtText) {
		this.stmtText = stmtText;
	}
	
	/**
	 * Gets the listener name.
	 *
	 * @return the listener name
	 */
	public String getListenerName() {
		return listenerName;
	}
	
	/**
	 * Sets the listener name.
	 *
	 * @param listenerName the new listener name
	 */
	public void setListenerName(String listenerName) {
		this.listenerName = listenerName;
	}
}
