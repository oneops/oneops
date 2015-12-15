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
package com.oneops.antenna.cache;

import org.apache.log4j.Logger;

/**
 * A sink key used to identify the subscriber list values in Cache. Since the sink
 * configuration is unique for an organization, <b>org</b> name is used as the cache
 * key. {@link java.lang.Object#equals(Object)} and {@link Object#hashCode()} methods
 * have been modified to get this uniqueness.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @version 1.0
 */
public class SinkKey {

    private static Logger logger = Logger.getLogger(SinkKey.class);
    /**
     * Default org name
     */
    public static final String DEFAULT_PATH = "ONEOPS_DEFAULT";
    /**
     * Org namespace
     */
    private String nsPath;
    /**
     * Org name
     */
    private String org;

    /**
     * Constructor for SinkKey
     *
     * @param nsPath nspath
     */
    public SinkKey(String nsPath) {
        this.nsPath = nsPath;
        String[] nsParts = nsPath.split("/");
        if (nsParts.length < 4) {
            logger.warn("NsPath doesn't contains all required parts. Setting the org to " + DEFAULT_PATH);
            // This is probably a cloud service
            org = DEFAULT_PATH;
        } else {
            // Org will be the second token.
            org = nsParts[1];
        }
    }

    /* Fluent interfaces */
    public String getNsPath() {
        return nsPath;
    }

    public SinkKey setNsPath(String nsPath) {
        this.nsPath = nsPath;
        return this;
    }

    public String getOrg() {
        return org;
    }

    public SinkKey setOrg(String org) {
        this.org = org;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SinkKey sinkKey = (SinkKey) o;
        if (org != null ? !org.equals(sinkKey.org) : sinkKey.org != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return org != null ? org.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SinkKey{");
        sb.append("nsPath='").append(nsPath).append('\'');
        sb.append(", org='").append(org).append('\'');
        sb.append('}');
        return sb.toString();
    }


    /**
     * Checks whether the nspath is valid or not
     *
     * @return <code>true</code> if it has a valid nspath, else reurn <code>false</code>
     */
    public boolean hasValidNsPath() {
        return !DEFAULT_PATH.equalsIgnoreCase(org);
    }
}
