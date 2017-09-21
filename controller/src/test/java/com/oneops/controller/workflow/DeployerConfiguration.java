package com.oneops.controller.workflow;

import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.util.CmsUtil;
import com.oneops.controller.cms.CMSClient;
import com.oneops.controller.cms.CmsWoProvider;
import com.oneops.controller.util.ControllerUtil;
import com.oneops.workflow.WorkflowPublisher;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.retry.support.RetryTemplate;

import javax.sql.DataSource;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class DeployerConfiguration {

    @Bean
    public DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("deployer-test-data1.sql")
                .build();
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
    public CmsWoProvider getWoProvider(MapperScannerConfigurer mapperScannerConfigurer, DJMapper djMapper, DJDpmtMapper djDpmtMapper) {
        CmsWoProvider woProvider = new CmsWoProvider();
        woProvider.setDjMapper(djMapper);
        woProvider.setDpmtMapper(djDpmtMapper);
        woProvider.setCmsUtil(new CmsUtil());
        return woProvider;
    }

    @Bean
    public CmsDpmtProcessor getDpmtProcessor(DJDpmtMapper djDpmtMapper) {
        CmsDpmtProcessor dpmtProcessor = new CmsDpmtProcessor();
        dpmtProcessor.setDpmtMapper(djDpmtMapper);
        return dpmtProcessor;
    }

    @Bean
    public CMSClient getCmsClient(CmsWoProvider woProvider) {
        CMSClient cmsClient = new CMSClient();
        cmsClient.setCmsWoProvider(woProvider);
        cmsClient.setControllerUtil(new ControllerUtil());
        return cmsClient;
    }

    @Bean
    public DeploymentCache getDeploymentCache() {
        return new LocalDeploymentCache();
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
    public Deployer getDeployer(@Qualifier("dispatchExecutor") ThreadPoolExecutor threadPoolExecutor) {
        return new Deployer();
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
