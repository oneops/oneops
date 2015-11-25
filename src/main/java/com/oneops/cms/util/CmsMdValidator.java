package com.oneops.cms.util;

import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.service.CmsMdManager;

/**
 * The Class CmsMdValidator.
 */
public class CmsMdValidator {

    @SuppressWarnings("unused")
	private CmsMdManager mdManager;

    /**
     * Sets the md manager.
     *
     * @param mdManager the new md manager
     */
    public void setMdManager(CmsMdManager mdManager) {
        this.mdManager = mdManager;
    }

    /**
     * Validate update clazz.
     *
     * @param clazz the clazz
     * @return the cI validation result
     */
    public CIValidationResult validateUpdateClazz(CmsClazz clazz) {
        CIValidationResult result = new CIValidationResult();
        result.setValidated(true);
        //empty validation still
        return result;
    }


    /**
     * Validate update relation.
     *
     * @param relation the relation
     * @return the cI validation result
     */
    public CIValidationResult validateUpdateRelation(CmsRelation relation) {
        CIValidationResult result = new CIValidationResult();
        result.setValidated(true);
        //empty validation still
        return result;
    }

}
