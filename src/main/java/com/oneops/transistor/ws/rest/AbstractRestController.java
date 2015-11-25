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

    protected void sendError(HttpServletResponse response, int code, CmsError ex) throws IOException {
            ErrorResponse error = new ErrorResponse(code, ex);
            response.setStatus(error.getCode());
            response.getWriter().write(gson.toJson(error));
            logger.error(ex);
    }

}
