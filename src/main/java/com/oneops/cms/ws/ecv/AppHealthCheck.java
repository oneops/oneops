package com.oneops.cms.ws.ecv;

import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.ecv.health.Health;
import com.oneops.ecv.health.IHealth;
import com.oneops.ecv.health.IHealthCheck;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


public class AppHealthCheck implements IHealthCheck {
    private static Logger logger = Logger.getLogger(AppHealthCheck.class);

    @Autowired
     private CmsMdManager mdManager;
    private static final int DEFAULT_CID = Integer.valueOf(System.getProperty("cms.defaultClassId", "100"));

    @Override
    public IHealth getHealth() {
        IHealth health = Health.FAILED_HEALTH;
        try {
            CmsClazz clazz = mdManager.getClazz(DEFAULT_CID);
            health = Health.OK_HEALTH;
        } catch (Throwable e) {
            logger.error("Exception occurred determining health", e);
            health = new Health(HttpStatus.INTERNAL_SERVER_ERROR.value(), Boolean.FALSE, e.getMessage(), getName());
        }
        return health;
    }

    @Override
    public String getName() {
        return "cms.Health";
    }
}
