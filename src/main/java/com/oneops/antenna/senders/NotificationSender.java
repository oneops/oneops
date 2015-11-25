package com.oneops.antenna.senders;

import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;

/**
 * The Interface NotificationSender.
 */
public interface NotificationSender {
	
	/**
	 * Post message.
	 *
	 * @param msg the msg
	 * @param subscriber the subscriber
	 * @return true, if successful
	 */
	public boolean postMessage(NotificationMessage msg, BasicSubscriber subscriber);
}
