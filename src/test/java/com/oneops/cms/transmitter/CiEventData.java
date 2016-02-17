/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.cms.transmitter;

import java.io.Serializable;
import java.util.List;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.transmitter.domain.CMSEvent;

public class CiEventData implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<CmsRfcCI> rfcCiEvents;
	private List<CmsCI> cmsCiEvents;
	private List<CmsRelease> cmsReleaseEvents;
	
	private transient List<CMSEvent> rfcCiCMSEvents;
	private transient List<CMSEvent> cmsCiCMSEvents;
	private transient List<CMSEvent> cmsReleaseCMSEvents;
	
	public List<CmsRfcCI> getRfcCiEvents() {
		return rfcCiEvents;
	}
	public void setRfcCiEvents(List<CmsRfcCI> rfcCiEvents) {
		this.rfcCiEvents = rfcCiEvents;
	}
	public List<CmsCI> getCmsCiEvents() {
		return cmsCiEvents;
	}
	public void setCmsCiEvents(List<CmsCI> cmsCiEvents) {
		this.cmsCiEvents = cmsCiEvents;
	}
	public List<CmsRelease> getCmsReleaseEvents() {
		return cmsReleaseEvents;
	}
	public void setCmsReleaseEvents(List<CmsRelease> cmsReleaseEvents) {
		this.cmsReleaseEvents = cmsReleaseEvents;
	}
	public List<CMSEvent> getRfcCiCMSEvents() {
		return rfcCiCMSEvents;
	}
	public void setRfcCiCMSEvents(List<CMSEvent> rfcCiCMSEvents) {
		this.rfcCiCMSEvents = rfcCiCMSEvents;
	}
	public List<CMSEvent> getCmsCiCMSEvents() {
		return cmsCiCMSEvents;
	}
	public void setCmsCiCMSEvents(List<CMSEvent> cmsCiCMSEvents) {
		this.cmsCiCMSEvents = cmsCiCMSEvents;
	}
	public List<CMSEvent> getCmsReleaseCMSEvents() {
		return cmsReleaseCMSEvents;
	}
	public void setCmsReleaseCMSEvents(List<CMSEvent> cmsReleaseCMSEvents) {
		this.cmsReleaseCMSEvents = cmsReleaseCMSEvents;
	}
	
}
