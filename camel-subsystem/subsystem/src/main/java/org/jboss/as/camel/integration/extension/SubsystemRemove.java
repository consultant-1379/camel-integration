package org.jboss.as.camel.integration.extension;

import org.jboss.as.camel.integration.service.CamelIntegrationService;
import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * Handler responsible for removing the subsystem resource from the model
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class SubsystemRemove extends AbstractRemoveStepHandler {

    static final SubsystemRemove INSTANCE = new SubsystemRemove();

    private final Logger log = Logger.getLogger(SubsystemRemove.class);

    private SubsystemRemove() {
    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {
        log.trace("performRuntime called in subsystem remove");
        context.removeService(CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME);
    }

}
