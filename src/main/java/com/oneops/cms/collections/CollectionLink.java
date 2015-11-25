package com.oneops.cms.collections;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.collections.CollectionNode;

/**
 * The Class CollectionLink.
 */
public class CollectionLink extends CmsCIRelation {
	
	private static final long serialVersionUID = -8581563728690157243L;
	private CollectionNode linkedNode;
	
	/**
	 * Sets the linked node.
	 *
	 * @param linkedNode the new linked node
	 */
	public void setLinkedNode(CollectionNode linkedNode) {
		this.linkedNode = linkedNode;
	}
	
	/**
	 * Gets the linked node.
	 *
	 * @return the linked node
	 */
	public CollectionNode getLinkedNode() {
		return linkedNode;
	}
	
	/**
	 * Gets the to ci.
	 *
	 * @return the to ci
	 */
	@Override
	public CmsCI getToCi() {
		return linkedNode;
	}
}
