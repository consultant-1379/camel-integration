package org.jboss.as.camel.integration.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.List;

import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

/**
 * Tests all management expects for subsystem, parsing, marshaling, model definition and other Here is an example that allows you a fine grained
 * controler over what is tested and how. So it can give you ideas what can be done and tested. If you have no need for advanced testing of subsystem
 * you look at {@link SubsystemBaseParsingTestCase} that testes same stuff but most of the code is hidden inside of test harness
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    public SubsystemParsingTestCase() {
        super(SubsystemExtension.SUBSYSTEM_NAME, new SubsystemExtension());
    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
        final String subsystemXml = "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
        final List<ModelNode> operations = super.parse(subsystemXml);

        ///Check that we have the expected number of operations
        Assert.assertEquals(1, operations.size());

        //Check that each operation has the correct content
        final ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        final PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        final PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(SubsystemExtension.SUBSYSTEM_NAME, element.getValue());
    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        //Parse the subsystem xml and install into the controller
        final String subsystemXml = "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
        final KernelServices services = super.installInController(subsystemXml);

        //Read the whole model and make sure it looks as expected
        final ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(SubsystemExtension.SUBSYSTEM_NAME));
    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second controller started with the xml marshalled from the first one
     * results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        //Parse the subsystem xml and install into the first controller
        final String subsystemXml = "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
        final KernelServices servicesA = super.installInController(subsystemXml);
        //Get the model and the persisted xml from the first controller
        final ModelNode modelA = servicesA.readWholeModel();
        final String marshalled = servicesA.getPersistedSubsystemXml();

        //Install the persisted xml from the first controller into a second controller
        final KernelServices servicesB = super.installInController(marshalled);
        final ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Starts a controller with the given subsystem xml and then checks that a second controller started with the operations from its describe action
     * results in the same model
     */
    @Test
    public void testDescribeHandler() throws Exception {
        //Parse the subsystem xml and install into the first controller
        final String subsystemXml = "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
        final KernelServices servicesA = super.installInController(subsystemXml);
        //Get the model and the describe operations from the first controller
        final ModelNode modelA = servicesA.readWholeModel();
        final ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SubsystemExtension.SUBSYSTEM_NAME)).toModelNode());
        final List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();
        //Install the describe options from the first controller into a second controller
        final KernelServices servicesB = super.installInController(operations);
        final ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Tests that the subsystem can be removed
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        //Parse the subsystem xml and install into the first controller
        final String subsystemXml = "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
        final KernelServices services = super.installInController(subsystemXml);
        //Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        //TODO Chek that any services that were installed were removed here
    }
}
