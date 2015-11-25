/*
 * Copyright 2013-2014 WalmartLabs.
 *
 */
package com.oneops.antenna.domain.filter;

import com.oneops.antenna.domain.NotificationMessage;

/**
 * A filter interface for notification messages.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @version 1.0
 */
public interface MessageFilter {
    /**
     * Tests whether or not the specified notification message to be filtered out.
     *
     * @param msg {@link  com.oneops.antenna.domain.NotificationMessage}
     * @return true if and only if message should be included
     */
    public boolean accept(NotificationMessage msg);
}
