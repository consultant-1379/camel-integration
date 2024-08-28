package org.jboss.as.camel.integration.deployment;

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.*;
import org.jboss.logging.Logger;

/**
 * Deployment processor that will start or stop previously created camel context
 */
public class StartStopCamelContextDP extends CamelIntegrationServiceAbstractDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4004;

    Logger log = Logger.getLogger(StartStopCamelContextDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit unit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(unit);
        final CamelContextService camelService = this.getCamelContextService(unit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            try {
                final DeploymentMetaInfo dmi = toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
                if (dmi.getCamelContext().getStatus().isStarted() || dmi.getCamelContext().getStatus().isStarting()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Camel context is already started or starting, skiping...");
                    }
                    return;
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("Starting CamelContext for deployment: " + unit.getName());
                    }
                    startCamelContext(toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO),
                            toplevelDeploymentUnit.getAttachment(Attachments.MODULE).getClassLoader(), camelService);
                }
            } catch (final Exception e) {
                throw new DeploymentUnitProcessingException(e);
            }
        }

    }

    @Override
    public void undeploy(final DeploymentUnit unit) {
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(unit);
        final CamelContextService camelService = this.getCamelContextService(unit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            final DeploymentMetaInfo dmi = toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
            if (dmi.getCamelContext().getStatus().isStopping() || dmi.getCamelContext().getStatus().isStopped()) {
                if (log.isTraceEnabled()) {
                    log.trace("Camel context is already stopped or is stoppig, skipping");
                }
                return;
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Stopping CamelContext for deployment " + unit.getName());
                }
                stopCamelContext(dmi, toplevelDeploymentUnit.getAttachment(Attachments.MODULE).getClassLoader(), camelService);
            }
        }

    }

    private void startCamelContext(final DeploymentMetaInfo dmi, final ClassLoader mcl, final CamelContextService camelService) throws Exception {
        final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mcl);
            camelService.startCamelContext(dmi);
        } catch (final Exception e) {
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(tcl);
        }
    }

    private void stopCamelContext(final DeploymentMetaInfo dmi, final ClassLoader mcl, final CamelContextService camelService) {
        final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mcl);
            camelService.stopCamelContext(dmi);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(tcl);
        }
    }

}
