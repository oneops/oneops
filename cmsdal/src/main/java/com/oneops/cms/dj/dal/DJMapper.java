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
package com.oneops.cms.dj.dal;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.util.TimelineQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * The Interface DJMapper.
 */
public interface DJMapper {

  long getNextDjId();

  long getNextCiId();

  Integer getReleaseStateId(String stateName);

  Integer getRfcCiActionId(String stateName);

  void createRelease(CmsRelease release);

  void brushReleaseExecOrder(long releaseId);

  CmsRelease getReleaseById(long releaseId);

  List<CmsRelease> getReleaseBy3(@Param("nsPath") String nsPath,
      @Param("releaseName") String releaseName, @Param("releaseState") String releaseState);

  List<CmsRelease> getLatestRelease(@Param("nsPath") String nsPath,
      @Param("releaseState") String releaseState);

  int updateRelease(CmsRelease release);

  int deleteRelease(long releaseId);

  void commitRelease(@Param("releaseId") long releaseId, @Param("setDfValue") Boolean setDfValue,
      @Param("newCiState") Integer newCiState, @Param("delete4real") boolean delete4real,
      @Param("userId") String userId, @Param("desc") String desc);


  void createRfcCI(CmsRfcCI rfcCi);

  void createRfcLog(CmsRfcCI rfcCi);

  int rmRfcCIfromRelease(long rfcId);

  void updateRfcCI(CmsRfcCI rfcCI);

  void updateRfcLog(CmsRfcCI rfcCI);


  void insertRfcCIAttribute(CmsRfcAttribute attr);

  void updateRfcCIAttribute(CmsRfcAttribute attr);

  CmsRfcCI getRfcCIById(long rfcId);

  CmsRfcCI getOpenRfcCIByCiId(long ciId);
  CmsRfcCI getOpenRfcCIByCiIdNoAttrs(long ciId);

  List<CmsRfcCI> getOpenRfcCIByCiIdList(@Param("ciIds") List<Long> ciIds);

  List<CmsRfcCI> getRfcCIBy3(@Param("releaseId") long releaseId,
      @Param("isActive") Boolean isActive, @Param("ciId") Long ciId);

  List<CmsRfcCI> getRfcCIByReleaseAndClass(@Param("releaseId") long releaseId,
                                                 @Param("className") String className);

  List<CmsRfcCI> getRfcCIByClazzAndName(@Param("nsPath") String nsPath,
      @Param("clazzName") String clazzName, @Param("ciName") String ciName,
      @Param("isActive") Boolean isActive, @Param("state") String state);

  List<CmsRfcCI> getOpenRfcCIByClazzAndNameLower(@Param("nsPath") String nsPath,
      @Param("clazzName") String clazzName, @Param("ciName") String ciName);

