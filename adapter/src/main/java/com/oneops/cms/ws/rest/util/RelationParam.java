package com.oneops.cms.ws.rest.util;

import com.oneops.cms.util.domain.AttrQueryCondition;

import java.util.List;

public class RelationParam {

    private String relationName;
    private String nsPath;
    private boolean recursive;
    private Long fromCiId;
    private Long toCiId;
    private String fromClass;
    private String targetClass;
    private String groupBy;
    private List<AttrQueryCondition> conditions;

    public RelationParam(String relationName, String relationShortName, String nsPath, Boolean recursive, Long ciId, String direction, String fromClass, String targetClass, String groupBy, List<AttrQueryCondition> conditions) {
        this.relationName = relationName == null ? relationShortName : relationName;
        this.nsPath = nsPath;
        this.recursive = recursive == null ? false : recursive;
        if ("from".equalsIgnoreCase(direction)) {
            fromCiId = ciId;
        } else if ("to".equalsIgnoreCase(direction)) {
            toCiId = ciId;
        }
        this.fromClass = fromClass;
        this.targetClass = targetClass;
        this.groupBy = groupBy;
        this.conditions = conditions;
    }

    public String getRelationName() {
        return relationName;
    }

    public String getNsPath() {
        return nsPath;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public Long getFromCiId() {
        return fromCiId;
    }

    public Long getToCiId() {
        return toCiId;
    }

    public String getFromClass() {
        return fromClass;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public List<AttrQueryCondition> getConditions() {
        return conditions;
    }
}
