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


import com.oneops.daq.dao.PerfDao;
import com.oneops.daq.jms.SensorPublisher;
import com.oneops.ops.PerfArchive;
import com.oneops.ops.PerfDatasource;
import com.oneops.ops.PerfHeader;
import com.oneops.ops.dao.PerfDataAccessor;
import com.oneops.sensor.events.PerfEvent;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class PerfWriter extends PerfDao {
    private static SensorPublisher sensorPub = null;

    private static Logger logger = Logger.getLogger(PerfWriter.class);

    /**
     * Process Aggregate.
     *
     * @param perfEvent the perf event
     * @return true, if successful
     * @throws IOException  Signals that an I/O exception has occurred.
     * @throws JMSException
     */
    public boolean processAggregate(PerfEvent perfEvent) throws IOException, JMSException {
        PerfDataAccessor perfDataAccessor = getPerfDataAccessor();
        perfDataAccessor.writeBucket(perfEvent);

        if (sensorPub != null) {
            //logger.info("pushlishing event: "+perfEvent.toString());
            perfEvent.setChannel(perfDataAccessor.getHostName());
            // remove stat part of bucket for sensor
            String bucket = perfEvent.getBucket().split("-")[0];
            perfEvent.setBucket(bucket);
            sensorPub.enrichAndPublish(perfEvent);
        } else {
            logger.debug("skipping sensorPub.enrichAndPublish(perfEvent) due to null sensorPub");
        }
        return true;
    }

    /**
     * Process.
     *
     * @param perfEvent the perf event
     * @param ip        the ip
     * @return true, if successful
     * @throws IOException  Signals that an I/O exception has occurred.
     * @throws JMSException
     */
    public boolean process(PerfEvent perfEvent, String ip) throws IOException, JMSException {

        String columnKey = perfEvent.getCiId() + ":" + perfEvent.getGrouping();
        PerfDataAccessor perfDataAccessor = getPerfDataAccessor();
        PerfHeader header = perfDataAccessor.getHeader(columnKey);

        logger.debug("column key: " + columnKey + " ip: " + ip + " header ip: " + header.getIp());
        if (header.getIp() == null || header.getIp().equalsIgnoreCase("")) {
            header.setIp(ip);
        } else if (!header.getIp().equalsIgnoreCase(ip)) {
            logger.error("header ip mismatch - header ip:" + header.getIp() + " event ip:" + ip + " ...updating");
            header.setIp(ip);
            //return false;
        }

        logger.debug("### EVENT header:" + header.toLogString());

        // map for values keyed by datasource and aggregate
        Map<String, Double> dsRraValueMap = new HashMap<String, Double>();

        if (header.getStep() == 0) {
            logger.info("header should have been created for ip: " + ip + " column key: " + columnKey);
            header.setStep(60);
        }

        long timestamp = perfEvent.getTimestamp();

        // do nothing if old value
        long updated = header.getUpdated();

        // fix issue where incoming timestamps had extra digit
        if (timestamp < updated - 1000000) {
            long newUpdated = timestamp - 60;
            logger.warn("resetting updated timestamp. was: " + updated + " now: " + newUpdated);
            updated = newUpdated;
        }
        // prevent issue where incoming timestamps had extra digit
        long now = System.currentTimeMillis() / 1000;
        if (timestamp > now + (60 * 60 * 24 * 7)) {
            logger.error("timestamp of: " + timestamp + " is more than a week in the future, dropping.");
            return true;
        }

        if (timestamp <= updated) {
            logger.warn("not processing : timestamp (" + timestamp + ") < updated (" + updated + ")");
            //return true;
        }

        // find the aligned timestamps for the pdp values
        long step = Long.valueOf(header.getStep());

        long delta = timestamp - updated;
        long startTime = PerfDataAccessor.bucketize(timestamp, step);
        long endTime = startTime + step;
        long lastStartTime = PerfDataAccessor.bucketize(updated, step);
        long lastEndTime = lastStartTime + step;
        long lastDelta = updated % step;
        long thisDelta = timestamp % step;


        logger.debug("startTime:" + startTime + " endTime:" + endTime
                + " step:" + step + " lastStartTime:" + lastStartTime + " lastEndTime:" + lastEndTime);

        Map<String, Double> metrics = perfEvent.getMetrics().getAvg();

        if (metrics == null) {
            logger.info("null metrics");
            return true;
        }

        Iterator<String> k = metrics.keySet().iterator();
        while (k.hasNext()) {
            String dsKey = (String) k.next();
            PerfDatasource ds = header.getDsMap().get(dsKey);

            // lenient new ds
            if (ds == null) {
                logger.info("header.getDsMap missing: " + dsKey);
                ds = new PerfDatasource();
                double nv = metrics.get(dsKey);
                ds.setType(PerfDatasource.GAUGE);
                ds.setLast(nv);
                ds.setInput(nv);
                ds.setPdp(nv);
                header.getDsMap().put(dsKey, ds);
            }
            logger.debug("### DS: " + dsKey + " " + ds.toLogString());
            if (delta > ds.getHeartbeat()) {
                delta = ds.getHeartbeat();
            }

            double v = metrics.get(dsKey);
            double last = ds.getLast();

            // rate based on type
            String type = ds.getType();

            // set if first sample
            if (updated == 0 ||
                    // outside the heartbeat
                    (updated < (timestamp - ds.getHeartbeat()))) {
                ds.setInput(v);
                ds.setLast(v);
            }

            // check if the sample is within the same pdp with the previous sample
            double diff = endTime - timestamp;

            logger.debug("updated:" + updated + " startTime:" + startTime + " diff(curr-upd):" + diff);

            if (lastStartTime == startTime) {
                // last / new value ratio
                double lr = new Double(lastDelta) / new Double(thisDelta);
                double nr = new Double(thisDelta - lastDelta) / new Double(thisDelta);
                logger.debug("lastStartTime == startTime ; thisDelta:" + thisDelta + " - lastDelta:" + lastDelta + " ) / thisDelta:" + thisDelta);

                double nv = 0;
                if (type.equalsIgnoreCase(PerfDatasource.COUNTER) ||
                        type.equalsIgnoreCase(PerfDatasource.DERIVE)) {

                    nv = Util.calcRate(ds.getType(), v, ds.getInput(), thisDelta);
                    logger.debug("using Util.calcRate (" + ds.getType() + ", " + v + ", " + ds.getInput() + ", " + thisDelta + ") - nv: " + nv);
                } else {
                    nv = v;
                }

                // calculate the pdp
                double pdp = new Double((nv * nr) + (last * lr));
                if (pdp == Double.NaN) {
                    pdp = 0;
                }
                logger.debug("cv:" + v + " pdp:" + pdp + " nv:" + nv + " nr:" + nr + " last:" + last + " lr:" + lr);
                consolidate(header, dsKey, endTime, pdp, dsRraValueMap);

                // update the header values
                ds.setInput(v);
                ds.setLast(nv);

            } else {

                // start with the first pdp after the last update
                long pdpTs = lastEndTime;

                // last value ratio and last value
                double lr = new Double(lastDelta) / new Double(step);
                logger.debug("updated < startTime - last update in some previous bucket:" + pdpTs + " lastDelta:" + lastDelta + " lr:" + lr + " step:" + step);

                // new value ratio and new value
                double nr = new Double((step - lastDelta)) / new Double(step);
                double nv;
                if (type.equalsIgnoreCase(PerfDatasource.COUNTER) ||
                        type.equalsIgnoreCase(PerfDatasource.DERIVE)) {

                    nv = Util.calcRate(ds.getType(), v, ds.getInput(), delta);
                    logger.debug("using Util.calcRate (" + ds.getType() + ", " + v + ", " + ds.getInput() + ", " + delta + ") returned: " + nv);
                } else {
                    nv = v;
                }

                if (Double.isNaN(last) || updated == 0) {
                    last = nv;
                    ds.setLast(nv);
                }


                // calculate the pdp
                double pdp = new Double((nv * nr) + (last * lr));

                logger.debug("pdp:" + pdp + " nv:" + nv + " nr:" + nr + " last:" + last + " lr:" + lr);

                while (pdpTs <= startTime) {
                    // max lets go back is 1 step
                    if ((startTime - step) > pdpTs) {
                        logger.debug("old pdpTs: " + pdpTs + " new pdpTs:" + (startTime - step));
                        pdpTs = startTime - step;
                    }
                    logger.debug(perfEvent.getCiId() + " " + dsKey + " filling in " + pdpTs + " to " + startTime);
                    // consolidate pdp and cdp (in header)
                    if (Double.isNaN(pdp)) {
                        pdp = v;
                    }
                    consolidate(header, dsKey, pdpTs, pdp, dsRraValueMap);
                    pdpTs += step;
                }

                // update the header values
                ds.setInput(v);
                ds.setLast(nv);

                logger.debug("header-post consolidate " + header.toLogString());
            }

            header.setUpdated(Long.valueOf(timestamp));
            logger.debug("at the end write info header");

        }
        HashMap<String, PerfEvent> perfEventMap = new HashMap<String, PerfEvent>();
        perfEventMap.put("1m", perfEvent);

        // writes to cass and populates perfEventMap for sensor
        perfDataAccessor.writeSampleToHeaderAndBuckets(columnKey, endTime, header, dsRraValueMap, perfEventMap);

        for (String key : perfEventMap.keySet()) {
            perfEvent = perfEventMap.get(key);

            if (perfEvent.getMetrics().getAvg().size() < 1) {
                logger.debug("skipping sensorPub because metrics are empty for: " + columnKey);
                continue;
            }

            if (sensorPub != null) {
                //logger.info("pushlishing event: "+perfEvent.toString());
                perfEvent.setChannel(perfDataAccessor.getHostName());
                sensorPub.enrichAndPublish(perfEvent);
            } else {
                logger.debug("skipping sensorPub.enrichAndPublish(perfEvent) due to null sensorPub");
            }
        }
        return true;

    }

    /*
*  consolidate ; update rra values for
*/
    private Map<String, Double> consolidate(PerfHeader header, String dsKey,
                                            long endTime, Double pdp, Map<String, Double> dsAggregateMap) {

        if (dsAggregateMap == null) {
            dsAggregateMap = new HashMap<String, Double>();
        }
        for (String rraKey : header.getRraMap().keySet()) {
            PerfArchive rra = header.getRraMap().get(rraKey);

            // delta to end of the archive bucket
            long archiveDelta = endTime % (rra.getSteps() * header.getStep());

            // set the delta to the full period if end of the cdp range
            long cdpStartTime, archiveRange;

            // calculate new cdp
            Double cdp = null;
            String cdpKey = dsKey + ":" + rraKey;
            Map<String, Double> cdpMap = header.getCdpMap();
            Double lastCdp = cdpMap.get(cdpKey);

            if (archiveDelta == 0) {
                archiveRange = rra.getSteps() * header.getStep();
                cdpStartTime = endTime - archiveRange;
            } else {
                archiveRange = archiveDelta;
                cdpStartTime = endTime - archiveDelta;
            }

            if (lastCdp == null) {
                lastCdp = Double.NaN;
            }
            String statFunction = rra.getConsolidationFunction();

            if (rraKey.contains(LOGBUCKET)) {
                logger.debug("consolidate: " + rraKey + " stat:" + statFunction + " dsKey=" + dsKey + " pdp=" + pdp + " bucket:"
                        + endTime + " archiveDelta:" + archiveDelta + " cdpKey:" + cdpKey + " lastCdp:" + lastCdp
                        + " updated:" + header.getUpdated() + " cdpStartTime:" + cdpStartTime + " archiveRange:" + archiveRange);
            }

            if (pdp.isNaN()) {
                // if empty pdp value keep the last cdp value
                cdp = lastCdp;

            } else if ((header.getUpdated() < cdpStartTime) || lastCdp.isNaN()) {
                // old or empty cdp data does not need consolidation
                // count starts at 1
                if (statFunction.equalsIgnoreCase(COUNT)) {
                    cdp = 1.0;
                } else {
                    cdp = pdp;
                }

            } else {
                // check the consolidation function and calculate accordingly
                if (statFunction.equalsIgnoreCase(MAX)) {
                    if (pdp > lastCdp) {
                        cdp = pdp;
                    } else {
                        cdp = lastCdp;
                    }
                } else if (statFunction.equalsIgnoreCase(MIN)) {
                    if (pdp < lastCdp) {
                        cdp = pdp;
                    } else {
                        cdp = lastCdp;
                    }
                } else if (statFunction.equalsIgnoreCase(COUNT)) {
                    cdp = lastCdp + 1.0;

                } else if (statFunction.equalsIgnoreCase(SUM)) {
                    cdp = lastCdp + pdp;

                } else if (statFunction.equalsIgnoreCase(AVERAGE)) {
                    long lastRange = header.getUpdated() - cdpStartTime;
                    if (lastRange < archiveRange) {
                        double lastValueRate = Double.valueOf(lastRange) / Double.valueOf(archiveRange);
                        double pdpRate = Double.valueOf((archiveRange - lastRange)) / Double.valueOf(archiveRange);

                        cdp = (lastCdp * lastValueRate) + (pdp * pdpRate);
                        if (rraKey.contains(LOGBUCKET)) {
                            logger.debug("lastCdp: " + lastCdp + " lastValueRate:" + lastValueRate + " pdp:" + pdp + " pdpRate:" + pdpRate);
                        }

                    } else {
                        cdp = pdp;
                    }

                    if (rraKey.contains(LOGBUCKET)) {
                        logger.debug(rraKey + " cdp:" + cdp + " lv:" + lastCdp + " lastRange:" + lastRange + " pdp:" + pdp);
                    }
                }

            }

            if (cdp.isNaN()) {
                cdp = 0.0;
            }

            if (rraKey.contains(LOGBUCKET)) {
                logger.debug(rraKey + " cdp:" + cdp + " archiveDelta:" + archiveDelta);
            }
            cdpMap.put(cdpKey, cdp);

            if (archiveDelta == 0) {
                // need to update rra data / end of consolidation period and reset cdp
                dsAggregateMap.put(dsKey + ":" + rraKey + "::" + endTime, cdp);

                //start new bucket
                if (statFunction.equalsIgnoreCase(COUNT)) {
                    cdp = 1.0;
                } else if (rraKey.contains("average-1m")) {
                    cdp = pdp;
                }
                cdpMap.put(cdpKey, cdp);
            }
        }

        PerfDatasource ds = header.getDsMap().get(dsKey);
        ds.setPdp(pdp);
        Util.logMapDouble("dsAggregateMap", dsAggregateMap, logger);

        return dsAggregateMap;
    }

    /**
     * Sets the sensor publisher.
     *
     * @param sp the new sensor publisher
     */
    public void setSensorPublisher(SensorPublisher sp) {
        sensorPub = sp;
    }
}

