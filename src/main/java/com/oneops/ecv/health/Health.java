package com.oneops.ecv.health;


import javax.servlet.http.HttpServletResponse;

/**
 * Created by glall on 10/30/14.
 */
public class Health implements IHealth {


    public static final Health OK_HEALTH = new Health();
    public static final Health FAILED_HEALTH = new Health(HttpServletResponse.SC_SERVICE_UNAVAILABLE, Boolean.FALSE);
    public static final Health OFFLINE_HEALTH = new Health(HttpServletResponse.SC_SERVICE_UNAVAILABLE, Boolean.FALSE, "MarkedOffline", "Offline");
    private int statusCode = HttpServletResponse.SC_OK;
    private boolean isOKstatus = Boolean.TRUE;
    private String message;
    private String name;

    public Health() {
        this.name = this.getClass().getName();
        this.message = "Default health : oK ";
    }

    public Health(String message, String name) {
        this.message = message;
        this.name = name;
    }

    public Health(String message) {
        this.message = message;
        this.name = this.getClass().getName();
    }

    public Health(int statusCode, boolean isOKstatus, String message, String name) {
        this.statusCode = statusCode;
        this.isOKstatus = isOKstatus;
        this.message = message;
        this.name = name;
    }

    public Health(int statusCode, boolean isOKstatus) {
        this.statusCode = statusCode;
        this.isOKstatus = isOKstatus;
    }


    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public boolean isOK() {
        return isOKstatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Health{");
        sb.append("statusCode=").append(statusCode);
        sb.append(", isOKstatus=").append(isOKstatus);
        sb.append(", message='").append(message).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
