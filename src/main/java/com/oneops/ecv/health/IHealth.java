package com.oneops.ecv.health;


public interface IHealth {

    int getStatusCode();

    boolean isOK();

    String getMessage();

    /**
     * Returns a unique name for the health check(may be CacheCheck)
     *
     * @return unique name.
     */
    public String getName();


}
