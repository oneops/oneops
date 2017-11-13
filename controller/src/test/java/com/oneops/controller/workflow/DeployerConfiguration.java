package com.oneops.controller.workflow;

import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.util.CmsUtil;
import com.oneops.controller.cms.CMSClient;
import com.oneops.controller.cms.CmsWoProvider;
import com.oneops.controller.util.ControllerUtil;
import com.oneops.workflow.WorkflowPublisher;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ThreadPoolExecutor;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class DeployerConfiguration {

  @Bean
  public DataSource getDataSource() throws Exception {
    EmbeddedPostgres server = getEmbeddedServer();
    createSchema(server);
    return setDataSource(server);
  }

  private EmbeddedPostgres getEmbeddedServer() throws Exception {
    return EmbeddedPostgres.builder().start();
  }

  private void createSchema(EmbeddedPostgres server) throws Exception {
    try (Connection connection = server.getPostgresDatabase().getConnection()) {
      try (Statement statement = connection.createStatement()) {
        execute(statement, String.format("CREATE ROLE %s WITH LOGIN SUPERUSER PASSWORD 'testpwd'", "kloopzcm"));
        execute(statement, String.format("CREATE DATABASE %s OWNER %s ENCODING = 'utf8'", "kloopzdb", "kloopzcm"));
      }
    }
  }

  private DataSource setDataSource(EmbeddedPostgres server) throws Exception {
    PGPoolingDataSource dataSource = new PGPoolingDataSource();
    dataSource.setUser("kloopzcm");
    dataSource.setPassword("testpwd");
    dataSource.setPortNumber(server.getPort());
    dataSource.setDatabaseName("kloopzdb");

    Flyway flyway = new Flyway();
    flyway.setPlaceholderReplacement(false);
    flyway.setLocations("classpath:deployer");
    flyway.setDataSource(dataSource);
    flyway.migrate();

    return dataSource;
  }

  private void execute(Statement statement, String sql) throws SQLException {
    statement.execute(sql);
  }

  @Bean
  public RetryTemplate getRetryTemplate() {
    return Mockito.mock(RetryTemplate.class);
  }

  @Bean
  public DataSourceTransactionManager getTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public SqlSessionFactoryBean getSessionFactoryBean(DataSource dataSource) {
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource(dataSource);
    sqlSessionFactoryBean.setTypeAliasesPackage("com.oneops.cms.dj.domain");
    return sqlSessionFactoryBean;
  }

  @Bean
  public MapperScannerConfigurer getMapperScannerConfigurer() {
    MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
    mapperScannerConfigurer.setBasePackage("com.oneops.cms.dj.dal");
    return mapperScannerConfigurer;
  }

  @Bean
  public CmsWoProvider getWoProvider(MapperScannerConfigurer mapperScannerConfigurer,
      DJMapper djMapper, DJDpmtMapper djDpmtMapper, CmsCmRfcMrgProcessor rfcProcessor) {
    CmsWoProvider woProvider = new CmsWoProvider();
    woProvider.setDjMapper(djMapper);
    woProvider.setDpmtMapper(djDpmtMapper);
    woProvider.setCmsUtil(new CmsUtil());
    woProvider.setCmrfcProcessor(rfcProcessor);
    return woProvider;
  }

  @Bean
  public CmsDpmtProcessor getDpmtProcessor(DJDpmtMapper djDpmtMapper) {
    CmsDpmtProcessor dpmtProcessor = new CmsDpmtProcessor();
    dpmtProcessor.setDpmtMapper(djDpmtMapper);
    dpmtProcessor.setCmProcessor(Mockito.mock(CmsCmProcessor.class));
    return dpmtProcessor;
  }

  @Bean
  public CMSClient getCmsClient(CmsWoProvider woProvider, CmsDpmtProcessor dpmtProcessor) {
    CMSClient cmsClient = new CMSClient();
    cmsClient.setCmsWoProvider(woProvider);
    cmsClient.setControllerUtil(new ControllerUtil());
    cmsClient.setCmsUtil(new CmsUtil());
    cmsClient.setCmsDpmtProcessor(dpmtProcessor);
    return cmsClient;
  }

  @Bean
  public CmsCmRfcMrgProcessor getCmsRfcProcessor() {
    return Mockito.mock(CmsCmRfcMrgProcessor.class);
  }

  @Bean
  public WoDispatcher getWoDispatcher() {
    return Mockito.mock(WoDispatcher.class);
  }

  @Bean
  public WorkflowPublisher getWorkflowPublisher() {
    return Mockito.mock(WorkflowPublisher.class);
  }

  @Bean
  public DeployerImpl getDeployer(
      @Qualifier("dispatchExecutor") ThreadPoolExecutor threadPoolExecutor) {
    return new DeployerImpl();
  }

  @Bean(name = "dispatchExecutor")
  public ThreadPoolExecutor getDispatchExecutor() {
    return Mockito.mock(ThreadPoolExecutor.class);
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer propertiesResolver() {
    return new PropertySourcesPlaceholderConfigurer();
  }

}
