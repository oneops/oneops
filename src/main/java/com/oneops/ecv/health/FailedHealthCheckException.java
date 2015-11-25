package com.oneops.ecv.health;


public class FailedHealthCheckException extends RuntimeException {

    public IHealth health;

    public FailedHealthCheckException() {
    }

    public FailedHealthCheckException(String message) {
        super(message);
    }


    public FailedHealthCheckException(IHealth health) {
        super();
        this.health = health;

    }


    public FailedHealthCheckException(Throwable cause) {
        super(cause);
    }

    public FailedHealthCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedHealthCheckException(IHealth health, Throwable cause) {
        super(cause);
        this.health = health;
    }

    public IHealth getHealth() {
        return health;
    }

    public void setHealth(IHealth health) {
        this.health = health;
    }


}
