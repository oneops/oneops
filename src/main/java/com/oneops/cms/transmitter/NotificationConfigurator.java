package com.oneops.cms.transmitter;

import com.oneops.cms.transmitter.domain.EventSource;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Notification configurator contains notification rules for antenna notification.
 * Notification rules reads during initialization bean from XML config file placed in classpath.
 *
 */
public class NotificationConfigurator {

    static Logger logger = Logger.getLogger(NotificationConfigurator.class);

    private NotificationRuleList ruleList;
    private Map<EventSource,List<NotificationRule>> ruleMap;
    private boolean configured = false;

    public void init() {
        InputStream is = this.getClass().getResourceAsStream("/notification.config.xml");
        if(is == null) {
            logger.warn("Notification config file not found!");
            return;
        }
        Serializer serializer = new Persister();
        try {
            this.ruleList = serializer.read(NotificationRuleList.class, is);
            is.close();
        } catch (Exception e) {
            logger.error("Read configuration file error!");
            e.printStackTrace();
        }
        if(ruleList != null && ruleList.getRules() != null){
            ruleMap = new HashMap<EventSource, List<NotificationRule>>();
            for(NotificationRule rule: ruleList.getRules()) {
                EventSource source = rule.getSource();
                if(!ruleMap.containsKey(source)) {
                    ruleMap.put(source, new ArrayList<NotificationRule>());
                }
                ruleMap.get(source).add(rule);
            }
            configured = true;
        }
    }

    public boolean isConfigured() {
        return configured;
    }

    public NotificationRule getRule(EventSource source, String clazz) {
        List<NotificationRule> rules = ruleMap.get(source);
        NotificationRule result = null;
        if(rules != null) {
            for(NotificationRule rule: rules){
                if(rule.getClazz() == null) {
                    result = rule;
                } else if(clazz != null && rule.getClazz().equals(clazz)) {
                    return rule;
                }
            }
        }
        return result;
    }
}