  List<CmsRfcCI> getOpenRfcCIByNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike,
      @Param("clazzName") String clazzName, @Param("ciName") String ciName);

  List<CmsRfcCI> getOpenRfcCIByClazzAnd2Names(@Param("nsPath") String nsPath,
      @Param("clazzName") String clazzName, @Param("ciName") String ciName,
      @Param("altCiName") String altCiName);

  List<CmsRfcCI> getClosedRfcCIByCiId(long ciId);

  List<CmsRfcAttribute> getRfcCIAttributes(long rfcId);

  List<CmsRfcAttribute> getRfcCIAttributesByRfcIdList(@Param("rfcIds") Set<Long> rfcIds);

  void createRfcRelation(CmsRfcRelation rel);

  void createRfcRelationLog(CmsRfcRelation rel);

  int rmRfcRelationfromRelease(long rfcId);

  int updateRfcRelation(CmsRfcRelation rel);

  void updateRfcRelationLog(CmsRfcRelation rel);

  void insertRfcRelationAttribute(CmsRfcAttribute attr);

  void updateRfcRelationAttribute(CmsRfcAttribute attr);

  @Deprecated
  void upsertRfcRelationAttribute(CmsRfcAttribute attr);

  CmsRfcRelation getRfcRelationById(long rfcId);

  CmsRfcRelation getOpenRfcRelationByCiRelId(long ciRelationId);

  List<CmsRfcRelation> getRfcRelationByReleaseId(long releaseId);

  List<CmsRfcRelation> getClosedRfcRelationByCiId(long ciId);

  List<CmsRfcRelation> getRfcRelationsByNs(@Param("nsPath") String nsPath,
      @Param("isActive") Boolean isActive, @Param("state") String state);

  List<CmsRfcRelation> getRfcRelationBy4(@Param("releaseId") long releaseId,
      @Param("isActive") Boolean isActive, @Param("fromCiId") Long fromCiId,
      @Param("toCiId") Long toCiId);

  List<CmsRfcRelation> getOpenRfcRelationBy2(@Param("fromCiId") Long fromCiId,
      @Param("toCiId") Long toCiId, @Param("relName") String relName,
      @Param("shortRelName") String shortRelName);

  List<CmsRfcRelation> getOpenFromRfcRelationByTargetClass(@Param("fromCiId") long fromCiId,
      @Param("relName") String relName, @Param("shortRelName") String shortRelName,
      @Param("targetClassName") String targetClassName);

  List<CmsRfcRelation> getOpenFromRfcRelationByAttrs(
      @Param("fromCiId") long fromCiId,
      @Param("relName") String relName,
      @Param("shortRelName") String shortRelName,
      @Param("targetClassName") String targetClassName,
      @Param("attrList") List<CmsRfcBasicAttribute> attrList);


  List<CmsRfcRelation> getOpenToRfcRelationByTargetClass(@Param("toCiId") long toCiId,
      @Param("relName") String relName, @Param("shortRelName") String shortRelName,
      @Param("targetClassName") String targetClassName);

  List<CmsRfcRelation> getOpenToRfcRelationByAttrs(
      @Param("toCiId") long toCiId,
      @Param("relName") String relName,
      @Param("shortRelName") String shortRelName,
      @Param("targetClassName") String targetClassName,
      @Param("attrList") List<CmsRfcBasicAttribute> attrList);


  List<CmsRfcRelation> getRfcRelationBy3(@Param("releaseId") long releaseId,
      @Param("isActive") Boolean isActive, @Param("ciRelationId") Long ciRelationId);

  List<CmsRfcRelation> getOpenRfcRelations(@Param("relationName") String relationName,
      @Param("shortRelName") String shortRelName, @Param("nsPath") String nsPath,
      @Param("fromClazzName") String fromClazzName, @Param("toClazzName") String toClazzName);

  List<CmsRfcRelation> getOpenRfcRelationsNsLike(@Param("relationName") String relationName,
      @Param("shortRelName") String shortRelName, @Param("ns") String ns,
      @Param("nsLike") String nsLike, @Param("fromClazzName") String fromClazzName,
      @Param("toClazzName") String toClazzName);

  List<CmsRfcRelation> getOpenRfcRelationByCiIds(@Param("relationName") String relationName,
      @Param("shortRelName") String shortRelName,
      @Param("fromCiIds") List<Long> fromCiIds,
      @Param("toCiIds") List<Long> toCiIds);

  List<CmsRfcRelation> getRfcRelationByReleaseAndClass(@Param("releaseId") long releaseId,
      @Param("relationName") String relationName, @Param("shortRelName") String shortRelName);

  List<CmsRfcAttribute> getRfcRelationAttributes(long rfcId);

  List<CmsRfcAttribute> getRfcRelationAttributesByRfcIdList(@Param("rfcIds") Set<Long> rfcIds);

  List<Long> getLinkedRfcRelationId(@Param("releaseId") long releaseId,
      @Param("isActive") Boolean isActive, @Param("rfcId") long rfcId);

  long countCiRfcByReleaseId(long releaseId);
  long countRelationRfcByReleaseId(long releaseId);

  long countOpenRfcCisByNs(String nsPath);

  long countOpenRfcRelationsByNs(String nsPath);

  void rmRfcsByNs(String nsPath);

  void rmToRelByNs(String nsPath);

  void rmFromRelByNs(String nsPath);

  long countCiNotUpdatedByRfc(@Param("fromCiId") long fromCiId,
      @Param("relationName") String relationName,
      @Param("shortRelName") String shortRelName, @Param("rfcId") long rfcId);

  List<TimelineRelease> getReleasesByNsPath(TimelineQueryParam queryParam);

  List<TimelineRelease> getReleasesByCiFilter(TimelineQueryParam queryParam);

  List<TimelineRelease> getReleasesByRelationFilter(TimelineQueryParam queryParam);

  List<CmsRfcCI> getAppliedRfcCIsAfterRfcId(@Param("ciId") Long ciId, @Param("afterRfcId") Long afterRfcId, @Param("toRfcId") Long toRfcId);

  List<CmsRfcCI> getRfcCIsAppliedBetweenTwoReleases(@Param("nsPath") String nsPath,
      @Param("fromReleaseId") Long fromReleaseId, @Param("toReleaseId") Long toReleaseId);

  List<CmsRfcRelation> getRfcRelationsAppliedBetweenTwoReleases(@Param("nsPath") String nsPath,
      @Param("fromReleaseId") Long fromReleaseId, @Param("toReleaseId") Long toReleaseId);


  Long getTagId(@Param("tag")String tag);

  void createTag(@Param("tag")String tag);

  void createAltNs(@Param("nsId") long nsId, @Param("tagId") long tagId, @Param("rfcId") long rfcId);

  void deleteAltNs(@Param("nsId") long nsId, @Param("rfcId") long rfcId);

  List<CmsRfcCI> getRfcCIByAltNsAndTag(@Param("nsPath") String nsPath, @Param("tag") String tag,
      @Param("releaseId") Long releaseId, @Param("isActive") boolean isActive,
      @Param("ciId") Long ciId);

  List<CmsAltNs> getAltNsBy(@Param("rfcId") long rfcCI);

  List<Integer> getDeploymentDistinctStepsTotal(@Param("deploymentId") long deploymentId);
}
