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
package com.oneops.cms.cm.service;

import static com.oneops.cms.util.CmsConstants.BASE_REALIZED_AS;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.CmsException;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsProcessor;
import com.oneops.cms.util.CIValidationResult;
import com.oneops.cms.util.CmsCmValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.QueryConditionMapper;
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * The Class CmsCmProcessor.
 */
public class CmsCmProcessor {

	private Logger logger = Logger.getLogger(this.getClass());

	private static final int CHUNK_SIZE = 100;
	
	private CIMapper ciMapper;
	private DJMapper djMapper;
	private UtilMapper utilMapper;
	private CmsCmValidator cmValidator;
	private CmsNsProcessor cmsNsProcessor;
	private CmsMdProcessor mdProcessor;
	private QueryConditionMapper qcm = new QueryConditionMapper();
	private Set<String> bomRelations = new HashSet<>();

	public CmsCmProcessor() {
		bomRelations.add(BASE_REALIZED_AS);
	}

	/**
	 * Sets the md processor.
	 *
	 * @param mdProcessor the new md processor
	 */
	public void setMdProcessor(CmsMdProcessor mdProcessor) {
		this.mdProcessor = mdProcessor;
	}

	/**
	 * Sets the cm validator.
	 *
	 * @param cmValidator the new cm validator
	 */
	public void setCmValidator(CmsCmValidator cmValidator) {
		this.cmValidator = cmValidator;
	}


	public void setCmsNsProcessor(CmsNsProcessor cmsNsProcessor) {
		this.cmsNsProcessor = cmsNsProcessor;
	}

	/**
	 * Sets the ci mapper.
	 *
	 * @param ciMapper the new ci mapper
	 */
	public void setCiMapper(CIMapper ciMapper) {
		this.ciMapper = ciMapper;
	}
	
	/**
	 * 
	 * @param utilMapper
	 */
	public void setUtilMapper(UtilMapper utilMapper) {
		this.utilMapper = utilMapper;
	}

	/**
	 * Creates the ci.
	 *
	 * @param ci the ci
	 * @return the cms ci
	 */
	public CmsCI createCI(CmsCI ci) {
		
		
        List<CmsCI> existingCiList = getCiBy3NakedLower(ci.getNsPath(), ci.getCiClassName(), ci.getCiName());
        if (existingCiList.size()>0) {
        	CmsCI existingCi = existingCiList.get(0);
            String errMsg = "the ci of this ns/class/ci-name already exists " + existingCi.getNsPath() + ';' + existingCi.getCiClassName() + ';' + existingCi.getCiName();
            logger.error(errMsg);
            throw new CmsException(CmsError.CMS_CI_OF_NS_CLASS_NAME_ALREADY_EXIST_ERROR, errMsg);
        }
		
		CIValidationResult validation = cmValidator.validateNewCI(ci);
		
		if (!validation.isValidated()) {
			throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, validation.getErrorMsg());
		}
		
		if (ci.getCiId() == 0) {
			ci.setCiId(ciMapper.getNextCmId());
		}

		if (ci.getCiGoid() == null) {
			ci.setCiGoid(String.valueOf(ci.getNsId()) + '-' + String.valueOf(ci.getCiClassId()) + '-' + String.valueOf(ci.getCiId()));
		}
		
		ci.setCiStateId(100);
		
		ciMapper.createCI(ci);
		for (CmsCIAttribute attr : ci.getAttributes().values()) {
			attr.setCiId(ci.getCiId());
			ciMapper.addCIAttribute(attr);
		}
		
		if (validation.isNeedNScreation()) {
			CmsNamespace ns = new CmsNamespace();
			
			if (ci.getNsPath().length() > 1) {
				if (validation.getUseClassNameInNS()) {
					ns.setNsPath(ci.getNsPath() + "/" + ci.getCiClassName() + "/" + ci.getCiName());
				} else {
					ns.setNsPath(ci.getNsPath() + "/" + ci.getCiName());
				}
			} else {
				if (validation.getUseClassNameInNS()) {
					ns.setNsPath(ci.getNsPath() + ci.getCiClassName() + "/"+ ci.getCiName());
				} else {	
					ns.setNsPath(ci.getNsPath() + ci.getCiName());
				}
			}
			cmsNsProcessor.createNs(ns);
		}

