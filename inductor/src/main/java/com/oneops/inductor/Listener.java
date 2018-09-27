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

import static com.oneops.cms.util.CmsConstants.QUEUE_TIME;
import static com.oneops.cms.util.CmsConstants.REQUEST_DEQUE_TS;
import static com.oneops.cms.util.CmsConstants.REQUEST_ENQUE_TS;
import static com.oneops.cms.util.CmsConstants.SEARCH_TS_PATTERN;
import static com.oneops.inductor.InductorConstants.ACTION_ORDER_TYPE;
import static com.oneops.inductor.InductorConstants.SEARCH_TS_FORMATS;
import static com.oneops.inductor.InductorConstants.WORK_ORDER_TYPE;
import static com.oneops.inductor.InductorConstants.ADD_FAIL_CLEAN;
import static org.apache.commons.httpclient.util.DateUtil.formatDate;
import static org.apache.commons.httpclient.util.DateUtil.parseDate;

import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Listener - consumes from queue by cloud to execute local or remote puppet modules or chef recipes
 * for work or action orders <p> onMessage is mapped to a Spring ListenerContainer.messageListener
 */

@Component
public class Listener implements MessageListener, ApplicationContextAware {

  private static final Logger logger = Logger.getLogger(Listener.class);
  final private Gson gson = new Gson();

  private ApplicationContext applicationContext = null;
  // Number active work orders being processed
  private AtomicInteger activeThreads = new AtomicInteger(0);
  private Semaphore semaphore = null;
  private MessagePublisher messagePublisher = null;
  private Config config = null;
  private File dataDir = null;
  private WorkOrderExecutor workOrderExecutor;
  private ActionOrderExecutor actionOrderExecutor;
  private MetricRegistry registry;

  @Autowired
  ClassMatchingWoExecutor classMatchingWoExecutor;

