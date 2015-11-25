package com.oneops.antenna.domain;


/**
 * The Class BasicMessage.
 */
public class BasicMessage {

	private String fromAddress;
	private String subject;
	private String txtMessage;

	/**
	 * Gets the from address.
	 *
	 * @return the from address
	 */
	public String getFromAddress() {
		return fromAddress;
	}
	
	/**
	 * Sets the from address.
	 *
	 * @param fromAddress the new from address
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	/**
	 * Gets the subject.
	 *
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * Sets the subject.
	 *
	 * @param subject the new subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * Gets the txt message.
	 *
	 * @return the txt message
	 */
	public String getTxtMessage() {
		return txtMessage;
	}
	
	/**
	 * Sets the txt message.
	 *
	 * @param txtMessage the new txt message
	 */
	public void setTxtMessage(String txtMessage) {
		this.txtMessage = txtMessage;
	}

}
