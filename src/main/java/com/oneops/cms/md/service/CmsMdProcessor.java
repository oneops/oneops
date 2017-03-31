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
package com.oneops.cms.md.service;

import com.oneops.cms.exceptions.MDException;
import com.oneops.cms.md.dal.ClazzMapper;
import com.oneops.cms.md.dal.RelationMapper;
import com.oneops.cms.md.domain.*;
import com.oneops.cms.util.CIValidationResult;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsMdValidator;
import com.oneops.cms.util.CmsUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

/**
 * The Class CmsMdProcessor.
 */
public class CmsMdProcessor {

    private ClazzMapper clazzMapper;
    private RelationMapper relationMapper;
    private CmsMdValidator mdValidator;
    private CmsUtil cmsUtil;
    private boolean cacheEnabled = false;
    public static int BASE_CLASS_ID = 100;

    static final Logger logger = Logger.getLogger(CmsMdProcessor.class);
    private final AttrComparator attrComparator = new AttrComparator();
    final private Map<String, CmsClazz> mdClazzCache = new ConcurrentHashMap<>();
    final private Map<Integer, CmsClazz> mdClazzCacheById = new ConcurrentHashMap<>();
    final private Map<String, CmsRelation> mdRelationCache = new ConcurrentHashMap<>();
    final private Map<Long, CmsRelation> mdRelationCacheById = new ConcurrentHashMap<>();
    final private Map<Integer, List<CmsClazzRelation>> clazzRelCache = new ConcurrentHashMap<>();

    static {
        logger.warn(">>> Initializing CmsMdProcessor");
    }

    /**
     * Initializes md processor cache(s).
     */
    @PostConstruct
    public void initCache() {
        if (cacheEnabled) {
            logger.info("Populating class relation target cache...");
            clazzRelCache.putAll(getAllTargets());
        }
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }


    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    /**
     * Retrives attribute
     *
     * @param ciClassId ciClassId
     * @param attrName  attribute name
     * @return description
     */
    public CmsClazzAttribute getAttribute(int ciClassId, String attrName) {
        List<CmsClazzAttribute> attributes = getClazz(ciClassId).getMdAttributes();
        Optional<CmsClazzAttribute> attributeOptional = attributes.stream().filter(attr -> attr.getAttributeName().equals(attrName)).findFirst();
        return attributeOptional.isPresent() ? attributeOptional.get() : new CmsClazzAttribute();
    }

    /**
     * Sets the md validator.
     *
     * @param mdValidator the new md validator
     */
    public void setMdValidator(CmsMdValidator mdValidator) {
        this.mdValidator = mdValidator;
    }


    /**
     * Sets the clazz mapper.
     *
     * @param clazzMapper the new clazz mapper
     */
    @Autowired
    public void setClazzMapper(ClazzMapper clazzMapper) {
        this.clazzMapper = clazzMapper;
    }

