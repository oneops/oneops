package com.oneops.cms.ws.rest.util;

public class RelationParam {

    private String nsPath;
    private Long ciId;
    private String direction;
    private String relationName;
    private String shortRelationName;
    private String targetClazz;
    private String fromClazz;
    private boolean recursive;
    private String groupBy;

    public RelationParam(String nsPath, String relationName, String shortRelationName, String targetClazz, boolean recursive) {
        this.nsPath = nsPath;
        this.relationName = relationName;
        this.shortRelationName = shortRelationName;
        this.targetClazz = targetClazz;
        this.recursive = recursive;
    }

    public String getNsPath() {
        return nsPath;
    }

    public void setNsPath(String nsPath) {
        this.nsPath = nsPath;
    }

    public Long getCiId() {
        return ciId;
    }

    public void setCiId(Long ciId) {
        this.ciId = ciId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getShortRelationName() {
        return shortRelationName;
    }

    public void setShortRelationName(String shortRelationName) {
        this.shortRelationName = shortRelationName;
    }

    public String getTargetClazz() {
        return targetClazz;
    }

    public void setTargetClazz(String targetClazz) {
        this.targetClazz = targetClazz;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getFromClazz() {
        return fromClazz;
    }

    public void setFromClazz(String fromClazz) {
        this.fromClazz = fromClazz;
    }
}
