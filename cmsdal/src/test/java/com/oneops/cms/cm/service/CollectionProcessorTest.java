/*******************************************************************************
 *   Copyright 2017 Walmart, Inc.
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
package com.oneops.cms.cm.service;

import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.collections.CollectionProcessor;
import com.oneops.cms.collections.def.CollectionLinkDefinition;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CollectionProcessorTest {

  private static ApplicationContext context;

  private CmsCmProcessor cmProcessor;

  private Gson gson = new Gson();


  @BeforeClass
  public void setUp() {
    context = new ClassPathXmlApplicationContext("**/test-wo-context.xml");
    cmProcessor = context.getBean(CmsCmProcessor.class);


  }

  @Test
  public void getRelationTest() {
    CollectionProcessor pr = new CollectionProcessor();
    pr.setCmProcessor(cmProcessor);
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("custom-payload.json");
    JsonParser parser = new JsonParser();
    JsonElement jsonElement = parser.parse(new InputStreamReader(is));
    final CollectionLinkDefinition relationDef = gson
        .fromJson(jsonElement, CollectionLinkDefinition.class);
    List<CmsCIRelation> realizedAsReltions = new ArrayList<>();
    CmsCIRelation e = new CmsCIRelation();
    e.setRelationName("base.RealizedAs");
    e.setFromCiId(456);
    e.setToCiId(123);
    //return platform relation
    realizedAsReltions.add(e);
    //when(opsMapper.getProcedureForCiByAction(CI_WITH_ACTIVE_ACTION, null, null, null)).thenReturn(blockingList);
    final long manifestPlatformId = 789;
    when(cmProcessor.getToCIRelationsNakedNoAttrs(123, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName())).thenReturn(
        Collections.singletonList(e));
    CmsCIRelation manifestRequiresRel = new CmsCIRelation();
    manifestRequiresRel.setFromCiId(manifestPlatformId);
    manifestRequiresRel.setToCiId(456);
    manifestRequiresRel.setRelationName("manifest.Requires");
    when(cmProcessor.getToCIRelationsNakedNoAttrs(456, "manifest.Requires", null, "manifest.Platform")).thenReturn(
        Collections.singletonList(manifestRequiresRel));
    List<CmsCIRelation> cloudRelations = new ArrayList<>();


    Map<String,Long> clouds = new HashMap<>();
    clouds.put("cloud1",1001l );
    clouds.put("cloud2",1002l);

    Map<String,CmsCI> cloudCis = new HashMap<>();

    clouds.forEach((k,v)->{
      cloudRelations.add(relation(manifestPlatformId,k,v));
    });

    clouds.forEach((k,v)->{
      cloudCis.put(k,newCloudCi(k,v));
      when(cmProcessor.getCiById(v)).thenReturn(cloudCis.get(k));
    });

    when(cmProcessor.getFromCIRelationsNakedNoAttrs(manifestPlatformId, "base.Consumes", null , "account.Cloud" )).thenReturn(cloudRelations);

    final List<CmsCI> cis = pr.getFlatCollection(123, relationDef);
    Assert.assertTrue(cis.size()==cloudRelations.size());

    cis.stream().forEach(
        ci ->{
          ci.getAttributes().forEach(
              (k,v)->{
                final CmsCI cmsCI = cloudCis.get(ci.getCiName());
                Assert.assertTrue(cmsCI.getAttributes().size()==2);
                if(!k.contains("base.Consumes")){
                  Assert.assertTrue(cmsCI.getAttribute(k)!=null);
                  Assert.assertTrue(cmsCI.getAttribute(k).getDfValue().equals(cmsCI.getAttribute(k).getDfValue()));
                }else {
                  Assert.assertTrue(k.equals("base.Consumes."+ci.getCiName()+"-pct_deploy"));
                }

              }
          );
        }
    );

  }

  private CmsCI newCloudCi(String k, Long v) {
    CmsCI cloud1 = new CmsCI();
    cloud1.setCiName(k);
    cloud1.setCiId(v);
    Map<String, CmsCIAttribute> attributes = new HashMap<>();
    CmsCIAttribute attribute = new CmsCIAttribute();
    attribute.setDfValue("value1");
    attribute.setAttributeName(cloud1.getCiName()+"-"+"location");
    attributes.put(attribute.getAttributeName(),attribute);
    cloud1.setAttributes(attributes);
    return cloud1;
  }

  private CmsCIRelation relation(long manifestPlatformId, String cloudName, long cloudId
      ) {
    CmsCIRelation cloudRelation = new CmsCIRelation();
    cloudRelation.setFromCiId(manifestPlatformId);
    cloudRelation.setToCiId(cloudId);
    cloudRelation.setRelationName("base.Consumes");
    Map<String, CmsCIRelationAttribute> attributes = new HashMap<>();

    CmsCIRelationAttribute attribute = new CmsCIRelationAttribute();
    attribute.setDfValue(cloudName+"-consumes-value");
    attribute.setAttributeName(cloudName+"-"+"pct_deploy");
    attributes.put(attribute.getAttributeName(),attribute);
    cloudRelation.setAttributes(attributes);
    cloudRelation.setComments("{\"toCiName\":\"" + cloudName
        + "\",\"toCiClass\":\"account.Cloud\",\"fromCiClass\":\"manifest.Platform\",\"fromCiName\":\"solr\"}");
    return cloudRelation;
  }



}