		return getCiById(ci.getCiId());
	}
	
	/**
	 * Gets the ci by goid.
	 *
	 * @param goid the goid
	 * @return the ci by goid
	 */
	public CmsCI getCiByGoid(String goid) {
		CmsCI ci = ciMapper.getCIbyGoid(goid);
		populateAttrs(ci);
		return ci;
	}
	
	public void populateRelCis(List<CmsCIRelation> rels, boolean fromCis, boolean toCis) {
		populateRelCisLocal(rels,  fromCis, toCis, true);
	}

	public void populateRelCisNoAttrs(List<CmsCIRelation> rels, boolean fromCis, boolean toCis) {
		populateRelCisLocal(rels,  fromCis, toCis, false);
	}

	private void populateRelCisLocal(List<CmsCIRelation> rels, boolean fromCis, boolean toCis, boolean populateAttrs) {
		if (rels.size() == 0) {
			return;
		}

		Set<Long> ids = new HashSet<>();
		if (fromCis) {
			ids.addAll(rels.stream().map(CmsCIRelation::getFromCiId).collect(Collectors.toList()));
		}
		if (toCis) {
			ids.addAll(rels.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toList()));
		}
		Map<Long, CmsCI> ciMap = getCiByIdListLocal(new ArrayList<>(ids), populateAttrs).stream()
				.collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));
		for (CmsCIRelation rel : rels) {
			if (fromCis) {
				rel.setFromCi(ciMap.get(rel.getFromCiId()));
			}
			if (toCis) {
				rel.setToCi(ciMap.get(rel.getToCiId()));
			}
		}
	}

	private void populateAttrs(CmsCI ci) {
		if (ci != null) {
			CmsClazz clazz = mdProcessor.getClazz(ci.getCiClassId());
			Map<Integer, CmsClazzAttribute> clazzAttrs = getClazzAttrsMap(clazz.getMdAttributes()); 
			//first lets populate the attr names for ci attrs
			for (CmsCIAttribute attr : ciMapper.getCIAttrsNaked(ci.getCiId())) {
				attr.setAttributeName(clazzAttrs.get(attr.getAttributeId()).getAttributeName());
				ci.addAttribute(attr);
				clazzAttrs.remove(attr.getAttributeId());
			}
			// now populate default values from missing attrs
			for (CmsClazzAttribute clazzAttr : clazzAttrs.values()) {
				CmsCIAttribute attr = new CmsCIAttribute();
				attr.setAttributeId(clazzAttr.getAttributeId());
				attr.setAttributeName(clazzAttr.getAttributeName());
				attr.setCiId(ci.getCiId());
				attr.setDfValue(clazzAttr.getDefaultValue());
				attr.setDjValue(clazzAttr.getDefaultValue());
				ci.addAttribute(attr);
			}
		}
	}

	private void populateAttrsSimple(List<CmsCI> cis) {
		if (cis.size() == 0) {
			return;
		}
		List<Long> ciIds = new ArrayList<Long>();
		Map<Long,List<CmsCIAttribute>> attrMap = new HashMap<Long,List<CmsCIAttribute>>();
		for (CmsCI ci : cis) {
			ciIds.add(ci.getCiId());
			attrMap.put(ci.getCiId(), new ArrayList<CmsCIAttribute>());
		}
		List<CmsCIAttribute> allAttrs = ciMapper.getCIAttrsNakedByCiIdList(ciIds);
		//convert to map
		for (CmsCIAttribute attr: allAttrs) {
			attrMap.get(attr.getCiId()).add(attr);
		}
		Map<Integer, CmsClazz> clazzes = new HashMap<Integer, CmsClazz>();
		for (CmsCI ci : cis) {
			CmsClazz clazz = null;
			if (!clazzes.containsKey(ci.getCiClassId())) {
				clazz = mdProcessor.getClazz(ci.getCiClassId());
				clazzes.put(clazz.getClassId(), clazz);
			} else {
				clazz = clazzes.get(ci.getCiClassId());
			}
			Map<Integer, CmsClazzAttribute> clazzAttrs = getClazzAttrsMap(clazz.getMdAttributes()); 
			//first lets populate the attr names for ci attrs
			if (attrMap.containsKey(ci.getCiId())) {
				for (CmsCIAttribute attr : attrMap.get(ci.getCiId())) {
					attr.setAttributeName(clazzAttrs.get(attr.getAttributeId()).getAttributeName());
					ci.addAttribute(attr);
					clazzAttrs.remove(attr.getAttributeId());
				}
			}
			// now populate default values from missing attrs
			for (CmsClazzAttribute clazzAttr : clazzAttrs.values()) {
				CmsCIAttribute attr = new CmsCIAttribute();
				attr.setAttributeId(clazzAttr.getAttributeId());
				attr.setAttributeName(clazzAttr.getAttributeName());
				attr.setCiId(ci.getCiId());
				attr.setDfValue(clazzAttr.getDefaultValue());
				attr.setDjValue(clazzAttr.getDefaultValue());
				ci.addAttribute(attr);
			}
		}
	}
	
	
    private void populateAttrs(List<CmsCI> cis) {
		int fromIndex = 0;
		int toIndex = cis.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : cis.size();
		List<CmsCI> subList = cis.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			populateAttrsSimple(subList);
			fromIndex += CHUNK_SIZE;
			toIndex = cis.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : cis.size();
			subList = cis.subList(fromIndex, toIndex);
		}
		if (subList.size() >0) {
			populateAttrsSimple(subList);
		}
    }

	
	private Map<Integer, CmsClazzAttribute> getClazzAttrsMap(List<CmsClazzAttribute> attrs) {
		Map<Integer, CmsClazzAttribute> clazzAttrsMap = new HashMap<Integer, CmsClazzAttribute>();
		for (CmsClazzAttribute attr : attrs) {
			clazzAttrsMap.put(attr.getAttributeId(), attr);
		}
		return clazzAttrsMap;
	}

	private void populateRelAttrs(CmsCIRelation rel) {
		if (rel != null) {
			CmsRelation mdRel = mdProcessor.getRelation(rel.getRelationId());

			Map<Integer, CmsRelationAttribute> relAttrs = getRelAttrsMap(mdRel.getMdAttributes()); 
			//first lets populate the attr names for ci attrs
			for (CmsCIRelationAttribute attr : ciMapper.getCIRelationAttrsNaked(rel.getCiRelationId())) {
				attr.setAttributeName(relAttrs.get(attr.getAttributeId()).getAttributeName());
				rel.addAttribute(attr);
				relAttrs.remove(attr.getAttributeId());
			}
			// now populate default values from missing attrs
			for (CmsRelationAttribute mdAttr : relAttrs.values()) {
				CmsCIRelationAttribute attr = new CmsCIRelationAttribute();
				attr.setAttributeId(mdAttr.getAttributeId());
				attr.setAttributeName(mdAttr.getAttributeName());
				attr.setCiRelationId(rel.getCiRelationId());
				attr.setDfValue(mdAttr.getDefaultValue());
				attr.setDjValue(mdAttr.getDefaultValue());
				rel.addAttribute(attr);
			}
		}
	}
	
	private void populateRelAttrsSimple(List<CmsCIRelation> rels) {

		if (rels.size() == 0) {
			return;
		}
		
		List<Long> ciRelIds = new ArrayList<Long>();
		Map<Long,List<CmsCIRelationAttribute>> attrMap = new HashMap<Long,List<CmsCIRelationAttribute>>();
		for (CmsCIRelation rel : rels) {
			ciRelIds.add(rel.getCiRelationId());
			attrMap.put(rel.getCiRelationId(), new ArrayList<CmsCIRelationAttribute>());

		}
		
		List<CmsCIRelationAttribute> allAttrs = ciMapper.getCIRelationAttrsNakedByRelIdList(ciRelIds);
		
		//convert to map
		for (CmsCIRelationAttribute attr: allAttrs) {
			attrMap.get(attr.getCiRelationId()).add(attr);
		}
		
		Map<Integer, CmsRelation> mdRels = new HashMap<Integer, CmsRelation>();
		for (CmsCIRelation rel : rels) {
			CmsRelation mdRel = null;
			if (!mdRels.containsKey(rel.getRelationId())) {
				mdRel = mdProcessor.getRelation(rel.getRelationId());
				mdRels.put(rel.getRelationId(), mdRel);
			} else {
				mdRel = mdRels.get(rel.getRelationId());
			}
			Map<Integer, CmsRelationAttribute> relAttrs = getRelAttrsMap(mdRel.getMdAttributes()); 
			//first lets populate the attr names for ci attrs
			for (CmsCIRelationAttribute attr : attrMap.get(rel.getCiRelationId())) {
				attr.setAttributeName(relAttrs.get(attr.getAttributeId()).getAttributeName());
				rel.addAttribute(attr);
				relAttrs.remove(attr.getAttributeId());
			}
			// now populate default values from missing attrs
			for (CmsRelationAttribute mdAttr : relAttrs.values()) {
				CmsCIRelationAttribute attr = new CmsCIRelationAttribute();
				attr.setAttributeId(mdAttr.getAttributeId());
				attr.setAttributeName(mdAttr.getAttributeName());
				attr.setCiRelationId(rel.getCiRelationId());
				attr.setDfValue(mdAttr.getDefaultValue());
				attr.setDjValue(mdAttr.getDefaultValue());
				rel.addAttribute(attr);
			}
		}
	}
	
    private void populateRelAttrs(List<CmsCIRelation> rels) {
		int fromIndex = 0;
		int toIndex = rels.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : rels.size();
		List<CmsCIRelation> subList = rels.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			populateRelAttrsSimple(subList);
			fromIndex += CHUNK_SIZE;
			toIndex = rels.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : rels.size();
			subList = rels.subList(fromIndex, toIndex);
		}
		if (subList.size() >0) {
			populateRelAttrsSimple(subList);
		}
    }

	
	private Map<Integer, CmsRelationAttribute> getRelAttrsMap(List<CmsRelationAttribute> attrs) {
		Map<Integer, CmsRelationAttribute> clazzAttrsMap = new HashMap<Integer, CmsRelationAttribute>();
		for (CmsRelationAttribute attr : attrs) {
			clazzAttrsMap.put(attr.getAttributeId(), attr);
		}
		return clazzAttrsMap;
	}

	@SuppressWarnings("unused")
	private void populateAttrsNoMd(CmsCI ci) {
		if (ci != null) {
			for (CmsCIAttribute attr : ciMapper.getCIAttrs(ci.getCiId())) {
				ci.addAttribute(attr);
			}
		}
	}
	
	/**
	 * Gets the ci by3.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the ci by3
	 */
	public List<CmsCI> getCiBy3(String ns, String clazzName, String ciName) {
		List<CmsCI> ciList = getCiBy3Naked(ns, clazzName, ciName);
		populateAttrs(ciList);
		return ciList;
	}

	/**
	 * Gets the ci by3lower.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the ci by3lower
	 */
	public List<CmsCI> getCiBy3lower(String ns, String clazzName, String ciName) {
		List<CmsCI> ciList = getCiBy3NakedLower(ns, clazzName, ciName);
		populateAttrs(ciList);
		return ciList;
	}
	
	/**
	 * Gets the ci by3 ns like.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the ci by3 ns like
	 */
	public List<CmsCI> getCiBy3NsLike(String ns, String clazzName, String ciName) {

		String nsLike = CmsUtil.likefyNsPath(ns);

		CiClassNames names = parseClassName(clazzName);
		
		List<CmsCI> ciList = ciMapper.getCIby3NsLike(ns,nsLike, names.className, names.shortClassName, ciName);
		populateAttrs(ciList);
		return ciList;
	}
	
	/**
	 * Gets the ci by state ns like no attrs.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param state - ci state
	 * @return the ci by3 ns like
	 */
	public List<CmsCI> getCiByNsLikeByStateNaked(String ns, String clazzName, String state) {
		String nsLike = CmsUtil.likefyNsPath(ns);
		List<CmsCI> ciList = ciMapper.getCIbyStateNsLike(ns, nsLike, clazzName, state);
		return ciList;
	}
	
	/**
	 * Gets the ci by state ns like no attrs.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param state - ci state
	 * @return the ci by3 ns like
	 */
	public List<CmsCI> getCiByNsLikeByState(String ns, String clazzName, String state) {
		List<CmsCI> ciList = getCiByNsLikeByStateNaked(ns, clazzName, state);
		populateAttrs(ciList);
		return ciList;
	}
	
	/**
	 * Gets the ci by name.
	 *
	 * @param name the name
	 * @param oper the oper
	 * @return the ci by name
	 */
	public List<CmsCI> getCiByName(String name, String oper) {
		List<CmsCI> ciList = ciMapper.getCiByName(name, oper);
		return ciList;
	}
	
	
	/**
	 * Gets the ci by3 naked.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the ci by3 naked
	 */
	public List<CmsCI> getCiBy3Naked(String ns, String clazzName, String ciName) {
		
		CiClassNames names = parseClassName(clazzName);

		return ciMapper.getCIby3(ns, names.className, names.shortClassName, ciName);
	}

	/**
	 * Gets the ci by3 naked lower.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the ci by3 naked lower
	 */
	public List<CmsCI> getCiBy3NakedLower(String ns, String clazzName, String ciName) {

		CiClassNames names = parseClassName(clazzName);
		
		return ciMapper.getCIby3lower(ns, names.className, names.shortClassName, ciName);
	}
	
	/**
	 * Gets the ci by3with2 names naked.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param altName the alt name
	 * @return the ci by3with2 names naked
	 */
	public List<CmsCI> getCiBy3with2NamesNaked(String ns, String clazzName, String ciName, String altName) {
		return ciMapper.getCIby3with2Names(ns, clazzName, ciName, altName);
	}
	
	/**
	 * Gets the ci by3with2 names.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param altName the alt name
	 * @return the ci by3with2 names
	 */
	public List<CmsCI> getCiBy3with2Names(String ns, String clazzName, String ciName, String altName) {
		List<CmsCI> ciList = ciMapper.getCIby3with2Names(ns, clazzName, ciName, altName);
		populateAttrs(ciList);
		return ciList;
	}

	
	/**
	 * Gets the ci by attributes.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param attrs the attrs
	 * @param recursive the recursive
	 * @return the ci by attributes
	 */
	public List<CmsCI> getCiByAttributes(String ns, String clazzName, List<AttrQueryCondition> attrs, boolean recursive) {
		
		qcm.convertConditions(attrs);

		CiClassNames names = parseClassName(clazzName);
		List<CmsCI> ciList = null;
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(ns);
			ciList = ciMapper.getCIbyAttributesNsLike(ns, nsLike, names.className, names.shortClassName, attrs);
		} else {
			ciList = ciMapper.getCIbyAttributes(ns, names.className, names.shortClassName, attrs);
		}
		populateAttrs(ciList);
		return ciList;
	}


	/**
	 * Gets the ci by attributes.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param attrs the attrs
	 * @param recursive the recursive
	 * @return the ci by attributes
	 */
	public List<CmsCI> getCiByAttributes(String ns, String clazzName, List<AttrQueryCondition> attrs, boolean recursive, String altNs, String tag) {

		qcm.convertConditions(attrs);

		CiClassNames names = parseClassName(clazzName);
		List<CmsCI> ciList = null;
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(ns);
			ciList = ciMapper.getCIbyAttributesNsLikeWithAltNs(ns, nsLike, names.className, names.shortClassName, attrs, altNs, tag);
		} else {
			ciList = ciMapper.getCIbyAttributesWithAltNs(ns, names.className, names.shortClassName, attrs, altNs, tag);
		}
		populateAttrs(ciList);
		return ciList;
	}

	/**
	 * Gets the ci by id list naked.
	 *
	 * @param ids the ids
	 * @return the ci by id list naked
	 */
	public List<CmsCI> _getCiByIdListNaked(List<Long> ids) {
		return ciMapper.getCIByIdList(ids);
	}

	/**
	 * Gets the ci by id list naked.
	 *
	 * @param ids the ids
	 * @return the ci by id list naked
	 */
	public List<CmsCI> getCiByIdListNaked(List<Long> ids) {
		return getCiByIdListLocal(ids, false);
	}	

	private List<CmsCI> getCiByIdListLocal(List<Long> ids, boolean populateAttrs) {
		List<CmsCI> cis = new ArrayList<CmsCI>();
		
		if (ids == null || ids.size() ==0) {
			return cis;
		}
		
		int fromIndex = 0;
		int toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
		List<Long> subList = ids.subList(fromIndex, toIndex);
		while (subList.size() == CHUNK_SIZE) {
			List<CmsCI> ciChank = ciMapper.getCIByIdList(subList);
			if (populateAttrs) {
				populateAttrsSimple(ciChank);
			}
			cis.addAll(ciChank);
			fromIndex += CHUNK_SIZE;
			toIndex = ids.size() > (fromIndex + CHUNK_SIZE) ? fromIndex + CHUNK_SIZE : ids.size();
			subList = ids.subList(fromIndex, toIndex);
		}
		if (subList.size()>0) {
			List<CmsCI> ciChank = ciMapper.getCIByIdList(subList);
			if (populateAttrs) {
				populateAttrsSimple(ciChank);
			}
			cis.addAll(ciChank);
		}
		return cis;
	}
	
	
	/**
	 * Gets the ci by id list.
	 *
	 * @param ids the ids
	 * @return the ci by id list
	 */
	public List<CmsCI> getCiByIdList(List<Long> ids) {
		return getCiByIdListLocal(ids, true);
	}	
	/**
	 * Gets the ci by id.
	 *
	 * @param id the id
	 * @return the ci by id
	 */
	public CmsCI getCiById(long id) {
		CmsCI ci = ciMapper.getCIById(id);
		populateAttrs(ci);
		return ci;
	}

	/**
	 * Gets the ci by id naked.
	 *
	 * @param id the id
	 * @return the ci by id naked
	 */
	public CmsCI getCiByIdNaked(long id) {
		return ciMapper.getCIById(id);
	}


	public void updateAllBomPeerCIs(CmsCI ci, long manifestCiId) {
		List<CmsCIRelation> realizedAsRelations = getFromCIRelations(manifestCiId, BASE_REALIZED_AS, ci.getCiClassName());
		for (CmsCIRelation relation : realizedAsRelations) {
			CmsCI bomCi = relation.getToCi();
			bomCi.setAttributes(ci.getAttributes());
			updateCI(bomCi);
		}
	}
	
	/**
	 * Update ci.
	 *
	 * @param ci the ci
	 * @return the cms ci
	 */
	public CmsCI updateCI(CmsCI ci) {
		CmsCI existingCi = getCiById(ci.getCiId());

		if (existingCi == null) {
			throw new CIValidationException(CmsError.VALIDATION_COULDNT_FIND_CI_FOR_UPDATE_ERROR,
                                            "Could not find the ci to update. ci_id = " + ci.getCiId());
		}
		
		CIValidationResult validation = cmValidator.validateUpdateCI(ci);
		if (!validation.isValidated()) {
			throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, validation.getErrorMsg());
		}

		if (ci.getCiState() != null && !ci.getCiState().equals(existingCi.getCiState())) {
			Integer ciStateId = ciMapper.getCiStateId(ci.getCiState());
			if (ciStateId == null) {
				throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, "There is no such ci state defined - " + ci.getCiState());
			}
			ci.setCiStateId(ciStateId);
		} else if (ci.getCiState() == null && ci.getCiStateId() == 0 
				&& existingCi.getCiStateId() > 0) {
			logger.info("incoming update request ci :"+ci.getCiId()+ " for state id= " + ci.getCiStateId()
					+ " existing ci state id = " + existingCi.getCiStateId());
			ci.setCiStateId(existingCi.getCiStateId());
		} else if (ci.getCiStateId() == 0) {
			ci.setCiStateId(100);
		}

		boolean ciChanged = !cmValidator.cisEqual(existingCi, ci);
		
		for(CmsCIAttribute updAttr : ci.getAttributes().values()){
			updAttr.setCiId(ci.getCiId());
			CmsCIAttribute existingAttr = existingCi.getAttribute(updAttr.getAttributeName());
			if (existingAttr == null || existingAttr.getCiAttributeId() == 0) {
				ciMapper.addCIAttributeAndPublish(updAttr);
			} else {
				if (!cmValidator.attrsEqual(existingAttr, updAttr, updAttr.getOwner() != null)) {
					updAttr.setCiAttributeId(existingAttr.getCiAttributeId());
					ciMapper.updateCIAttribute(updAttr);
					ciChanged = true;
				}
			}
		}
		
		if (ciChanged) {
			ciMapper.updateCI(ci);
		}
		return getCiById(ci.getCiId());
	}
	
	/**
	 * Update ci state.
	 *
	 * @param ciId, String new ciState
	 * @return the cms ci
	 */
	public CmsCI updateCiState(long ciId, String ciState, String user) {
		
		CmsCI existingCi = ciMapper.getCIById(ciId);

		if (existingCi == null) {
			throw new CIValidationException(CmsError.VALIDATION_COULDNT_FIND_CI_FOR_UPDATE_ERROR,
                                            "Could not find the ci to update. ci_id = " + ciId);
		}

		Integer ciStateId = ciMapper.getCiStateId(ciState);
		if (ciStateId == null) {
			throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, "There is no such ci state defined - " + ciState);
		}
		existingCi.setCiStateId(ciStateId);
		existingCi.setUpdatedBy(user);
		ciMapper.updateCI(existingCi);

		return existingCi;
	}
	

	
	
	/**
	 * Delete ci.
	 *
	 * @param ciId the ci id
	 */
	public void deleteCI(long ciId, String userId) {
		deleteCI(ciId, true, userId);
	}

	/**
	 * Delete ci.
	 *
	 * @param ciId the ci id
	 * @param delete4real the delete4real
	 */
	public void deleteCI(long ciId, boolean delete4real, String userId) {
		ciMapper.deleteCI(ciId, delete4real, userId);
	}
	

	/**
	 * Creates the relation.
	 *
	 * @param relation the relation
	 * @return the cms ci relation
	 */
	public CmsCIRelation createRelation(CmsCIRelation relation) {

		CmsCI fromCi = relation.getFromCi();
		CmsCI newFromCi = null;
		if (fromCi != null) {
			fromCi.setCreatedBy(relation.getCreatedBy());
			newFromCi = createCI(fromCi);
			relation.setFromCiId(newFromCi.getCiId());
		}
		
		CmsCI toCi = relation.getToCi();
		CmsCI newToCi = null;
		if (toCi != null) {
			toCi.setCreatedBy(relation.getCreatedBy());
			newToCi = createCI(toCi);
			relation.setToCiId(newToCi.getCiId());
		}

		CmsCIRelation newRel = createRelationSimple(relation);
		if (newFromCi != null) newRel.setFromCi(newFromCi);
		if (newToCi != null) newRel.setToCi(newToCi);
		return newRel;
	
	}
	
	private CmsCIRelation createRelationSimple(CmsCIRelation relation) {

		CIValidationResult validation = cmValidator.validateRelation(relation);
		
		if (!validation.isValidated()) {
			logger.debug(validation.getErrorMsg());
			throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, validation.getErrorMsg());
		}

		
		if (relation.getCiRelationId() == 0) {
			relation.setCiRelationId(ciMapper.getNextCmId());
		}

		if (relation.getRelationGoid() == null) {
			relation.setRelationGoid(String.valueOf(relation.getFromCiId()) + '-' + String.valueOf(relation.getRelationId()) + '-' +String.valueOf(relation.getToCiId()));
		}
		
		ciMapper.createRelation(relation);
		
		for (CmsCIRelationAttribute attr : relation.getAttributes().values()) {
			//assuming attrs get populated with 
			attr.setCiRelationId(relation.getCiRelationId());
			ciMapper.addRelationAttribute(attr);
		}
		

		return getRelationById(relation.getCiRelationId());
	}

	/**
	 * Gets the relation by id.
	 *
	 * @param relId the rel id
	 * @return the relation by id
	 */
	public CmsCIRelation getRelationById(long relId) {
		CmsCIRelation rel = ciMapper.getCIRelation(relId);
		if (rel != null) {
			populateRelAttrs(rel);
		}
		return rel;
	}

	/**
	 * Gets the from ci relations.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param toClazzName the to clazz name
	 * @return the from ci relations
	 */
	public List<CmsCIRelation> getFromCIRelations(long fromId,
			String relationName, String toClazzName) {
		return getFromCIRelationsLocal(fromId,relationName, null, toClazzName);
	}

	/**
	 * Gets the from ci relations.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @return the from ci relations
	 */
	public List<CmsCIRelation> getFromCIRelations(long fromId,
			String relationName, String shortRelName,String toClazzName) {
		return getFromCIRelationsLocal(fromId, relationName, shortRelName, toClazzName);
	}

	/**
	 * Gets the from ci relations by multiple names
	 *
	 * @param fromId the from id
	 * @param relationNames the relation names
	 * @param shortRelNames the short relation names
	 * @return the from ci relations
	 */
	public Map<String, List<CmsCIRelation>> getFromCIRelationsByMultiRelationNames(long fromId, List<String> relationNames, List<String> shortRelNames) {
		List<CmsCIRelation> relations = getFromCIRelationsLocal(fromId, relationNames, shortRelNames);
		Map<String, List<CmsCIRelation>> relationsMap;
		if (relations != null) {
			relationsMap = relations.stream().collect(Collectors.groupingBy(CmsCIRelation::getRelationName));
		}
		else {
			relationsMap = Collections.emptyMap();
		}
		return relationsMap;
	}

	/**
	 * Gets the specific CI Relation using name and class
	 * @param fromId the ciId of which the relation needs to be found from 
	 * @param relationName  relationName (eg. manifest.WatchedBy)
	 * @param shortRelName short relation Name (watchedby)
	 * @param toClazzName className (manifest.compute)
	 * @param toCiName (name of the class )
	 * @return the list of relations found.
	 */
	public List<CmsCIRelation> getFromCIRelationsByClassAndCiName(long fromId,
			String relationName, String shortRelName,String toClazzName,String toCiName) {
		List<CmsCIRelation> relList = ciMapper.getFromCIRelationsByToClassAndCiName(fromId, relationName, shortRelName, toClazzName,toCiName);
		populateRelAttrs(relList);
		if(logger.isDebugEnabled()){
			logger.debug(" got "+relList.size()+" for  r:"+relationName+":shortRelName"+shortRelName+":toClazzName"+toClazzName+":toCiName"+toCiName);
		}
		return relList;
	}

	/**
	 * Gets a list of 'from' CIs for relations with a given 'to' ciId.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @return list of CIs
	 */
	public List<CmsCI> getFromCIs(long toId, String relationName, String shortRelName, String toClazzName) {
		CiClassNames names = parseClassName(toClazzName);
		List<Long> ids = ciMapper.getToCIRelations(toId, relationName, shortRelName, names.className, names.shortClassName).stream()
				.map(CmsCIRelation::getFromCiId).collect(Collectors.toList());
		return getCiByIdList(ids);
	}

	/**
	 * Gets a list of 'to' CIs for relations with a given 'from' ciId.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @return list of CIs
	 */
	public List<CmsCI> getToCIs(long fromId, String relationName, String shortRelName, String toClazzName) {
		CiClassNames names = parseClassName(toClazzName);
		List<Long> ids = ciMapper.getFromCIRelations(fromId, relationName, shortRelName, names.className, names.shortClassName).stream()
				.map(CmsCIRelation::getToCiId).collect(Collectors.toList());
		return getCiByIdList(ids);
	}

	private List<CmsCIRelation> getFromCIRelationsLocal(long fromId,
			String relationName, String shortRelName, String toClazzName) {

		List<CmsCIRelation> relList = getFromCIRelationsNakedLocal(fromId, relationName, shortRelName,toClazzName);

		populateRelCis(relList, false, true);
		/*
		for (CmsCIRelation rel : relList) {
			rel.setToCi(getCiById(rel.getToCiId()));
		}
		*/
		return relList;
	}

	private List<CmsCIRelation> getFromCIRelationsLocal(long fromId, List<String> relationNames, List<String> shortRelNames) {
		List<CmsCIRelation> relList = getFromCIRelationsNakedLocal(fromId, relationNames, shortRelNames);
		populateRelCis(relList, false, true);
		return relList;
	}

	/**
	 * Gets the from ci relations by to ci ids.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toCiIds the to ci ids
	 * @return the from ci relations by to ci ids
	 */
	public List<CmsCIRelation> getFromCIRelationsByToCiIds(long fromId, String relationName,
			String shortRelName, List<Long> toCiIds) {
		
		List<CmsCIRelation> relList = getFromCIRelationsByToCiIdsNaked(fromId, relationName, shortRelName, toCiIds);
		
		populateRelCis(relList, false, true);
		/*
		for (CmsCIRelation rel : relList) {
			rel.setToCi(getCiById(rel.getToCiId()));
		}
		*/
		return relList;
	}

	
	/**
	 * Gets the from ci relations naked.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param toClazzName the to clazz name
	 * @return the from ci relations naked
	 */
	public List<CmsCIRelation> getFromCIRelationsNaked(long fromId,
			String relationName, String toClazzName) {
		return getFromCIRelationsNakedLocal(fromId, relationName, null, toClazzName);
	}
	
	/**
	 * Gets the from ci relations naked.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @return the from ci relations naked
	 */
	public List<CmsCIRelation> getFromCIRelationsNaked(long fromId, String relationName,
			String shortRelName, String toClazzName) {
		return getFromCIRelationsNakedLocal(fromId, relationName, shortRelName, toClazzName);
	}
	
	/**
	 * Gets the from ci relations by to ci ids naked.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toCiIds the to ci ids
	 * @return the from ci relations by to ci ids naked
	 */
	public List<CmsCIRelation> getFromCIRelationsByToCiIdsNaked(long fromId, String relationName,
			String shortRelName, List<Long> toCiIds) {
		
		List<CmsCIRelation> relList = ciMapper.getFromCIRelationsByToCiIDs(fromId, relationName, shortRelName, toCiIds);
		populateRelAttrs(relList);
		return relList;
	}

	/**
	 * Gets relations by name and toCi ids naked, no attributes.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromCiIds the to ci ids
	 * @return the from ci relations by to ci ids naked
	 */
	public List<CmsCIRelation> getCIRelationsByFromCiIdsNakedNoAttrs(String relationName,
			String shortRelName, List<Long> fromCiIds) {
		if (fromCiIds==null || fromCiIds.size()==0) return new ArrayList<>();
		return ciMapper.getCIRelationsByFromCiIDs(relationName, shortRelName, fromCiIds);
	}

	/**
	 * Gets relations by name and toCi ids naked, no attributes.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toCiIds the to ci ids
	 * @return the from ci relations by to ci ids naked
	 */
	public List<CmsCIRelation> getCIRelationsByToCiIdsNakedNoAttrs(String relationName,
			String shortRelName, List<Long> toCiIds) {

		return ciMapper.getCIRelationsByToCiIDs(relationName, shortRelName, toCiIds);
	}

	private List<CmsCIRelation> getFromCIRelationsNakedLocal(long fromId,
			String relationName, String shortRelName, String toClazzName) {
		
		CiClassNames names = parseClassName(toClazzName);
		
		List<CmsCIRelation> relList = ciMapper.getFromCIRelations(fromId, relationName, shortRelName, names.className, names.shortClassName);
		populateRelAttrs(relList);
		return relList;
	}

	private List<CmsCIRelation> getFromCIRelationsNakedLocal(long fromId, List<String> relationNames, List<String> shortRelNames) {
		List<CmsCIRelation> relList = ciMapper.getFromCIRelationsByMultiRelationNames(fromId, relationNames, shortRelNames);
		populateRelAttrs(relList);
		return relList;
	}

	
	/**
	 * Gets the from ci relations by attrs.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @param attrs the attrs
	 * @return the from ci relations by attrs
	 */
	public List<CmsCIRelation> getFromCIRelationsByAttrs(long fromId,
			String relationName, String shortRelName, String toClazzName, List<AttrQueryCondition> attrs) {
		
		List<CmsCIRelation> relList = getFromCIRelationsByAttrsNaked(fromId, relationName, shortRelName,toClazzName, attrs);

		populateRelCis(relList, false, true);
		
		/*
		for (CmsCIRelation rel : relList) {
			rel.setToCi(getCiById(rel.getToCiId()));
		}
		*/
		return relList;

	
	}	

	
	/**
	 * Gets the from ci relations by attrs naked.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @param attrs the attrs
	 * @return the from ci relations by attrs naked
	 */
	public List<CmsCIRelation> getFromCIRelationsByAttrsNaked(long fromId,
			String relationName, String shortRelName, String toClazzName, List<AttrQueryCondition> attrs) {
		
		qcm.convertConditions(attrs);

		List<CmsCIRelation> relList = ciMapper.getFromCIRelationsByAttrs(fromId, relationName, shortRelName, toClazzName, attrs);
		populateRelAttrs(relList);
		return relList;
	}
	
	/**
	 * Gets the to ci relations by attrs.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param attrs the attrs
	 * @return the to ci relations by attrs
	 */
	public List<CmsCIRelation> getToCIRelationsByAttrs(long toId,
			String relationName, String shortRelName, String fromClazzName, List<AttrQueryCondition> attrs) {
		
		List<CmsCIRelation> relList = getToCIRelationsByAttrsNaked(toId, relationName, shortRelName,fromClazzName, attrs);

		populateRelCis(relList, true, false);
		/*
		for (CmsCIRelation rel : relList) {
			rel.setFromCi(getCiById(rel.getFromCiId()));
		}
		*/
		return relList;
	}	
	
	/**
	 * Gets the to ci relations by attrs naked.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param attrs the attrs
	 * @return the to ci relations by attrs naked
	 */
	public List<CmsCIRelation> getToCIRelationsByAttrsNaked(long toId,
			String relationName, String shortRelName, String fromClazzName, List<AttrQueryCondition> attrs) {
		
		qcm.convertConditions(attrs);
		
		List<CmsCIRelation> relList = ciMapper.getToCIRelationsByAttrs(toId, relationName, shortRelName, fromClazzName, attrs);
		populateRelAttrs(relList);
		return relList;
	}
	

	/**
	 * Gets the from ci relations naked no attrs.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @return the from ci relations naked no attrs
	 */
	public List<CmsCIRelation> getFromCIRelationsNakedNoAttrs(long fromId,
			String relationName, String shortRelName, String toClazzName) {
		CiClassNames names = parseClassName(toClazzName);

		return ciMapper.getFromCIRelations(fromId, relationName, shortRelName, names.className, names.shortClassName);
	}

	/**
	 * Gets the cI relations naked.
	 *
	 * @param nsPath the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations naked
	 */
	public List<CmsCIRelation> getCIRelationsNaked(
			String nsPath, String relationName, String shortRelName, String fromClazzName, String toClazzName) {
		
		CiClassNames toNames = parseClassName(toClazzName);
		CiClassNames fromNames = parseClassName(fromClazzName);
		
		List<CmsCIRelation> relList = ciMapper.getCIRelations(nsPath, relationName, shortRelName, fromNames.className, fromNames.shortClassName, toNames.className, toNames.shortClassName); 
		populateRelAttrs(relList);
		return relList;

	}

	/**
	 * Gets the cI relations ns like naked.
	 *
	 * @param nsPath the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations ns like naked
	 */
	public List<CmsCIRelation> getCIRelationsNsLikeNaked(
			String nsPath, String relationName, String shortRelName, String fromClazzName, String toClazzName) {
		
		List<CmsCIRelation> relList = getCIRelationsNsLikeNakedNoAttrs(nsPath, relationName, shortRelName, fromClazzName, toClazzName);
		populateRelAttrs(relList);
		return relList;
	}
	
	
	/**
	 * Gets the cI relations naked no attrs.
	 *
	 * @param nsPath the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations naked no attrs
	 */
	public List<CmsCIRelation> getCIRelationsNakedNoAttrs(
			String nsPath, String relationName, String shortRelName, String fromClazzName, String toClazzName) {
		
		CiClassNames toNames = parseClassName(toClazzName);
		
		CiClassNames fromNames = parseClassName(fromClazzName);
		
		return ciMapper.getCIRelations(nsPath, relationName, shortRelName, fromNames.className, fromNames.shortClassName, toNames.className, toNames.shortClassName); 
	}

	/**
	 * Gets the cI relations - do not laod attributes for relations and CIs.
	 *
	 * @param nsPath the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @return the cI relations naked no attrs
	 */
	public List<CmsCIRelation> getCIRelationsNoAttrs(String nsPath, String relationName, String shortRelName) {

		List<CmsCIRelation> relList = ciMapper.getCIRelations(nsPath, relationName, shortRelName, null, null, null, null);
		populateRelCisNoAttrs(relList, true, true);
		return relList;
	}

	/**
	 * Gets the cI relations naked no attrs by ciState of the relation.
	 *
	 * @param nsPath the ns path
	 * @param relationName the relation name
	 * @param ciState the ciState of the relation
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations naked no attrs
	 */
	public List<CmsCIRelation> getCIRelationsNakedNoAttrsByState(
			String nsPath, String relationName, String ciState, String fromClazzName, String toClazzName) {
		return ciMapper.getCIRelationsByState(nsPath, relationName, ciState, fromClazzName, toClazzName); 
	}


	/**
	 * Gets the cI relations ns like naked no attrs optionally loads cis.
	 *
	 * @param ns the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations ns like naked no attrs
	 */
	public List<CmsCIRelation> getCIRelationsNsLikeNakedNoAttrs(
			String ns, String relationName, String shortRelName, String fromClazzName, String toClazzName, boolean loadFromCi, boolean loadToCi) {

		String nsLike = CmsUtil.likefyNsPath(ns);

		CiClassNames toNames = parseClassName(toClazzName);

		CiClassNames fromNames = parseClassName(fromClazzName);

		List<CmsCIRelation> ciRelationsNsLike = ciMapper.getCIRelationsNsLike(ns, nsLike, relationName, shortRelName, fromNames.className, fromNames.shortClassName, toNames.className, toNames.shortClassName);
		populateRelCis(ciRelationsNsLike, loadFromCi, loadToCi);
		return ciRelationsNsLike;
	}
	
	/**
	 * Gets the cI relations ns like naked no attrs.
	 *
	 * @param ns the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations ns like naked no attrs
	 */
	public List<CmsCIRelation> getCIRelationsNsLikeNakedNoAttrs(
			String ns, String relationName, String shortRelName, String fromClazzName, String toClazzName) {
		return getCIRelationsNsLikeNakedNoAttrs(ns, relationName, shortRelName, fromClazzName, toClazzName, false, false);
	}
	
	/**
	 * Gets the cI relations 
	 *
	 * @param nsPath the ns path
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @return the cI relations naked
	 */
	public List<CmsCIRelation> getCIRelations(String nsPath, String relationName, String shortRelName, String fromClazzName, String toClazzName) {
		CiClassNames toNames = parseClassName(toClazzName);
		CiClassNames fromNames = parseClassName(fromClazzName);
		List<CmsCIRelation> relList = ciMapper.getCIRelations(nsPath, relationName, shortRelName, fromNames.className, fromNames.shortClassName, toNames.className, toNames.shortClassName);
		populateRelAttrs(relList);
		populateRelCis(relList, true, true);
		return relList;

	}

	public List<CmsCIRelation> getCIRelationsWithToCIAndNoAttrs(String nsPath,
			String relationName,
			String shortRelName,
			String fromClazzName,
			String toClazzName) {
		CiClassNames toNames = parseClassName(toClazzName);
		CiClassNames fromNames = parseClassName(fromClazzName);
		List<CmsCIRelation> relList = ciMapper.getCIRelations(nsPath, relationName,
				shortRelName, fromNames.className, fromNames.shortClassName,
				toNames.className, toNames.shortClassName);
		populateRelCis(relList, false, true);
		return relList;

	}
	
	/**
	 * Gets the to ci relations.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param fromClazzName the from clazz name
	 * @return the to ci relations
	 */
	public List<CmsCIRelation> getToCIRelations(long toId, String relationName,
			String fromClazzName) {
		return getToCIRelationsLocal(toId, relationName, null, fromClazzName);
	}

	/**
	 * Gets the to ci relations.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @return the to ci relations
	 */
	public List<CmsCIRelation> getToCIRelations(long toId, String relationName, String shortRelName,
			String fromClazzName) {
		return getToCIRelationsLocal(toId, relationName, shortRelName, fromClazzName);
	}
	
	private List<CmsCIRelation> getToCIRelationsLocal(long toId, String relationName, String shortRelName,
			String fromClazzName) {

		List<CmsCIRelation> relList = getToCIRelationsNakedLocal(toId, relationName, shortRelName, fromClazzName);

		populateRelCis(relList, true, false);
		
		/*
		for (CmsCIRelation rel : relList) {
			rel.setFromCi(getCiById(rel.getFromCiId()));
		}
		*/
		return relList;
	}
	
	/**
	 * Gets the to ci relations by from ci ids.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromCiIds the from ci ids
	 * @return the to ci relations by from ci ids
	 */
	public List<CmsCIRelation> getToCIRelationsByFromCiIds(long toId, String relationName,
			String shortRelName, List<Long> fromCiIds) {
		
		List<CmsCIRelation> relList = getToCIRelationsByFromCiIdsNaked(toId, relationName, shortRelName, fromCiIds);
		
		populateRelCis(relList, true, false);
		
		/*
		for (CmsCIRelation rel : relList) {
			rel.setFromCi(getCiById(rel.getFromCiId()));
		}
		*/
		return relList;
	}

	
	/**
	 * Gets the to ci relations naked.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param fromClazzName the from clazz name
	 * @return the to ci relations naked
	 */
	public List<CmsCIRelation> getToCIRelationsNaked(long toId, String relationName,
			String fromClazzName) {
		return getToCIRelationsNakedLocal(toId, relationName, null, fromClazzName);
	}

	/**
	 * Gets the to ci relations naked.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @return the to ci relations naked
	 */
	public List<CmsCIRelation> getToCIRelationsNaked(long toId, String relationName, String shortRelName,
			String fromClazzName) {
		return getToCIRelationsNakedLocal(toId, relationName, shortRelName, fromClazzName);
	}
	
	/**
	 * Gets the from to ci relations naked.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param toId the to id
	 * @return the from to ci relations naked
	 */
	public List<CmsCIRelation> getFromToCIRelationsNaked(long fromId, String relationName, long toId) {
		List<CmsCIRelation> rels = ciMapper.getFromToCIRelations(fromId, relationName, toId);
		populateRelAttrs(rels);
		return rels;
	}
	
	/**
	 * Gets the from ci relations by target nsPath.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param toClazzName target class
	 * @param toNsPath target nsPath
	 * @return the from to ci relations
	 */
	
	public List<CmsCIRelation> getFromCIRelationsByNs(long fromId, String relationName, String shortRelName, String toClazzName, String toNsPath) {
		
		List<CmsCIRelation> relList = getFromCIRelationsByNsNaked(fromId, relationName, shortRelName, toClazzName, toNsPath);

		populateRelCis(relList, false, true);
		
		/*
		for (CmsCIRelation rel : relList) {
			rel.setToCi(getCiById(rel.getToCiId()));
		}
		*/
		return relList;
	}
	
	/**
	 * Gets the from ci relations by target nsPath without populating toCi.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param toClazzName target class
	 * @param toNsPath target nsPath
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getFromCIRelationsByNsNaked(long fromId, String relationName, String shortRelName, String toClazzName, String toNsPath) {
		return getFromCIRelationsByNsNaked(fromId, relationName, shortRelName, toClazzName, toNsPath, false);
	}

	/**
	 * Gets the from ci relations by target nsPath without populating toCi.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param toClazzName target class
	 * @param toNsPath target nsPath
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getFromCIRelationsByNsNaked(long fromId, String relationName, String shortRelName, String toClazzName, String toNsPath, boolean recursive) {
		
		CiClassNames toNames = parseClassName(toClazzName);
		List<CmsCIRelation> relList = null;
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(toNsPath);
			relList = ciMapper.getFromCIRelationsByNSLike(fromId, relationName, shortRelName, toNames.className, toNames.shortClassName, toNsPath, nsLike);
		} else {	
			relList = ciMapper.getFromCIRelationsByNS(fromId, relationName, shortRelName, toNames.className, toNames.shortClassName, toNsPath);
		}
		populateRelAttrs(relList);
		return relList;
	}
	
	/**
	 * Gets the to ci relations by target nsPath and toCiId.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param fromClazzName target class
	 * @param fromNsPath target nsPath
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getToCIRelationsByNs(long toId, String relationName, String shortRelName, String fromClazzName, String fromNsPath) {
		List<CmsCIRelation> relList = getToCIRelationsByNsNoAttrs(toId, relationName, shortRelName, fromClazzName, fromNsPath);
		populateRelAttrs(relList);
		return relList;
	}

	/**
	 * Gets the to ci relations by target nsPath and toCiId with no relation attributes.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param fromClazzName target class
	 * @param fromNsPath target nsPath
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getToCIRelationsByNsNoAttrs(long toId, String relationName, String shortRelName, String fromClazzName, String fromNsPath) {
		CiClassNames fromNames = parseClassName(fromClazzName);
		List<CmsCIRelation> relList = ciMapper.getToCIRelationsByNS(toId, relationName, shortRelName, fromNames.className, fromNames.shortClassName, fromNsPath);
		populateRelCis(relList, true, false);
		return relList;
	}

	/**
	 * Gets the to ci relations by target nsPath without populating fromCi.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param fromClazzName target class
	 * @param fromNsPath target nsPath
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getToCIRelationsByNsNaked(long toId, String relationName, String shortRelName, String fromClazzName, String fromNsPath) {
		return getToCIRelationsByNsNaked(toId, relationName, shortRelName, fromClazzName, fromNsPath, false); 
	}

	/**
	 * Gets the to ci relations by target nsPath recursivley without populating fromCi.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short relation name
	 * @param fromClazzName target class
	 * @param fromNsPath target nsPath
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getToCIRelationsByNsNaked(long toId, String relationName, String shortRelName, String fromClazzName, String fromNsPath, boolean recursive) {
		
		CiClassNames fromNames = parseClassName(fromClazzName);
		List<CmsCIRelation> relList;
		if(recursive) {
			String nsLike = CmsUtil.likefyNsPath(fromNsPath);
			relList = ciMapper.getToCIRelationsByNSLike(toId, relationName, shortRelName, fromNames.className, fromNames.shortClassName, fromNsPath, nsLike);
		} else { 
			relList = ciMapper.getToCIRelationsByNS(toId, relationName, shortRelName, fromNames.className, fromNames.shortClassName, fromNsPath);
		}
		populateRelAttrs(relList);
		return relList;
	}

	/**
	 * Gets the from to ci relations.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param toId the to id
	 * @return the from to ci relations
	 */
	public List<CmsCIRelation> getFromToCIRelations(long fromId, String relationName, long toId) {
		
		List<CmsCIRelation> relList = ciMapper.getFromToCIRelations(fromId, relationName, toId);
		populateRelAttrs(relList);
		return relList;
	
	}

	
	private List<CmsCIRelation> getToCIRelationsNakedLocal(long toId, 
			String relationName, String shortRelName, String fromClazzName) {

		CiClassNames fromNames = parseClassName(fromClazzName);
	
		List<CmsCIRelation> relList = ciMapper.getToCIRelations(toId, relationName, shortRelName, fromNames.className, fromNames.shortClassName);
		populateRelAttrs(relList);
		
		return relList;
	}

	/**
	 * Gets the to ci relations by from ci ids naked.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromCiIds the from ci ids
	 * @return the to ci relations by from ci ids naked
	 */
	public List<CmsCIRelation> getToCIRelationsByFromCiIdsNaked(long toId, String relationName,
			String shortRelName, List<Long> fromCiIds) {
		
		List<CmsCIRelation> relList = ciMapper.getToCIRelationsByFromCiIDs(toId, relationName, shortRelName, fromCiIds);
		populateRelAttrs(relList);
		return relList;
	}

	
	/**
	 * Gets the to ci relations naked no attrs.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the from clazz name
	 * @return the to ci relations naked no attrs
	 */
	public List<CmsCIRelation> getToCIRelationsNakedNoAttrs(long toId, 
			String relationName, String shortRelName, String fromClazzName) {
		
		CiClassNames fromNames = parseClassName(fromClazzName);

		return ciMapper.getToCIRelations(toId, relationName, shortRelName, fromNames.className, fromNames.shortClassName);
	}
	
	
	/**
	 * Gets the all ci relations.
	 *
	 * @param ciId the ci id
	 * @return the all ci relations
	 */
	public List<CmsCIRelation> getAllCIRelations(long ciId) {

		List<CmsCIRelation> relList = new ArrayList<CmsCIRelation>();
		relList.addAll(getFromCIRelations(ciId,null,null));
		relList.addAll(getToCIRelations(ciId,null,null));
		return relList;
	}

	/**
	 * Delete relation.
	 *
	 * @param relId the rel id
	 */
	public void deleteRelation(long relId) {
		deleteRelation(relId, true);
	}

	/**
	 * Delete relation.
	 *
	 * @param relId the rel id
	 * @param delete4real the delete4real
	 */
	public void deleteRelation(long relId, boolean delete4real) {
		ciMapper.deleteRelation(relId, delete4real);
	}
	
	
	/**
	 * Update relation.
	 *
	 * @param relation the relation
	 * @return the cms ci relation
	 */
	public CmsCIRelation updateRelation(CmsCIRelation relation) {

		CmsCI fromCi = relation.getFromCi();
		CmsCI newFromCi = null;
		if (fromCi != null) {
			fromCi.setUpdatedBy(relation.getUpdatedBy());
			newFromCi = updateCI(fromCi);
			relation.setFromCiId(newFromCi.getCiId());
		}
		
		CmsCI toCi = relation.getToCi();
		CmsCI newToCi = null;
		if (toCi != null) {
			toCi.setUpdatedBy(relation.getUpdatedBy());
			newToCi = updateCI(toCi);
			relation.setToCiId(newToCi.getCiId());
		}

		CmsCIRelation newRel = updateRelationSimple(relation);
		if (newFromCi != null) newRel.setFromCi(newFromCi);
		if (newToCi != null) newRel.setToCi(newToCi);
		return newRel;
	}
	
	private CmsCIRelation updateRelationSimple(CmsCIRelation relation) {

		CIValidationResult validation = cmValidator.validateRelation(relation);
		
		if (!validation.isValidated()) {
			logger.debug(validation.getErrorMsg());
			throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, validation.getErrorMsg());
		}
		
		CmsCIRelation existingRel = getRelationById(relation.getCiRelationId()); 

		if (relation.getRelationState() != null && !relation.getRelationState().equals(existingRel.getRelationState())) {
			Integer relStateId = ciMapper.getCiStateId(relation.getRelationState());
			if (relStateId == null) {
				throw new CIValidationException(CmsError.VALIDATION_COMMON_ERROR, "There is no such ci state defined - " + relation.getRelationState());
			}
			relation.setRelationStateId(relStateId);
		} else if (relation.getRelationState() == null && relation.getRelationStateId() == 0
				&& existingRel.getRelationStateId() > 0) {
			logger.info("incoming update request rel :"+relation.getRelationId()+ " for state id= " + relation.getRelationStateId()
					+ " existing ci state id = " + existingRel.getRelationStateId());
			relation.setRelationStateId(existingRel.getRelationStateId());
		} else if (relation.getRelationStateId() == 0) {
			relation.setRelationStateId(100);
		}

        if (relation.getRelationStateId()!=existingRel.getRelationStateId() || (relation.getComments()!=null && !relation.getComments().equals(existingRel.getComments()))){
            ciMapper.updateRelation(relation);
        }


		for(CmsCIRelationAttribute updAttr : relation.getAttributes().values()){
			updAttr.setCiRelationId(relation.getCiRelationId());
			
			CmsCIRelationAttribute existingAttr = existingRel.getAttribute(updAttr.getAttributeName());
			if (existingAttr == null || existingAttr.getCiRelationAttributeId() == 0) {
				ciMapper.addRelationAttributeAndPublish(updAttr);
			} else {
				if (!cmValidator.attrsEqual(existingAttr, updAttr, true)) {
					updAttr.setCiRelationAttributeId(existingAttr.getCiRelationAttributeId());
					ciMapper.updateCIRelationAttribute(updAttr);
				}
			}
		}
		return getRelationById(relation.getCiRelationId());		
	}
	
	/**
	 * Gets the template obj for manifest obj.
	 *
	 * @param manifestCi the manifest ci
	 * @param env the env
	 * @return the template obj for manifest obj
	 */
	public CmsCI getTemplateObjForManifestObj(CmsCI manifestCi, CmsCI env) {
		
		List<CmsCIRelation> boxList = getToCIRelations(manifestCi.getCiId(), "manifest.Requires", null);
		CmsCI box = null;
		String template = null;
		CmsCI templateCi = null;
		if (boxList.size()>0) {
			box = boxList.get(0).getFromCi();
			template = boxList.get(0).getAttribute("template").getDjValue();
		}
		String avail = null;
		if (box.getAttribute("availability") == null || box.getAttribute("availability").getDfValue().equals("default")) {
			avail = env.getAttribute("availability").getDfValue();
		} else {
			avail = box.getAttribute("availability").getDfValue();
		}
		
		if (box != null && template != null) {
			String mgmtTemplNsPath = "/public/" + box.getAttribute("source").getDfValue() 
									+ "/packs/" + box.getAttribute("pack").getDfValue()
									+ "/" + box.getAttribute("version").getDfValue()
									+ "/" + avail;

			List<CmsCI> templateCis = getCiBy3(mgmtTemplNsPath, "mgmt." +  manifestCi.getCiClassName(), template);
			if (templateCis.size() >0 ) {
				templateCi = templateCis.get(0);
			}

		} else {
			throw new CmsException(CmsError.CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR,
                                "Can not figure out template for manifest ciId - " + manifestCi.getCiId());
		}

		return templateCi;
		
	}

	
	/**
	 * Gets the env by ns.
	 *
	 * @param nsPath the ns path
	 * @return the env by ns
	 */
	public CmsCI getEnvByNS(String nsPath) {
		// asuming the namespace is constructed as
		//org/assembly/env/.... like this  "/oneops/usr/DEV1/bom"
		String[] nsParts = nsPath.split("/");
		String envNsPath = "/" + nsParts[1] + "/" + nsParts[2];
		String envName = nsParts[3];
		
		List<CmsCI> envCis = getCiBy3(envNsPath, "manifest.Environment", envName);
		if (envCis.size() >0) {
			return envCis.get(0); 
		}
		
		return null;
	}

	/**
	 * Gets the env state.
	 *
	 * @param envId the env id
	 * @return the env state
	 */
	public Map<Long,List<Long>> getEnvState(long envId) {
		CmsCI env = ciMapper.getCIById(envId);
		String manifestNs = env.getNsPath() + "/" + env.getCiName() + "/manifest/";	
		String nsLike = CmsUtil.likefyNsPath(manifestNs);
		List<HashMap<String, Object>> result = ciMapper.getEnvState(nsLike);
		Map<Long,List<Long>> resultMap = new HashMap<Long,List<Long>>();
		for (HashMap<String, Object> row : result) {
			Long badCiId = (Long)row.get("bad_ci");
			Long badBomCiId = (Long)row.get("bad_bom_ci");
			if (!resultMap.containsKey(badCiId)) {
				resultMap.put(badCiId, new ArrayList<Long>());
			}
			if (badBomCiId != null) resultMap.get(badCiId).add(badBomCiId);
		}
		return resultMap;
	}
	
    /**
     * Upsert cms ci.
     *
     * @param cmsCi the cms ci
     * @return the cms ci
     */
    public CmsCI upsertCmsCI(CmsCI cmsCi) {
        if (cmsCi.getCiId() == 0 ) {
            CmsCI createdCi = createCI(cmsCi);
            return getCiById(createdCi.getCiId());
        } else {
            CmsCI updatedCi = updateCI(cmsCi);
            return getCiById(updatedCi.getCiId());
        }
    }

    /**
     * Upsert relation.
     *
     * @param rel the rel
     * @return the cms ci relation
     */
    public CmsCIRelation upsertRelation(CmsCIRelation rel) {
        CmsCIRelation newRel = null;
        if(rel.getRelationId() == 0) {
            newRel = createRelation(rel);
        } else {
            newRel = updateRelation(rel);
        }
        return newRel;
    }

	/**
	 * Gets the count by3.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param recursive the recursive
	 * @return the count by3
	 */
	public long getCountBy3(String ns, String clazzName, String ciName, boolean recursive) {
		
		long count = 0;
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(ns);
			count = ciMapper.getCountBy3NsLike(ns, nsLike, clazzName, ciName);
		} else {
			count = ciMapper.getCountBy3(ns, clazzName, ciName);
		}
		return count;
	}
    
	/**
	 * Gets the count by3 group by ns.
	 *
	 * @param ns the ns
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @return the count by3 group by ns
	 */
	public Map<String, Long> getCountBy3GroupByNs(String ns, String clazzName, String ciName) {
		String nsLike = CmsUtil.likefyNsPath(ns);
		return parseStatsQueryResult(ciMapper.getCountBy3NsLikeGroupByNs(ns, nsLike, clazzName, ciName));
	}

	// the ibatis returns this as List<Map<String,Object>> where map keys are colum names
	private Map<String, Long> parseStatsQueryResult(List<Map<String,Object>> stats) {
		Map<String, Long> result = new HashMap<String, Long>();
		for (Map<String,Object> row : stats) {
			String nspath = (String)row.get("path");
			Long cnt = (Long)row.get("cnt");
			result.put(nspath, cnt);
		}
		return result;
	}

	// the ibatis returns this as List<Map<String,Object>> where map keys are colum names
	private Map<Long, Long> parseStatsQueryResultGroupByCiId(List<Map<String,Object>> stats) {
		Map<Long, Long> result = new HashMap<Long, Long>();
		for (Map<String,Object> row : stats) {
			Long ciId = (Long)row.get("ci_id");
			Long cnt = (Long)row.get("cnt");
			result.put(ciId, cnt);
		}
		return result;
	}
	
	/**
	 * Gets the count from ci relations by ns.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @param toNsPath the to ns path
	 * @param recursive the recursive
	 * @return the count from ci relations by ns
	 */
	public long getCountFromCIRelationsByNS(long fromId,
			String relationName, String shortRelName, String toClazzName, String toNsPath, boolean recursive) {
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(toNsPath);
			return ciMapper.getCountFromCIRelationsByNSLike(fromId, relationName, shortRelName, toClazzName, toNsPath, nsLike);
		} else {	
			return ciMapper.getCountFromCIRelationsByNS(fromId, relationName, shortRelName, toClazzName,  toNsPath);
		}
	}

	/**
	 * Gets the count from ci relations group by ns.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @param toNsPath the to ns path
	 * @return the count from ci relations group by ns
	 */
	public Map<String, Long> getCountFromCIRelationsGroupByNs(long fromId,
			String relationName, String shortRelName, String toClazzName, String toNsPath) {
			String nsLike = CmsUtil.likefyNsPath(toNsPath);
			return parseStatsQueryResult(ciMapper.getCountFromCIRelationsByNSLikeGroupByNs(fromId, relationName, shortRelName, toClazzName, toNsPath, nsLike));
	}
	
	/**
	 * Gets the count ci relations group by from ci_id.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @param nsPath nsPath of the relation
	 * @return the count from ci relations group by ns
	 */
	public Map<Long, Long> getCounCIRelationsGroupByFromCiId(
			String relationName, String shortRelName, String toClazzName, String nsPath) {
		if (StringUtils.isBlank(toClazzName) && isBomRelation(relationName) && CmsUtil.isOrgLevel(nsPath)) {
			String nsLike = CmsUtil.likefyNsPathWithBom(nsPath);
			return parseStatsQueryResultGroupByCiId(ciMapper.getCountCIRelationsByNSLikeAndRelName(relationName, shortRelName, nsLike));
		}
		else {
			String nsLike = CmsUtil.likefyNsPath(nsPath);
			return parseStatsQueryResultGroupByCiId(ciMapper.getCountCIRelationsByNSLikeGroupByFromCiId(relationName, shortRelName, toClazzName, nsPath, nsLike));
		}
	}

	private boolean isBomRelation(String relationName) {
		return (relationName != null && bomRelations.contains(relationName));
	}

	/**
	 * Gets the count ci relations group by to ci_id.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the to clazz name
	 * @param nsPath the to ns path
	 * @return the count from ci relations group by ns
	 */
	public Map<Long, Long> getCountCIRelationsGroupByToCiId(
			String relationName, String shortRelName, String fromClazzName, String nsPath) {
			String nsLike = CmsUtil.likefyNsPath(nsPath);
			return parseStatsQueryResultGroupByCiId(ciMapper.getCountCIRelationsByNSLikeGroupByToCiId(relationName, shortRelName, fromClazzName, nsPath, nsLike));
	}
	
	/**
	 * Gets the count to ci relations by ns.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param fromClazzName the to clazz name
	 * @param recursive the recursive
	 * @return the count to ci relations by ns
	 */
	public long getCountToCIRelationsByNS(long toId,
			String relationName, String shortRelName, String fromClazzName, String fromNsPath, boolean recursive) {
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(fromNsPath);
			return ciMapper.getCountToCIRelationsByNSLike(toId, relationName, shortRelName, fromClazzName, fromNsPath, nsLike);
		} else {	
			return ciMapper.getCountToCIRelationsByNS(toId, relationName, shortRelName, fromClazzName,  fromNsPath);
		}
	}

	/**
	 * Gets the count to ci relations g roup by ns.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param toClazzName the to clazz name
	 * @param toNsPath the to ns path
	 * @return the count to ci relations group by ns
	 */
	public Map<String, Long> getCountToCIRelationsGroupByNs(long fromId,
			String relationName, String shortRelName, String toClazzName, String toNsPath) {
			String nsLike = CmsUtil.likefyNsPath(toNsPath);
			return parseStatsQueryResult(ciMapper.getCountToCIRelationsByNSLikeGroupByNs(fromId, relationName, shortRelName, toClazzName, toNsPath, nsLike));
	}
	
	
	/**
	 * Reset pendingDeletions by ns recursive.
	 *
	 * @param  nsPath
	 */
	public void resetDeletionsByNs(String nsPath) {
		String nsLike = CmsUtil.likefyNsPath(nsPath);
		ciMapper.resetDeletionsByNsLike(nsPath, nsLike);	
		ciMapper.resetRelDeletionsByNsLike(nsPath, nsLike);
	}
		
	/**
	 * Update simple cms vars
	 * @param varName
	 * @param varValue
	 * @param updatedBy
	 */
	public void updateCmSimpleVar(String varName, String varValue, String criteria, String updatedBy) {
		utilMapper.updateCmSimpleVar(varName, varValue, criteria, updatedBy);
	}
	
	/**
	 * 
	 * @param varName
	 */
	public CmsVar getCmSimpleVar(String varName) {
		return utilMapper.getCmSimpleVar(varName);
	}

	public List<CmsVar> getCmVarByLongestMatchingCriteria(String varNameLike, String criteria) {
		return utilMapper.getCmVarByLongestMatchingCriteria(varNameLike, criteria);
	}

	private CiClassNames parseClassName(String clazzName) {
		
		CiClassNames names = new CiClassNames();
		if (clazzName != null) {
			if (clazzName.contains(".")) {
				names.className = clazzName;
			} else {
				names.shortClassName = clazzName;
			}
		}
		return names;
	}

	public String getAttributeDescription(String nsPath, String ciName, String attrName) {
        List<CmsCI> list =getCiBy3(nsPath, null, ciName);
        if (!list.isEmpty()) {
            CmsCI cmsCi = list.get(0);
            return mdProcessor.getAttribute(cmsCi.getCiClassId(), attrName).getDescription();
        }
		return null;
	}

	public long getNextDjId() {
		return djMapper.getNextDjId();
	}

	private class CiClassNames {
		String className = null;
		String shortClassName = null;
	}
	
	public CmsCIRelation bootstrapRelation(CmsCI fromCi, CmsCI toCi, String relName, String nsPath, String createdBy, Date created) {
		CmsCIRelation newRel = new CmsCIRelation();
		newRel.setNsPath(nsPath);

		CmsRelation targetRelation = mdProcessor.getRelation(relName);

		newRel.setRelationId(targetRelation.getRelationId());
		newRel.setRelationName(targetRelation.getRelationName());

		//bootstrap the default values from Class definition
		for (CmsRelationAttribute mdRelAttr : targetRelation.getMdAttributes()) {
			if (mdRelAttr.getDefaultValue() != null) {
				CmsCIRelationAttribute relAttr = new CmsCIRelationAttribute();
				relAttr.setAttributeId(mdRelAttr.getAttributeId());
				relAttr.setAttributeName(mdRelAttr.getAttributeName());
				relAttr.setDfValue(mdRelAttr.getDefaultValue());
				relAttr.setDjValue(mdRelAttr.getDefaultValue());
				newRel.addAttribute(relAttr);
			}
		}

	    newRel.setFromCiId(fromCi.getCiId());
	    newRel.setToCiId(toCi.getCiId());
	    newRel.setComments(CmsUtil.generateRelComments(fromCi.getCiName(), fromCi.getCiClassName(), toCi.getCiName(), toCi.getCiClassName()));
	    newRel.setCreated(created);
	    newRel.setCreatedBy(createdBy);
		return newRel;
	}


	public void createAltNs(CmsAltNs cmsAltNs, long ciId) {

		CmsNamespace ns = null;
		if (cmsAltNs.getNsId() != 0) {
			ns = cmsNsProcessor.getNsById(cmsAltNs.getNsId());
		} else {
			ns = cmsNsProcessor.getNs(cmsAltNs.getNsPath());
		}
		if (ns ==null){
			ns = new CmsNamespace();
			ns.setNsPath(cmsAltNs.getNsPath());
			ns = cmsNsProcessor.createNs(ns);
		}
		ciMapper.createAltNs(ns.getNsId(), cmsAltNs.getTag(), ciId);
	}

	public List<CmsAltNs> getAltNsByCiAndTag(long ciId, String tag) {
        return ciMapper.getAltNsByCiAndTag(ciId, tag);
	}

	public List<CmsCI> getCmCIByAltNsAndTag(String nsPath,
											String clazzName,
											String altNsPath, String tag,
											boolean recursive) {

		

		CiClassNames names = parseClassName(clazzName);
		if (recursive) {
			String nsLike = CmsUtil.likefyNsPath(nsPath);
			return ciMapper.getCmCIByAltNsAndTagNsLike(nsLike, nsPath, names.className, names.shortClassName, altNsPath, tag);
		} else {
			return ciMapper.getCmCIByAltNsAndTag(nsPath, names.className, names.shortClassName, altNsPath, tag);
		}
	}

	public void deleteAltNs(long nsId, long ciId) {
		ciMapper.deleteAltNs(nsId, ciId);
	}

	public DJMapper getDjMapper() {
		return djMapper;
	}

	public void setDjMapper(DJMapper djMapper) {
		this.djMapper = djMapper;
	}

	public boolean getVarByMatchingCriteriaBoolean(String varNameLike, String criteria) {
		List<CmsVar> vars = getCmVarByLongestMatchingCriteria(varNameLike, criteria);
		if (vars != null && !vars.isEmpty()) {
			CmsVar var = vars.get(0);
			return Boolean.valueOf(var.getValue());
		}
		return false;
	}

}
