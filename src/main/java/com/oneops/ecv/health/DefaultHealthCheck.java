package com.oneops.ecv.health;


public class DefaultHealthCheck implements IHealthCheck {
    @Override
    public IHealth getHealth() {
        return Health.OK_HEALTH;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
