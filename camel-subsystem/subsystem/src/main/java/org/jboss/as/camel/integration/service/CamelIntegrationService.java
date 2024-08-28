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

import org.jboss.logging.Logger;
import org.jboss.msc.service.*;

/**
 * Camel integration service
 */
public class CamelIntegrationService implements Service<CamelIntegrationService> {

    public static final ServiceName CAMEL_INTEGRATION_SERVICE_NAME = ServiceName.JBOSS.append("camel-integration-system");

    private final Logger log = Logger.getLogger(CamelIntegrationService.class);

    private CamelContextService camelService;

    private boolean sharedCamelDeployment;

    private String camelContextName;

    /**
     * Full arg constructor
     * 
     * @param shared
     *            Are we using shared context or per deployment
     * @param ctxName
     *            Name for the shared context
     */
    public CamelIntegrationService(final Boolean shared, final String ctxName) {
        log.info("Creating Camel integration service with shared=" + shared + ", ctxName=" + ctxName);
        this.sharedCamelDeployment = shared;
        this.camelContextName = ctxName;
    }

    @Override
    public CamelIntegrationService getValue() throws RuntimeException {
        return this;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        log.info("Starting camel integration service....");
        this.camelService = new CamelContextService(sharedCamelDeployment, camelContextName);

    }

    @Override
    public void stop(final StopContext context) {
        log.info("Stopping camel integration service...");
        this.camelService.destroy();
    }

    /**
     * @return the camelService
     */
    public CamelContextService getCamelService() {
        return camelService;
    }

    /**
     * @return the sharedCamelDeployment
     */
    public boolean isSharedCamelDeployment() {
        return sharedCamelDeployment;
    }

    /**
     * @param sharedCamelDeployment
     *            the sharedCamelDeployment to set
     */
    public void setSharedCamelDeployment(final boolean sharedCamelDeployment) {
        this.sharedCamelDeployment = sharedCamelDeployment;
    }

    /**
     * @return the camelContextName
     */
    public String getCamelContextName() {
        return camelContextName;
    }

    /**
     * @param camelContextName
     *            the camelContextName to set
     */
    public void setCamelContextName(final String camelContextName) {
        this.camelContextName = camelContextName;
    }

}
