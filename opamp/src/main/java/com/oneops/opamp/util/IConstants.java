package com.oneops.opamp.util;

public interface IConstants {

	public static final String X_CMS_USER = "X-Cms-User";
	public static final String ONEOPS_AUTO_REPLACE_USER_PROP_NAME = "oneops-auto-replace-user";
	public static final String ONEOPS_AUTOREPLACE_USER = System.getProperty(ONEOPS_AUTO_REPLACE_USER_PROP_NAME,
			"oneops-autoreplace");
	
	public static final String AzureServiceBus_Event_attribute_resourceId="resourceId";
	public static final String AzureServiceBus_Event_attribute_status="status";
	public static final String AzureServiceBus_Event_attribute_status_failed="Failed";
	public static final String AzureServiceBus_Event_attribute_resourceProviderName="resourceProviderName";
	public static final String AzureServiceBus_Event_attribute_resourceProviderName_Value="Microsoft.Compute";
	
}
