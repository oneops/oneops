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
package com.oneops.cms.ns.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.oneops.cms.ns.dal.NSMapper;
import com.oneops.cms.ns.domain.CmsNamespace;

/**
 * The Class CmsNsManagerImpl.
 */
public class CmsNsManagerImpl implements CmsNsManager {

	private CmsNsProcessor cmsNsProcessor;

	public void setCmsNsProcessor(CmsNsProcessor cmsNsProcessor) {
		this.cmsNsProcessor = cmsNsProcessor;
	}

	public void setNsMapper(NSMapper nsMapper) {
		cmsNsProcessor.setNsMapper(nsMapper);
	}

	public CmsNamespace createNs(CmsNamespace ns) {
		return cmsNsProcessor.createNs(ns);
	}

	public CmsNamespace getNs(String nsPath) {
		return cmsNsProcessor.getNs(nsPath);
	}

	public void deleteNs(String nsPath) {
		cmsNsProcessor.deleteNs(nsPath);
	}

	public CmsNamespace getNsById(long nsId) {
		return cmsNsProcessor.getNsById(nsId);
	}

	public void deleteNsById(long nsId) {
		cmsNsProcessor.deleteNsById(nsId);
	}

	public List<CmsNamespace> getNsLike(String nsPath) {
		return cmsNsProcessor.getNsLike(nsPath);
	}

	public void lockNs(String nsPath) {
		cmsNsProcessor.lockNs(nsPath);
	}
}
