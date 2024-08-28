package org.jboss.as.camel.integration.extension;

import org.jboss.as.controller.*;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Subsystem definition
 * 
 * @author Dejan Kitic
 */
public class SubsystemDefinition extends SimpleResourceDefinition {

    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();
    protected static final SimpleAttributeDefinition ENABLED = new SimpleAttributeDefinitionBuilder(SubsystemExtension.ENABLED, ModelType.BOOLEAN)
            .setAllowExpression(true).setXmlName(SubsystemExtension.ENABLED).setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(true)).setAllowNull(false).build();

    protected static final SimpleAttributeDefinition CONTEXT_NAME = new SimpleAttributeDefinitionBuilder(SubsystemExtension.CONTEXT_NAME,
            ModelType.STRING).setAllowExpression(true).setXmlName(SubsystemExtension.CONTEXT_NAME)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES).setDefaultValue(new ModelNode(SubsystemExtension.DEFAULT_SHARED_CONTEXT_NAME))
            .setAllowNull(false).build();

    private SubsystemDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH, SubsystemExtension.getResourceDescriptionResolver(null),
        //We always need to add an 'add' operation
                SubsystemAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                SubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(final ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        //you can register aditional operations here
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(ENABLED, null, SharedCamelContextEnableHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(CONTEXT_NAME, null, SharedCamelContextNameHandler.INSTANCE);
    }
}
