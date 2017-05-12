package com.oneops.inductor;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.metrics.OneOpsMetrics.INDUCTOR;

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

public class StatCollector {

	private String statFileName;
	private static FileChannel statChannel;
	private static long delayInSecs = 60;

	private MetricRegistry metrics;
	private Meter rsyncFailed;
	private Meter woFailed;

	private static Logger logger = Logger.getLogger(StatCollector.class);
	private final ScheduledExecutorService statScheduler = Executors.newSingleThreadScheduledExecutor();

	public void init() {
		try {
			statChannel = FileChannel.open(Paths.get(statFileName), StandardOpenOption.WRITE);
			logger.info("initializing StatCollector file : " + statFileName);
		} catch (IOException e) {
			logger.error("Error while creating stat file " + statFileName, e);
		}
		rsyncFailed = metrics.meter(name(INDUCTOR, "rsyncFailure"));
		woFailed = metrics.meter(name(INDUCTOR, "woFailure"));

		statScheduler.scheduleWithFixedDelay(this::writeStat, delayInSecs, delayInSecs, TimeUnit.SECONDS);
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
			String statMsg = "Inductor Stat - | failed_count=" + woFailed.getOneMinuteRate() + ", rsync_count=" +  rsyncFailed.getOneMinuteRate();
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

	public static FileChannel getStatChannel() {
		return statChannel;
	}

	public static void setStatChannel(FileChannel statChannel) {
		StatCollector.statChannel = statChannel;
	}

	public static void setDelayInSecs(long delay) {
		StatCollector.delayInSecs = delay;
	}

}