  /**
   * allow it to run via cmdline
   */
  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
  }

  /**
   * init - configuration / defaults
   */
  public void init() {
    dataDir = new File(config.getDataDir());
    checkFreeSpace();
    logger.info(this);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      DefaultMessageListenerContainer listenerContainer = (DefaultMessageListenerContainer) applicationContext
          .getBean("listenerContainer");
      logger.info("Stopping listener container...");
      listenerContainer.stop();
      while (activeThreads.get() > 0) {
        logger.info("Shutdown in progress. sleeping for 10sec. activeThreads: " + activeThreads);
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          logger.info("Got InterruptedException, but will still let the activeThreads complete.");
        }
      }
      logger.info("Shutdown done.");
    }));

    File testDir = new File(dataDir, "../test");
    logger.info("Verification test directory created: " + testDir.mkdirs());
  }

  /**
   * check for free space - shutdownshutdown listener and gracefully exit if full
   */
  @SuppressWarnings("static-access")
  private void checkFreeSpace() {
    long freeMB = dataDir.getFreeSpace() / 1024 / 1024;

    if (freeMB < config.getMinFreeSpaceMB()) {
      DefaultMessageListenerContainer listenerContainer = (DefaultMessageListenerContainer)
          applicationContext.getBean("listenerContainer");

      logger.info("Stopping listener container due to "
          + config.getDataDir() + " free space mb: "
          + freeMB + " ... min_free_space_mb: " + config.getMinFreeSpaceMB());

      listenerContainer.stop();
      while (activeThreads.get() > 0) {
        logger
            .error("Shutdown in progress due " + config.getDataDir() + " free space mb: "
                + freeMB + " ... min_free_space_mb: " + config.getMinFreeSpaceMB()
                + ". sleeping for 10sec. activeThreads: " + activeThreads);
        try {
          Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
          logger.info(
              "Got InterruptedException, but will still let the activeThreads complete.");
        }
      }
      Runtime.getRuntime().exit(1);
    } else {
      logger.info(config.getDataDir() + " free space mb: " + freeMB);
    }
  }

  /**
   * for unit test setup
   */
  public void setConfig(Config config) {
    this.config = config;
  }

  /**
   * MessageListener mapped in application-context.xml - will deserialize to a WorkOrder
   * (iaas/swdist) or ActionOrder (procedure)
   *
   * @param msg Message
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  public void onMessage(Message msg) {
    try {
      checkFreeSpace();
      activeThreads.getAndIncrement();
      if (msg instanceof TextMessage) {
        String msgText = ((TextMessage) msg).getText();
        final String correlationID = msg.getJMSCorrelationID();
        Map<String, String> responseMsgMap;
        String type = msg.getStringProperty("type");
        CmsWorkOrderSimpleBase wo;

        switch (type) {
          // WorkOrder
          case WORK_ORDER_TYPE: {
            long t = System.currentTimeMillis();
            wo = getWorkOrderOf(msgText, CmsWorkOrderSimple.class);
            wo.putSearchTag("iWoCrtTime", Long.toString(System.currentTimeMillis() - t));

            String logKey = workOrderExecutor.getLogKey(wo);
            logger.info(logKey + " Inductor: " + config.getIpAddr());

            preProcess(wo);
            wo.putSearchTag("rfcAction", wo.getAction());
            Response response = runWoWithMatchingExecutor((CmsWorkOrderSimple)wo);
            if (response == null || response.getResult() == Result.NOT_MATCHED) {
              responseMsgMap = workOrderExecutor.processAndVerify(wo, correlationID);

              if(computeKciFailed(responseMsgMap)) {
                CmsWorkOrderSimple workorder = updateWorkOrderWithAction(wo, ADD_FAIL_CLEAN);
                workOrderExecutor.process(workorder, correlationID);
              }
            }
            else {
              responseMsgMap = response.getResponseMap();
              postExecTags(wo);
            }
            break;
          }
          // ActionOrder
          case ACTION_ORDER_TYPE: {
            long t = System.currentTimeMillis();
            wo = getWorkOrderOf(msgText, CmsActionOrderSimple.class);
            wo.putSearchTag("iAoCrtTime", Long.toString(System.currentTimeMillis() - t));
            preProcess(wo);

            Response response = runAoWithMatchingExecutor((CmsActionOrderSimple) wo);
            if (response == null || response.getResult() == Result.NOT_MATCHED) {
              responseMsgMap = actionOrderExecutor.processAndVerify(wo, correlationID);
            }
            else {
              responseMsgMap = response.getResponseMap();
              postExecTags(wo);
            }
            break;
          }
          default:
            logger.error(new IllegalArgumentException("Unknown msg type - " + type));
            msg.acknowledge();
            return;
        }

        // Controller will process this message
        responseMsgMap.put("correlationID", correlationID);
        responseMsgMap.put("type", type);

        long startTime = System.currentTimeMillis();
        if (!correlationID.equals("test")) {
          messagePublisher.publishMessage(responseMsgMap);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // ack message
        logger.debug("Send message took:" + duration + "ms");
        msg.acknowledge();
      }
    } catch (JMSException | SecurityException | IOException | IllegalArgumentException e) {
      logger.error("Error occurred in processing message", e);
    } finally {
      // Decrement the total number of active threads consumed by 1
      activeThreads.getAndDecrement();
      clearStateFile();
    }
  }

  private Response runWoWithMatchingExecutor(CmsWorkOrderSimple wo) {
    preExecTags(wo);
    Response response;
    if (config.isCloudStubbed(wo)) {
      return Response.getNotMatchingResponse();
    }
    if (config.isVerifyMode()) {
      response = classMatchingWoExecutor.executeAndVerify(wo, config.getDataDir());
    }
    else {
      response = classMatchingWoExecutor.execute(wo, config.getDataDir());
    }
    return response;
  }

  private Response runAoWithMatchingExecutor(CmsActionOrderSimple ao) {
    preExecTags(ao);
    if (config.isCloudStubbed(ao)) {
      return Response.getNotMatchingResponse();
    }
    return classMatchingWoExecutor.execute(ao, config.getDataDir());
  }

  private void preProcess(CmsWorkOrderSimpleBase wo) {
    setStateFile(wo);
    setQueueTime(wo);
  }

  private void preExecTags(CmsWorkOrderSimpleBase wo) {
    wo.putSearchTag("inductor", config.getIpAddr());
  }

  private void postExecTags(CmsWorkOrderSimpleBase wo) {
    wo.putSearchTag(
        CmsConstants.RESPONSE_ENQUE_TS, DateUtil.formatDate(new Date(), SEARCH_TS_PATTERN));
  }

  private CmsWorkOrderSimpleBase getWorkOrderOf(String msgText, Class c) {
    CmsWorkOrderSimpleBase wo;
    JsonReader reader = new JsonReader(new StringReader(msgText));
    reader.setLenient(true);
    wo = gson.fromJson(reader, c);
    return wo;
  }

  /**
   * set state file by thread id
   */
  private void setStateFile(CmsWorkOrderSimpleBase wo) {
    String filename = getStateFileName();
    String content = System.currentTimeMillis() + " "
        + wo.getClassName() + "::"
        + wo.getAction() + " "
        + wo.getNsPath() + System.lineSeparator();
    writeStateFile(filename, content);
  }

  private String getStateFileName() {
    return config.getDataDir() + "/state-" + Thread.currentThread().getId();
  }


  /**
   * clear state file by thread id
   */
  private void clearStateFile() {
    String filename = getStateFileName();
    String content = "idle" + System.lineSeparator();
    content += System.currentTimeMillis();
    writeStateFile(filename, content);
  }

  private void writeStateFile(String filename, String content) {
    try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename))) {
      bw.write(content);
      bw.close();
      logger.debug("clear state file: " + filename);
    } catch (IOException e) {
      logger.error("could not write file: " + filename + " msg:" + e.getMessage());
    }
  }

  /**
   * Set the queue time in the wo/ao for search/analytics
   */
  private <T> void setQueueTime(CmsWorkOrderSimpleBase<T> wo) {
    String totalTime, requestDequeTs;
    try {
      requestDequeTs = formatDate(new Date(), SEARCH_TS_PATTERN);
      wo.putSearchTag(REQUEST_DEQUE_TS, requestDequeTs);
      totalTime = String.valueOf(getTimeDiff(wo) / 1000.0);
      wo.getSearchTags().put(QUEUE_TIME, totalTime);
    } catch (Exception e) {
      logger.error("Exception occurred while setting queue time " + e);
    }
  }

  private <T> long getTimeDiff(CmsWorkOrderSimpleBase<T> wo) throws DateParseException {
    String currentDate = formatDate(new Date(), SEARCH_TS_PATTERN);
    long currentTime = parseDate(currentDate, SEARCH_TS_FORMATS).getTime();
    long requestEnqueTime = parseDate(wo.getSearchTags().get(REQUEST_ENQUE_TS), SEARCH_TS_FORMATS)
        .getTime();
    return currentTime - requestEnqueTime;
  }

  /**
   * setter for spring to wire the MessagePublisher
   */
  public void setMessagePublisher(MessagePublisher mp) {
    this.messagePublisher = mp;
  }

  public InductorStatus getStatus() {
    InductorStatus stat = new InductorStatus();
    stat.setQueueBacklog(0);
    stat.setQueueName(config.getInQueue());
    return stat;
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

  public void setWorkOrderExecutor(WorkOrderExecutor workOrderExecutor) {
    this.workOrderExecutor = workOrderExecutor;
  }

  public void setActionOrderExecutor(ActionOrderExecutor actionOrderExecutor) {
    this.actionOrderExecutor = actionOrderExecutor;
  }

  public void setRegistry(MetricRegistry registry) {
    this.registry = registry;
  }

  public MetricRegistry getRegistry() {
    return registry;
  }

  public boolean computeKciFailed(Map<String, String> responseMsgMap) {
    return responseMsgMap.containsKey("compute_kci_failure");
  }

  public CmsWorkOrderSimple updateWorkOrderWithAction(CmsWorkOrderSimpleBase wo, String action) {
    CmsWorkOrderSimple workorder = (CmsWorkOrderSimple) wo;
    workorder.getRfcCi().setRfcAction(action);
    workorder.getSearchTags().put("rfcAction", action);
    return workorder;
  }
}
