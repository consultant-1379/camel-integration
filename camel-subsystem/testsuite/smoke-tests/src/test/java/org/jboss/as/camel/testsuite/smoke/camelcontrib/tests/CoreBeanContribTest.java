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
package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class CoreBeanContribTest {

    private static final String DEP_NAME_CONTRIB = "test-camelbean-contrib.war";
    private static final String DEP_NAME_CLIENT = "test-camelbean-client.war";
    private static final Logger log = LoggerFactory.getLogger(CoreBeanContribTest.class);

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @Inject
    private SomeAppClient someAppClient;

    @Deployment(name = DEP_NAME_CONTRIB, managed = false, testable = false)
    public static Archive<?> createContribWar() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, DEP_NAME_CONTRIB);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeCamelBean.class);
        return testWar;
    }

    @Deployment(name = DEP_NAME_CLIENT, managed = false, testable = true)
    public static Archive<?> createClientWar() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, DEP_NAME_CLIENT);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeAppClient.class);
        testWar.addClass(SomeAppClientImpl.class);
        return testWar;
    }

    @InSequence(1)
    @Test
    public void testDeployContrib() throws Exception {
        log.info("------>Smoke testDeployContrib Test<------");
        deployer.deploy(DEP_NAME_CONTRIB);
    }

    @InSequence(2)
    @Test
    public void testDeployClient() throws Exception {
        log.info("------>Smoke testDeployContrib Test<------");
        deployer.deploy(DEP_NAME_CLIENT);

    }

    @InSequence(3)
    @OperateOnDeployment(DEP_NAME_CLIENT)
    @Test
    public void CoreBeanContribTest_invokeRoute_WithContributedProcessor() throws Exception {
        log.info("------>Invoke CoreBeanContribTest Route<------");
        Assert.assertNotNull(this.someAppClient);
        this.someAppClient.createRouteWithContribProcessor();
        final File output = new File("target/" + TestConstants.CONTRIB_RESULT_FILENAME);
        Assert.assertTrue(output.exists());
        final List<String> lines = Files.readAllLines(Paths.get(output.getPath()), Charset.forName("UTF-8"));
        Assert.assertEquals(TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, lines.get(0));
    }

}
