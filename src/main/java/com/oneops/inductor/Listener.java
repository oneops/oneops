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
package com.oneops.inductor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;

import static com.oneops.inductor.InductorConstants.*;

/**
 *         Listener - consumes from queue by cloud to execute local or 
 *         remote puppet modules or chef recipes for work or action orders
 * 
 *         onMessage is mapped to a Spring ListenerContainer.messageListener
 */

@Component
public class Listener implements MessageListener, ApplicationContextAware {

	private static Logger logger = Logger.getLogger(Listener.class);

	final private Gson gson = new Gson();
	final private Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

	private ApplicationContext applicationContext = null;

	// Number active work orders being processed
	private int activeThreads = 0;

	private Semaphore semaphore = null;

	private MessagePublisher messagePublisher = null;

	private Config config = null;
	
	private File filesystem = null;

	/**
	 * init - configuration / defaults
	 */
	public void init() throws Exception {

		filesystem = new File(config.getDataDir());
		checkFreeSpace();
		
		semaphore = new Semaphore(config.getLocalMaxConsumers());
		
		logger.info(this);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				DefaultMessageListenerContainer listenerContainer = (DefaultMessageListenerContainer) 
						applicationContext.getBean("listenerContainer");

				logger.info("Stopping listener container...");
				listenerContainer.stop();
				while (activeThreads > 0) {
					logger.info("Shutdown in progress. sleeping for 10sec. activeThreads: "
							+ activeThreads);
					try {
						Thread.currentThread().sleep(10000);
					} catch (InterruptedException e) {
						logger.info("Got InterruptedException, but will still let the activeThreads complete.");
					}
				}

				logger.info("Shutdown done.");
			}
		});

	}
	

	/**
	 * check for free space - shutdown listener and gracefully exit if full
	 */	
	@SuppressWarnings("static-access")
	private void checkFreeSpace() {
		long freeMB = filesystem.getFreeSpace() / 1024 / 1024;
		
		if (freeMB < config.getMinFreeSpaceMB()) {
			DefaultMessageListenerContainer listenerContainer = (DefaultMessageListenerContainer) 
					applicationContext.getBean("listenerContainer");

			logger.info("Stopping listener container due to "+config.getDataDir()+" free space mb: "+freeMB+
						" ... min_free_space_mb: "+config.getMinFreeSpaceMB());
			listenerContainer.stop();
			while (activeThreads > 0) {
				logger.error("Shutdown in progress due "+config.getDataDir()+" free space mb: "+freeMB+
						" ... min_free_space_mb: "+config.getMinFreeSpaceMB()+
						". sleeping for 10sec. activeThreads: "+ activeThreads);
				try {
					Thread.currentThread().sleep(10000);
				} catch (InterruptedException e) {
					logger.info("Got InterruptedException, but will still let the activeThreads complete.");
				}
			}
			Runtime.getRuntime().exit(1);
		} else
			logger.info(config.getDataDir()+" free space mb: "+freeMB );
	}
	

	/**
	 * for unit test setup
	 */
	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Increment the total number of active threads consumed by 1
	 */
	private synchronized void addActive() {
		activeThreads++;
	}

	/**
	 * Decrement the total number of active threads consumed by 1
	 */
	private synchronized void removeActive() {
		activeThreads--;
	}

	/**
	 * MessageListener mapped in application-context.xml - will deserialize to a
	 * WorkOrder (iaas/swdist) or ActionOrder (procedure)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 * 
	 * @param msg Message
	 */
	public void onMessage(Message msg) {

		try {			
			checkFreeSpace();
			addActive();
			
			if (msg instanceof TextMessage) {

				String msgText = ((TextMessage) msg).getText();
				final String correlationID = msg.getJMSCorrelationID();
				Map<String, String> responseMsgMap = null;

				String type = msg.getStringProperty("type");
				CmsWorkOrderSimpleBase wo;

				// WorkOrder
				if (type.equals(WORK_ORDER_TYPE)) {
					JsonReader reader = new JsonReader(
							new StringReader(msgText));
					reader.setLenient(true);
					wo = gson.fromJson(reader, CmsWorkOrderSimple.class);
					setStateFile((CmsWorkOrderSimple) wo);
					setQueueTime(wo);
					((CmsWorkOrderSimple) wo).getSearchTags()
							.put("rfcAction",
									((CmsWorkOrderSimple) wo).getRfcCi()
											.getRfcAction());
					WorkOrderExecutor woe = new WorkOrderExecutor(config,semaphore);
					responseMsgMap = woe.process(wo, correlationID);
				
				// ActionOrder
				} else if (type.equals(ACTION_ORDER_TYPE)) {
					JsonReader reader = new JsonReader(new StringReader(msgText));
					reader.setLenient(true);
					wo = gsonPretty
							.fromJson(reader, CmsActionOrderSimple.class);
					setStateFile((CmsActionOrderSimple) wo);
					setQueueTime(wo);
					ActionOrderExecutor aoe = new ActionOrderExecutor(config,semaphore);
					responseMsgMap = aoe.process(wo, correlationID);
				} else {
					logger.error(new IllegalArgumentException(
							"Unknown msg type - " + type));
					msg.acknowledge();
					return;
				}

				// Controller will process this message
				responseMsgMap.put("correlationID", correlationID);
				responseMsgMap.put("type", type);

				long startTime = System.currentTimeMillis();

				if (!correlationID.equals("test"))
					messagePublisher.publishMessage(responseMsgMap);
				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime;

				// ack message
				logger.debug("send message took:" + duration + "ms");
				msg.acknowledge();

			}
		} catch (JMSException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.error(e);
			e.printStackTrace();
		} finally {
			removeActive();
			clearStateFile();
		}
	}

	/**
	 * set state file by thread id
	 */
	private void setStateFile(CmsWorkOrderSimple wo) {
		String filename = config.getDataDir() + "/state-"
				+ Thread.currentThread().getId();
		FileWriter fstream;
		try {
			fstream = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fstream);
			String content = System.currentTimeMillis() + " "
					+ wo.getRfcCi().getCiClassName() + "::"
					+ wo.getRfcCi().getRfcAction() + " "
					+ wo.getRfcCi().getNsPath() + "\n";
			bw.write(content);
			bw.close();

			logger.debug("set state file: " + filename);

		} catch (IOException e) {
			logger.error("could not write file: " + filename + " msg:"
					+ e.getMessage());
		}
	}

	/**
	 * set state file by thread id
	 */
	private void setStateFile(CmsActionOrderSimple ao) {
		String filename = config.getDataDir() + "/state-"
				+ Thread.currentThread().getId();
		FileWriter fstream;
		try {
			fstream = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fstream);
			String content = System.currentTimeMillis() + " "
					+ ao.getCi().getCiClassName() + "::" + ao.getActionName()
					+ " " + ao.getCi().getNsPath() + "\n";
			bw.write(content);
			bw.close();

			logger.debug("set state file: " + filename);

		} catch (IOException e) {
			logger.error("could not write file: " + filename + " msg:"
					+ e.getMessage());
		}
	}

	/**
	 * clear state file by thread id
	 */
	private void clearStateFile() {
		String filename = config.getDataDir() + "/state-"
				+ Thread.currentThread().getId();
		FileWriter fstream;
		try {
			fstream = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fstream);
			String content = "idle\n";
			content += System.currentTimeMillis();

			bw.write(content);
			bw.close();

			logger.debug("clear state file: " + filename);

		} catch (IOException e) {
			logger.error("could not write file: " + filename + " msg:"
					+ e.getMessage());
		}
	}

	/**
	 * Set the queue time in the wo/ao for search/analytics
	 * 
	 * @param wo
	 */
	private void setQueueTime(CmsWorkOrderSimpleBase wo) {
		String totalTime, requestDequeTs;
		try {
			requestDequeTs = DateUtil.formatDate(new Date(),
					CmsConstants.SEARCH_TS_PATTERN);
			wo.getSearchTags().put(CmsConstants.REQUEST_DEQUE_TS,
					requestDequeTs);

			totalTime = String
					.valueOf((DateUtil
							.parseDate(
									requestDequeTs,
									Arrays.asList(new String[] { CmsConstants.SEARCH_TS_PATTERN }))
							.getTime() - DateUtil
							.parseDate(
									wo.getSearchTags().get(
											CmsConstants.REQUEST_ENQUE_TS),
									Arrays.asList(new String[] { CmsConstants.SEARCH_TS_PATTERN }))
							.getTime()) / 1000.0);
			wo.getSearchTags().put(CmsConstants.QUEUE_TIME, totalTime);
		} catch (Exception e) {
			//logger.error("Exception occured while setting queue time " + e);
		}
	}

	/**
	 * setter for spring to wire the MessagePublisher
	 */
	public void setMessagePublisher(MessagePublisher mp) {
		this.messagePublisher = mp;
	}

	public InductorStatus getStatus() {
		InductorStatus stat = new InductorStatus();

		// TODO: find way to get backlog, return stats
		// int backLog = listenerContainer..getQueueBacklog();
		stat.setQueueBacklog(0);
		stat.setQueueName(config.getInQueue());
		// stat.setRunning(isRunning);
		// Date dateLastRun = new Date(lastRun);
		// stat.setLastRun(dateLastRun);
		return stat;
	}

	/**
	 * allow it to run via cmdline
	 */
	public static void main(String[] args) throws JMSException {

		@SuppressWarnings("unused")
		// spring based bootstrap - see
		// src/main/resources/application-context.xml
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"application-context.xml");
	}

	@Override
	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		this.applicationContext = ac;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Inductor{ ");
		sb.append(config);
		sb.append(", semaphore=").append(semaphore);
		sb.append('}');
		return sb.toString();
	}
}
