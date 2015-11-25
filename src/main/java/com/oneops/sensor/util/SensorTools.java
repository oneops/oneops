package com.oneops.sensor.util;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.sensor.thresholds.Threshold;
import com.oneops.sensor.thresholds.ThresholdsDao;
import org.apache.log4j.Logger;
import rx.schedulers.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.oneops.sensor.Sensor.READ_ROWCOUNT;

public class SensorTools {
    private CmsCmProcessor cmProcessor;
    private ThresholdsDao tsDao;
    private OpsEventDao opsEventDao;
    private CiOpsProcessor coProcessor;

    private static Logger logger = Logger.getLogger(SensorTools.class);

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public void setTsDao(ThresholdsDao tsDao) {
        this.tsDao = tsDao;
    }

    public void setOpsEventDao(OpsEventDao opsEventDao) {
        this.opsEventDao = opsEventDao;
    }

    public void setCoProcessor(CiOpsProcessor coProcessor) {
        this.coProcessor = coProcessor;
    }

    /**
     * Validate all the threshold statements.
     */
    public void validateThresholds() {
        final CountDownLatch latch = new CountDownLatch(1);
        tsDao.getAllThreshold(READ_ROWCOUNT).subscribeOn(Schedulers.io())
                .subscribe(this::processThreshold,
                        (t) -> {
                            latch.countDown();
                            logger.error("Error while processing the thresholds", t);
                            throw new RuntimeException(t);
                        }, () -> {
                            latch.countDown();
                            logger.info("Completed the threshold validation.");
                        });
        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Error while validating the threshold", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Processs each threshold.
     *
     * @param tr {@link Threshold}
     */
    private void processThreshold(Threshold tr) {
        List<Long> bomCiIds = tsDao.getManifestCiIds(tr.getManifestId());
        List<CmsCI> bomCIs = cmProcessor.getCiByIdListNaked(bomCiIds);

        Set<Long> bomCiIdsSet = new HashSet<>();
        for (CmsCI bomCi : bomCIs) {
            bomCiIdsSet.add(bomCi.getCiId());
        }
        // Mow lets check maped cassnadra CiIds with CMS ciIds
        int remainingBomsCount = 0;
        for (Long mapedCiId : bomCiIds) {
            if (!bomCiIdsSet.contains(mapedCiId)) {
                logger.warn("Found orphan ciId = " + mapedCiId + " it is not in cms but in the map, will remove");
                opsEventDao.removeCi(mapedCiId);
                coProcessor.removeManifestMap(mapedCiId, tr.getManifestId());
                logger.info("Removing ciId = " + mapedCiId + " from the manifest map");
            } else {
                remainingBomsCount++;
            }
        }
        if (remainingBomsCount == 0) {
            // No boms left need to clean up the thresholds
            logger.error("No instances for this manifet id found in CMS, manifestId = " + tr.getManifestId() + ", the threshold " + tr.getSource() + " will be removed");
            tsDao.removeManifestThreshold(tr.getManifestId(), tr.getSource());
        }
    }
}
