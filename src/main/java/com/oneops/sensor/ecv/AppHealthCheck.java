package com.oneops.sensor.ecv;

import com.oneops.ecv.health.Health;
import com.oneops.ecv.health.IHealth;
import com.oneops.ecv.health.IHealthCheck;
import com.oneops.ops.CiOpsProcessor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


public class AppHealthCheck implements IHealthCheck {
    private static Logger logger = Logger.getLogger(AppHealthCheck.class);
    private static final long DEFAULT_CID = Long.valueOf(System.getProperty("sensor.defaultCiD", "1"));
    @Autowired
    private CiOpsProcessor coProcessor;

    @Override
    public IHealth getHealth() {
        IHealth health = Health.FAILED_HEALTH;
        try {
            @SuppressWarnings("unused")
			String ciState = coProcessor.getCIstate(DEFAULT_CID);
                health = Health.OK_HEALTH;
        } catch (Throwable e) {
            logger.error("Exception occurred determining health", e);
            health = new Health(HttpStatus.INTERNAL_SERVER_ERROR.value(), Boolean.FALSE, e.getMessage(), getName());
        }
        return health;
    }

    @Override
    public String getName() {
        return "sensor.Health";
    }
}
