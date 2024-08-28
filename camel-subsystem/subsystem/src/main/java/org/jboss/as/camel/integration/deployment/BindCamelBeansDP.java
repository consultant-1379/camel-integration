package org.jboss.as.camel.integration.deployment;

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.server.deployment.*;
import org.jboss.logging.Logger;

/***
 * Deployment processor that will register previously created camel context with this deployment
 */
public class BindCamelBeansDP extends CamelIntegrationServiceAbstractDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4006;

    Logger log = Logger.getLogger(BindCamelBeansDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)) {
            if (log.isTraceEnabled()) {
                log.trace("Binding beans in deployment unit " + deploymentUnit.getName());
            }
            final CamelContextService camelService = this.getCamelContextService(deploymentUnit);
            final CamelContributionMetaInfoHolder contribMeta = deploymentUnit
                    .getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER);
            try {
                camelService.bindBeans(contribMeta);
            } catch (final Exception e) {
                throw new DeploymentUnitProcessingException(e);
            }

        }

    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        if (log.isTraceEnabled()) {
            log.trace("undeploy : " + deploymentUnit);
        }
        final CamelContextService camelService = this.getCamelContextService(deploymentUnit);
        try {
            camelService.unbindBeans(deploymentUnit.getName());
        } catch (final Exception e) {
            log.error(e);
        }

    }

}
