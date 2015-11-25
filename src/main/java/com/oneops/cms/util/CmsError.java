package com.oneops.cms.util;

/**
 * Represents internal error
 */
public interface CmsError {

    /**
     * Fist digit of code is type of error:
     *
     * 1 - CmsException
     * 2 - DJException
     * 3 - MDException
     * 4 - OpsException
     * 5 - CmsSecurityException
     */
    final static int CMS_EXCEPTION = 1;
    final static int DJ_EXCEPTION = 2;
    final static int MD_EXCEPTION = 3;
    final static int OPS_EXCEPTION = 4;
    final static int VALIDATION_EXCEPTION = 5;
    final static int TRANSISTOR_EXCEPTION = 6;
    final static int RUNTIME_EXCEPTION = 9;

    /* CMS errors */
    final static int CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR = 1001;
    final static int CMS_CI_OF_NS_CLASS_NAME_ALREADY_EXIST_ERROR = 1002;
    final static int CMS_BAD_WO_CLASS_ERROR = 1003;
    final static int CMS_CANT_FIND_BINDING_FOR_CI_ID_ERROR = 1004;
    final static int CMS_CANT_FIND_REALIZEDAS_FOR_BOMC_ERROR = 1005;
    final static int CMS_NO_CI_WITH_GIVEN_ID_ERROR = 1006;
    final static int CMS_NO_RELATION_WITH_GIVEN_ID_ERROR = 1007;
    final static int CMS_CANT_FIND_REQUIRES_FOR_CI_ERROR = 1008;
    final static int CMS_DUPCI_NAME_ERROR = 1008;
    
    final static int DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR = 2001;
    final static int DJ_CANT_RESOLVE_RELEASE_STATE_ERROR = 2002;
    final static int DJ_CANT_RESOLVE_CI_STATE_ERROR = 2003;
    final static int DJ_CANT_RESOLVE_NAMESPACE_ERROR = 2004;
    final static int DJ_MUST_SPECIFY_USER_ID_ERROR = 2005;
    final static int DJ_RFC_RELEASE_NOT_OPEN_ERROR = 2006;
    final static int DJ_NO_CI_WITH_GIVEN_ID_ERROR = 2007;
    final static int DJ_VALIDATION_ERROR = 2008;
    final static int DJ_RFC_DOESNT_EXIST_ERROR = 2009;
    final static int DJ_NO_RELATION_WITH_GIVEN_ID_ERROR = 2010;
    final static int DJ_MORE_THEN_ONE_RFC_IN_RELEASE_ERROR = 2011;
    final static int DJ_RELATION_RFC_DOESNT_EXIST_ERROR = 2012;
    final static int DJ_STATE_ALREADY_DEPLOYMENT_ERROR = 2013;
    final static int DJ_DEPLOYMENT_NOT_ACTIVE_ERROR = 2014;
    final static int DJ_DEPLOYMENT_NOT_FAILED_ERROR = 2015;
    final static int DJ_DEPLOYMENT_NOT_ACTIVE_FAILED_ERROR = 2016;
    final static int DJ_NOT_SUPPORTED_STATE_ERROR = 2017;
    final static int DJ_INCONSITENCY_WITH_DEPLOYMENT_RECORD_ERROR = 2018;
    final static int DJ_SYSTEM_ERROR = 2019;
    final static int DJ_CI_RFC_WITH_THIS_NAME_ALREADY_EXIST_ERROR = 2019;
    final static int DJ_CI_ID_IS_NEED_ERROR = 2020;
    final static int DJ_MUST_SPECIFY_CI_ID_OR_NSPATH_ERROR = 2021;
    final static int DJ_NO_DEPLOYMENT_WITH_GIVEN_ID_ERROR = 2022;
    final static int DJ_NO_RELEASE_WITH_GIVEN_ID_ERROR = 2023;
    final static int DJ_NO_RFC_WITH_GIVEN_ID_ERROR = 2024;
    final static int DJ_OPEN_RELEASE_WRONG_TYPE_ERROR = 2025;
    final static int DJ_BAD_CI_NAME_ERROR = 2026;
 


