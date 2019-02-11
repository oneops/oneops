package com.oneops.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.Manifest;

/**
 * {@code Version} wrapper around manifest meta-inf to get build meta-information.
 *
 * @author Gaurav Lall
 */

@Component
public class Version {

    private static Logger logger = Logger.getLogger(Version.class);
    private final HashMap<String, String> buildInfo = new HashMap<>(5);
    private ServletContext servletContext;

    @Value("${oo.version.Path:/META-INF/MANIFEST.MF}")
    private  String manifestPath ;

    @Autowired(required=false)
    public void setContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    @PostConstruct
    public void init() {
        if (servletContext != null) {
            try {

                final InputStream servletStream = servletContext.getResourceAsStream(manifestPath);
                if (servletStream != null) {
                    Manifest mf = new Manifest(servletStream);
                    mf.getMainAttributes().forEach(
                            (k, v) -> buildInfo.put(k.toString(), v.toString())
                    );
                }
                logger.info("Version : "+buildInfo);
            } catch (IOException e) {
                logger.error("Exception in getting the manifest-info", e);
            }
        }
    }

    public String getGitVersion() {
        return get("gitTag");
    }

    public String get(String key) {
        return buildInfo.get(key);
    }

}
