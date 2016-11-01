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

package com.oneops.daq.web;

import com.google.gson.Gson;
import com.oneops.daq.dao.PerfDao;
import com.oneops.daq.domain.*;
import com.oneops.ops.PerfDataRequest;
import com.oneops.ops.dao.PerfDataAccessor;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Perf data controller.
 */
@Controller
public class PerfController {

    private static final Logger logger = Logger.getLogger(PerfController.class);
    private PerfDao perfDao = null;
    private PerfDataAccessor perfDataAccessor;
    private Gson gson = new Gson();

    /**
     * Daq api home
     *
     * @return a string
     */
    @RequestMapping("/daq-api")
    @ResponseBody
    public String home() {
        return "Perf data controller is up!";
    }

    /**
     * Root is home.
     *
     * @return the string
     */
    @RequestMapping("/")
    @ResponseBody
    public String root() {
        return home();
    }

    /**
     * Gets the perf data.
     *
     * @param request  the request, contains json array of {@link PerfDataRequest}
     * @param response the response, contains json array of tabled results.
     * @return the perf data
     * @throws Exception the exception
     */
    @RequestMapping(value = "/getPerfData", method = {GET, POST})
    public void getPerfData(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String reqSet = ServletRequestUtils.getStringParameter(request, "reqSet");
        PerfDataRequest[] reqs = gson.fromJson(reqSet, PerfDataRequest[].class);

        long startTime = System.currentTimeMillis();
        StringBuilder bu = new StringBuilder("[ ");
        for (int i = 0; i < reqs.length; i++) {
            PerfDataRequest req = reqs[i];
            if (i > 0) {
                bu.append(",");
            }
            bu.append(perfDataAccessor.getPerfDataSeries(req));
        }
        bu.append("\n]");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

        response.getOutputStream().print(bu.toString());
        response.setStatus(200);
    }

    /**
     * Report perf metric counts
     *
     * @param request  the request
     * @param response the response
     * @return string
     * @throws Exception the exception
     */
    @RequestMapping("/reportMetricCounts")
    public void reportMetricCounts(HttpServletRequest request, HttpServletResponse response) throws Exception {

        long startTime = System.currentTimeMillis();
        perfDataAccessor.reportMetricCounts();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

        response.getOutputStream().print("done.\n");
        response.setStatus(200);
    }

    /**
     * Purge perf metrics.
     *
     * @param request  the request
     * @param response the response
     * @return string
     * @throws Exception the exception
     */
    @RequestMapping("/purgeMetrics")
    public void purgeMetrics(HttpServletRequest request, HttpServletResponse response) throws Exception {

        long startTime = System.currentTimeMillis();
        String startEpoch = request.getParameter("startEpoch");

        if (startEpoch == null || startEpoch.isEmpty()) {
            startEpoch = Long.valueOf((startTime / 1000) - (60 * 60 * 24 * 14)).toString();
            logger.info("No startEpoch specified. using 2 weeks ago: " + startEpoch);
        }

        String bucket = request.getParameter("bucket");
        if (bucket == null || bucket.isEmpty()) {
            bucket = "1m";
            logger.info("No bucket specified. using 1m bucket.");
        }

        perfDataAccessor.purgeMetrics(Long.valueOf(startEpoch), bucket);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

        response.getOutputStream().print("done.\n");
        response.setStatus(200);
    }


    /**
     * Gets the chart data.
     *
     * @param request  the request
     * @param response the response
     * @return the chart
     * @throws Exception the exception
     */
    @RequestMapping("/getChart")
    public void getChart(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String chartSetJson = ServletRequestUtils.getStringParameter(request, "chartSet");
        Chart[] reqs = gson.fromJson(chartSetJson, Chart[].class);

        long startTime = System.currentTimeMillis();
        StringBuilder der = new StringBuilder("[");
        for (int i = 0; i < reqs.length; i++) {
            Chart chart = reqs[i];
            if (i > 0) {
                der.append("[");
            }
            chart = perfDao.getChart(chart.getKey());
            der.append(gson.toJson(chart));
        }
        der.append("]");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

        response.getOutputStream().print(der.toString());
        response.setStatus(200);
    }

    /**
     * Sets the chart.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @RequestMapping("/setChart")
    public void setChart(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String chartSetJson = ServletRequestUtils.getStringParameter(request, "chartSet");
        Chart[] reqs = gson.fromJson(chartSetJson, Chart[].class);

        long startTime = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < reqs.length; i++) {
            Chart chart = reqs[i];
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append("\"ok\"");
            perfDao.setChart(chart);
        }
        stringBuilder.append("]");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

        response.getOutputStream().print(stringBuilder.toString());
        response.setStatus(200);
    }

    public PerfDao getPerfDao() {
        return perfDao;
    }

    public void setPerfDao(PerfDao perfDao) {
        this.perfDao = perfDao;
    }

    public PerfDataAccessor getPerfDataAccessor() {
        return perfDataAccessor;
    }

    public void setPerfDataAccessor(PerfDataAccessor perfDataAccessor) {
        this.perfDataAccessor = perfDataAccessor;
    }

}
