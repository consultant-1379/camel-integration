package org.jboss.as.camel.integration.deployment;

import org.jboss.as.server.deployment.*;
import org.jboss.as.server.deployment.module.DelegatingClassFileTransformer;
import org.jboss.logging.Logger;

/**
 * Deployment processor that will add class file transformers for camel context injections
 */
public class ClassTransformerDP extends CamelIntegrationServiceAbstractDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.CONFIGURE_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4002;

    Logger log = Logger.getLogger(ClassTransformerDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)) {
            if (log.isTraceEnabled()) {
                log.trace("Adding classfile transformers for deployment: " + deploymentUnit.getName());
            }
            final DelegatingClassFileTransformer transformer = deploymentUnit.getAttachment(DelegatingClassFileTransformer.ATTACHMENT_KEY);
            if (transformer != null) {
                final CamelContextServiceMetaInfo metaInfoHolder = deploymentUnit
                        .getAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
                transformer.addTransformer(new CamelContextClassfileTransformer(metaInfoHolder));
            }
        }

    }

    @Override
    public void undeploy(final DeploymentUnit context) {
        if (log.isTraceEnabled()) {
            log.trace("UnDeploy " + context.getName());
        }
    }

}
