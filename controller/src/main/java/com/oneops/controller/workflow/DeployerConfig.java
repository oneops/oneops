package com.oneops.controller.workflow;

import com.oneops.cache.ConfigProvider;
import com.oneops.cms.util.CmsConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@ComponentScan(basePackages = {"com.oneops.cache"})
@Conditional(DeployerConfig.DeployerEnabledCondition.class)
public class DeployerConfig {

  @Bean
  public ConfigProvider getCacheConfigProvider() {
    return new ConfigProviderImpl();
  }

  @Bean
  public DeploymentCache getDeploymentCache() {
    return new HazelcastDpmtCache();
  }

  static class DeployerEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      return Boolean.valueOf(
          context.getEnvironment().getProperty(CmsConstants.DEPLOYER_ENABLED_PROPERTY, "false"));
    }
  }

}
