package com.oneops.sensor.util;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.sensor.thresholds.Threshold;
import com.oneops.sensor.thresholds.ThresholdsDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import static com.oneops.sensor.StmtBuilder.THRESHOLDS_JSON_SIZE_FLOOR;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
@Service
public class MonitorRestorer {
    private static final String HEARTBEAT = "heartbeat";
    private static final String DURATION = "duration";
    private static Logger logger = Logger.getLogger(MonitorRestorer.class);

    @Autowired
    private ThresholdsDao tsDao;
    @Autowired
    private CmsCmManager cmManager;


    public void restore(boolean writeMode, Long manifestIdDefault) {
        List<CmsCIRelation> watchedBys;
        if (manifestIdDefault==null) {
            watchedBys = cmManager.getCIRelationsNsLike("/", "manifest.WatchedBy", null, null, null);
        } else {
            watchedBys = cmManager.getFromCIRelations(manifestIdDefault, "manifest.WatchedBy", null);
        }
        Map<Long, List<CmsCIRelation>> collect = watchedBys.stream().collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));
        for (Long manifestId : collect.keySet()) {
            List<CmsCIRelation> realizedAsRels = cmManager.getFromCIRelations(manifestId, "base.RealizedAs", null);
            if (realizedAsRels.isEmpty()) continue;
            for (CmsCIRelation relation : realizedAsRels) {
                restoreMapping(manifestId, relation.getToCiId(), writeMode);
                for (CmsCIRelation rel : collect.get(manifestId)) {
                    restoreMonitor(manifestId, relation.getToCiId(), rel.getToCi(), writeMode);
                }
            }
        }
    }


    private void restoreMonitor(Long manifestId, Long ciId, CmsCI monitor, boolean writeMode) {
        boolean isHeartBeat = monitor.getAttributes().get(HEARTBEAT).getDfValue().equals("true");
        String hbDuration = monitor.getAttributes().get(DURATION).getDfValue();
        String source = monitor.getCiName();


        long checksum = getChecksum(monitor);


        String thresholdsJson = monitor.getAttributes().get("thresholds").getDfValue();

        Threshold threshold = tsDao.getThreshold(manifestId, source);
        if (threshold != null) {
            logger.info("RestoreMonitor###: threshold for manifestId:" + manifestId + " and source:" + source + " not found. Will add");

            if (thresholdsJson == null || thresholdsJson.length() <= THRESHOLDS_JSON_SIZE_FLOOR) {
                thresholdsJson = "n";
            }
            if (writeMode) {
                tsDao.addCiThresholds(ciId, manifestId, source, checksum, thresholdsJson, isHeartBeat, hbDuration);
            }
        }
    }

    private void restoreMapping(Long manifestId, Long ciId, boolean writeMode) {
        if (tsDao.getManifestId(ciId) == null) {
            //add manifest mapping regardless of the monitors
            logger.info("RestoreMonitor###: manifest mapping is missing for ciId:" + ciId);
            if (writeMode) {
                tsDao.addManifestMap(ciId, manifestId);
            }
        }
    }

    private long getChecksum(CmsCI monitor) {
        long checksum = 0;
        String thresholds = monitor.getAttributes().get("thresholds").getDfValue();
        if (thresholds != null) {
            CRC32 crc = new CRC32();
            String crcStr = thresholds + monitor.getAttributes().get(HEARTBEAT).getDfValue() + monitor.getAttributes().get(DURATION).getDfValue();
            crc.update(crcStr.getBytes());
            checksum = crc.getValue();
        }
        return checksum;
    }
}
