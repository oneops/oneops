package com.oneops.ecv.health;


import com.oneops.ecv.health.IHealth;

import java.util.Comparator;

public class HealthCheckUtils {


    public static final Comparator<IHealth> STATUS_CODE_COMPARATOR = new Comparator<IHealth>() {
        @Override
        public int compare(IHealth o1, IHealth o2) {
            return Integer.valueOf(o2.getStatusCode()).compareTo(Integer.valueOf(o1.getStatusCode()));
        }
    };

}
