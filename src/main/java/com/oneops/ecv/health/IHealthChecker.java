package com.oneops.ecv.health;

import java.util.List;

public interface IHealthChecker {

    List<IHealthCheck> getHealthChecksToRun();

    void setHealthChecksToRun(List<IHealthCheck> healthChecks);
}
