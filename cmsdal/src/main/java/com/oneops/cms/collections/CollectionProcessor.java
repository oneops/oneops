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

import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import java.util.ArrayList;
import java.util.List;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.collections.def.CollectionLinkDefinition;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;

/**
 * The Class CollectionProcessor.
 */
public class CollectionProcessor {

  private static final String RELATION_ATTRIBUTE_SUFFIX = ".";
  private CmsCmRfcMrgProcessor cmrfcProcessor;
	private CmsCmProcessor cmProcessor;
	
	/**
	 * Sets the cmrfc processor.
	 *
	 * @param cmrfcProcessor the new cmrfc processor
	 */
	public void setCmrfcProcessor(CmsCmRfcMrgProcessor cmrfcProcessor) {
		this.cmrfcProcessor = cmrfcProcessor;
	}

	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	/**
	 * Gets the flat collection.
	 *
	 * @param anchorCiId the anchor ci id
	 * @param relationDef the relation def
	 * @return the flat collection
	 */
	public List<CmsCI> getFlatCollection(long anchorCiId, CollectionLinkDefinition relationDef) {
		
		List<CmsCI> result = new ArrayList<CmsCI>();
		
		if ("from".equalsIgnoreCase(relationDef.getDirection())) {
			List<CmsCIRelation> relations = null;
			if (relationDef.getRelationAttrs() != null && relationDef.getRelationAttrs().size()>0 ) {
				relations = cmProcessor.getFromCIRelationsByAttrsNaked(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName(), relationDef.getRelationAttrs());
			} else {
				relations = cmProcessor.getFromCIRelationsNakedNoAttrs(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName());
			}
			for (CmsCIRelation rel : relations) {
				if (relationDef.getReturnObject()) {
				    result.add(addRelationAttributes(relationDef,rel));
				}
				if (relationDef.getRelations() != null) {
					for (CollectionLinkDefinition relDef : relationDef.getRelations()) {
						result.addAll(getFlatCollection(rel.getToCiId(), relDef));
					}
				}
			}
		} else {

			List<CmsCIRelation> relations = null;
			if (relationDef.getRelationAttrs() != null && relationDef.getRelationAttrs().size()>0 ) {
				relations = cmProcessor.getToCIRelationsByAttrsNaked(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName(),relationDef.getRelationAttrs());
			} else {	
				relations = cmProcessor.getToCIRelationsNakedNoAttrs(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName());
			}
			for (CmsCIRelation rel : relations) {
				if (relationDef.getReturnObject()) {
          result.add(addRelationAttributes(relationDef,rel));
				}
				if (relationDef.getRelations() != null) {
					for (CollectionLinkDefinition relDef : relationDef.getRelations()) {
						result.addAll(getFlatCollection(rel.getFromCiId(), relDef));
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Gets the flat collection rfc.
	 * @param anchorCiId the anchor ci id
	 * @param relationDef the relation def
	 * @return the flat collection rfc
	 */
	public List<CmsRfcCI> getFlatCollectionRfc(long anchorCiId, CollectionLinkDefinition relationDef) {
		
		List<CmsRfcCI> result = new ArrayList<>();
		
		if ("from".equalsIgnoreCase(relationDef.getDirection())) {
			List<CmsRfcRelation> relations = null;
			if (relationDef.getRelationAttrs() != null && relationDef.getRelationAttrs().size()>0 ) {
				relations = cmrfcProcessor.getFromCIRelationsByAttrs(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName(), "df", relationDef.getRelationAttrs());
			} else {	
				relations = cmrfcProcessor.getFromCIRelationsNakedNoAttrs(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName());
			}
			for (CmsRfcRelation rel : relations) {
				if (relationDef.getReturnObject()) {
          result.add(addRelationAttributes(relationDef, rel));
				}
				//
				if (relationDef.getRelations() != null) {
					for (CollectionLinkDefinition relDef : relationDef.getRelations()) {
						result.addAll(getFlatCollectionRfc(rel.getToCiId(), relDef));
					}
				}
			}
		} else {
			List<CmsRfcRelation> relations = null;
			if (relationDef.getRelationAttrs() != null && relationDef.getRelationAttrs().size()>0 ) {
				relations = cmrfcProcessor.getToCIRelationsByAttrs(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName(), "df", relationDef.getRelationAttrs());
			} else {	
				relations = cmrfcProcessor.getToCIRelationsNakedNoAttrs(anchorCiId, relationDef.getRelationName(), relationDef.getRelationShortName(), relationDef.getTargetClassName());
			}
			for (CmsRfcRelation rel : relations) {
				if (relationDef.getReturnObject()) {
          result.add(addRelationAttributes(relationDef, rel));
        }
				if (relationDef.getRelations() != null) {
					for (CollectionLinkDefinition relDef : relationDef.getRelations()) {
						result.addAll(getFlatCollectionRfc(rel.getFromCiId(), relDef));
					}
				}
			}
		}
		
		return result;
	}

  private CmsRfcCI addRelationAttributes(CollectionLinkDefinition relationDef, CmsRfcRelation rel) {
    final CmsRfcCI aRfc = cmrfcProcessor.getCiById(rel.getToCiId(), "df");
    if(relationDef.getReturnRelationAttributes()){
      rel.getAttributes().forEach((k,v)->{
        CmsRfcAttribute attribute = new CmsRfcAttribute();
        attribute.setAttributeName(rel.getRelationName()+RELATION_ATTRIBUTE_SUFFIX+k);
        attribute.setNewValue(v.getNewValue());
        attribute.setOldValue(v.getOldValue());
        aRfc.getAttributes().put(attribute.getAttributeName(),attribute);
      });

    }
    return aRfc;
  }

  private CmsCI addRelationAttributes(CollectionLinkDefinition relationDef, CmsCIRelation rel) {
    final CmsCI aCi = cmProcessor.getCiById(rel.getToCiId());
    if(relationDef.getReturnRelationAttributes()){
      rel.getAttributes().forEach((k,v)->{
        CmsCIAttribute attribute = new CmsCIAttribute();
        attribute.setAttributeName(rel.getRelationName()+ RELATION_ATTRIBUTE_SUFFIX +k);
        attribute.setDfValue(v.getDfValue());
        attribute.setDjValue(v.getDjValue());
        aCi.getAttributes().put(attribute.getAttributeName(),attribute);
      });

    }
    return aCi;
  }


  /**
     * Upsert collection rfc.
     *
     * @param rfcNode the rfc node
     * @param userId the user id
     */
    public void upsertCollectionRfc(CollectionRfcNode rfcNode, String userId) {
    	CmsRfcCI rfc = cmrfcProcessor.upsertCiRfc(rfcNode, userId);

        for(CollectionRfcLink link: rfcNode.getRelations()) {
        	link.setFromCiId(rfc.getCiId());
        	link.setFromRfcId(rfc.getRfcId());
            cmrfcProcessor.upsertRelationRfc(link, userId);
            upsertCollectionRfc(link.getLinkedNode(), userId);
        }
    }

    /**
     * Upsert collection rfc.
     *
     * @param rfcNodeList the rfc node list
     * @param userId the user id
     */
    public void upsertCollectionRfc(List<CollectionRfcNode> rfcNodeList, String userId) {
        for(CollectionRfcNode rfcNode: rfcNodeList) {
            upsertCollectionRfc(rfcNode, userId);
        }
    }

    /**
     * Upsert collection ci.
     *
     * @param ciNode the ci node
     */
    public void upsertCollectionCi(CollectionNode ciNode) {
        CmsCI ci = cmProcessor.upsertCmsCI(ciNode);

        for(CollectionLink link: ciNode.getRelations()) {
        	link.setFromCiId(ci.getCiId());
            cmProcessor.upsertRelation(link);
            upsertCollectionCi(link.getLinkedNode());
        }
    }

    /**
     * Upsert collection ci.
     *
     * @param ciNodeList the ci node list
     */
    public void upsertCollectionCi(List<CollectionNode> ciNodeList) {
        for(CollectionNode ciNode: ciNodeList) {
            upsertCollectionCi(ciNode);
        }
    }

}