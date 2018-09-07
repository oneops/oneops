package com.oneops.crawler.plugins.quota;

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.OneOpsFacade;
import com.oneops.crawler.SearchDal;
import com.oneops.crawler.ThanosClient;
import com.oneops.crawler.plugins.Config;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.oneops.crawler.ThanosClient.CloudResourcesUtilizationStats;

public class ScaleDownTest {

    OneOpsPlatformScaleDownPlugin plugin = null;
    OneOpsFacade ooFacade = null;
    ThanosClient thanosClient = null;
    SearchDal searchDal = null;
    Environment env = null;
    ArrayList<Deployment> deployments = null;
    Platform platform_2 = null;
    Platform platform_1 = null;
    StringBuilder esQuery = new StringBuilder();
    Map<String, Organization> organizationsMapCache = new HashMap<String, Organization>();
    Config config = new Config();

    @BeforeMethod
    public void setUp() throws Exception {
        //create environment mock
        env = new Environment();
        env.setProfile("QA");
        env.setPath("/testOrg/testAssembly");
        env.setName("testEnv");
        env.setId(1);

        //create platform mocks and set them to above env
        platform_1 = new Platform();
        platform_1.setName("platform_1");
        platform_1.setId(101);
        platform_1.setPath("/testOrg/testAssembly/dev/manifest/tomcat/1");
        platform_1.setTotalComputes(10);
        platform_1.setTotalCores(40);
        platform_1.setEnable("enable");
        platform_1.setAutoReplaceEnabled(true);

        ArrayList<String> clouds = new ArrayList<>();
        clouds.add("cdc1");
        clouds.add("ctf3");
        platform_1.setActiveClouds(clouds);

        platform_2 = new Platform();
        platform_2.setName("platform_2");
        platform_2.setId(102);
        platform_2.setPath("/testOrg/testAssembly");
        platform_2.setTotalComputes(100);
        platform_2.setTotalCores(400);
        platform_2.setEnable("enable");
        platform_2.setAutoReplaceEnabled(true);

        clouds = new ArrayList<>();
        clouds.add("testCloud");
        clouds.add("devCloud");
        platform_2.setActiveClouds(clouds);

        env.addPlatform(platform_2);
        env.addPlatform(platform_1);

        plugin = new OneOpsPlatformScaleDownPlugin();

        ooFacade = Mockito.mock(OneOpsFacade.class);
        searchDal = Mockito.mock(SearchDal.class);
        thanosClient = Mockito.mock(ThanosClient.class);

        plugin.setOoFacade(ooFacade);
        plugin.setSearchDal(searchDal);
        plugin.setThanosClient(thanosClient);

        HashMap<String, String> customConfig = new HashMap<>();
        customConfig.put("scaleDownEnabled", "true");
        customConfig.put("prop1", "val1");

        config.setCustomConfigs(customConfig);
        plugin.setConfig(config);
    }

