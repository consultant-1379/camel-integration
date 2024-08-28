package org.jboss.as.camel.integration.extension;

import java.util.List;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.as.camel.integration.deployment.*;
import org.jboss.as.camel.integration.service.CamelIntegrationService;
import org.jboss.as.camel.integration.service.TransactionUtil;
import org.jboss.as.controller.*;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.txn.service.TransactionManagerService;
import org.jboss.as.txn.service.TransactionSynchronizationRegistryService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.inject.*;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

/**
 * Handler responsible for adding the subsystem resource to the model
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class SubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();

    private SubsystemAdd() {
    }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        model.setEmptyObject();
        SubsystemDefinition.ENABLED.validateAndSet(operation, model);
        SubsystemDefinition.CONTEXT_NAME.validateAndSet(operation, model);
    }

    /** {@inheritDoc} */
    @Override
    public void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model,
                                final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers)
        throws OperationFailedException {
        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            public void execute(final DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(CamelContextServiceAnnotationScannerDP.PHASE, CamelContextServiceAnnotationScannerDP.PRIORITY,
                        new CamelContextServiceAnnotationScannerDP());
                context.completeStep();

            }
        }, OperationContext.Stage.RUNTIME);
    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model,
                                  final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers)
        throws OperationFailedException {
        /**
         * Scan for annotations
         */
        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            public void execute(final DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(CamelContextServiceAnnotationScannerDP.PHASE, CamelContextServiceAnnotationScannerDP.PRIORITY,
                        new CamelContextServiceAnnotationScannerDP());
                processorTarget.addDeploymentProcessor(CamelContributionAnnotationScannerDP.PHASE, CamelContributionAnnotationScannerDP.PRIORITY,
                        new CamelContributionAnnotationScannerDP());
                processorTarget.addDeploymentProcessor(CamelContextDependencyDP.PHASE, CamelContextDependencyDP.PRIORITY,
                        new CamelContextDependencyDP());
                processorTarget.addDeploymentProcessor(ClassTransformerDP.PHASE, ClassTransformerDP.PRIORITY, new ClassTransformerDP());
                processorTarget.addDeploymentProcessor(CreateCamelContextDP.PHASE, CreateCamelContextDP.PRIORITY, new CreateCamelContextDP());
                processorTarget
                        .addDeploymentProcessor(StartStopCamelContextDP.PHASE, StartStopCamelContextDP.PRIORITY, new StartStopCamelContextDP());
                processorTarget.addDeploymentProcessor(RegisterCamelContextDP.PHASE, RegisterCamelContextDP.PRIORITY, new RegisterCamelContextDP());
                processorTarget.addDeploymentProcessor(BindCamelBeansDP.PHASE, BindCamelBeansDP.PRIORITY, new BindCamelBeansDP());
                processorTarget.addDeploymentProcessor(CleanupDP.PHASE, CleanupDP.PRIORITY, new CleanupDP());

            }
        }, OperationContext.Stage.RUNTIME);

        // set the transaction manager to be accessible via TransactionUtil
        final Injector<TransactionManager> transactionManagerInjector = new Injector<TransactionManager>() {
            @Override
            public void inject(final TransactionManager value) throws InjectionException {
                TransactionUtil.setTransactionManager(value);
            }

            @Override
            public void uninject() {
                TransactionUtil.setTransactionManager(null);
            }
        };
        // set the transaction service registry to be accessible via TransactionUtil (after service is installed below)
        final Injector<TransactionSynchronizationRegistry> transactionRegistryInjector = new Injector<TransactionSynchronizationRegistry>() {
            @Override
            public void inject(final TransactionSynchronizationRegistry value) throws InjectionException {
                TransactionUtil.setTransactionSynchronizationRegistry(value);
            }

            @Override
            public void uninject() {
                TransactionUtil.setTransactionSynchronizationRegistry(null);
            }
        };
        final Boolean sharedContext = SubsystemDefinition.ENABLED.resolveModelAttribute(context, model).asBoolean();
        final String sharedContextName = SubsystemDefinition.CONTEXT_NAME.resolveModelAttribute(context, model).asString();
        final CamelIntegrationService service = new CamelIntegrationService(sharedContext, sharedContextName);
        final CastingInjector<TransactionSynchronizationRegistry> injectorTs = new CastingInjector<TransactionSynchronizationRegistry>(
                transactionRegistryInjector, TransactionSynchronizationRegistry.class);
        final CastingInjector<TransactionManager> injectorTm = new CastingInjector<TransactionManager>(transactionManagerInjector,
                TransactionManager.class);
        final ServiceController<CamelIntegrationService> controller = context.getServiceTarget()
                .addService(CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME, service).addListener(verificationHandler)
                .addDependency(TransactionManagerService.SERVICE_NAME, injectorTm)
                .addDependency(TransactionSynchronizationRegistryService.SERVICE_NAME, injectorTs).setInitialMode(Mode.ACTIVE).install();
        newControllers.add(controller);
    }
}
