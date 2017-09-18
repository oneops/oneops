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
	private boolean returnRelationAttributes = false;
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

  public boolean getReturnRelationAttributes() {
    return returnRelationAttributes;
  }

  public void setReturnRelationAttributes(boolean returnRelationAttributes) {
    this.returnRelationAttributes = returnRelationAttributes;
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
