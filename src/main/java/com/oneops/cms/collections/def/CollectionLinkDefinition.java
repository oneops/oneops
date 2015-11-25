package com.oneops.cms.collections.def;

import java.io.Serializable;
import java.util.List;

import com.oneops.cms.util.domain.AttrQueryCondition;

/**
 * The Class CollectionLinkDefinition.
 */
public class CollectionLinkDefinition extends BasicLinkDefinition implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private boolean returnObject = false;
	private boolean returnRelation = false;
	private List<AttrQueryCondition> relationAttrs = null;
	private List<AttrQueryCondition> targetAttrs = null;
	private List<CollectionLinkDefinition> relations = null;
	
	/**
	 * Gets the relation attrs.
	 *
	 * @return the relation attrs
	 */
	public List<AttrQueryCondition> getRelationAttrs() {
		return relationAttrs;
	}
	
	/**
	 * Sets the relation attrs.
	 *
	 * @param relationAttrs the new relation attrs
	 */
	public void setRelationAttrs(List<AttrQueryCondition> relationAttrs) {
		this.relationAttrs = relationAttrs;
	}
	
	/**
	 * Gets the target attrs.
	 *
	 * @return the target attrs
	 */
	public List<AttrQueryCondition> getTargetAttrs() {
		return targetAttrs;
	}
	
	/**
	 * Sets the target attrs.
	 *
	 * @param targetAttrs the new target attrs
	 */
	public void setTargetAttrs(List<AttrQueryCondition> targetAttrs) {
		this.targetAttrs = targetAttrs;
	}
	
	/**
	 * Gets the return object.
	 *
	 * @return the return object
	 */
	public boolean getReturnObject() {
		return returnObject;
	}
	
	/**
	 * Sets the return object.
	 *
	 * @param returnObject the new return object
	 */
	public void setReturnObject(boolean returnObject) {
		this.returnObject = returnObject;
	}

	/**
	 * Gets the return relation.
	 *
	 * @return the return relation
	 */
	public boolean getReturnRelation() {
		return returnRelation;
	}
	
	/**
	 * Sets the return relation.
	 *
	 * @param returnRelation the new return relation
	 */
	public void setReturnRelation(boolean returnRelation) {
		this.returnRelation = returnRelation;
	}

	/**
	 * Sets the relations.
	 *
	 * @param relations the new relations
	 */
	public void setRelations(List<CollectionLinkDefinition> relations) {
		this.relations = relations;
	}
	
	/**
	 * Gets the relations.
	 *
	 * @return the relations
	 */
	public List<CollectionLinkDefinition> getRelations() {
		return relations;
	}
}
