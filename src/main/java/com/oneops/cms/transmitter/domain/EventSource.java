package com.oneops.cms.transmitter.domain;

public enum EventSource {
	deployment, release, opsprocedure, cm_ci, cm_ci_rel, rfc_ci, rfc_relation, namespace, novalue;
	
	public static EventSource toEventSource(String str)
    {
        try {
            return valueOf(str.toLowerCase());
        } 
        catch (Exception ex) {
            return novalue;
        }
    }   
	
}
