package com.oneops.cms.collections;


import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

/**
 * The Class CollectionRfcLink.
 */
public class CollectionRfcLink extends CmsRfcRelation {
	
	private static final long serialVersionUID = 1L;
	
	private CollectionRfcNode linkedNode;
	
	/**
	 * Sets the linked node.
	 *
	 * @param linkedNode the new linked node
	 */
	public void setLinkedNode(CollectionRfcNode linkedNode) {
		this.linkedNode = linkedNode;
	}
	
	/**
	 * Gets the linked node.
	 *
	 * @return the linked node
	 */
	public CollectionRfcNode getLinkedNode() {
		return linkedNode;
	}
	
	/**
	 * Gets the to rfc ci.
	 *
	 * @return the to rfc ci
	 */
	@Override
	public CmsRfcCI getToRfcCi() {
		return linkedNode;
	}
	
}
