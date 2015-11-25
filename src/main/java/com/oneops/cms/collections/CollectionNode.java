package com.oneops.cms.collections;

import java.util.HashSet;
import java.util.Set;

import com.oneops.cms.cm.domain.CmsCI;

/**
 * The Class CollectionNode.
 */
public class CollectionNode extends CmsCI {
	
	private static final long serialVersionUID = -7886638965176209510L;

	private Set<CollectionLink> relations;

	/**
	 * Sets the relations.
	 *
	 * @param relations the new relations
	 */
	public void setRelations(Set<CollectionLink> relations) {
		this.relations = relations;
	}

	/**
	 * Gets the relations.
	 *
	 * @return the relations
	 */
	public Set<CollectionLink> getRelations() {
		return relations;
	} 

	/**
	 * Adds the relations.
	 *
	 * @param relation the relation
	 */
	public void addRelations(CollectionLink relation) {
		if (this.relations == null) {
			this.relations = new HashSet<CollectionLink>();
		}
	    this.relations.add(relation);
	}
	
	
}
