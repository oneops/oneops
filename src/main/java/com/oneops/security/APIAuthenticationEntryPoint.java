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


