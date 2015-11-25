package com.oneops.ecv.health;


public class HeathCheckerNotFoundException extends RuntimeException {

    public HeathCheckerNotFoundException() {
    }


    public HeathCheckerNotFoundException(String message) {
        super(message);
    }


    public HeathCheckerNotFoundException(Throwable cause) {
        super(cause);
    }


    public HeathCheckerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
