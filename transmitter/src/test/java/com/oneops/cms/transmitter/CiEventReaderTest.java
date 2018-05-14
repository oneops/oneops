package com.oneops.cms.transmitter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.simple.domain.CmsCISimpleWithTags;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.CMSEventRecord;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;

public class CiEventReaderTest {

  CIEventReader ciEventReader;
  CIMapperImpl mapper = new CIMapperImpl();

  @Before
  public void setup() {
    ciEventReader = new CIEventReader();
    ciEventReader.init();
    SqlSessionFactory sessionFactory = mock(SqlSessionFactory.class);
    SqlSession session = mock(SqlSession.class);
    when(session.getMapper(CIMapper.class)).thenReturn(mapper);
    when(sessionFactory.openSession()).thenReturn(session);
    ciEventReader.setSessionFactory(sessionFactory);
  }

  @Test
  public void eventForDesignCi() {
    addCiWithNsPath("/org1/assembly1/_design/plt1",  "catalog.Compute");
    CmsCI orgCi = new CmsCI();
    orgCi.setCiId(1400);
    orgCi.setNsPath("/");
    orgCi.setCiClassName("account.Organization");
    orgCi.setCiName("org1");
    orgCi.setCiStateId(100);

    CmsCIAttribute tagAttr = new CmsCIAttribute();
    tagAttr.setCiId(1400);
    tagAttr.setAttributeName("tags");
    tagAttr.setDfValue("{\"Owner\":\"user 1\",\"costcenter\":\"c100\",\"pillar\":\"p-o\"}");
    tagAttr.setDjValue("{\"Owner\":\"user 1\",\"costcenter\":\"c100\",\"pillar\":\"p-o\"}");

    mapper.addCI(orgCi, Collections.singletonList(tagAttr));

    CMSEventRecord eventRecord = new CMSEventRecord();
    eventRecord.setCreated(new Date());
    eventRecord.setSourcePk(1500);
    eventRecord.setEventType("add");
    eventRecord.setSourceName("cm_ci");

    CMSEvent event = ciEventReader.populateEvent(eventRecord);
    CmsCISimpleWithTags ciSimple = (CmsCISimpleWithTags) event.getPayload();
    assertThat(ciSimple.getCiId(), is(1500L));
    assertThat(ciSimple.getCiName(), is("compute"));
    assertThat(ciSimple.getCiAttributes().size(), is(1));
    assertThat(ciSimple.getCiAttributes().get("size"), is("M"));

    assertThat(ciSimple.getOrg(), is("org1"));
    assertThat(ciSimple.getAssembly(), is("assembly1"));
    assertThat(ciSimple.getPlatform(), is("plt1"));
    Map<String, String> tags = ciSimple.getTags();
    assertThat(tags.size(), is(3));
    assertThat(tags.get("Owner"), is("user 1"));
    assertThat(tags.get("costcenter"), is("c100"));
    assertThat(tags.get("pillar"), is("p-o"));
  }

  @Test
  public void eventForManifestCi() {
    addCiWithNsPath("/org1/assembly1/stg/bom/plt1/2", "manifest.Compute");
    CmsCI orgCi = new CmsCI();
    orgCi.setCiId(1400);
    orgCi.setNsPath("/");
    orgCi.setCiClassName("account.Organization");
    orgCi.setCiName("org1");
    orgCi.setCiStateId(100);

    CmsCIAttribute tagAttr = new CmsCIAttribute();
    tagAttr.setCiId(1400);
    tagAttr.setAttributeName("tags");
    tagAttr.setDfValue("{\"Owner\":\"user 1\",\"costcenter\":\"c100\",\"pillar\":\"p-o\"}");
    tagAttr.setDjValue("{\"Owner\":\"user 1\",\"costcenter\":\"c100\",\"pillar\":\"p-o\"}");

    mapper.addCI(orgCi, Collections.singletonList(tagAttr));

    CMSEventRecord eventRecord = new CMSEventRecord();
    eventRecord.setCreated(new Date());
    eventRecord.setSourcePk(1500);
    eventRecord.setEventType("add");
    eventRecord.setSourceName("cm_ci");

    CMSEvent event = ciEventReader.populateEvent(eventRecord);
    CmsCISimpleWithTags ciSimple = (CmsCISimpleWithTags) event.getPayload();
    assertThat(ciSimple.getCiId(), is(1500L));
    assertThat(ciSimple.getCiName(), is("compute"));
    assertThat(ciSimple.getCiAttributes().size(), is(1));
    assertThat(ciSimple.getCiAttributes().get("size"), is("M"));

    assertThat(ciSimple.getOrg(), is("org1"));
    assertThat(ciSimple.getAssembly(), is("assembly1"));
    assertThat(ciSimple.getEnv(), is("stg"));
    assertThat(ciSimple.getPlatform(), is("plt1"));
    Map<String, String> tags = ciSimple.getTags();
    assertThat(tags.size(), is(3));
    assertThat(tags.get("Owner"), is("user 1"));
    assertThat(tags.get("costcenter"), is("c100"));
    assertThat(tags.get("pillar"), is("p-o"));
  }

