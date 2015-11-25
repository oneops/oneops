package com.oneops.cms.transmitter.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import com.oneops.cms.transmitter.MainScheduler;
import com.oneops.cms.transmitter.domain.PubStatus;

@Controller
public class PubController  {
	private MainScheduler scheduler;
	private static Logger logger = Logger.getLogger(PubController.class);


	@RequestMapping("/pub.do")
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		if ("stop".equalsIgnoreCase(request.getParameter("action"))) {
			stopPublisher();
			return new ModelAndView("LocalRedirect");
		} else if ("start".equalsIgnoreCase(request.getParameter("action"))) {
			startPublisher();
			return new ModelAndView("LocalRedirect");
		}
		PubStatus status = scheduler.getStatus();
		logger.info("Returning CMSPublisher");
        return new ModelAndView("CMSPublisher", "status", status);
	}

	public void init() {
		startPublisher();
	}
	
	private void startPublisher() {
		if (!scheduler.getStatus().getIsRunning()) {
			scheduler.startTheJob();
			logger.info("!!!Publisher started!!!");
		}else {
			logger.info("Publisher already running.");
		}
	}
	
	private void stopPublisher() {
		if (scheduler.getStatus().getIsRunning()) {
			scheduler.stopPublishing();
			logger.warn("!!!Publisher stopped!!!");
		}else {
			logger.info("Publisher already stopped.");
		}
	}
	

	public void setScheduler(MainScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@RequestMapping(value="/publisher/status", method = RequestMethod.GET)
	@ResponseBody
	public PubStatus getStatus() {
		PubStatus status = scheduler.getStatus();
		return status;
	}

	@RequestMapping(value="/publisher/stop", method = RequestMethod.PUT)
	@ResponseBody
	public PubStatus stop() {
        stopPublisher();
		PubStatus status = scheduler.getStatus();
		return status;
	}

	@RequestMapping(value="/publisher/start", method = RequestMethod.PUT)
	@ResponseBody
	public PubStatus start() {
		startPublisher();
		PubStatus status = scheduler.getStatus();
		return status;
	}
	
	@RequestMapping(value="/publisher/queue/backlog", method = RequestMethod.GET)
	@ResponseBody
	public int getQueueBacklog() {
		return scheduler.getStatus().getQueueBacklog();
	}
	
	@RequestMapping(value="/publisher/cievents/queue/backlog", method = RequestMethod.GET)
	@ResponseBody
	public int getCiEventsQueueBacklog() {
		return scheduler.getStatus().getCiEventsQueueBacklog();
	}
}
