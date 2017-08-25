package com.oneops.cms.ds;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${CMS_DB_USER}")
    private String dbUser;

    @Value("${CMS_DB_PASS}")
    private String dbPass;

    @Value("${CMS_DB_HOST}")
    private String primaryDbHost;

    @Value("adapter-${ONEOPS_COMPUTE_CI_ID:0}")
    private String applicationName;

    @Value("${IS_QUERY_STANDBY_ENABLED:false}")
    private boolean isQueryStandByEnabled;


    @Value("${oo.adapter.primary.initial:5}")
    private int primaryInitialSize;

    @Value("${oo.adapter.primary.max.active:10}")
    private int primaryMaxActiveSize;

    @Value("${oo.adapter.primary.max.idle:5}")
    private int primaryMaxIdleSize;

    @Value("${oo.adapter.standby.initial:3}")
    private int standbyInitialSize;

    @Value("${oo.adapter.standby.max.active:10}")
    private int standbyMaxActiveSize;

    @Value("${oo.adapter.standby.max.idle:3}")
    private int standbyMaxIdleSize;

    @Autowired
    Environment env;

    private static final String DRIVER = "org.postgresql.Driver";
    private static final String JDBC_URL = "jdbc:postgresql://%s/kloopzdb?autoReconnect=true&ApplicationName=%s";
    private static final Logger logger = Logger.getLogger(DataSourceConfig.class);

    private String jdbcUrl(String dbHost) {
        return String.format(JDBC_URL, dbHost, applicationName);
    }

    private void setNumConnections(BasicDataSource ds, int initialSize, int maxActive, int maxIdle) {
        ds.setInitialSize(initialSize);
        ds.setMaxActive(maxActive);
        ds.setMaxIdle(maxIdle);
    }

    private BasicDataSource getBaseDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUsername(dbUser);
        ds.setPassword(dbPass);
        ds.setDriverClassName(DRIVER);
        ds.setTestOnBorrow(true);
        ds.setValidationQuery("select 1");
        ds.setDefaultAutoCommit(false);
        return ds;
    }

    private DataSource getPrimaryDataSource() {
        BasicDataSource ds = getBaseDataSource();
        ds.setUrl(jdbcUrl(primaryDbHost));
        setNumConnections(ds, primaryInitialSize, primaryMaxActiveSize, primaryMaxIdleSize);
        return ds;
    }

    private DataSource getReadOnlyDataSource() {
        BasicDataSource ds = getBaseDataSource();
        ds.setUrl(jdbcUrl(env.getProperty("CMS_DB_READONLY_HOST")));
        setNumConnections(ds, standbyInitialSize, standbyMaxActiveSize, standbyMaxIdleSize);
        return ds;
    }

    private DataSource getRoutingDataSource(DataSource primaryDataSource, DataSource readOnlyDataSource) {
        RoutingDataSource ds = new RoutingDataSource();
        Map<Object, Object> dsMap = new HashMap<>();
        dsMap.put(DataType.DEFAULT, primaryDataSource);
        dsMap.put(DataType.READ_ONLY, readOnlyDataSource);
        ds.setTargetDataSources(dsMap);
        ds.setDefaultTargetDataSource(primaryDataSource);
        return ds;
    }

    @Bean(name = "oneopsCMSDS")
    public DataSource dataSource() {
        logger.info("isQueryStandByEnabled : " + isQueryStandByEnabled);
        DataSource primaryDataSource = getPrimaryDataSource();

        if (isQueryStandByEnabled) {
            DataSource readOnlyDataSource = getReadOnlyDataSource();
            return getRoutingDataSource(primaryDataSource, readOnlyDataSource);
        }
        else {
            return primaryDataSource;
        }
    }


}
