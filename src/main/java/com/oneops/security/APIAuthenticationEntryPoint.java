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
package com.oneops.security;

import com.google.gson.Gson;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;
import com.oneops.util.ErrorResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class APIAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
    private static final Logger logger = Logger.getLogger(APIAuthenticationEntryPoint.class);

    private Gson gson = new Gson();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException, ServletException {
        logger.error("Exception serving request"+exception.getMessage());
        CmsBaseException ce = new CmsBaseException(CmsError.RUNTIME_EXCEPTION, exception.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
        response.setStatus(error.getCode());
        response.getWriter().write(gson.toJson(error));
    }
}


