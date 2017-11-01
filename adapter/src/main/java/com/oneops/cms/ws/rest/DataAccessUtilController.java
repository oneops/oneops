package com.oneops.cms.ws.rest;

import com.oneops.cms.ds.DataAccessAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DataAccessUtilController extends AbstractRestController {

  @Autowired
  DataAccessAspect dataAccessAspect;

  @RequestMapping(method = RequestMethod.PUT, value = "/ds/standby")
  @ResponseBody
  public void updateQueryStandbyFlag(@RequestParam(value="enabled", required = true) Boolean enabled) {
    dataAccessAspect.setQueryStandByEnabled(enabled);
  }

}
