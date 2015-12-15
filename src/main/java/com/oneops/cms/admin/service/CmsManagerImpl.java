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
package com.oneops.cms.admin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.md.dal.ClazzMapper;
import com.oneops.cms.md.dal.RelationMapper;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.CmsStuckDpmtCollection;

public class CmsManagerImpl implements CmsManager {
	
	private ClazzMapper clazzMapper;
	private RelationMapper relationMapper;
	private CIMapper ciMapper;
	//private NSMapper nsMapper;
	private UtilMapper utilMapper;

	private final AttrComparator attrComparator = new AttrComparator();	
	
	public void setClazzMapper(ClazzMapper clazzMapper) {
		this.clazzMapper = clazzMapper;
	}

    public void setRelationMapper(RelationMapper relationMapper) {
		this.relationMapper = relationMapper;
	}

	public void setCiMapper( CIMapper ciMapper ) {
		this.ciMapper = ciMapper;
	}
	
	public void setUtilMapper(UtilMapper utilMapper) {
		this.utilMapper = utilMapper;
	}

	public CmsClazz getClazz(String clazzName) {
		CmsClazz clazz = clazzMapper.getClazz(clazzName);
		List<CmsClazzAttribute> attrs = getAllClazzAttrs(clazz, false );
		Collections.sort(attrs,attrComparator);
		clazz.setMdAttributes(attrs);
		clazz.setFromRelations(clazzMapper.getFromClazzRelations(clazz.getClassId()));
		clazz.setToRelations(clazzMapper.getToClazzRelations(clazz.getClassId()));
		return clazz;
	}
	
	private List<CmsClazzAttribute> getAllClazzAttrs(CmsClazz clazz, boolean isSuperClass ) {
	
		List<CmsClazzAttribute> superAttrs = null;
		
		if (clazz.getSuperClassId()>0) {
			CmsClazz superClazz = clazzMapper.getClazzById(clazz.getSuperClassId());
			superAttrs = getAllClazzAttrs(superClazz, true);
		}
		List<CmsClazzAttribute> thisAttrs = null;
		if (isSuperClass) {
			thisAttrs = clazzMapper.getInheritableClazzAttrs(clazz.getClassId());
		} else {
			thisAttrs = clazzMapper.getClazzAttrs(clazz.getClassId());
		}

		Map<String,CmsClazzAttribute> attrsMap = new HashMap<String, CmsClazzAttribute>();
			
		for (CmsClazzAttribute attr : thisAttrs) {
			attr.setInherited(isSuperClass);
			if (isSuperClass) attr.setInheritedFrom(clazz.getClassName());
			attrsMap.put(attr.getAttributeName(), attr);
		}
		
		if (superAttrs != null) {
			for (CmsClazzAttribute superAttr : superAttrs) {
				if (!attrsMap.containsKey(superAttr.getAttributeName())) {
					attrsMap.put(superAttr.getAttributeName(), superAttr);
				}
			}
		}
		
		return new ArrayList<CmsClazzAttribute>(attrsMap.values());

	}
	
	public List<CmsClazz> getClazzes() {
		return clazzMapper.getClazzes();
	}
	
	private class AttrComparator implements Comparator<CmsClazzAttribute> {

		public int compare(CmsClazzAttribute arg0, CmsClazzAttribute arg1) {
			return arg0.getAttributeName().compareToIgnoreCase(arg1.getAttributeName());
		}
		
	}

	public CmsRelation getRelation(String relationName) {
		CmsRelation relation = relationMapper.getRelation(relationName);
		relation.setMdAttributes(relationMapper.getRelationAttrs(relation.getRelationId()));
		relation.setTargets(relationMapper.getTargets(relation.getRelationId()));
		return relation;
	}

	@Override
	public List<CmsCI> getCiList(String nsPath, String clazzName, String ciName) {
		String className = null;
		String shortClassName = null;
		if (clazzName != null) {
			if (clazzName.contains(".")) {
				className = clazzName;
			} else {
				shortClassName = clazzName;
			}
		}

		return ciMapper.getCIby3(nsPath, className, shortClassName, ciName);
	}
	
	@Override
	public CmsStuckDpmtCollection getStuckDpmts(){
		CmsStuckDpmtCollection stuckDpmtColl = new CmsStuckDpmtCollection();
		stuckDpmtColl.setCmsStuckDpmts(utilMapper.getCmsStuckDpmts());
		stuckDpmtColl.setInProgressStuckDpmts(utilMapper.getInProgressStuckDpmts());
		stuckDpmtColl.setPausedStuckDpmts(utilMapper.getPausedStuckDpmts());
		return stuckDpmtColl;
	}
	
	@Override
	public CmsCI getCI( long id ) {
		return ciMapper.getCIById(id);
	}

	@Override
	public List<CmsCIAttribute> getCIAttributes( long id )	{
		return ciMapper.getCIAttrs(id);
	}

	@Override
	public List<CmsCIRelation> getFromRelation( long ciId ) {
		List<CmsCIRelation> l = ciMapper.getFromCIRelations( ciId, null, null, null, null );
		for(CmsCIRelation rel: l) {
			rel.setFromCi(ciMapper.getCIById(rel.getFromCiId()));
			rel.setToCi(ciMapper.getCIById(rel.getToCiId()));
		}
		return l;
	}

	@Override
	public List<CmsCIRelation> getToRelation( long ciId ) {
	    List<CmsCIRelation> l = ciMapper.getToCIRelations( ciId, null, null, null, null );
		for(CmsCIRelation rel: l) {
			rel.setFromCi(ciMapper.getCIById(rel.getFromCiId()));
			rel.setToCi(ciMapper.getCIById(rel.getToCiId()));
		}
		return l;
	}

	@Override
	public CmsCIRelation getCIRelation( long relId ) {
		return ciMapper.getCIRelation( relId );
	}

	@Override
	public List<CmsCIRelationAttribute> getCIRelationAttributes( long relId ) {
		return ciMapper.getCIRelationAttrs(relId);
	}

	@Override
	public void flushCache() {
		clazzMapper.flushCache();
		relationMapper.flushCache();
	}

}
