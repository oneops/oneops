package com.oneops;

public class Util {

    /**
     * Builds a release rfcNsPath for a rfc rfcNsPath.
     *
     * @param rfcNsPath - rfc NsPath
     */
    public static String getReleaseNsPath(String rfcNsPath) {
        //  /oneops/montest/mtest/bom/custom/1
        // Lets strip off platform parts for the release
        String[] nsParts = rfcNsPath.split("/");
        StringBuilder releaseNs = new StringBuilder();
        for (int i = 1; i < nsParts.length; i++) {
            if (nsParts[i].equals("_design")) {
                break;
            }
            releaseNs.append("/").append(nsParts[i]);
            if (nsParts[i].equals("bom") || nsParts[i].equals("manifest")) {
                break;
            }
        }
        return releaseNs.toString();
    }
}
