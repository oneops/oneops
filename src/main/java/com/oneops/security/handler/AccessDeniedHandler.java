package com.oneops.security.handler;

import com.google.gson.Gson;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;
import com.oneops.util.ErrorResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by glall on 6/8/15.
 */
public class AccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {
    private static final Logger logger = Logger.getLogger(AccessDeniedHandler.class);

    private Gson gson = new Gson();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        logger.error("Exception serving request" + accessDeniedException.getMessage());
        CmsBaseException ce = new CmsBaseException(CmsError.RUNTIME_EXCEPTION, accessDeniedException.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage());
        response.setStatus(error.getCode());
        response.getWriter().write(gson.toJson(error));
    }
}
