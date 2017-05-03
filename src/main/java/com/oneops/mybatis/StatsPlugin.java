package com.oneops.mybatis;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}), @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class StatsPlugin implements Interceptor {
    private Log logger = LogFactory.getLog(StatsPlugin.class);
    private static Map<String, Stats> map = new HashMap<>();


    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = invocation.proceed();
        try {
            String id = ((MappedStatement) invocation.getArgs()[0]).getId();
            map.computeIfAbsent(id, t -> new Stats()).addTime(System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.warn(e, e);
        }
        return proceed;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    public static Map<String, Stats> getStatsMap() {
        return map;
    }

    public void setProperties(Properties properties) {
    }
}
