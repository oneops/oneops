package com.oneops.inductor;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.metrics.OneOpsMetrics.INDUCTOR;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.log4j.Logger;
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
  private Config config;
  private DefaultMessageListenerContainer listenerContainer;
  private DecimalFormat format;

  public StatCollector(Config config) {
    this.config = config;
  }

  public void init() {
    try {
      if (config.isJMXEnabled()) {
        jmxReporter = JmxReporter.forRegistry(metrics).build();
        jmxReporter.start();
      }

      if (config.isAutoShutDown()) {
        autoShutDownScheduler
            .scheduleWithFixedDelay(this::shutDown, delayInSecs, delayInSecs, SECONDS);
      }

      logger.info("Initializing StatCollector file : " + statFileName);
      statChannel = FileChannel.open(Paths.get(statFileName), CREATE, WRITE);
    } catch (IOException e) {
      logger.error("Error while creating stat file " + statFileName, e);
    }
    rsyncFailed = metrics.meter(name(INDUCTOR, "rsyncFailure"));
    woFailed = metrics.meter(name(INDUCTOR, "woFailure"));
    format = new DecimalFormat();
    format.setMaximumFractionDigits(2);
    statScheduler
        .scheduleWithFixedDelay(this::writeStat, delayInSecs, delayInSecs, SECONDS);
  }

  private void shutDown() {
    if (config.isAutoShutDown() && rsyncFailed.getFifteenMinuteRate() > config
        .getAutoShutDownThreshold()) {
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
          "Inductor Stat - | failed_count=" + format.format(woFailed.getOneMinuteRate())
              + ", rsync_count="
              + format.format(rsyncFailed.getOneMinuteRate()) + "\n";
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

  public static FileChannel getStatChannel() {
    return statChannel;
  }

  public static void setStatChannel(FileChannel statChannel) {
    StatCollector.statChannel = statChannel;
  }

  public static void setDelayInSecs(long delay) {
    StatCollector.delayInSecs = delay;
  }

  /**
   * Sets the inductor stats file name. If the file name is relative, it will set as
   * "inductor_home/log/statFile". No change for absolute path.
   *
   * @param statFile s file name. Can be absolute or relative.
   */
  public void setStatFileName(String statFile) throws IOException {
    if (Paths.get(statFile).isAbsolute()) {
      this.statFileName = statFile;
    } else {
      this.statFileName = Paths.get(config.getDataDir(), "/../", "log", statFile).toFile()
          .getCanonicalPath();
    }
  }

  public String getStatFileName() {
    return statFileName;
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
