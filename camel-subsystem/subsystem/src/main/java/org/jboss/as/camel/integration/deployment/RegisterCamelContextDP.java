package org.jboss.as.camel.integration.deployment;

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.*;
import org.jboss.logging.Logger;

/***
 * Deployment processor that will register previously created camel context with this deployment
 */
public class RegisterCamelContextDP extends CamelIntegrationServiceAbstractDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4005;

    Logger log = Logger.getLogger(RegisterCamelContextDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        final CamelContextService camelService = this.getCamelContextService(deploymentUnit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            if (camelService.getCamelContextForDeployment(toplevelDeploymentUnit.getName()) != null) {
                if (log.isTraceEnabled()) {
                    log.trace("This deployment unit " + deploymentUnit.getName() + " already has registered camel context, skipping...");
                }
                return;
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Registering CamelContext with deployment: " + deploymentUnit.getName());
                }
                try {
                    final DeploymentMetaInfo dmi = toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
                    camelService.registerCamelContext(dmi, toplevelDeploymentUnit.getName());
                } catch (final Exception e) {
                    throw new DeploymentUnitProcessingException(e);
                }
            }
        }

    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        final CamelContextService camelService = this.getCamelContextService(deploymentUnit);
        final DeploymentMetaInfo dmi = camelService.getCamelContextForDeployment(toplevelDeploymentUnit.getName());
        if (dmi != null) {
            if (log.isTraceEnabled()) {
                log.trace("Deregistering CamelContext for deployment " + toplevelDeploymentUnit.getName());
            }
            camelService.deregisterCamelContext(toplevelDeploymentUnit.getName());
            toplevelDeploymentUnit.putAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO, dmi);
        }

    }

}
