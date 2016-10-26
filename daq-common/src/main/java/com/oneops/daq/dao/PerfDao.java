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

package com.oneops.daq.dao;

import com.oneops.daq.Util;
import com.oneops.daq.domain.Chart;
import com.oneops.daq.domain.Series;
import com.oneops.ops.dao.CassandraConstants;
import com.oneops.ops.dao.PerfDataAccessor;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.mutation.Mutator;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PerfDao - encapsulates cassandra data access for performance data.
 */
@Component
public class PerfDao implements CassandraConstants {

    protected static final StringSerializer stringSerializer = StringSerializer.get();
    protected static final BytesArraySerializer bytesSerializer = BytesArraySerializer.get();
    protected static final LongSerializer longSerializer = LongSerializer.get();
    protected static final DoubleSerializer doubleSerializer = DoubleSerializer.get();

    protected static final String DATA_CF = "data";
    protected static final String HEADER_SCF = "header";
    protected static final String CI_METRIC_CF = "ci_metric";
    protected static final String CHART_SCF = "chart";

    private static Logger logger = Logger.getLogger(PerfDao.class);
    private static FileChannel statChannel;

    private String stateFilename;
    private PerfDataAccessor perfDataAccessor;

    public final AtomicLong eventCounter = new AtomicLong();
    public final AtomicLong hectorExceptionCount = new AtomicLong();
    public final AtomicLong jmsExceptionCount = new AtomicLong();
    public final AtomicLong batchDuration = new AtomicLong();

    // tmp counters to show conversion
    public final AtomicLong oldCount = new AtomicLong();
    public final AtomicLong newCount = new AtomicLong();


    /**
     * Sets the state filename and open a file channel for writing.
     *
     * @param filename file name of state
     */
    public void setStateFilename(String filename) {
        stateFilename = filename;
        File sFile = new File(stateFilename);
        try {
            if (!sFile.exists()) {
                sFile.createNewFile();
            }
            logger.info("Creating the file channel for " + stateFilename);
            statChannel = FileChannel.open(Paths.get(stateFilename), StandardOpenOption.WRITE);
        } catch (Exception ex) {
            logger.error("Error setting stat file." + sFile.getAbsolutePath(), ex);
            System.exit(1);
        }
    }

    /**
     * Append stats string to the perfsink.state file.
     *
     * @param stat stat message
     * @throws IOException throws if there is any error writing to the channel.
     */
    public void appendStat(String stat) throws IOException {
        statChannel.write(ByteBuffer.wrap(stat.getBytes()));
        statChannel.force(false);
    }

    /**
     * Write stats string to the perfsink.state file. This will truncate the
     * existing content.
     *
     * @param stat stat message
     * @throws IOException throws if there is any error writing to the channel.
     */
    public void writeStat(String stat) throws IOException {
        statChannel.truncate(0);
        appendStat(stat);
    }

    /**
     * Closes the stat file channel.
     */
    public void closeStatFile() {
        if (statChannel != null && statChannel.isOpen()) {
            try {
                statChannel.close();
            } catch (IOException ex) {
                logger.error("Error closing the stat channel for " + stateFilename, ex);
            }
        }
    }

