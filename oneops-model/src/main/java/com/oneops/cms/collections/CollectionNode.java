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
package com.oneops.cms.collections;

import com.oneops.cms.cm.domain.CmsCI;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class CollectionNode.
 */
public class CollectionNode extends CmsCI {

  private static final long serialVersionUID = -7886638965176209510L;

  private Set<CollectionLink> relations;

  /**
   * Gets the relations.
   *
   * @return the relations
   */
  public Set<CollectionLink> getRelations() {
    return relations;
  }

  /**
   * Sets the relations.
   *
   * @param relations the new relations
   */
  public void setRelations(Set<CollectionLink> relations) {
    this.relations = relations;
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
