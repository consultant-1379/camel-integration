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
package org.jboss.as.camel.integration.service;

import java.lang.reflect.*;

import org.apache.camel.CamelContext;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

/**
 * Proxy class that will be injecteded into clients
 */
public class CamelContextProxy implements InvocationHandler {

    private static final Logger log = Logger.getLogger(CamelContextProxy.class);
    CamelContext camelContext;

    /**
     * Full arg constructor
     * 
     * @param deployment
     *            Deployment name
     */
    public CamelContextProxy(final String deployment) {
        final ServiceController<?> serviceController = CurrentServiceContainer.getServiceContainer().getService(
                CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME);
        final CamelIntegrationService camelIntegrationService = (CamelIntegrationService) serviceController.getService();
        this.camelContext = camelIntegrationService.getCamelService().getCamelContextForDeployment(deployment).getCamelContext();
    }

    @Override
    public Object invoke(final Object proxy, final Method m, final Object[] args) throws Throwable {
        try {
            if (log.isTraceEnabled()) {
                log.trace("invoking method " + m + " with args " + args);
            }
            return m.invoke(camelContext, args);
        } catch (final InvocationTargetException e) {
            throw e.getTargetException();
        } catch (final Exception e) {
            throw e;
        }
    }
}
