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
package com.oneops.cms.util.domain;

import java.util.List;

public class CmsStuckDpmtCollection {
	
	List<CmsStuckDpmt> cmsStuckDpmts;
	
	List<CmsStuckDpmt> inProgressStuckDpmts;
	
	List<CmsStuckDpmt> pausedStuckDpmts;

	public List<CmsStuckDpmt> getCmsStuckDpmts() {
		return cmsStuckDpmts;
	}

	public void setCmsStuckDpmts(List<CmsStuckDpmt> cmsStuckDpmts) {
		this.cmsStuckDpmts = cmsStuckDpmts;
	}

	public List<CmsStuckDpmt> getInProgressStuckDpmts() {
		return inProgressStuckDpmts;
	}

	public void setInProgressStuckDpmts(List<CmsStuckDpmt> inProgressStuckDpmts) {
		this.inProgressStuckDpmts = inProgressStuckDpmts;
	}

	public List<CmsStuckDpmt> getPausedStuckDpmts() {
		return pausedStuckDpmts;
	}

	public void setPausedStuckDpmts(List<CmsStuckDpmt> pausedStuckDpmts) {
		this.pausedStuckDpmts = pausedStuckDpmts;
	}
	
	
	
}
