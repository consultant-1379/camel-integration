package org.jboss.as.camel.integration.extension;

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

/**
 * This is the barebone test example that tests subsystem It does same things that {@link SubsystemParsingTestCase} does but most of internals are
 * already done in AbstractSubsystemBaseTest If you need more control over what happens in tests look at {@link SubsystemParsingTestCase}
 * 
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(SubsystemExtension.SUBSYSTEM_NAME, new SubsystemExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
    }

}
