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

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.camel.integration.service.CamelIntegrationService;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceController;

/**
 * Service locator for CamelContextService
 */
public class CamelContextServiceLocator {

    /**
     * Locate Camel context service for this deployment unit
     * 
     * @param deploymentUnit
     *            Deployment unit
     * @return CamelContextService for this deployment unit
     */
    public CamelContextService getCamelContextService(final DeploymentUnit deploymentUnit) {
        final ServiceController<?> serviceControler = deploymentUnit.getServiceRegistry().getService(
                CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME);
        final CamelIntegrationService camelIntegrationService = (CamelIntegrationService) serviceControler.getService();
        return camelIntegrationService.getCamelService();
    }

}
