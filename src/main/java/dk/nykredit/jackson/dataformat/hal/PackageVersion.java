/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nykredit.jackson.dataformat.hal;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reading module version from Maven properties files.
 */
public final class PackageVersion implements Versioned {

    public static final Version VERSION;

    private static final String MAVEN_PROPERTIES = "/META-INF/maven/dk.nykredit.jackson.dataformat/jackson-dataformat-hal/pom.properties";

    static {
        Version version = Version.unknownVersion();
        InputStream is = PackageVersion.class.getResourceAsStream(MAVEN_PROPERTIES);
        if (is != null) {
            try {
                Properties p = new Properties();
                p.load(is);
                version  = VersionUtil.parseVersion(p.getProperty("version"), p.getProperty("groupId"), p.getProperty("artifactId"));
            } catch (IOException e) {
                // Silently ignore
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
        VERSION = version;
    }

    @Override
    public Version version() {
        return VERSION;
    }

}
