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
import com.oneops.daq.dao.LogDao;
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


/**
 * The Class PerfAndLogController.
 */
@Controller
public class PerfAndLogController {
	private static final Logger logger = Logger.getLogger(PerfAndLogController.class);

	private PerfDao perfDao = null;
	private PerfDataAccessor perfDataAccessor;
	private LogDao logDao = null;

	private Gson gson = new Gson();

	/**
	 * Home.
	 *
	 * @return the string
	 */
	@RequestMapping("/daq-api")
	public @ResponseBody
	String home() {
		return "<html>Perf And Log up ... will add stats soon</html>";
	}

	/**
	 * Home.
	 *
	 * @return the string
	 */
	@RequestMapping("/")
	@ResponseBody
	public String root() {
		return home();
	}



	/**
	 * Inits the.
	 */
	public void init() {
		String daqHost = "daq";
		logger.info("Starting up DAQ Web Controller using backend " + daqHost);
		//TODO init via spring initialization
	}


	/**
	 * Gets the available log types.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the available log types
	 * @throws Exception the exception
	 */
	@RequestMapping("/getAvailableLogTypes")
	public void getAvailableLogTypes(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String out = "";
		String[] ciList = null;
		String ciId = ServletRequestUtils.getStringParameter(request, "ci_id");
		long startTime = System.currentTimeMillis();
		String ciListParam = "";
		if (ciId == null) {
			ciListParam = ServletRequestUtils.getStringParameter(request,
					"ci_list");
			if (ciListParam == null) {
				response.getOutputStream().print("no ci_id or ci_list param");
				response.setStatus(500);
				return;
			}
			ciList = ciListParam.split(",");
			out = "[";
			out += logDao.getAvailableLogTypes(ciList);
			out += "]";
		} else {
			ciList = new String[1];
			ciList[0] = ciId;
			out = logDao.getAvailableLogTypes(ciList);
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		logger.debug(request.getRemoteAddr() + " took " + duration + " ms");
		response.getOutputStream().print(out);
		response.setStatus(200);
	}

	/*
	 * handler for /daq-api/getLogData - input: json array of GetLogDataRequest
	 * output: json array of tabled results (2dim array for rows/cols of result
	 * set) - think JsonCsvTableArray
	 */
	/**
	 * Gets the log data.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the log data
	 * @throws Exception the exception
	 */
	@RequestMapping("/getLogData")
	public void getLogData(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String reqSet = ServletRequestUtils.getStringParameter(request,
				"reqSet");
		GetLogDataRequest[] reqs = new GetLogDataRequest[100];
		reqs = gson.fromJson(reqSet, reqs.getClass());

		long startTime = System.currentTimeMillis();

		StringBuilder os = new StringBuilder("[ ");
		for (int i = 0; i < reqs.length; i++) {
			GetLogDataRequest req = reqs[i];
			if (i > 0) {
			    os.append(",");
			}
			GetLogDataResponse resp = logDao.getLogData(req);
			os.append(gson.toJson(resp));
		}
		os.append("]");

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

		response.getOutputStream().print(os.toString());
		response.setStatus(200);
	}

	/*
	 * handler for /daq-api/getLogData - input: json array of GetLogDataRequest
	 * output: json array of tabled results (2dim array for rows/cols of result
	 * set) - think JsonCsvTableArray
	 */
	/**
	 * Gets the action or workorder log data.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the action or workorder log data
	 * @throws Exception the exception
	 */
	@RequestMapping("/getActionOrWorkorderLogData")
	public void getActionOrWorkorderLogData(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String reqSet = ServletRequestUtils.getStringParameter(request,
				"reqSet");

		GetLogDataByIdRequest[] reqs = new GetLogDataByIdRequest[100];
		reqs = gson.fromJson(reqSet, reqs.getClass());

		long startTime = System.currentTimeMillis();

		StringBuilder s = new StringBuilder("[ ");
		for (int i = 0; i < reqs.length; i++) {
			GetLogDataByIdRequest req = reqs[i];
			if (i > 0) {
			    s.append(",");
			}
			GetLogDataByIdResponse resp = logDao
					.getLogDataByActionOrWorkorder(req);
			s.append(gson.toJson(resp));
		}
		s.append("]");

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

        response.setCharacterEncoding("UTF-8");
		response.getWriter().print(s.toString());
		response.setStatus(200);
	}

	/*
	 * handler for /daq-api/getPerfData - input: json array of
	 * GetPerfDataRequest output: json array of tabled results (2dim array for
	 * rows/cols of result set) - think JsonCsvTableArray
	 */
	/**
	 * Gets the perf data.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the perf data
	 * @throws Exception the exception
	 */
	@RequestMapping(value = "/getPerfData", 
			method = { RequestMethod.GET, RequestMethod.POST })
	public void getPerfData(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String reqSet = ServletRequestUtils.getStringParameter(request,
				"reqSet");
		PerfDataRequest[] reqs = new PerfDataRequest[100];
		reqs = gson.fromJson(reqSet, reqs.getClass());

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
	 * reportsMetricCounts
	 *
	 * @param request the request
	 * @param response the response
	 * @return string
	 * @throws Exception the exception
	 */
	@RequestMapping("/reportMetricCounts")
	public void reportMetricCounts(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		long startTime = System.currentTimeMillis();
		perfDataAccessor.reportMetricCounts();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

		response.getOutputStream().print("done.\n");
		response.setStatus(200);
	}
	
	/**
	 * purgeMetrics
	 *
	 * @param request the request
	 * @param response the response
	 * @return string
	 * @throws Exception the exception
	 */
	@RequestMapping("/purgeMetrics")
	public void purgeMetrics(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		long startTime = System.currentTimeMillis();
		String startEpoch = request.getParameter("startEpoch");
		
		if (startEpoch == null || startEpoch.isEmpty() ) {
			startEpoch = Long.valueOf((startTime/1000) - (60*60*24*14) ).toString();
			logger.info("no startEpoch specified. using 2 weeks ago: "+startEpoch);
		}

		String bucket = request.getParameter("bucket");
		if (bucket == null || bucket.isEmpty() ) {
			bucket = "1m";
			logger.info("no bucket specified. using 1m bucket.");
		}		
		
		perfDataAccessor.purgeMetrics(Long.valueOf(startEpoch),bucket);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		logger.debug(request.getRemoteAddr() + " took " + duration + " ms");

		response.getOutputStream().print("done.\n");
		response.setStatus(200);
	}
	
	
	/**
	 * Gets the chart.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the chart
	 * @throws Exception the exception
	 */
	@RequestMapping("/getChart")
	public void getChart(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String chartSetJson = ServletRequestUtils.getStringParameter(request,
				"chartSet");

		Chart[] reqs = new Chart[100];
		reqs = gson.fromJson(chartSetJson, reqs.getClass());

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
	 * @param request the request
	 * @param response the response
	 * @throws Exception the exception
	 */
	@RequestMapping("/setChart")
	public void setChart(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String chartSetJson = ServletRequestUtils.getStringParameter(request,
				"chartSet");

		Chart[] reqs = new Chart[100];
		reqs = gson.fromJson(chartSetJson, reqs.getClass());

		long startTime = System.currentTimeMillis();

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < reqs.length; i++) {
			Chart chart = reqs[i];
			if (i > 0){
			stringBuilder.append(",");
			}
			stringBuilder.append("\"ok\"");
			perfDao.setChart(chart);
		}
		stringBuilder.append("]");
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		logger.debug(request.getRemoteAddr()  + " took " + duration + " ms");

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

	public LogDao getLogDao() {
		return logDao;
	}

	public void setLogDao(LogDao logDao) {
		this.logDao = logDao;
	}

}
