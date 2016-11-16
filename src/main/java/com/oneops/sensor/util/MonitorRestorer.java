package com.oneops.sensor.util;

import static com.oneops.sensor.StmtBuilder.THRESHOLDS_JSON_SIZE_FLOOR;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.sensor.thresholds.Threshold;
import com.oneops.sensor.thresholds.ThresholdsDao;

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
    private static final String THRESHOLDS = "thresholds";
    private static final String IS_ENABLED = "enable";
    
    
    private static Logger logger = Logger.getLogger(MonitorRestorer.class);

    private ThresholdsDao tsDao;
    private CmsCmManager cmManager;
    private CmsCmProcessor cmProcessor;

    public void setTsDao(ThresholdsDao tsDao) {
		this.tsDao = tsDao;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}


	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

    public void restore(boolean writeMode, Long manifestIdDefault) {
        List<CmsCIRelation> watchedBys;
        long counter = 0;
        long restoredMonitors = 0;
        if (manifestIdDefault==null) {
            watchedBys = cmProcessor.getCIRelationsNsLikeNakedNoAttrs("/", "manifest.WatchedBy", null, null, null);
        } else {
            watchedBys = cmManager.getFromCIRelations(manifestIdDefault, "manifest.WatchedBy", null);
        }
        long startTime = System.currentTimeMillis();
        Map<Long, List<CmsCIRelation>> monitors = watchedBys.stream().collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));
        for (Long manifestId : monitors.keySet()) {
        	counter++;
            List<CmsCIRelation> realizedAsRels = cmManager.getFromCIRelations(manifestId, "base.RealizedAs", null);
            if (realizedAsRels.isEmpty()) continue;
            Set<Long> cmsBomCiIds = realizedAsRels.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet());
            restoreRealizedAsMapping(manifestId, cmsBomCiIds, writeMode);
            boolean isFirstBom = true;
            for (CmsCIRelation relation : realizedAsRels) {
            	restoreManifestMapping(manifestId, relation.getToCiId(), writeMode);
            	if (isFirstBom) {
	                for (CmsCIRelation rel : monitors.get(manifestId)) {
	                	if (rel.getToCi() == null) {
	                		rel.setToCi(cmProcessor.getCiById(rel.getToCiId()));
	                	}		
	                    if (restoreMonitor(manifestId, relation.getToCiId(), rel.getToCi(), writeMode)) {
	                    	restoredMonitors++;
	                    }
	                }
            	}
            	isFirstBom = false;
            }
            if (counter%100 == 0) {
            	logger.info("Time to process " + counter + " - " + (System.currentTimeMillis() - startTime) + " ms!");
            }
        }
        logger.info(">>>>>>>>>>> Monitor restored " + restoredMonitors + ";");
    	logger.info(">>>>>>>>>>> Monitor restoration is done!!!" + counter + " - " + (System.currentTimeMillis() - startTime) + " ms!");

    }


    private boolean restoreMonitor(Long manifestId, Long ciId, CmsCI monitor, boolean writeMode) {
    	
    	if (!"true".equals(monitor.getAttributes().get(IS_ENABLED).getDjValue())) {
    		return false;
    	}
    	
        boolean isHeartBeat = "true".equals(monitor.getAttributes().get(HEARTBEAT).getDjValue());
        String hbDuration = monitor.getAttributes().get(DURATION).getDjValue();
        String source = monitor.getCiName();


        long checksum = getChecksum(monitor);


        String thresholdsJson = monitor.getAttributes().get(THRESHOLDS).getDjValue();

        Threshold threshold = tsDao.getThreshold(manifestId, source);
        if (threshold == null) {
            logger.info("RestoreMonitor###: threshold for manifestId:" + manifestId + " and source:" + source + " not found. Will add");

            if (thresholdsJson == null || thresholdsJson.length() <= THRESHOLDS_JSON_SIZE_FLOOR) {
                thresholdsJson = "n";
            }
            if (writeMode) {
                tsDao.addCiThresholds(ciId, manifestId, source, checksum, thresholdsJson, isHeartBeat, hbDuration);
            }
            return true;
        }
        return false;
    }

    private void restoreManifestMapping(Long manifestId, Long ciId, boolean writeMode) {
        if (tsDao.getManifestId(ciId) == null) {
            //add manifest mapping regardless of the monitors
            logger.info("RestoreMonitor###: bom -> manifest mapping is missing for ciId: " + ciId + " for manifestId: " + manifestId);
            if (writeMode) {
                tsDao.addManifestMap(ciId, manifestId);
            }
        }
    }

    private void restoreRealizedAsMapping(Long manifestId, Set<Long> cmsBomCiIds, boolean writeMode) {
    	Set<Long> opsdbBomCiId = tsDao.getManifestCiIds(manifestId).stream().collect(Collectors.toSet());
    	for (Long bomCiId : cmsBomCiIds) {
            //add manifest mapping regardless of the monitors
            if (!opsdbBomCiId.contains(bomCiId)) {
	    		logger.info("RestoreMonitor###: manifest -> bom mapping is missing for manifestId: " + manifestId + " for ciId: " + bomCiId);
	            if (writeMode) {
	                tsDao.addManifestMap(bomCiId, manifestId);
	            }
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
