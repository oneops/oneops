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

    @Autowired
    Environment env;

    @Value("${CMS_DB_USER}")
    private String dbUser;

    @Value("${CMS_DB_PASS}")
    private String dbPass;

    @Value("${CMS_DB_HOST}")
    private String dbHost;

    @Value("#{servletContext.servletContextName}-${ONEOPS_COMPUTE_CI_ID:0}")
    private String applicationName;

    private static final String DRIVER = "org.postgresql.Driver";
    private static final String JDBC_URL = "jdbc:postgresql://%s/kloopzdb?autoReconnect=true&ApplicationName=%s";
    private static final String IS_QUERY_STANDBY_ENABLED_PROPERTY = "IS_QUERY_STANDBY_ENABLED";
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
        ds.setUrl(jdbcUrl(dbHost));
        setNumConnections(ds, 5, 10, 5);
        return ds;
    }

    private DataSource getReadOnlyDataSource() {
        BasicDataSource ds = getBaseDataSource();
        ds.setUrl(jdbcUrl(env.getProperty("CMS_DB_READONLY_HOST")));
        setNumConnections(ds, 2, 10, 2);
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
        boolean isStandByEnabled = Boolean.valueOf(env.getProperty(IS_QUERY_STANDBY_ENABLED_PROPERTY, "false"));
        logger.info("isStandByEnabled : " + isStandByEnabled);
        DataSource primaryDataSource = getPrimaryDataSource();

        if (isStandByEnabled) {
            DataSource readOnlyDataSource = getReadOnlyDataSource();
            return getRoutingDataSource(primaryDataSource, readOnlyDataSource);
        }
        else {
            return primaryDataSource;
        }
    }

}
