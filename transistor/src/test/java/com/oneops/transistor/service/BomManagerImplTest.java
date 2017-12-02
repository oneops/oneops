/*******************************************************************************
 *
 *   Copyright 2015 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.util.CloudUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.oneops.cms.util.CmsConstants.DEPLOYED_TO;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Mockito.*;


public class BomManagerImplTest {

    private Map<String, CmsCI> cloudCis;
    private final String[] clouds = {"c1", "c2", "c3", "c4"};
    //ids are 0,1,2,3


    @BeforeClass
    public void setup() {
        cloudCis = IntStream.range(0, clouds.length)
                .mapToObj(i -> (ci(clouds[i], i)))
                .collect(toMap(CmsCI::getCiName, Function.identity()));
    }


    @Test(expectedExceptions = {})
    public void somePrimaryOnManifestAllPrimaryDeployed() throws Exception {
        CmsCmProcessor cmProcessor =mock(CmsCmProcessor.class);
        BomManagerImpl impl = getInstance(cmProcessor);

        String[] primaryClouds = {"c1", "c2"};
        String[] secondaryClouds = {"c3", "c4"};
        List<CmsCIRelation> platformCloudRels = Stream.of(primaryClouds)
                .map(s -> (createPrimaryCloud(s)))
                .collect(toList());
        List<CmsCIRelation> secondaryCloudRels = Stream.of(secondaryClouds)
                .map(s -> (createSecondaryCloud(s)))
                .collect(toList());
        platformCloudRels.addAll(secondaryCloudRels);

        PlatformBomGenerationContext context = platformContext("/test/a1/e1", "p1");
        doAnswer(invocationOnMock -> Stream.of(clouds)
                    .map(cloud -> (fqdnDeployedToRelation(cloud, "1")))
                    .collect(Collectors.toList()))
                .when(context).getBomRelations();

        doAnswer(i -> Arrays.asList(relation(ci("manifest.Fqdn", "c1", 1234), "Entrypoint")))
                .when(context).getEntryPoints();

        impl.check4Secondary(context, platformCloudRels);
    }


    private BomManagerImpl getInstance(CmsCmProcessor cmProcessor) {
        BomManagerImpl impl = mock(BomManagerImpl.class, CALLS_REAL_METHODS);
        impl.setCmProcessor(cmProcessor);
        TransUtil transUtil = new TransUtil();
        CmsUtil util = new CmsUtil();
        transUtil.setCmsUtil(util);
        impl.setTrUtil(transUtil);
        impl.setCloudUtil(new CloudUtil());
        return impl;
    }

    @Test(expectedExceptions = {TransistorException.class},expectedExceptionsMessageRegExp=".* <c1,c2>.*")
    public void primaryInactiveDeployingSecondaryMakingAllSec() throws Exception {
        CmsCmProcessor cmProcessor =mock(CmsCmProcessor.class);
        BomManagerImpl impl = getInstance(cmProcessor);
        String[] inactivePrimaryclouds = {"c1", "c2"};
        String[] secondaryClouds = {"c3", "c4"};
        List<CmsCIRelation> platformCloudRels = Stream.of(inactivePrimaryclouds)
                .map(s -> (createInactivePrimaryCloud(s)))
                .collect(toList());
        List<CmsCIRelation> secondaryCloudRels = Stream.of(secondaryClouds)
                .map(s -> (createSecondaryCloud(s)))
                .collect(toList());
        platformCloudRels.addAll(secondaryCloudRels);

        PlatformBomGenerationContext context = platformContext("/test/a1/e1", "p1");
        //returns all secondary clouds deployed
        doAnswer(invocationOnMock -> Stream.of(clouds)
                    .map(cloud -> (fqdnDeployedToRelation(cloud, "2")))
                    .collect(Collectors.toList()))
                .when(context).getBomRelations();

        doAnswer(i -> Arrays.asList(relation(ci("manifest.Fqdn", "c1", 1234), "Entrypoint")))
                .when(context).getEntryPoints();

        //should not allow
        impl.check4Secondary(context, platformCloudRels);
    }

    @Test(expectedExceptions = {TransistorException.class},expectedExceptionsMessageRegExp=".* <c1,c2>.*")
    public void primaryOfflineDeployingSecondaryMakingAllSec() throws Exception {
        CmsCmProcessor cmProcessor = mock(CmsCmProcessor.class);
        BomManagerImpl impl = getInstance(cmProcessor);
        String[] offlinePrimaryClouds = {"c1", "c2"};
        String[] secondaryClouds = {"c3", "c4"};
        List<CmsCIRelation> platformCloudRels = Stream.of(offlinePrimaryClouds)
                .map(s -> (createOfflinePrimaryCloud(s)))
                .collect(toList());

        List<CmsCIRelation> secondaryCloudRels = Stream.of(secondaryClouds)
                .map(s -> (createSecondaryCloud(s)))
                .collect(toList());

        platformCloudRels.addAll(secondaryCloudRels);

        PlatformBomGenerationContext context = platformContext("/test/a1/e1", "p1");
        //returns all secondary clouds deployed
        doAnswer(invocationOnMock -> Stream.of(clouds)
                    .map(cloud -> (fqdnDeployedToRelation(cloud, "1")))
                    .collect(Collectors.toList()))
                .when(context).getBomRelations();

        doAnswer(i -> Arrays.asList(relation(ci("manifest.Fqdn", "c1", 1234), "Entrypoint")))
                .when(context).getEntryPoints();

        //should not allow
        impl.check4Secondary(context, platformCloudRels);
    }



    @Test(expectedExceptions = {})
    public void noEntryPoint() throws Exception {
        CmsCmProcessor cmProcessor =mock(CmsCmProcessor.class);
        BomManagerImpl impl = getInstance(cmProcessor);
        String[] inactivePrimaryclouds = {"c1", "c2"};
        String[] secondaryClouds = {"c3", "c4"};
        List<CmsCIRelation> platformCloudRels = Stream.of(inactivePrimaryclouds)
                .map(s -> (createInactivePrimaryCloud(s)))
                .collect(toList());
        List<CmsCIRelation> secondaryCloudRels = Stream.of(secondaryClouds)
                .map(s -> (createSecondaryCloud(s)))
                .collect(toList());
        platformCloudRels.addAll(secondaryCloudRels);

        PlatformBomGenerationContext context = platformContext("/test/a1/e1", "p1");
        //returns all secondary clouds deployed
        doAnswer(invocationOnMock -> Stream.of(clouds)
                    .map(cloud -> (fqdnDeployedToRelation(cloud, "2")))
                    .collect(Collectors.toList()))
                .when(context).getBomRelations();

        doAnswer(i -> Collections.EMPTY_LIST)
                .when(context).getEntryPoints();

        //should not allow
        impl.check4Secondary(context, platformCloudRels);
    }

    private CmsCIRelation relation(CmsCI toCi,String relationName ) {
        CmsCIRelation rel = new CmsCIRelation();
        rel.setToCi(toCi);
        rel.setRelationName(relationName);
        return rel;
    }



    @Test(expectedExceptions = {})
    public void allPrimaryCloudsOnManifest() throws Exception {
        BomManagerImpl impl = mock(BomManagerImpl.class, CALLS_REAL_METHODS);
        impl.setCloudUtil(new CloudUtil());
        List<CmsCIRelation> platformCloudRels = Stream.of(clouds)
                .map(s -> (createPrimaryCloud(s)))
                .collect(toList());
        impl.check4Secondary(platformContext("/test/a1/e1", "p1"), platformCloudRels);
    }


    private CmsCIRelation getCmsCIRelation(String c4, String relationName, Map<String, CmsCIRelationAttribute> primaryAttributes) {
        CmsCIRelation cloudRel = new CmsCIRelation();
        cloudRel.setAttributes(primaryAttributes);
        String platform = "p1";
        cloudRel.setComments("{\"toCiName\":\"" + c4 + "\",\"toCiClass\":\"account.Cloud\",\"fromCiClass\":\"manifest.Platform\",\"fromCiName\":\"" + platform + "\"}");
        cloudRel.setToCiId(cloudCis.get(c4).getCiId());
        cloudRel.setToCi(cloudCis.get(c4));
        cloudRel.setFromCiId(1);
        cloudRel.setRelationName(relationName);
        return cloudRel;
    }

    private CmsCIRelation fqdnDeployedToRelation(String cloud, String priority) {
        CmsCIRelation rel = getCmsCIRelation(cloud,
                                             DEPLOYED_TO,
                                             Stream.of(entryRelAttribute("priority", priority, priority))
                                                     .collect(toMap((e) -> e.getKey(), (e) -> e.getValue())));
        rel.setFromCi(ci("bom.Fqdn", "fqdn-" + cloud, 1234));
        return rel;
    }

    private CmsCIRelation createInactivePrimaryCloud(String cloud) {
        return getCmsCIRelation(cloud, "base.Consumes", relAtrributes("inactive", "1"));
    }
    private CmsCIRelation createOfflinePrimaryCloud(String cloud) {
        return getCmsCIRelation(cloud, "base.Consumes", relAtrributes("offline", "1"));

    }
    private CmsCIRelation createPrimaryCloud(String c4) {
        return getCmsCIRelation(c4, "base.Consumes", relAtrributes("active", "1"));
    }

    private Map<String, CmsCIRelationAttribute> relAtrributes(String active, String primary) {
        return Collections.unmodifiableMap(Stream.of(
                entryRelAttribute("dpmt_order", "1", "1"),
                entryRelAttribute("adminstatus", active, active),
                entryRelAttribute("priority", primary,primary)
        ).collect(toMap((e) -> e.getKey(), (e) -> e.getValue())));
    }

    private CmsCIRelation createSecondaryCloud(String c4) {
        return getCmsCIRelation(c4, "base.Consumes", relAtrributes("active","2"));
    }

    private AbstractMap.SimpleEntry<String, CmsCIRelationAttribute> entryRelAttribute(String name, String dfValue, String dj_Value) {
        return new AbstractMap.SimpleEntry<>(name, relationAttribute(name, dfValue, dj_Value));
    }

    private CmsCIRelationAttribute relationAttribute(String name, String df, String dj) {
        CmsCIRelationAttribute atrrib = new CmsCIRelationAttribute();
        atrrib.setAttributeName(name);
        atrrib.setDfValue(df);
        atrrib.setDjValue(dj);
        return atrrib;
    }

    private CmsCI ci(String className,String ciName, int i) {
        CmsCI ci = ci(ciName, i);
        ci.setCiClassName(className);
        return ci;
    }
    private CmsCI ci(String ciName, int i) {
        CmsCI ci = new CmsCI();
        ci.setCiName(ciName);
        ci.setCiId(i);
        return ci;
    }

    private PlatformBomGenerationContext platformContext(String envNsPath, String platformName) {
        CmsCI platform = new CmsCI();
        platform.setCiId((long) (Math.random() * 10000));
        platform.setNsPath(envNsPath + "/manifest/" + platformName + "/1");
        PlatformBomGenerationContext context = mock(PlatformBomGenerationContext.class);
        doAnswer(invocation -> platform).when(context).getPlatform();
        doAnswer(invocation -> platform.getNsPath().replaceFirst("/manifest/", "/bom/")).when(context).getBomNsPath();
        return context;
    }
}
