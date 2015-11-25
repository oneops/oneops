package com.oneops.ecv.health;


public interface IHealthCheck {


    /**
     * Any exception should return boolean status of false
     *
     * @return true
     */
    public IHealth getHealth();

    /**
     * Returns a unique name for the health check(may be CacheCheck)
     *
     * @return unique name.
     */
    public String getName();


}
