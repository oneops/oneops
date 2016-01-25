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

package com.oneops.daq;

import com.oneops.sensor.events.PerfEvent;
import me.prettyprint.hector.api.exceptions.HectorException;
import org.apache.log4j.Logger;
import org.springframework.jms.UncategorizedJmsException;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.concurrent.Callable;

public class AsyncPerfWorker implements Callable<Void> {

    private PerfWriter perfDao;
    private PerfEvent perfEvent;
    private Logger logger;
    private String ip;
    private final static int PRINT_STATUS_INCREMENT = 100;


    public AsyncPerfWorker(PerfWriter perfDao, PerfEvent event, String ip, Logger logger) {
        this.perfDao = perfDao;
        this.perfEvent = event;
        this.ip = ip;
        this.logger = logger;
    }

    @Override
    public Void call() {
        try {
            long startTime = System.currentTimeMillis();
            if (perfEvent.isAggregate()) {
                perfDao.processAggregate(perfEvent);
                perfDao.newCount.incrementAndGet();
            } else {
                perfDao.process(perfEvent, ip);
                perfDao.oldCount.incrementAndGet();
            }
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("process " + perfEvent.getCiId() + " " + perfEvent.getGrouping() + " took " + duration + " ms");

            long eventCount = perfDao.eventCounter.incrementAndGet();
            perfDao.batchDuration.getAndAdd(duration);
            if (eventCount % PRINT_STATUS_INCREMENT == 0) {
                writeStats(eventCount);
            }

        } catch (HectorException he) {
            logger.error("invalid message: " + he.toString(), he);
            long heCount = perfDao.hectorExceptionCount.incrementAndGet();
            logger.warn("total hector exceptions: " + heCount);
        } catch (JMSException je) {
            logger.error("invalid message: " + je.toString(), je);            
            long jeCount = perfDao.jmsExceptionCount.incrementAndGet();
            logger.warn("total jms exceptions: " + jeCount);
        } catch (UncategorizedJmsException uje) {
            logger.error("invalid message: " + uje.toString(), uje);
            long jeCount = perfDao.jmsExceptionCount.incrementAndGet();
            logger.warn("total (uncat) jms exceptions: " + jeCount);
        } catch (Exception e) {
            // log and ignore
            logger.error("invalid message: " + e.toString(), e);
            return null;
        }
        return null;
    }


    private void writeStats(long eventCount) {
        long avgDuration = perfDao.batchDuration.get() / PRINT_STATUS_INCREMENT;
        String stats = "processed event count: " + eventCount +
                " old: " + perfDao.oldCount.get() +
                " new: " + perfDao.newCount.get() +
                " avg ms: " + avgDuration +
                " hector ex: " + perfDao.hectorExceptionCount.get() +
                " jms ex: " + perfDao.jmsExceptionCount.get();
        logger.info(stats);
        perfDao.batchDuration.set(0);
        try {
            perfDao.writeStat(stats);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not write stats.");
        }
    }
}