    /**
     * Gets the chart.
     *
     * @param key the key
     * @return the chart
     */
    public Chart getChart(String key) {
        Chart chart = new Chart();
        chart.setKey(key);

        List<HSuperColumn<String, String, String>> superColumns = perfDataAccessor.getChart(key);
        // create maps for ds,rra,cdp (aggregator will use the zoneMap)
        Map<String, Series> seriesMap = new HashMap<>();
        chart.setSeriesMap(seriesMap);

        for (int i = 0; i < superColumns.size(); i++) {
            List<HColumn<String, String>> columns = superColumns.get(i).getColumns();

            // SC to Map<String,String>
            String scName = superColumns.get(i).getName();

            Map<String, String> sc = new HashMap<>();
            for (int j = 0; j < columns.size(); j++) {
                sc.put(columns.get(j).getName(), columns.get(j).getValue());
            }

            // chart attributes
            if (scName.equalsIgnoreCase(CHART)) {
                chart.setCreated(sc.get("created"));
                chart.setCreator(sc.get("creator"));
                chart.setName(sc.get(NAME));
                chart.setType(sc.get(TYPE));
                chart.setTitle(sc.get("title"));
                chart.setDescription(sc.get("description"));
                chart.setStart(sc.get("start"));
                chart.setEnd(sc.get("end"));
                chart.setYmax(sc.get("ymax"));
                chart.setYmin(sc.get("ymin"));
                chart.setTheme(sc.get("theme"));
                chart.setStep(sc.get(STEP));
                chart.setUpdated(sc.get(UPDATED));

                // series attributes
            } else if (scName.indexOf(SERIES) == 0) {
                Series series = new Series();
                String seriesName = sc.get(NAME);
                series.setName(seriesName);
                series.setType(sc.get(TYPE));
                series.setDatasource(sc.get("datasource"));
                series.setxAxisId(sc.get("xAxisId"));
                series.setyAxisId(sc.get("yAxisId"));
                series.setStackGroup(sc.get("stackGroup"));
                series.setColor(sc.get("color"));
                series.setWeight(sc.get("weight"));
                series.setRenderer(sc.get("renderer"));
                series.setOffset(Integer.parseInt(sc.get("yAxisId")));

                seriesMap.put(seriesName, series);


            } else {
                Util.logMapString("couldnt map: " + scName, sc, logger);

            }
        }

        return chart;
    }

    /**
     * Sets the chart.
     *
     * @param chart the new chart
     */
    public void setChart(Chart chart) {
        Map<String, String> columnsMap = new HashMap<>();

        columnsMap.put(NAME, chart.getName());
        columnsMap.put(TYPE, chart.getType());
        columnsMap.put(STEP, chart.getStep());
        columnsMap.put("start", chart.getStart());
        columnsMap.put("end", chart.getEnd());
        columnsMap.put("title", chart.getTitle());
        columnsMap.put("creator", chart.getCreator());
        columnsMap.put("created", chart.getCreated());
        columnsMap.put("updated", chart.getUpdated());
        columnsMap.put("ymax", chart.getYmax());
        columnsMap.put("ymin", chart.getYmin());
        columnsMap.put("height", chart.getHeight());
        columnsMap.put("width", chart.getWidth());
        columnsMap.put("theme", chart.getTheme());
        columnsMap.put("description", chart.getDescription());

        Mutator<String> mutator = perfDataAccessor.newMutator();
        //TODO: mutator could be contained inside PerfDataCollector.

        perfDataAccessor.insert(mutator, chart.getKey(), columnsMap, CHART, CHART);

        logger.debug("write CHART: ");

        StringBuilder pendingKeys = new StringBuilder("chart");

        // update datasource headers
        Map<String, Series> seriesMap = chart.getSeries();
        for (String seriesKey : seriesMap.keySet()) {
            Series series = seriesMap.get(seriesKey);
            columnsMap = new HashMap<>();
            columnsMap.put(NAME, series.getName());
            columnsMap.put(TYPE, series.getType());
            columnsMap.put("datasource", series.getDatasource());
            columnsMap.put("xAxisId", series.getxAxisId());
            columnsMap.put("yAxisId", series.getyAxisId());
            columnsMap.put("stackGroup", series.getStackGroup());
            columnsMap.put("color", series.getColor());
            columnsMap.put("weight", series.getWeight());
            columnsMap.put("renderer", series.getRenderer());
            columnsMap.put("offset", Integer.valueOf(series.getOffset()).toString());
            perfDataAccessor.insert(mutator, chart.getKey(), columnsMap, CHART, SERIES + "_" + series.getName());
            pendingKeys.append(", " + seriesKey);
        }

        logger.debug("write keys:" + pendingKeys);
        perfDataAccessor.execute(mutator);

    }


    public PerfDataAccessor getPerfDataAccessor() {
        return perfDataAccessor;
    }


    public void setPerfDataAccessor(PerfDataAccessor perfDataAccessor) {
        this.perfDataAccessor = perfDataAccessor;
    }

}
