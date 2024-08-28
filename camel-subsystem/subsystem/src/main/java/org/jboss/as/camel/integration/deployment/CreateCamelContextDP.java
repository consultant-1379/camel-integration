package org.jboss.as.camel.integration.deployment;

import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.*;
import org.jboss.logging.Logger;

/**
 * Deployment processor that will create camel context for this deployment
 */
public class CreateCamelContextDP extends CamelIntegrationServiceAbstractDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4003;

    Logger log = Logger.getLogger(CreateCamelContextDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);

        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)
                && !toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            if (log.isTraceEnabled()) {
                log.trace("Creating camel context for deployment: " + deploymentUnit.getName() + ", using toplevel deployment name:"
                        + toplevelDeploymentUnit.getName());
            }
            try {
                final DeploymentMetaInfo dmi = this.getCamelContextService(deploymentUnit).createCamelContextForDeployment(
                        toplevelDeploymentUnit.getName());
                toplevelDeploymentUnit.putAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO, dmi);
            } catch (final Exception e) {
                throw new DeploymentUnitProcessingException(e);
            }
        }

    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        if (log.isTraceEnabled()) {
            log.trace("Undeploy method called for " + deploymentUnit.getName());
        }
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            if (log.isTraceEnabled()) {
                log.trace("Removing CamelContextInstance attachment");
                toplevelDeploymentUnit.removeAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
            }
        }
    }

}
