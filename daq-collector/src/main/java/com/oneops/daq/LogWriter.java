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

import com.eaio.uuid.UUID;
import com.oneops.daq.dao.LogDao;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.prettyprint.hector.api.factory.HFactory.createMutator;


public class LogWriter extends LogDao {
    private static Logger logger = Logger.getLogger(LogWriter.class);
    private static Yaml yaml = new Yaml();

    /**
     * Process.
     *
     * @param eventTimeEpoc the event time epoc
     * @param key the key
     * @param logEntry the log entry
     * @param actionWorkorderId the action workorder id
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void process(long eventTimeEpoc, String key, String logEntry, String actionWorkorderId) throws IOException {

        UUID uuid = new UUID(eventTimeEpoc,Thread.currentThread().getId()+System.currentTimeMillis());
        logger.debug(uuid.toString() +" key:"+key+" "+eventTimeEpoc+" " + logEntry);

        //mutator.incrementCounter(key.getBytes(), LOG_DATA_CF, "counter", 1);

        Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);

        mutator.addInsertion(
                key.getBytes(),
                LOG_DATA_CF,
                createDataColumn(uuid, logEntry));

        if (!actionWorkorderId.isEmpty()) {
            mutator.addInsertion(
                    actionWorkorderId.getBytes(),
                    LOG_ACTION_WORKORDER_MAP_CF,
                    createDataColumn(uuid, key));
        }

        mutator.execute();
    }

    /**
     * Write ci log type.
     *
     * @param wre the wre
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public void writeCiLogType(WorkorderResponseEvent wre)
            throws IOException {

        long ciId = wre.getResponseWorkorder().getResultCi().getCiId();
        String appName = Util.normalizeClassName(wre.getResponseWorkorder());
        String ip = wre.getResponseWorkorder().getResultCi().getCiAttributes().get("private_ip");

//		byte[] key = new Long(ciId).toString().getBytes();
        byte[] key =  Long.valueOf(ciId).toString().getBytes();


        // initial auth: matching ciId with ip
        Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);
        mutator.addInsertion(
                key,
                LogDao.LOG_AUTH_CF,
                HFactory.createStringColumn(ip, "future: add key or token")
        );

        // perform the insert/updates
        mutator.execute();

        String config = wre.getLogConfig();
        if (config == null) {
            logger.debug("no "+appName+" log config for ci: "+ciId);
            return;
        }

        // convention based so uses a map -- key: 'monitor' is an array of monitor param maps that
        // could populate a template or will be inserted if match regex /#{nagios_config_group}_/

        Map<String,Object> data = (Map<String,Object>) yaml.load(config);

        if (data == null) {
            logger.debug("no "+appName+" log config for ci: "+ciId);
            return;
        }

        List< Map<String,Object> > logTypes = (ArrayList< Map<String,Object> >) data.get("log");

        mutator.addInsertion(
                key,
                CI_LOG_TYPE_CF,
                HFactory.createStringColumn("INDUCTOR", "log4j:/opt/oneops/log/inductor.log") );

        // write the buckets / archives
        for (int i=0; i<logTypes.size(); i++) {
            Map<String,Object> logConfig = logTypes.get(i);

            String logType = (String)logConfig.get("name");
            String logParser = (String)logConfig.get("parser");
            String logFile = (String)logConfig.get("log_file");
            logger.debug("log ci:"+ciId+" type:"+logType);

            mutator.addInsertion(
                    key,
                    CI_LOG_TYPE_CF,
                    HFactory.createStringColumn(logType+":"+logParser, logFile)
            );

        }

        // perform the insert/updates
        mutator.execute();

    }
}
