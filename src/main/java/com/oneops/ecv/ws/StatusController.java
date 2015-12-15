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
package com.oneops.ecv.ws;

import com.google.gson.Gson;
import com.oneops.ecv.auth.AuthUtil;
import com.oneops.ecv.auth.AuthenticationException;
import com.oneops.ecv.config.Config;
import com.oneops.ecv.config.ConfigException;
import com.oneops.ecv.health.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by glall on 1/13/15.
 */
@Controller
@RequestMapping("/ecv")

public class StatusController {

    public static final String IPV4_LOCAL_ADDRESS = "127.0.0.1";
    public static final String IPV6_LOCAL_ADDRESS = "0:0:0:0:0:0:0:1";
    public static final String ALLOWED_IPS_PROPERTY = "ecv.localHostAddresses";
    private static final String SEPARATOR = ",";
    public static final String SEPARATED_LIST_OF_LOCAL_IPS = IPV4_LOCAL_ADDRESS + SEPARATOR + IPV6_LOCAL_ADDRESS;
    private final String allowedIps = System.getProperty(ALLOWED_IPS_PROPERTY, SEPARATED_LIST_OF_LOCAL_IPS);
    protected Logger ECV_LOGGER = Logger.getLogger(this.getClass());

    private Config config;
    private IHealthChecker healthChecker;
    private AuthUtil authUtil;
    private Gson gson = new Gson();


    @ExceptionHandler(FailedHealthCheckException.class)
    @ResponseBody
    public void handleExceptions(FailedHealthCheckException e, HttpServletResponse response) throws IOException {
        IHealth health = e.getHealth();
        response.setStatus(health.getStatusCode());
        response.getWriter().write(gson.toJson(health));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public void handleAuthFailure(AuthenticationException e, HttpServletResponse response) throws IOException {
        ECV_LOGGER.error(e);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(e.getMessage());
    }


    @ExceptionHandler(ConfigException.class)
    @ResponseBody
    public void handleConfigException(ConfigException e, HttpServletResponse response) throws IOException {
        ECV_LOGGER.error(e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(e.getMessage());
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public IHealth status() {
        IHealth appHealth = Health.FAILED_HEALTH;
        String isOnline = config.getStatus();
        if (Config.ONLINE.equals(isOnline)) {
            List<IHealthCheck> healthChecks = healthChecker.getHealthChecksToRun();
            List<IHealth> results = new ArrayList<IHealth>(healthChecks.size());
            if ((healthChecks == null) || (healthChecks.isEmpty())) {
                return Health.OK_HEALTH;
            } else {
                for (IHealthCheck healthCheck : healthChecks) {
                    if (healthCheck != null) {
                        IHealth health = healthCheck.getHealth();
                        ECV_LOGGER.debug("Health got by healthCheck " + healthCheck.getName() + " result :" + health.toString());
                        results.add(health);
                        if (!health.isOK()) {
                            throw new FailedHealthCheckException(health);
                        }
                    }
                }
                if (results.isEmpty()) {
                    ECV_LOGGER.info("results were empty");
                    appHealth = Health.OK_HEALTH;
                } else {
                    //different health checks might have different status codes,one which has most weight will determine the overall status code
                    Collections.sort(results, HealthCheckUtils.STATUS_CODE_COMPARATOR);
                    IHealth healthThatMatters = results.get(0);
                    ECV_LOGGER.debug("over all status health got by healthCheck " + healthThatMatters.getName() + " result :" + healthThatMatters.toString());
                    appHealth = healthThatMatters;
                }
            }

        } else {
            ECV_LOGGER.warn("The node is marked offline ");
            throw new FailedHealthCheckException(Health.OFFLINE_HEALTH);
        }
        ECV_LOGGER.debug("over all status health got by healthCheck " + appHealth.getName() + " result :" + appHealth.toString());
        return appHealth;
    }

    @RequestMapping(value = "/status/offline", method = RequestMethod.PUT)
    @ResponseBody
    public IHealth offline(HttpServletRequest request) {
        checkIfAuthorized(request);
        config.setProperty(Config.OFFLINE);
        return status();
    }


    @RequestMapping(value = "/status/online", method = RequestMethod.PUT)
    @ResponseBody
    public IHealth online(HttpServletRequest request) {
        checkIfAuthorized(request);
        config.setProperty(Config.ONLINE);
        return status();
    }

    @RequestMapping(value = "/status/shutdown", method = RequestMethod.PUT)
    @ResponseBody
    public IHealth shutdown(HttpServletRequest request) {
        checkIfAuthorized(request);
        config.setTransientStatus(Config.SHUTDOWN);
        return status();
    }

    @RequestMapping(value = "/config/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, String>> getECVConfig() {
        return new ResponseEntity<Map<String, String>>(config.getInternalConfig(), HttpStatus.OK);
    }

    private void checkIfAuthorized(HttpServletRequest request) {
        boolean isAuthorized = isAuthorized(request);
        if (!isAuthorized) throw new AuthenticationException("Failed to authorize");
    }

    protected final boolean isAuthorized(HttpServletRequest req) {
        boolean isAuthorized;
        isAuthorized = authUtil.authenticate(req.getHeader("Authorization"));
        String remoteAddress = req.getRemoteAddr();
        boolean isLocalRequest = isLocal(remoteAddress);
        ECV_LOGGER.info("Authorizing validCredential :" + isAuthorized + " isLocal:" + isLocalRequest + " authorized:" + (isAuthorized && isLocalRequest));
        isAuthorized = isAuthorized && isLocalRequest;
        return isAuthorized;
    }

    protected boolean isLocal(String ip) {
        List<String> ips = Arrays.asList(allowedIps.split(SEPARATOR));
        if (ip != null && ip.indexOf("%") != -1) {
            ip = ip.substring(0, ip.indexOf("%"));
            ECV_LOGGER.debug(" ip to be checked " + ip);
        }
        boolean isValid = false;
        if (ips.contains(ip)) {
            isValid = true;
        }
        return isValid;
    }


    public void setConfig(Config config) {
        this.config = config;
    }

    public IHealthChecker getHealthChecker() {
        return healthChecker;
    }

    public void setHealthChecker(IHealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }

    public AuthUtil getAuthUtil() {
        return authUtil;
    }

    public void setAuthUtil(AuthUtil authUtil) {
        this.authUtil = authUtil;
    }


}
