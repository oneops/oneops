package com.oneops.cms.transmitter;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 *
 */
@Root
public class NotificationRuleList {

    @ElementList(inline=true, required=false)
    private List<NotificationRule> rules;

    public List<NotificationRule> getRules() {
        return rules;
    }
}