    /**
     * Sets the relation mapper.
     *
     * @param relationMapper the new relation mapper
     */
    @Autowired
    public void setRelationMapper(RelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    /**
     * Sets the cms util.
     *
     * @param cmsUtil the new cms util
     */
    @Autowired
    public void setCmsUtil(CmsUtil cmsUtil) {
        this.cmsUtil = cmsUtil;
    }

    /**
     * Gets the clazzes.
     *
     * @return the clazzes
     */
    public List<CmsClazz> getClazzes() {
        return clazzMapper.getClazzes();
    }

    /**
     * Gets the sub clazzes.
     *
     * @param clsName the cls name
     * @return the sub clazzes
     */
    public List<String> getSubClazzes(String clsName) {
        return clazzMapper.getSubClazzes(clsName);
    }

    /**
     * Gets the clazz.
     *
     * @param clazzId the clazz id
     * @return the clazz
     */
    public CmsClazz getClazz(int clazzId) {
        return getClazz(clazzId, false);
    }

    /**
     * Gets the clazz.
     *
     * @param clazzName the clazz name
     * @return the clazz
     */
    public CmsClazz getClazz(String clazzName) {
        return getClazz(clazzName, false);
    }

    /**
     * Flush cache.
     */
    public void flushCache() {
        clazzMapper.flushCache();
        relationMapper.flushCache();
    }


    /**
     * Gets the clazz by classId
     *
     * @param clazzId        the clazz id
     * @param includeActions the eager
     * @return the clazz
     */
    public CmsClazz getClazz(int clazzId, boolean includeActions) {
        CmsClazz clazz;
        if (cacheEnabled && !includeActions) {
            clazz = mdClazzCacheById.get(clazzId);
            if (clazz != null) {
                return clazz;
            }
        }

        clazz = clazzMapper.getClazzById(clazzId);
        if (clazz == null) return null;
        List<CmsClazzAttribute> attrs = getAllClazzAttrs(clazz, false);
        Collections.sort(attrs, attrComparator);
        clazz.setMdAttributes(attrs);
        clazz.setFromRelations(clazzMapper.getFromClazzRelations(clazz.getClassId()));
        clazz.setToRelations(clazzMapper.getToClazzRelations(clazz.getClassId()));
        if (includeActions) {
            clazz.setActions(getAllClazzActions(clazz, false));
        } else {
            if (cacheEnabled) {
                mdClazzCacheById.put(clazz.getClassId(), clazz);
                mdClazzCache.put(clazz.getClassName(), clazz);
            }
        }
        return clazz;
    }


    /**
     * Gets the clazz by name.
     *
     * @param clazzName      the clazz name
     * @param includeActions the eager
     * @return the clazz
     */
    public CmsClazz getClazz(String clazzName, boolean includeActions) {
        CmsClazz clazz;
        if (cacheEnabled && !includeActions) {
            clazz = mdClazzCache.get(clazzName);
            if (clazz != null) {
                return clazz;
            }
        }

        clazz = clazzMapper.getClazz(clazzName);
        if (clazz == null) {
            logger.error("Can't find class definition for " + clazzName);
            return null;
        }
        List<CmsClazzAttribute> attrs = getAllClazzAttrs(clazz, false);
        Collections.sort(attrs, attrComparator);
        clazz.setMdAttributes(attrs);
        clazz.setFromRelations(clazzMapper.getFromClazzRelations(clazz.getClassId()));
        clazz.setToRelations(clazzMapper.getToClazzRelations(clazz.getClassId()));
        if (includeActions) {
            clazz.setActions(getAllClazzActions(clazz, false));
        } else {
            if (cacheEnabled) {
                mdClazzCache.put(clazzName, clazz);
                mdClazzCacheById.put(clazz.getClassId(), clazz);
            }
        }

        return clazz;
    }

    /**
     * Gets the clazzes by package.
     *
     * @param packagePrefix the package prefix
     * @return the clazzes by package
     */
    public List<CmsClazz> getClazzesByPackage(String packagePrefix) {
        String pattern;
        if (packagePrefix.endsWith(".*")) {
            pattern = packagePrefix.replace('*', '%');
        } else {
            pattern = packagePrefix + ".%";
        }
        List<CmsClazz> clazzes = clazzMapper.getClazzesByPackage(pattern);
        for (CmsClazz clazz : clazzes) {
            List<CmsClazzAttribute> attrs = getAllClazzAttrs(clazz, false);
            Collections.sort(attrs, attrComparator);
            clazz.setMdAttributes(attrs);
        }
        return clazzes;
    }

    private List<CmsClazzAction> getAllClazzActions(CmsClazz clazz, boolean isSuperClass) {
        List<CmsClazzAction> superActions = null;

        if (clazz.getSuperClassId() > 0) {
            CmsClazz superClazz = clazzMapper.getClazzById(clazz.getSuperClassId());
            superActions = getAllClazzActions(superClazz, true);
        }
        List<CmsClazzAction> thisActions;
        if (isSuperClass) {
            thisActions = clazzMapper.getInheritableClazzActions(clazz.getClassId());
        } else {
            thisActions = clazzMapper.getClazzActions(clazz.getClassId());
        }

        Map<String, CmsClazzAction> actionsMap = new HashMap<>();

        for (CmsClazzAction act : thisActions) {
            act.setInherited(isSuperClass);
            if (isSuperClass) act.setInheritedFrom(clazz.getClassName());
            actionsMap.put(act.getActionName(), act);
        }

        if (superActions != null) {
            for (CmsClazzAction superAct : superActions) {
                if (!actionsMap.containsKey(superAct.getActionName())) {
                    actionsMap.put(superAct.getActionName(), superAct);
                }
            }
        }

        return new ArrayList<>(actionsMap.values());
    }

    private List<CmsClazzAttribute> getAllClazzAttrs(CmsClazz clazz, boolean isSuperClass) {

        List<CmsClazzAttribute> superAttrs = null;

        if (clazz.getSuperClassId() > 0) {
            CmsClazz superClazz = clazzMapper.getClazzById(clazz.getSuperClassId());
            if (superClazz != null) {
                superAttrs = getAllClazzAttrs(superClazz, true);
            }
        }

        List<CmsClazzAttribute> thisAttrs;
        if (isSuperClass) {
            thisAttrs = clazzMapper.getInheritableClazzAttrs(clazz.getClassId());
        } else {
            thisAttrs = clazzMapper.getClazzAttrs(clazz.getClassId());
        }

        Map<String, CmsClazzAttribute> attrsMap = new HashMap<>();

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

        return new ArrayList<>(attrsMap.values());

    }

    private class AttrComparator implements Comparator<CmsClazzAttribute> {

        @Override
        public int compare(CmsClazzAttribute arg0, CmsClazzAttribute arg1) {
            return arg0.getAttributeName().compareToIgnoreCase(arg1.getAttributeName());
        }

    }

    /**
     * Gets the relation with targets.
     *
     * @param relationName the relation name
     * @return the relation with targets
     */
    public CmsRelation getRelationWithTargets(String relationName) {
        CmsRelation relation = relationMapper.getRelation(relationName);
        if (relation != null) {
            relation.setMdAttributes(relationMapper.getRelationAttrs(relation.getRelationId()));
            relation.setTargets(relationMapper.getTargets(relation.getRelationId()));
        }
        return relation;
    }

    /**
     * Gets the relation with targets.
     *
     * @param relationName the relation name
     * @param fromClassId  the from class id
     * @param toClassId    the to class id
     * @return the relation with targets
     */
    public CmsRelation getRelationWithTargets(String relationName, int fromClassId, int toClassId) {
        CmsRelation relation = relationMapper.getRelation(relationName);
        if (relation != null) {
            relation.setMdAttributes(relationMapper.getRelationAttrs(relation.getRelationId()));
            relation.setTargets(relationMapper.getTargetsStrict(relation.getRelationId(), fromClassId, toClassId));
        }
        return relation;
    }


    /**
     * Gets the relation by name.
     *
     * @param relationName the relation name
     * @return the relation
     */
    public CmsRelation getRelation(String relationName) {
        CmsRelation relation;
        if (cacheEnabled) {
            relation = mdRelationCache.get(relationName);
            if (relation != null) {
                return relation;
            }
        }

        relation = relationMapper.getRelation(relationName);
        // Skip cache if relation is null.
        if (relation != null) {
            relation.setMdAttributes(relationMapper.getRelationAttrs(relation.getRelationId()));
            if (cacheEnabled) {
                mdRelationCache.put(relationName, relation);
                Integer relId = relation.getRelationId();
                mdRelationCacheById.put(relId.longValue(), relation);
            }
        }
        return relation;
    }

    /**
     * Gets the relation by ID
     *
     * @param relationId the relation id
     * @return the relation
     */
    public CmsRelation getRelation(long relationId) {
        CmsRelation relation;
        if (cacheEnabled) {
            relation = mdRelationCacheById.get(relationId);
            if (relation != null) {
                return relation;
            }
        }

        relation = relationMapper.getRelationById(relationId);
        // Skip cache if relation is null.
        if (relation != null) {
            relation.setMdAttributes(relationMapper.getRelationAttrs(relation.getRelationId()));
            if (cacheEnabled) {
                Integer relId = relation.getRelationId();
                mdRelationCacheById.put(relId.longValue(), relation);
                mdRelationCache.put(relation.getRelationName(), relation);
            }
        }
        return relation;
    }


    /**
     * Gets the targets.
     *
     * @param relationId the relation id
     * @return the targets
     */
    public List<CmsClazzRelation> getTargets(int relationId) {
        if (cacheEnabled) {
            // Fallback to relationMapper if not exists in cache.
            return clazzRelCache.computeIfAbsent(relationId, (r) -> relationMapper.getTargets(r));
        }
        return relationMapper.getTargets(relationId);
    }

    /**
     * Gets all the targets.
     *
     * @return a map containing all cms class relations with relation id as the key.
     */
    public Map<Integer, List<CmsClazzRelation>> getAllTargets() {
        return relationMapper.getAllTargets().stream().collect(toMap(CmsClazzRelation::getRelationId,
                (r) -> new ArrayList<>(singletonList(r)),
                (a, b) -> {
                    a.addAll(b);
                    return a;
                }));
    }

    /**
     * Gets the all relations.
     *
     * @return the all relations
     */
    public List<CmsRelation> getAllRelations() {
        List<CmsRelation> relations = relationMapper.getAllRelations();
        for (CmsRelation relation : relations) {
            relation.setMdAttributes(relationMapper.getRelationAttrs(relation.getRelationId()));
            relation.setTargets(relationMapper.getTargets(relation.getRelationId()));
        }
        return relations;
    }

    /**
     * Creates the clazz.
     *
     * @param clazz the clazz
     * @return the cms clazz
     */
    public CmsClazz createClazz(CmsClazz clazz) {

        clazz.setShortClassName(cmsUtil.getShortClazzName(clazz.getClassName()));

        if (getClazz(clazz.getClassName()) != null) {
            return updateClazz(clazz);
        }

        if (clazz.getClassId() == 0) {
            clazz.setClassId(clazzMapper.getNextClassId());
        }

        if (clazz.getSuperClassName() != null) {
            CmsClazz superClass = getClazz(clazz.getSuperClassName());
            if (superClass == null) {
                throw new MDException(CmsError.MD_SUPERCLASS_NOT_FOUND_ERROR, "Could not find superClass name: " + clazz.getSuperClassName());
            }
            clazz.setSuperClassId(superClass.getClassId());
        }
        if (clazz.getSuperClassId() == 0) {
            clazz.setSuperClassId(BASE_CLASS_ID);
        }

        clazzMapper.createClazz(clazz);
        for (CmsClazzAttribute attr : clazz.getMdAttributes()) {
            attr.setClassId(clazz.getClassId());
            clazzMapper.addClazzAttribute(attr);
        }

        for (CmsClazzAction act : clazz.getActions()) {
            act.setClassId(clazz.getClassId());
            clazzMapper.addClazzAction(act);
        }

        return getClazz(clazz.getClassId());
    }

    /**
     * Update clazz.
     *
     * @param clazz the clazz
     * @return the cms clazz
     */
    public CmsClazz updateClazz(CmsClazz clazz) {
        CmsClazz existingClazz = getClazz(clazz.getClassId(), true);

        if (existingClazz == null) {
            existingClazz = getClazz(clazz.getClassName(), true);
            if (existingClazz == null) {
                throw new MDException(CmsError.MD_METADATA_NOT_FOUND_ERROR, "Could not find the metadata to update. class_name = " + clazz.getClassName());
            } else {
                clazz.setClassId(existingClazz.getClassId());
            }
        }

        CIValidationResult validation = mdValidator.validateUpdateClazz(clazz);
        if (!validation.isValidated()) {
            throw new MDException(CmsError.MD_VALIDATION_ERROR, validation.getErrorMsg());
        }
        clazzMapper.updateClazz(clazz);

        List<CmsClazzAttribute> existingAttributes = existingClazz.getMdAttributes();
        for (CmsClazzAttribute updAttr : clazz.getMdAttributes()) {
            updAttr.setClassId((existingClazz.getClassId()));
            CmsClazzAttribute existingAttr = getAttribute(updAttr, existingAttributes);
            if (existingAttr == null) {
                clazzMapper.addClazzAttribute(updAttr);
            } else {
                if (!existingAttr.isInherited()) {
                    if (clazzMapper.getCountCiOfClazz(existingClazz.getClassId()) > 0
                            && !allowAttributeChanges(existingAttr, updAttr)) {
                        throw new MDException(CmsError.MD_CONFLICT_DATA_TYPE_ERROR, "Could not update class Id: " + existingClazz.getClassId() + " attribute - " + updAttr.getAttributeName() + " because of it has conflict in data type.");
                    }
                    updAttr.setAttributeId(existingAttr.getAttributeId());
                    clazzMapper.updateClazzAttribute(updAttr);
                    existingAttributes.remove(existingAttr);
                }
            }
        }
        if (!existingAttributes.isEmpty()) {
            for (CmsClazzAttribute attr : existingAttributes) {
                if (!attr.isInherited()) {
                    clazzMapper.deleteClazzAttribute(attr.getAttributeId(), true);
                }
            }
        }

        List<CmsClazzAction> existingActions = existingClazz.getActions();
        for (CmsClazzAction updAct : clazz.getActions()) {
            updAct.setClassId((existingClazz.getClassId()));
            CmsClazzAction existingAct = getAction(updAct, existingActions);
            if (existingAct == null) {
                clazzMapper.addClazzAction(updAct);
            } else {
                updAct.setActionId(existingAct.getActionId());
                clazzMapper.updateClazzAction(updAct);
                existingActions.remove(existingAct);
            }
        }
        if (!existingActions.isEmpty()) {
            for (CmsClazzAction act : existingActions) {
                clazzMapper.deleteClazzAction(act.getActionId());
            }
        }

        return getClazz(clazz.getClassId());
    }

    private boolean allowAttributeChanges(CmsClazzAttribute existingAttr, CmsClazzAttribute updAttr) {
        //isPropertyEquals(existingAttr.getValueFormat(), updAttr.getValueFormat()) &&
        return isPropertyEquals(existingAttr.getDataType(), updAttr.getDataType());
    }

    private boolean allowAttributeChanges(CmsRelationAttribute existingAttr, CmsRelationAttribute updAttr) {
        //isPropertyEquals(existingAttr.getValueFormat(), updAttr.getValueFormat()) &&
        return isPropertyEquals(existingAttr.getDataType(), updAttr.getDataType());
    }

    private boolean isPropertyEquals(String ex, String up) {
        return ((ex == null || ex.trim().isEmpty()) && (up == null || up.trim().isEmpty())) ||
                (ex != null && up != null && ex.equals(up));
    }

    private CmsClazzAttribute getAttribute(CmsClazzAttribute attr, List<CmsClazzAttribute> attributes) {
        for (CmsClazzAttribute item : attributes) {
            if (item.getAttributeId() == attr.getAttributeId()) {
                return item;
            } else if (item.getAttributeName().equals(attr.getAttributeName())) {
                return item;
            }
        }
        return null;
    }

    private CmsRelationAttribute getAttribute(CmsRelationAttribute attr, List<CmsRelationAttribute> attributes) {
        for (CmsRelationAttribute item : attributes) {
            if (item.getAttributeId() == attr.getAttributeId()) {
                return item;
            } else if (item.getAttributeName().equals(attr.getAttributeName())) {
                return item;
            }
        }
        return null;
    }

    private CmsClazzAction getAction(CmsClazzAction act, List<CmsClazzAction> actions) {
        for (CmsClazzAction item : actions) {
            if (item.getActionId() == act.getActionId()) {
                return item;
            } else if (item.getActionName().equals(act.getActionName())) {
                return item;
            }
        }
        return null;
    }

    /*
    private CmsClazzRelation getTarget(CmsClazzRelation rel, List<CmsClazzRelation> relations) {
        for(CmsClazzRelation item: relations) {
            if(item.getLinkId() == rel.getLinkId()) {
                return item;
            }
        }
        return null;
    }
	*/

    /**
     * Delete clazz.
     *
     * @param clazzId   the clazz id
     * @param deleteAll the delete all
     */
    public void deleteClazz(int clazzId, boolean deleteAll) {
        logger.debug("Delete class id = " + clazzId);
        if (!deleteAll && clazzMapper.getCountCiOfClazz(clazzId) > 0) {
            throw new MDException(CmsError.MD_NO_DELETE_HAS_CI_ERROR, "Could not delete class Id: " + clazzId + " because of it has some ci.");
        }
        clazzMapper.deleteClazz(clazzId, deleteAll);

    }

    /**
     * Creates the relation.
     *
     * @param relation the relation
     * @return the cms relation
     */
    public CmsRelation createRelation(CmsRelation relation) {
        relation.setShortRelationName(cmsUtil.getShortClazzName(relation.getRelationName()));

        if (getRelation(relation.getRelationName()) != null) {
            return updateRelation(relation);
        }
        if (relation.getRelationId() == 0) {
            relation.setRelationId(relationMapper.getNextRelationId());
        }
        relationMapper.createRelation(relation);
        for (CmsRelationAttribute attr : relation.getMdAttributes()) {
            attr.setRelationId(relation.getRelationId());
            relationMapper.addRelationAttribute(attr);
        }

        /*
        for(CmsClazzRelation link : relation.getTargets()) {
            link.setRelationId(relation.getRelationId());
            prepareTarget(link);
            relationMapper.addRelationTarget(link);
        }
        */
        for (CmsClazzRelation uppTarget : relation.getTargets()) {
            //added ability to specify target wildcard on the package like mgmt.service.*
            for (CmsClazzRelation target : parseTargets(uppTarget)) {
                target.setRelationId(relation.getRelationId());
                relationMapper.addRelationTarget(target);
            }
        }

        return getRelation(relation.getRelationName());
    }

    /**
     * Update relation.
     *
     * @param relation the relation
     * @return the cms relation
     */
    public CmsRelation updateRelation(CmsRelation relation) {
        CmsRelation existingRelation = getRelationWithTargets(relation.getRelationName());

        if (existingRelation == null) {
            throw new MDException(CmsError.MD_RELATION_NOT_FOUND_ERROR, "Could not find the relation to update. relation_name = " + relation.getRelationName());
        }

        CIValidationResult validation = mdValidator.validateUpdateRelation(relation);
        if (!validation.isValidated()) {
            throw new MDException(CmsError.MD_VALIDATION_ERROR, validation.getErrorMsg());
        }
        relationMapper.updateRelation(relation);

        List<CmsRelationAttribute> existingAttributes = existingRelation.getMdAttributes();
        for (CmsRelationAttribute updAttr : relation.getMdAttributes()) {
            updAttr.setRelationId((existingRelation.getRelationId()));
            CmsRelationAttribute existingAttr = getAttribute(updAttr, existingAttributes);
            if (existingAttr == null) {
                relationMapper.addRelationAttribute(updAttr);
            } else {
                if (relationMapper.getCountCiOfRelation(existingRelation.getRelationId()) > 0
                        && !allowAttributeChanges(existingAttr, updAttr)) {
                    throw new MDException(CmsError.MD_ATTRIBUTE_CONFLICT_ERROR, "Could not update relation " + relation.getRelationName() + ", conflicting attribute: " + updAttr.getAttributeName());
                }
                updAttr.setAttributeId(existingAttr.getAttributeId());
                relationMapper.updateRelationAttribute(updAttr);
                existingAttributes.remove(existingAttr);
            }
        }
        if (!existingAttributes.isEmpty()) {
            for (CmsRelationAttribute attr : existingAttributes) {
                relationMapper.deleteRelationAttribute(attr.getAttributeId(), true);
            }
        }

        List<CmsClazzRelation> existingTagets = existingRelation.getTargets();
        if (!existingTagets.isEmpty()) {
            for (CmsClazzRelation target : existingTagets) {
                relationMapper.deleteRelationTarget(target.getLinkId());
            }
        }

        for (CmsClazzRelation uppTarget : relation.getTargets()) {
            //added ability to specify target wildcard on the package like mgmt.service.*
            for (CmsClazzRelation target : parseTargets(uppTarget)) {
                target.setRelationId(existingRelation.getRelationId());
                relationMapper.addRelationTarget(target);
            }
        }

        return getRelation(relation.getRelationName());
    }

    /**
     * Delete relation.
     *
     * @param relationId the relation id
     * @param deleteAll  the delete all
     */
    public void deleteRelation(int relationId, boolean deleteAll) {
        if (!deleteAll && relationMapper.getCountCiOfRelation(relationId) > 0) {
            throw new MDException(CmsError.MD_NO_DELETE_HAS_CI_ERROR, "Could not delete relation Id: " + relationId + " because of it has some ci.");
        }
        relationMapper.deleteRelation(relationId, deleteAll);
    }

    private List<CmsClazzRelation> parseTargets(CmsClazzRelation link) {

        List<CmsClazz> fromClasses = getClassesByPattern(link.getFromClassName());
        if (fromClasses.size() == 0) {
            throw new MDException(CmsError.MD_CLASS_NOT_FOUND_IN_FROM_ERROR, "Can not find class by pattern: " + link.getFromClassName() + " in FROM target definition for relation: " + link.getRelationName());
        }

        List<CmsClazz> toClasses = getClassesByPattern(link.getToClassName());
        if (toClasses.size() == 0) {
            throw new MDException(CmsError.MD_CLASS_NOT_FOUND_IN_TO_ERROR, "Can not find class by pattern: " + link.getToClassName() + " in TO target definition for relation: " + link.getRelationName());
        }

        List<CmsClazzRelation> targets = new ArrayList<>();

        for (CmsClazz fromClass : fromClasses) {
            for (CmsClazz toClass : toClasses) {
                CmsClazzRelation target = new CmsClazzRelation();
                target.setFromClassId(fromClass.getClassId());
                target.setFromClassName(fromClass.getClassName());
                target.setToClassId(toClass.getClassId());
                target.setToClassName(toClass.getClassName());
                target.setDescription(link.getDescription());
                target.setIsStrong(link.getIsStrong());
                target.setLinkType(link.getLinkType());
                target.setRelationId(link.getRelationId());
                target.setRelationName(link.getRelationName());
                targets.add(target);
            }
        }
        return targets;
    }

    private List<CmsClazz> getClassesByPattern(String pattern) {
        List<CmsClazz> classes = new ArrayList<>();
        if (pattern == null) {
            return classes;
        }
        if (pattern.endsWith("*")) {
            classes.addAll(getClazzesByPackage(pattern));
        } else {
            CmsClazz clazz = getClazz(pattern);
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes;
    }

    public void invalidateCache() {
        this.mdClazzCache.clear();
        this.mdClazzCacheById.clear();
        this.mdRelationCache.clear();
        this.mdRelationCacheById.clear();
        // Clear and init cache.
        this.clazzRelCache.clear();
        initCache();
    }

}