    @Test
    public void testEligiblePlatform() throws Exception {
        CloudResourcesUtilizationStats cloudStats_dal_Platform_1 = new CloudResourcesUtilizationStats();
        CloudResourcesUtilizationStats cloudStats_dfw_Platform_1 = new CloudResourcesUtilizationStats();

        CloudResourcesUtilizationStats cloudStats_dfw_Platform_2 = new CloudResourcesUtilizationStats();


        cloudStats_dal_Platform_1.setReclaimVms(3);
        cloudStats_dfw_Platform_1.setReclaimVms(2);
        cloudStats_dal_Platform_1.setMinClusterSize(4);
        cloudStats_dal_Platform_1.setReclaim("yes");
        cloudStats_dfw_Platform_1.setMinClusterSize(5);

        cloudStats_dfw_Platform_2.setReclaimVms(0);
        cloudStats_dfw_Platform_2.setMinClusterSize(4);

        cloudStats_dal_Platform_1.setReclaim("yes");
        cloudStats_dfw_Platform_1.setReclaim("yes");

        ArrayList<CloudResourcesUtilizationStats> cloudStats_1 = new ArrayList<CloudResourcesUtilizationStats>();
        cloudStats_1.add(cloudStats_dal_Platform_1);
        cloudStats_1.add(cloudStats_dfw_Platform_1);

        ArrayList<CloudResourcesUtilizationStats> cloudStats_2 = new ArrayList<CloudResourcesUtilizationStats>();
        cloudStats_2.add(cloudStats_dfw_Platform_2);

        Mockito.when(thanosClient.getStats(Mockito.eq(platform_1.getPath())))
                .thenReturn(cloudStats_1);
        Mockito.when(thanosClient.getStats(Mockito.eq(platform_2.getPath())))
                .thenReturn(cloudStats_2);

        plugin.processEnvironment(env, organizationsMapCache);

        //make sure the scale down was called for platform having non-zero reclaim number returned by thanos
        Mockito.verify(ooFacade, Mockito.times(1)).scaleDown(platform_1.getId(),
                2, 5, OneOpsPlatformScaleDownPlugin.SCALE_DOWN_USER_ID);

        //make sure the scale down was not called for the platform having 0 reclaim count
        Mockito.verify(ooFacade, Mockito.times(0)).scaleDown(Mockito.eq(platform_2.getId()),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testForAutoReplaceFlag() throws Exception {
        CloudResourcesUtilizationStats cloudStats_dal_Platform_1 = new CloudResourcesUtilizationStats();
        CloudResourcesUtilizationStats cloudStats_dfw_Platform_1 = new CloudResourcesUtilizationStats();

        CloudResourcesUtilizationStats cloudStats_dfw_Platform_2 = new CloudResourcesUtilizationStats();


        cloudStats_dal_Platform_1.setReclaimVms(3);
        cloudStats_dfw_Platform_1.setReclaimVms(2);
        cloudStats_dal_Platform_1.setMinClusterSize(4);
        cloudStats_dfw_Platform_1.setMinClusterSize(5);

        cloudStats_dfw_Platform_2.setReclaimVms(0);
        cloudStats_dfw_Platform_2.setMinClusterSize(4);


        ArrayList<CloudResourcesUtilizationStats> cloudStats_1 = new ArrayList<CloudResourcesUtilizationStats>();
        cloudStats_1.add(cloudStats_dal_Platform_1);
        cloudStats_1.add(cloudStats_dfw_Platform_1);

        ArrayList<CloudResourcesUtilizationStats> cloudStats_2 = new ArrayList<CloudResourcesUtilizationStats>();
        cloudStats_2.add(cloudStats_dfw_Platform_2);

        Mockito.when(thanosClient.getStats(Mockito.eq(platform_1.getPath())))
                .thenReturn(cloudStats_1);
        Mockito.when(thanosClient.getStats(Mockito.eq(platform_2.getPath())))
                .thenReturn(cloudStats_2);

        platform_1.setAutoReplaceEnabled(false);
        platform_2.setAutoReplaceEnabled(false);
        plugin.processEnvironment(env, organizationsMapCache);

        //make sure the scale down was not called for the platform having auto-repalce = off
        Mockito.verify(ooFacade, Mockito.times(0)).scaleDown(Mockito.eq(platform_1.getId()),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testConfig() throws Exception {
        CloudResourcesUtilizationStats cloudStats_dal_Platform_1 = new CloudResourcesUtilizationStats();
        CloudResourcesUtilizationStats cloudStats_dfw_Platform_1 = new CloudResourcesUtilizationStats();

        cloudStats_dal_Platform_1.setReclaimVms(3);
        cloudStats_dfw_Platform_1.setReclaimVms(2);

        ArrayList<CloudResourcesUtilizationStats> cloudStats_1 = new ArrayList<CloudResourcesUtilizationStats>();
        cloudStats_1.add(cloudStats_dal_Platform_1);
        cloudStats_1.add(cloudStats_dfw_Platform_1);

        Mockito.when(thanosClient.getStats(Mockito.eq(platform_1.getPath())))
                .thenReturn(cloudStats_1);

        //now change the config to disable autoscale
        config.getCustomConfigs().put("scaleDownEnabled", "false");
        plugin.processEnvironment(env, organizationsMapCache);

        //make sure the scale down was not called at all
        Mockito.verify(ooFacade, Mockito.times(0)).scaleDown(Mockito.anyLong(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }
}



