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
package com.oneops.cms.ws.rest;

import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.ws.rest.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractRestController {

	final Logger logger = Logger.getLogger(this.getClass());
	private final Gson gson = new Gson();
	
	protected void sendError(HttpServletResponse response, int code, CmsError ex) throws IOException {
		ErrorResponse error = new ErrorResponse(code, ex);
		response.setStatus(error.getCode());
		response.getWriter().write(gson.toJson(error));
		logger.error(ex);
	}

    /**
     * Generic Exception handler for unknown Exception
     * @param e UnknownException
     * @param response ErrorResponse
     * @throws IOException
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletResponse response) throws IOException {
        logger.error("Exception serving request ",e);
        CmsBaseException ce = new CmsBaseException(CmsError.RUNTIME_EXCEPTION,e.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ce);
        response.setStatus(error.getCode());
        response.getWriter().write(gson.toJson(error));
    }
}