  @Test
  public void eventForMgmtCi() {
    addCiWithNsPath("/public/oneops/packs/tomcat/1/redundant", "catalog.Compute");

    CMSEventRecord eventRecord = new CMSEventRecord();
    eventRecord.setCreated(new Date());
    eventRecord.setSourcePk(1500);
    eventRecord.setEventType("add");
    eventRecord.setSourceName("cm_ci");

    CMSEvent event = ciEventReader.populateEvent(eventRecord);
    CmsCISimpleWithTags ciSimple = (CmsCISimpleWithTags) event.getPayload();
    assertThat(ciSimple.getCiId(), is(1500L));
    assertThat(ciSimple.getCiName(), is("compute"));
    assertThat(ciSimple.getCiAttributes().size(), is(1));
    assertThat(ciSimple.getCiAttributes().get("size"), is("M"));

    Map<String, String> tags = ciSimple.getTags();
    assertThat(tags.size(), is(0));
  }

  @Test
  public void eventForCloudCi() {

    CmsCI ci = new CmsCI();
    ci.setCiId(1600);
    ci.setNsPath("/org1/_clouds");
    ci.setCiClassName("account.Cloud");
    ci.setCiStateId(100);
    ci.setCiName("cl1");

    CmsCIAttribute attr = new CmsCIAttribute();
    attr.setCiId(1500);
    attr.setAttributeName("adminstatus");
    attr.setDfValue("active");
    attr.setDjValue("active");

    mapper.addCI(ci, Collections.singletonList(attr));

    CmsCI orgCi = new CmsCI();
    orgCi.setCiId(1400);
    orgCi.setNsPath("/");
    orgCi.setCiClassName("account.Organization");
    orgCi.setCiName("org1");
    orgCi.setCiStateId(100);

    CmsCIAttribute tagAttr = new CmsCIAttribute();
    tagAttr.setCiId(1400);
    tagAttr.setAttributeName("tags");
    tagAttr.setDfValue("{\"Owner\":\"user 1\",\"costcenter\":\"c100\",\"pillar\":\"p-o\"}");
    tagAttr.setDjValue("{\"Owner\":\"user 1\",\"costcenter\":\"c100\",\"pillar\":\"p-o\"}");

    mapper.addCI(orgCi, Collections.singletonList(tagAttr));

    CMSEventRecord eventRecord = new CMSEventRecord();
    eventRecord.setCreated(new Date());
    eventRecord.setSourcePk(1600);
    eventRecord.setEventType("add");
    eventRecord.setSourceName("cm_ci");

    CMSEvent event = ciEventReader.populateEvent(eventRecord);
    CmsCISimpleWithTags ciSimple = (CmsCISimpleWithTags) event.getPayload();
    assertThat(ciSimple.getCiId(), is(1600L));
    assertThat(ciSimple.getCiName(), is("cl1"));
    assertThat(ciSimple.getCiAttributes().size(), is(1));
    assertThat(ciSimple.getCiAttributes().get("adminstatus"), is("active"));
    assertThat(ciSimple.getOrg(), is("org1"));

    Map<String, String> tags = ciSimple.getTags();
    assertThat(tags.size(), is(3));
    assertThat(tags.get("Owner"), is("user 1"));
    assertThat(tags.get("costcenter"), is("c100"));
    assertThat(tags.get("pillar"), is("p-o"));
  }

  private void addCiWithNsPath(String nsPath, String clazz) {
    CmsCI ci = new CmsCI();
    ci.setCiId(1500);
    ci.setNsPath(nsPath);
    ci.setCiClassName(clazz);
    ci.setCiStateId(100);
    ci.setCiName("compute");

    CmsCIAttribute attr = new CmsCIAttribute();
    attr.setCiId(1500);
    attr.setAttributeName("size");
    attr.setDfValue("M");
    attr.setDjValue("M");

    mapper.addCI(ci, Collections.singletonList(attr));
  }

}
