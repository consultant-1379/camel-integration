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
package org.jboss.as.camel.integration.deployment;

import java.util.Iterator;
import java.util.List;

import org.jboss.as.server.deployment.*;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;

/**
 * Annotation scanning related deployment processors should extend this class.
 */
public abstract class AbstractAnnotationScannerDP extends CamelIntegrationServiceAbstractDP {

    Logger log = Logger.getLogger(CamelIntegrationServiceAbstractDP.class);

    @Override
    public abstract void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException;

    @Override
    public void undeploy(final DeploymentUnit context) {
        if (log.isTraceEnabled()) {
            log.trace("UnDeploy " + context.getName());
        }
    }

    /**
     * Filter resources that we don't want to process
     * 
     * @param resourceRoots
     *            ResourceRoots, root of the deployment
     * @param skip
     *            List of paths to exclude
     * @return Filtered list of resource roots
     */
    protected final List<ResourceRoot> filterResourceRoots(final List<ResourceRoot> resourceRoots, final String... skip) {
        final Iterator<ResourceRoot> iter = resourceRoots.iterator();
        while (iter.hasNext()) {
            final ResourceRoot resourceRoot = iter.next();
            for (final String s : skip) {
                if (resourceRoot.getRoot().toString().toUpperCase().contains(s)) {
                    iter.remove();
                }
            }
        }
        return resourceRoots;
    }

}
