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
import com.oneops.cms.util.CmsConstants;
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
import static org.testng.Assert.*;


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
    
    @Test
    public void testEmptyDeploymentOrderDoesNotThrowNFE(){
        CmsCmProcessor cmProcessor =mock(CmsCmProcessor.class);
        BomManagerImpl impl = getInstance(cmProcessor);

        String[] primaryClouds = {"c1", "c2"};
        List<CmsCIRelation> platformCloudRels = Stream.of(primaryClouds)
                .map(s -> (createPrimaryCloud(s)))
                .collect(toList());
        platformCloudRels.get(0).getAttribute("dpmt_order").setDjValue("");
        try {
            impl.getOrderedClouds(platformCloudRels, false);
        } catch (NumberFormatException e){
            fail("Shouldn't throw NFE");
        }
    }


    @Test(expectedExceptions = {})
    public void verifyOrderDeploymentOrder() {
        CmsCI p1 = ci("p1", 1);
        CmsCI p2 = ci("p2", 2);
        CmsCI p3 = ci("p3", 3);
        CmsCI p4 = ci("p4", 4);
        CmsCI p5 = ci("p5", 5);
        List<CmsCI> plats = Arrays.asList(p1, p2, p3, p4, p5);

        List<CmsCIRelation> links = new ArrayList<>();
        links.add(relation(CmsConstants.MANIFEST_LINKS_TO, p1, p2));
        links.add(relation(CmsConstants.MANIFEST_LINKS_TO, p2, p3));
        links.add(relation(CmsConstants.MANIFEST_LINKS_TO, p4, p3));

        Set<Long> disabledPlats = new HashSet<>();
        Set<Long> excludedPlats = new HashSet<>();

        BomManagerImpl impl = mock(BomManagerImpl.class, CALLS_REAL_METHODS);
        EnvBomGenerationContext context = mock(EnvBomGenerationContext.class);
        doAnswer(i -> plats)
                .when(context).getPlatforms();
        doAnswer(i -> links)
                .when(context).getLinksToRelations();
        doAnswer(i -> disabledPlats)
                .when(context).getDisabledPlatformIds();
        doAnswer(i -> excludedPlats)
                .when(context).getExcludedPlats();

        Map<Integer, List<CmsCI>> expected = new HashMap<>();
        expected.put(1, Arrays.asList(p3, p5));
        expected.put(2, Arrays.asList(p2, p4));
        expected.put(3, Arrays.asList(p1));
        Map<Integer, List<CmsCI>> orderedPlats = impl.getOrderedPlatforms(context);
        assertEquals(orderedPlats, expected);

        excludedPlats.addAll(Arrays.asList(p2.getCiId(), p4.getCiId(), p5.getCiId()));
        expected = new HashMap<>();
        expected.put(1, Arrays.asList(p3));
        expected.put(3, Arrays.asList(p1));
        orderedPlats = impl.getOrderedPlatforms(context);
        assertEquals(orderedPlats, expected);

        excludedPlats.clear();
        disabledPlats.addAll(Arrays.asList(p3.getCiId(), p5.getCiId()));
        expected = new HashMap<>();
        expected.put(2, Arrays.asList(p2, p4));
        expected.put(3, Arrays.asList(p1));
        expected.put(4, Arrays.asList(p3, p5));
        orderedPlats = impl.getOrderedPlatforms(context);
        assertEquals(orderedPlats, expected);
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

        doAnswer(i -> Arrays.asList(relation("Entrypoint", ci("manifest.Fqdn", "c1", 1234))))
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

        doAnswer(i -> Arrays.asList(relation("Entrypoint", ci("manifest.Fqdn", "c1", 1234))))
                .when(context).getEntryPoints();

        //should not allow
        impl.check4Secondary(context, platformCloudRels);
    }

    @Test
    public void changingSomeDeployedPrimaryToInactiveAndOthersToSecondary() throws Exception {
        CmsCmProcessor cmProcessor =mock(CmsCmProcessor.class);
        BomManagerImpl impl = getInstance(cmProcessor);
        String[] inactivePrimaryClouds = {"c1"};
        String[] activeSecondaryClouds = {"c2"};
        List<CmsCIRelation> platformCloudRels = Stream.of(inactivePrimaryClouds)
                .map(s -> (createInactivePrimaryCloud(s)))
                .collect(toList());
        List<CmsCIRelation> secondaryCloudRels = Stream.of(activeSecondaryClouds)
                .map(s -> (createSecondaryCloud(s)))
                .collect(toList());
        platformCloudRels.addAll(secondaryCloudRels);

        PlatformBomGenerationContext context = platformContext("/test/a1/e1", "p1");
        doAnswer(invocationOnMock -> Stream.of(clouds)
                    .map(cloud -> (fqdnDeployedToRelation(cloud, "1")))
                    .collect(Collectors.toList()))
                .when(context).getBomRelations();

        doAnswer(i -> Arrays.asList(relation("Entrypoint", ci("manifest.Fqdn", "fqdn", 1234))))
                .when(context).getEntryPoints();

        //should allow
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

        doAnswer(i -> Arrays.asList(relation("Entrypoint", ci("manifest.Fqdn", "c1", 1234))))
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

    private CmsCIRelation relation(String relationName, CmsCI toCi) {
        CmsCIRelation rel = new CmsCIRelation();
        rel.setRelationName(relationName);
        rel.setToCi(toCi);
        rel.setFromCiId((toCi.getCiId()));
        return rel;
    }


    private CmsCIRelation relation(String relationName, CmsCI fromCi, CmsCI toCi) {
        CmsCIRelation rel = relation(relationName, fromCi);
        rel.setToCi(toCi);
        rel.setToCiId((toCi.getCiId()));
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
    private CmsCI ci(String ciName, int ciId) {
        CmsCI ci = new CmsCI();
        ci.setCiName(ciName);
        ci.setCiId(ciId);
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
