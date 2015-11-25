package com.oneops.ecv.health;

import java.util.List;

/**
 * Created by glall on 10/29/14.
 */
public class HealthCheckerImpl implements IHealthChecker {

    List<IHealthCheck> healthChecks;

    @Override
    public List<IHealthCheck> getHealthChecksToRun() {
        return healthChecks;
    }

    @Override
    public void setHealthChecksToRun(List<IHealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
    }

}

