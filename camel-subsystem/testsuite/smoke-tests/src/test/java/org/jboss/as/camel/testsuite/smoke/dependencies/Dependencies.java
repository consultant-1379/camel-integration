/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package org.jboss.as.camel.testsuite.smoke.dependencies;

import java.io.File;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public class Dependencies {

    public static final String ORG_APACHE_CAMEL_CORE = "org.apache.camel:camel-core";
    public static final String ORG_JBOSS_CAMEL_INTEGRATION_API = "org.jboss.as.camel:camel-integration-api-module";

    /**
     * Maven resolver that will try to resolve dependencies using pom.xml of the project where this class is located.
     * 
     * @return MavenDependencyResolver
     */
    public static PomEquippedResolveStage getMavenResolver() {
        return Maven.resolver().loadPomFromFile("pom.xml");
        //return DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom("pom.xml");

    }

    /**
     * Resolve artifacts without dependencies
     * 
     * @param artifactCoordinates
     * @return
     */
    public static File resolveArtifactWithoutDependencies(final String artifactCoordinates) {
        final File[] artifacts = getMavenResolver().resolve(artifactCoordinates).withoutTransitivity().asFile();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates " + artifactCoordinates + " was not resolved");
        }
        if (artifacts.length != 1) {
            throw new IllegalStateException("Resolved more then one artifact with coordinates " + artifactCoordinates);
        }
        return artifacts[0];
    }

}
