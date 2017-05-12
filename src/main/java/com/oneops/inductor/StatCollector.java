package com.oneops.inductor;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.metrics.OneOpsMetrics.INDUCTOR;

import com.codahale.metrics.JmxReporter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class StatCollector {

  private static FileChannel statChannel;
  private static long delayInSecs = 60;
  private static Logger logger = Logger.getLogger(StatCollector.class);
  private final ScheduledExecutorService statScheduler = Executors
      .newSingleThreadScheduledExecutor();
  private final ScheduledExecutorService autoShutDownScheduler = Executors
      .newSingleThreadScheduledExecutor();
  private String statFileName;
  private MetricRegistry metrics;
  private Meter rsyncFailed;
  private Meter woFailed;
  private JmxReporter jmxReporter;
  @Autowired
  private Config config;
  private DefaultMessageListenerContainer listenerContainer;

  public static FileChannel getStatChannel() {
    return statChannel;
  }

  public static void setStatChannel(FileChannel statChannel) {
    StatCollector.statChannel = statChannel;
  }

  public static void setDelayInSecs(long delay) {
    StatCollector.delayInSecs = delay;
  }

  public void init() {
    try {

      if (config.isJMXEnabled()) {
        jmxReporter = JmxReporter.forRegistry(metrics).build();
        jmxReporter.start();
      }

      if (config.isAutoShutDown()) {
        autoShutDownScheduler
            .scheduleWithFixedDelay(this::shutDown, delayInSecs, delayInSecs, TimeUnit.SECONDS);
      }

      statChannel = FileChannel.open(Paths.get(statFileName), StandardOpenOption.WRITE);
      logger.info("initializing StatCollector file : " + statFileName);
    } catch (IOException e) {
      logger.error("Error while creating stat file " + statFileName, e);
    }
    rsyncFailed = metrics.meter(name(INDUCTOR, "rsyncFailure"));
    woFailed = metrics.meter(name(INDUCTOR, "woFailure"));
    statScheduler
        .scheduleWithFixedDelay(this::writeStat, delayInSecs, delayInSecs, TimeUnit.SECONDS);
  }

  private void shutDown() {

    if(config.isAutoShutDown() && rsyncFailed.getFifteenMinuteRate() > config.getAutoShutDownThreshold()){
      logger.warn("Shutting down");
      listenerContainer.stop();
    }
  }

  public void appendStat(String stat) throws IOException {
    statChannel.write(ByteBuffer.wrap(stat.getBytes()));
    statChannel.force(false);
  }

  public void addWoFailed() {
    woFailed.mark();
  }

  public void addRsyncFailed() {
    rsyncFailed.mark();
  }

  public void writeStat() {
    try {
      String statMsg =
          "Inductor Stat - | failed_count=" + woFailed.getOneMinuteRate() + ", rsync_count="
              + rsyncFailed.getOneMinuteRate();
      appendStat(statMsg);
    } catch (IOException e) {
      logger.error("Error while writing stat", e);
    }
  }

  public void close() {
    if (statChannel != null && statChannel.isOpen()) {
      try {
        statChannel.close();
      } catch (IOException ex) {
        logger.error("Error closing the stat channel for " + statFileName, ex);
      }
    }
    statScheduler.shutdown();
  }

  public void setStatFileName(String statFile) {
    this.statFileName = statFile;
  }

  public void setMetrics(MetricRegistry metrics) {
    this.metrics = metrics;
  }

  public void setListenerContainer(DefaultMessageListenerContainer listenerContainer) {
    this.listenerContainer = listenerContainer;
  }

  public DefaultMessageListenerContainer getListenerContainer() {
    return listenerContainer;
  }
}
