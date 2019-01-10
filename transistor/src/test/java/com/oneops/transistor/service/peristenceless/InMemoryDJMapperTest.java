package com.oneops.transistor.service.peristenceless;

import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.*;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class InMemoryDJMapperTest {
    private InMemoryDJMapper mapper;

    @BeforeMethod
    public void setup() {
        mapper = new InMemoryDJMapper();
    }

    @Test
    public void testGetNextDjId() throws Exception {
        assertEquals(mapper.getNextDjId(), 1);
        assertEquals(mapper.getNextDjId(), 2);
    }

    @Test
    public void testGetNextCiId() throws Exception {
        assertEquals(mapper.getNextCiId(), 1);
        assertEquals(mapper.getNextCiId(), 2);
    }

    @Test
    public void testGetReleaseStateId() throws Exception {
        assertEquals(mapper.getReleaseStateId(""), new Integer(1));
    }

    @Test

    public void testGetRfcCiActionId() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getRfcCiActionId(""));
    }

    @Test
    public void testCreateRelease() throws Exception {
        CmsRelease release = new CmsRelease();
        release.setReleaseId(1);
        mapper.createRelease(release);
        assertEquals(mapper.getReleaseById(1), release);
    }

    @Test
    public void testBrushReleaseExecOrder() throws Exception {

    }

    @Test
    public void testGetReleaseById() throws Exception {
        assertNull(mapper.getReleaseById(1));
        CmsRelease release = new CmsRelease();
        release.setReleaseId(1);
        mapper.createRelease(release);
        assertEquals(mapper.getReleaseById(1), release);
    }

    @Test
    public void testGetReleaseBy3() throws Exception {
        assertEquals(mapper.getReleaseBy3("", "", ""), new ArrayList());
    }

    @Test
    public void testGetLatestRelease() throws Exception {
        CmsRelease release = new CmsRelease();
        release.setReleaseId(1);
        mapper.createRelease(release);
        assertEquals(mapper.getLatestRelease("", "").size(), 1);
    }

    @Test
    public void testUpdateRelease() throws Exception {
        assertNull(mapper.getReleaseById(1));
        CmsRelease release = new CmsRelease();
        release.setReleaseId(1);
        mapper.createRelease(release);
        assertEquals(mapper.getReleaseById(1), release);
        CmsRelease release2 = new CmsRelease();
        release2.setReleaseId(2);
        mapper.updateRelease(release2);
        assertEquals(mapper.getReleaseById(2), release2);
    }

    @Test
    public void testDeleteRelease() throws Exception {

    }

    @Test
    public void testCommitRelease() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.commitRelease(1, true, 1, true, "", ""));
    }

    @Test
    public void testCreateRfcCI() throws Exception {
        assertEquals(mapper.getCis().size(), 0);
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        mapper.createRfcCI(rfcCi);
        assertEquals(mapper.getCis().size(), 1);
        assertEquals(mapper.getCis().get(1L), rfcCi);
    }

    @Test
    public void testCreateRfcLog() throws Exception {
    }

    @Test
    public void testRmRfcCIfromRelease() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.rmRfcCIfromRelease(1));
    }

    @Test
    public void testUpdateRfcCI() throws Exception {
        assertEquals(mapper.getCis().size(), 0);
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        mapper.createRfcCI(rfcCi);
        assertEquals(mapper.getCis().size(), 1);
        assertEquals(mapper.getCis().get(1L), rfcCi);

        CmsRfcCI rfcCi2 = new CmsRfcCI();
        rfcCi2.setRfcId(1);
        mapper.updateRfcCI(rfcCi2);
        assertEquals(mapper.getCis().size(), 1);
        assertEquals(mapper.getCis().get(1L), rfcCi2);
    }

    @Test
    public void testUpdateRfcLog() throws Exception {
    }

    @Test
    public void testInsertRfcCIAttribute() throws Exception {
    }

    @Test
    public void testUpdateRfcCIAttribute() throws Exception {
    }

    @Test
    public void testGetRfcCIById() throws Exception {
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        mapper.createRfcCI(rfcCi);
        assertEquals(mapper.getCis().size(), 1);
        assertEquals(mapper.getCis().get(1L), rfcCi);

    }

    @Test
    public void testGetOpenRfcCIByCiId() throws Exception {
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        rfcCi.setCiId(1);
        mapper.createRfcCI(rfcCi);
        CmsRfcCI rfcCi2 = new CmsRfcCI();
        rfcCi2.setRfcId(2);
        rfcCi2.setCiId(2);
        mapper.createRfcCI(rfcCi2);
        rfcCi2.setIsActiveInRelease(false);
        assertEquals(mapper.getOpenRfcCIByCiId(1L), rfcCi);
        assertNull(mapper.getOpenRfcCIByCiId(2L));
    }

    @Test
    public void testGetOpenRfcCIByCiIdList() {
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        rfcCi.setCiId(1);
        mapper.createRfcCI(rfcCi);
        CmsRfcCI rfcCi2 = new CmsRfcCI();
        rfcCi2.setRfcId(2);
        rfcCi2.setCiId(2);
        mapper.createRfcCI(rfcCi2);
        ArrayList<Long> ciIds = new ArrayList<>();
        ciIds.add(1L);
        ciIds.add(2L);
        assertEquals(2, mapper.getOpenRfcCIByCiIdList(ciIds).size());
    }

    @Test
    public void testGetRfcCIBy3() throws Exception {
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        rfcCi.setCiId(1);
        mapper.createRfcCI(rfcCi);
        CmsRfcCI rfcCi2 = new CmsRfcCI();
        rfcCi2.setRfcId(2);
        rfcCi2.setCiId(2);
        mapper.createRfcCI(rfcCi2);
        List<CmsRfcCI> list = mapper.getRfcCIBy3(1, true, 2L);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0), rfcCi2);
    }

    @Test
    public void testGetRfcCIByClazzAndName() throws Exception {
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setRfcId(1);
        rfcCi.setCiId(1);
        rfcCi.setNsPath("/test");
        rfcCi.setCiName("test");
        rfcCi.setCiState("open");
        rfcCi.setCiClassName("clazz");
        CmsRfcCI rfcCi2 = new CmsRfcCI();
        rfcCi2.setRfcId(2);
        rfcCi2.setCiId(2);
        rfcCi2.setNsPath("/test");
        rfcCi2.setCiName("test2");
        rfcCi2.setCiState("open");
        rfcCi2.setCiClassName("clazz");
        mapper.createRfcCI(rfcCi);
        mapper.createRfcCI(rfcCi2);
        List<CmsRfcCI> rfcCIByClazzAndName = mapper.getRfcCIByClazzAndName("/test", "clazz", "test", true, "open");
        assertEquals(rfcCIByClazzAndName.size(), 1);
        assertEquals(rfcCIByClazzAndName.get(0), rfcCi);
        rfcCIByClazzAndName = mapper.getRfcCIByClazzAndName("/test1", "clazz", "test", true, "open");
        assertEquals(rfcCIByClazzAndName.size(), 0);
    }

    @Test
    public void testGetOpenRfcCIByClazzAndNameLower() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenRfcCIByClazzAndNameLower("", "", ""));
    }

    @Test
    public void testGetOpenRfcCIByNsLike() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenRfcCIByNsLike("", "", "", ""));
    }

    @Test
    public void testGetOpenRfcCIByClazzAnd2Names() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenRfcCIByClazzAnd2Names("", "", "", ""));
    }

    @Test
    public void testGetClosedRfcCIByCiId() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getClosedRfcCIByCiId(1));
    }

    @Test
    public void testGetRfcCIAttributes() throws Exception {
        assertEquals(mapper.getRfcCIAttributes(1), new ArrayList());
    }

    @Test
    public void testGetRfcCIAttributesByRfcIdList() throws Exception {
        assertEquals(mapper.getRfcCIAttributesByRfcIdList(new HashSet()), new ArrayList());
    }

    @Test
    public void testCreateRfcRelation() throws Exception {
        assertEquals(mapper.getRelations().size(), 0);
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        mapper.createRfcRelation(rfcRelation);
        assertEquals(mapper.getRelations().size(), 1);
        assertEquals(mapper.getRelations().get(1L), rfcRelation);
    }

    @Test
    public void testCreateRfcRelationLog() throws Exception {
    }

    @Test
    public void testRmRfcRelationfromRelease() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.rmRfcRelationfromRelease(1));
    }

    @Test
    public void testUpdateRfcRelation() throws Exception {
        assertEquals(mapper.getRelations().size(), 0);
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        mapper.createRfcRelation(rfcRelation);
        CmsRfcRelation rfcRelation2 = new CmsRfcRelation();
        rfcRelation2.setRfcId(1);
        mapper.updateRfcRelation(rfcRelation2);


        assertEquals(mapper.getRelations().size(), 1);
        assertEquals(mapper.getRelations().get(1L), rfcRelation2);
    }

    @Test
    public void testUpdateRfcRelationLog() throws Exception {
    }

    @Test
    public void testInsertRfcRelationAttribute() throws Exception {
    }

    @Test
    public void testUpdateRfcRelationAttribute() throws Exception {
    }

    @Test
    public void testUpsertRfcRelationAttribute() throws Exception {
    }

    @Test
    public void testGetRfcRelationById() throws Exception {
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        mapper.createRfcRelation(rfcRelation);
        assertEquals(mapper.getRfcRelationById(1), rfcRelation);

    }

    @Test
    public void testGetOpenRfcRelationByCiRelId() throws Exception {
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        rfcRelation.setCiRelationId(1);
        mapper.createRfcRelation(rfcRelation);
        assertEquals(mapper.getOpenRfcRelationByCiRelId(1), rfcRelation);
    }

    @Test
    public void testGetRfcRelationByReleaseId() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getRfcRelationByReleaseId(1));
    }

    @Test
    public void testGetClosedRfcRelationByCiId() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getClosedRfcCIByCiId(1));
    }

    @Test
    public void testGetRfcRelationsByNs() throws Exception {
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        rfcRelation.setNsPath("/test");
        mapper.createRfcRelation(rfcRelation);
        CmsRfcRelation rfcRelation2 = new CmsRfcRelation();
        rfcRelation2.setRfcId(2);
        rfcRelation2.setNsPath("/test2");
        mapper.createRfcRelation(rfcRelation2);

        List<CmsRfcRelation> relations = mapper.getRfcRelationsByNs("/test", true, "open");
        assertEquals(relations.size(), 1);
        assertEquals(relations.get(0), rfcRelation);
    }

    @Test
    public void testGetRfcRelationBy4() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getRfcRelationBy4(1, false, 1L, 1L));
    }

    @Test
    public void testGetOpenRfcRelationBy2() throws Exception {
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        rfcRelation.setNsPath("/test");
        rfcRelation.setRelationName("test");
        rfcRelation.setFromCiId(1L);
        rfcRelation.setToCiId(1L);
        mapper.createRfcRelation(rfcRelation);
        CmsRfcRelation rfcRelation2 = new CmsRfcRelation();
        rfcRelation2.setRfcId(2);
        rfcRelation2.setNsPath("/test2");
        rfcRelation2.setRelationName("test2");
        rfcRelation2.setFromCiId(2L);
        rfcRelation2.setToCiId(2L);
        mapper.createRfcRelation(rfcRelation2);
        List<CmsRfcRelation> relations = mapper.getOpenRfcRelationBy2(1L, 1L, "test", "test");
        assertEquals(relations.size(), 1);
        assertEquals(relations.get(0), rfcRelation); 
    }

    @Test
    public void testGetOpenFromRfcRelationByTargetClass() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenFromRfcRelationByTargetClass(1L, "", "", ""));
    }

    @Test
    public void testGetOpenFromRfcRelationByAttrs() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenFromRfcRelationByAttrs(1, "", "", "", null));
    }

    @Test
    public void testGetOpenToRfcRelationByTargetClass() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenToRfcRelationByTargetClass(1, "", "", ""));
    }

    @Test
    public void testGetOpenToRfcRelationByAttrs() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getOpenToRfcRelationByAttrs(1, "", "", "", null));
    }

    @Test
    public void testGetRfcRelationBy3() throws Exception {
        CmsRfcRelation rfcRelation = new CmsRfcRelation();
        rfcRelation.setRfcId(1);
        rfcRelation.setCiRelationId(1);
        rfcRelation.setNsPath("/test");
        rfcRelation.setRelationName("test");
        rfcRelation.setFromCiId(1L);
        rfcRelation.setToCiId(1L);
        mapper.createRfcRelation(rfcRelation);
        CmsRfcRelation rfcRelation2 = new CmsRfcRelation();
        rfcRelation2.setCiRelationId(2);
        rfcRelation2.setRfcId(2);
        rfcRelation2.setNsPath("/test2");
        rfcRelation2.setRelationName("test2");
        rfcRelation2.setFromCiId(2L);
        rfcRelation2.setToCiId(2L);
        mapper.createRfcRelation(rfcRelation2);
        List<CmsRfcRelation> relations = mapper.getRfcRelationBy3(1, true, 1L);
        assertEquals(relations.size(), 1);
        assertEquals(relations.get(0), rfcRelation);
    }

    @Test
    public void testGetAltNsBy() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> mapper.getAltNsBy(1));
    }


}