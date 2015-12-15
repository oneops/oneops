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
package com.oneops.transistor.ws.rest.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.cms.util.domain.CmsVar;

/**
 * 
 * Transistor Meta-data cache filter: Invalidates the Md cache after rake install
 *
 */
public class TransistorMdCacheFilter implements Filter {
	
	private static final String MD_CACHE_STATUS_VAR = "MD_UPDATE_TIMESTAMP";
	private CmsMdManager mdManager;
	private CmsCmManager cmManager;
	Long lastUpdatedTs = null;
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		CmsVar cacheStatusVar = cmManager.getCmSimpleVar(MD_CACHE_STATUS_VAR);
		if (cacheStatusVar != null) {
			
			if(lastUpdatedTs == null) {
				lastUpdatedTs = Long.parseLong(cacheStatusVar.getValue());
			}
			//Invalidate the transistor meta-data cache if rake install has updated the cache TS 
			if(lastUpdatedTs.compareTo(Long.parseLong(cacheStatusVar.getValue())) < 0){
				mdManager.invalidateCache();
				lastUpdatedTs = Long.parseLong(cacheStatusVar.getValue());
				logger.info("Transistor metadata cache invalidated.");
			}
		}
		chain.doFilter(request,response);
	}

	@Override
	public void destroy() {
	}

	public void setMdManager(CmsMdManager mdManager) {
		this.mdManager = mdManager;
	}

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

}
