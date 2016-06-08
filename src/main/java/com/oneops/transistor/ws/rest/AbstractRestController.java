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
package com.oneops.transistor.ws.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.util.ErrorResponse;

public class AbstractRestController {
	protected Logger logger = Logger.getLogger(this.getClass());

	private Gson gson = new Gson();

	protected static final String ONEOPS_SYSTEM_USER = "oneops-system";

    protected void sendError(HttpServletResponse response, int code, CmsError ex) throws IOException {
            ErrorResponse error = new ErrorResponse(code, ex);
            response.setStatus(error.getCode());
            response.getWriter().write(gson.toJson(error));
            logger.error("Exception occurred while serving the request"+ex.getErrorCode());
    }

}