    final static int MD_SUPERCLASS_NOT_FOUND_ERROR = 3001;
    final static int MD_METADATA_NOT_FOUND_ERROR = 3002;
    final static int MD_VALIDATION_ERROR = 3003;
    final static int MD_CONFLICT_DATA_TYPE_ERROR = 3004;
    final static int MD_NO_DELETE_HAS_CI_ERROR = 3005;
    final static int MD_RELATION_NOT_FOUND_ERROR = 3006;
    final static int MD_ATTRIBUTE_CONFLICT_ERROR = 3007;
    final static int MD_CLASS_NOT_FOUND_IN_FROM_ERROR = 3008;
    final static int MD_CLASS_NOT_FOUND_IN_TO_ERROR = 3009;
    final static int MD_TARGET_IS_MISSING_ERROR = 3010;
    final static int MD_NO_CLASS_WITH_GIVEN_ID_ERROR = 3011;
    final static int MD_NO_CLASS_WITH_GIVEN_NAME_ERROR = 3012;
    final static int MD_NO_RELATION_WITH_GIVEN_NAME_ERROR = 3013;

    final static int OPS_THERE_IS_NO_PROCEDURE_DEFINITION_ERROR = 4001;
    final static int OPS_ONE_ACTION_MUST_BE_ERROR = 4002;
    final static int OPS_ALREADY_HAVE_ACTIVE_PROCEDURE_ERROR = 4003;
    final static int OPS_ALREADY_HAVE_OPENED_RELEASE_ERROR = 4004;
    final static int OPS_PROCEDURE_NOT_FOUND_ERROR = 4005;
    final static int OPS_MUST_SPECIFY_CI_ID_OR_NSPATH_ERROR = 4006;
    final static int OPS_ALREADY_HAVE_ACTIVE_DEPLOYMENT_ERROR = 4007;
    final static int OPS_ALREADY_HAVE_ACTIVE_ACTION_ERROR = 4008;
    final static int OPS_ACTION_IS_NOT_IN_PROGRESS = 4009;

    final static int VALIDATION_COMMON_ERROR = 5001;
    final static int VALIDATION_COULDNT_FIND_CI_FOR_UPDATE_ERROR = 5002;
    final static int VALIDATION_PROCEDURE_ID_OR_DEFINITION_SHOULD_BE_ERROR = 5003;

    final static int TRANSISTOR_ACTIVE_DEPLOYMENT_EXISTS = 6001;
    final static int TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF = 6002;
    final static int TRANSISTOR_CANNOT_TRAVERSE = 6003;
    final static int TRANSISTOR_BAD_NS_PATH = 6004;
    final static int TRANSISTOR_BAD_SCOPE = 6005;
    final static int TRANSISTOR_CANNOT_GET_ASSEMBLY = 6006;
    final static int TRANSISTOR_CANNOT_AVAILABILITY_MODE = 6007;
    final static int TRANSISTOR_CANNOT_CORRESPONDING_OBJECT = 6008;
    final static int TRANSISTOR_BAD_CLASS_NAME = 6009;
    final static int TRANSISTOR_CANNOT_ORG_BY_SCOPE = 6010;
    final static int TRANSISTOR_CANNOT_FIND_ASSEMBLY = 6011;
    final static int TRANSISTOR_CANNOT_FIND_ENVIRONMENT = 6012;
    final static int TRANSISTOR_ENVIRONMENT_IN_LOCKED_STATE = 6013;
    final static int TRANSISTOR_OPEN_MANIFEST_RELEASE = 6014;
    final static int TRANSISTOR_OPEN_BOM_RELEASE = 6014;
    final static int TRANSISTOR_BOM_INSTANCES_EXIST = 6015;
    final static int TRANSISTOR_BOM_GENERATION_FAILED = 6016;


    int getErrorCode();

}
