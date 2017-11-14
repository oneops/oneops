package com.oneops.cms.util;

import static com.oneops.cms.util.CmsConstants.ACTION_ORDER_TYPE;
import static com.oneops.cms.util.CmsConstants.WORK_ORDER_TYPE;

import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by glall on 5/13/17.
 */
public class Masker {

  private static final String MASK = "##############";

  /**
   * Masks the secured attributes in work-orders and action-orders
   *
   * @param cmsWoSimpleBase simple work order for which attributes need to be secured
   * @param type            of the workOrder or actionOrder
   * @return secured workOrder.
   */
  public static <T> CmsWorkOrderSimpleBase maskSecuredFields(CmsWorkOrderSimpleBase<T> cmsWoSimpleBase, String type) {

    //service CIs
    if (cmsWoSimpleBase.getServices() != null) {
      for (Entry<String, Map<String, CmsCISimple>> serviceEntry : cmsWoSimpleBase.getServices().entrySet()) {
        for (CmsCISimple ci : serviceEntry.getValue().values()) {
          maskSecure(ci);
        }
      }
    }

    //result CI
    if (cmsWoSimpleBase.getResultCi() != null) {
      CmsCISimple resultCi = cmsWoSimpleBase.getResultCi();
      maskSecure(resultCi);
    }

    //cloud CI
    if (cmsWoSimpleBase.getCloud() != null) {
      CmsCISimple cloudCi = cmsWoSimpleBase.getCloud();
      maskSecure(cloudCi);
    }

    //box CI
    if (cmsWoSimpleBase.getBox() != null) {
      CmsCISimple boxCi = cmsWoSimpleBase.getBox();
      maskSecure(boxCi);
    }

    //work-order: pay-load and rfcCI
    if (WORK_ORDER_TYPE.equals(type)) {
      CmsWorkOrderSimple cmsWo = (CmsWorkOrderSimple) cmsWoSimpleBase;
      if (cmsWo.getPayLoad() != null) {
        for (Entry<String, List<CmsRfcCISimple>> payloadEntry : cmsWo.getPayLoad().entrySet()) {
          for (CmsRfcCISimple rfcCi : payloadEntry.getValue()) {
            maskSecure(rfcCi);
          }
        }
      }

      if (cmsWo.getRfcCi() != null) {
        maskSecure(cmsWo.getRfcCi());
      }
    }


    //action-order: pay-load and CI
    if (ACTION_ORDER_TYPE.equals(type)) {
      CmsActionOrderSimple cmsAo = (CmsActionOrderSimple) cmsWoSimpleBase;
      if (cmsAo.getPayLoad() != null) {
        for (Entry<String, List<CmsCISimple>> payloadEntry : cmsAo.getPayLoad().entrySet()) {
          for (CmsCISimple ci : payloadEntry.getValue()) {
            maskSecure(ci);
          }
        }
      }

      if (cmsAo.getCi() != null) {
        maskSecure(cmsAo.getCi());
      }
    }

    return cmsWoSimpleBase;
  }

  /**
   * Masks the secured CI attributes
   *
   * @param ci the ci whose attributes need to be secured.
   */
  private static void maskSecure(CmsCISimple ci) {
    if (ci.getAttrProps() != null && ci.getAttrProps().get(CmsConstants.SECURED_ATTRIBUTE) != null) {
      for (Entry<String, String> secAttr : ci.getAttrProps().get(CmsConstants.SECURED_ATTRIBUTE).entrySet()) {
        if ("true".equals(secAttr.getValue())) {
          ci.getCiAttributes().put(secAttr.getKey(), MASK);
        }
      }
      ci.getAttrProps().remove(CmsConstants.ENCRYPTED_ATTR_VALUE);
    }
  }

  /**
   * Masks the secured RfcCI attributes
   *
   * @param rfcCI rfcCI for which attributes need to be secured.
   */
  private static void maskSecure(CmsRfcCISimple rfcCI) {
    if (rfcCI.getCiAttrProps() != null && rfcCI.getCiAttrProps().get(CmsConstants.SECURED_ATTRIBUTE) != null) {
      for (Entry<String, String> secAttr : rfcCI.getCiAttrProps().get(CmsConstants.SECURED_ATTRIBUTE).entrySet()) {
        if ("true".equals(secAttr.getValue())) {
          rfcCI.getCiAttributes().put(secAttr.getKey(), MASK);
        }
      }
    }
  }
}
