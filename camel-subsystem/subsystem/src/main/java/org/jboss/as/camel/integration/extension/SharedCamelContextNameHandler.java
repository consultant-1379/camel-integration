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
package org.jboss.as.camel.integration.extension;

import org.jboss.as.camel.integration.service.CamelIntegrationService;
import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;

/**
 * Handler for name attribute of shared camel context
 */
public class SharedCamelContextNameHandler extends AbstractWriteAttributeHandler<Void> {

    /**
     * Get instance of this singleton
     */
    public static final SharedCamelContextNameHandler INSTANCE = new SharedCamelContextNameHandler();

    private SharedCamelContextNameHandler() {
        super(SubsystemDefinition.CONTEXT_NAME);
    }

    @Override
    protected boolean applyUpdateToRuntime(final OperationContext context, final ModelNode operation, final String attributeName,
                                           final ModelNode resolvedValue, final ModelNode currentValue, final HandbackHolder<Void> handbackHolder)
        throws OperationFailedException {
        modifySharedContextName(context, operation, resolvedValue.asString());
        return false;
    }

    @Override
    protected void revertUpdateToRuntime(final OperationContext context, final ModelNode operation, final String attributeName,
                                         final ModelNode valueToRestore, final ModelNode valueToRevert, final Void handback)
        throws OperationFailedException {
        modifySharedContextName(context, operation, valueToRestore.asString());
    }

    private void modifySharedContextName(final OperationContext context, final ModelNode operation, final String value)
        throws OperationFailedException {
        final CamelIntegrationService service = (CamelIntegrationService) context.getServiceRegistry(true)
                .getRequiredService(CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME).getValue();
        service.setCamelContextName(value);
    }

}
