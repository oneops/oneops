package com.oneops.cms.ds;

import org.apache.log4j.Logger;

import java.util.function.Function;

public class CmsDataHelper<T, R> {

    Logger logger = Logger.getLogger(this.getClass());

    public R execute(Function<T, R> function, T param, boolean isReadOnlyData, String name) {
        try {
            if (isReadOnlyData) {
                logger.info("marking " + name + " read only ");
                DataTypeHolder.setReadOnlyData();
            }
            return function.apply(param);
        } catch(RuntimeException e) {
            if (isReadOnlyData) {
                DataTypeHolder.clear();
                logger.info("retrying the request " + name + " in primary");
                return function.apply(param);
            }
            throw e;
        } finally {
            DataTypeHolder.clear();
        }
    }

}
