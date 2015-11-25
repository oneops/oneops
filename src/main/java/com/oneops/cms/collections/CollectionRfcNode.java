package com.oneops.cms.collections;

import java.util.HashSet;
import java.util.Set;

import com.oneops.cms.dj.domain.CmsRfcCI;

/**
 * The Class CollectionRfcNode.
 */
public class CollectionRfcNode extends CmsRfcCI {
	
	private static final long serialVersionUID = 1L;
	
	private Set<CollectionRfcLink> relations;

	/**
	 * Sets the relations.
	 *
	 * @param relations the new relations
	 */
	public void setRelations(Set<CollectionRfcLink> relations) {
		this.relations = relations;
	}

	/**
	 * Gets the relations.
	 *
	 * @return the relations
	 */
	public Set<CollectionRfcLink> getRelations() {
		return relations;
	} 

	/**
	 * Adds the relations.
	 *
	 * @param relation the relation
	 */
	public void addRelations(CollectionRfcLink relation) {
		if (this.relations == null) {
			this.relations = new HashSet<CollectionRfcLink>();
		}
	    this.relations.add(relation);
	}
	
	
}
