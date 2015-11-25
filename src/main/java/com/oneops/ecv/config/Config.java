package com.oneops.ecv.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The ecv configurtation is backed on file backed via property.
 */
public class Config {
    //Change the local config file
    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String SHUTDOWN = "shutdown";

    public static final String STATUS_PROP_NAME = "status";
    private static final String ENABLE_HEALTH_CHECK_PROP_NAME = "ecv.enableHealthCheck";
    private static final Logger ECV_LOGGER = LoggerFactory.getLogger(Config.class);

    private Properties properties = new Properties();
    //using it to avoid synchronized , calls on the getProp.
    private HashMap<String, String> internal_props = new HashMap<String, String>();
    private String localConfigDir;
    private String localConfigFile;

    public Config() {

    }

    /**
     *
     *
     * @throws java.io.IOException
     */
    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        createLocalConfigDirectory();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getLocalConfigFile());
            properties.load(fis);
        } catch (FileNotFoundException ex) {
            // creates the configuration file and set default properties
            setDefaults();
            save();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        internal_props = new HashMap<String, String>((Map) properties);
        ECV_LOGGER.info("ECV initialized with these props " + internal_props + " using configFile" + getLocalConfigFile());
    }

    public void setDefaults() {
        properties.put(STATUS_PROP_NAME, ONLINE);
        properties.put(ENABLE_HEALTH_CHECK_PROP_NAME, System.getProperty(ENABLE_HEALTH_CHECK_PROP_NAME, Boolean.TRUE.toString()));
    }

    public void save() throws IOException {
        FileOutputStream fos = null;
        try {
            File props = new File(getLocalConfigFile());
            if (!props.exists()) {
                props.createNewFile();
            }
            fos = new FileOutputStream(getLocalConfigFile());
            properties.store(fos, "My Application Settings");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }


    private void setProperty(String key, String value) {
        properties.setProperty(key, value);
        internal_props.put(key, value);
        try {
            save();
        } catch (IOException e) {
            ECV_LOGGER.error("Exception occured while persisting the state of ecv. ");
            throw new ConfigException(" Error while persisting the property",e);
        }
    }

    /**
     * This method creates a localConfig directory if one does not exist.
     *
     * @return created Or not.
     */
    private void createLocalConfigDirectory() throws IOException {
        File file = new File(getLocalConfigDir());
        try {
            file.mkdir();
        } catch (Exception e) {
            final String message = "Unable to create a directory " + getLocalConfigDir() + " :" + e.getMessage();
            throw new IOException(message, e);
        }
    }


    public String getStatus() {
        return internal_props.get(STATUS_PROP_NAME);
    }

    public void setStatus(String status) {
        setProperty(STATUS_PROP_NAME, status);
    }

    public void setProperty(String status) {
        setProperty(STATUS_PROP_NAME, status);
    }

    public void setTransientStatus(String status) {
        internal_props.put(STATUS_PROP_NAME, status);
    }

    public Map<String,String> getInternalConfig(){
        return internal_props;
    }

    public boolean isHealthCheckEnabled() {
        return Boolean.valueOf(internal_props.get(ENABLE_HEALTH_CHECK_PROP_NAME));
    }

    public void disableHealthCheck() {
        setProperty(ENABLE_HEALTH_CHECK_PROP_NAME, Boolean.FALSE.toString());
    }

    public void enableHealthCheck() {
        setProperty(ENABLE_HEALTH_CHECK_PROP_NAME, Boolean.TRUE.toString());
    }

    public String getLocalConfigDir() {
        return localConfigDir;
    }

    public void setLocalConfigDir(String localConfigDir) {
        this.localConfigDir = localConfigDir;
    }

    public String getLocalConfigFile() {
        return localConfigDir + "/" + localConfigFile;
    }

    public void setLocalConfigFile(String localConfigFile) {
        this.localConfigFile = localConfigFile;
    }


}
