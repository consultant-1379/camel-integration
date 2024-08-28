package org.jboss.as.camel.integration.deployment;

import org.jboss.as.server.deployment.*;
import org.jboss.logging.Logger;

/**
 * Cleanup all attachments left after deployment process
 */
public class CleanupDP extends CamelIntegrationServiceAbstractDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.CLEANUP;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4006;
    /**
     * Prefix for removing trace message
     */
    private static final String REMOVING_ATTACHMENT = "Removing attachment: ";

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(CleanupDP.class);

    /**
     * Prefix for cleanup trace message
     */
    private static final String CLEANING_UP_AFTER_DEPLOYMENT = "Cleaning up after deployment: ";

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        if (log.isTraceEnabled()) {
            log.trace(CLEANING_UP_AFTER_DEPLOYMENT + deploymentUnit.getName());
        }
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            if (log.isTraceEnabled()) {
                log.trace(REMOVING_ATTACHMENT + CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
            }
            toplevelDeploymentUnit.removeAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
        }
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)) {
            if (log.isTraceEnabled()) {
                log.trace(REMOVING_ATTACHMENT + CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
            }
            deploymentUnit.removeAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        }

    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        if (log.isTraceEnabled()) {
            log.trace("Undeploy called for cleanup deployment processor on deployment unit:" + deploymentUnit.getName());
        }
    }

}
