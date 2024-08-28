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
package org.jboss.as.camel.testsuite.smoke.camelcore.tests;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.camel.testsuite.smoke.deployment.common.SimpleRouteBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CoreCamelComponentTest {

    private static final String DEP_NAME = "test-camel-core-components.war";

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB
    private SimpleRouteBuilder ejb;

    @Deployment(name = DEP_NAME)
    public static Archive<?> createTestWar() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, DEP_NAME);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SimpleRouteBuilder.class);
        testWar.addClass(CoreCamelComponentTest.class);
        return testWar;
    }

    @Test
    public void testCanLoadDirectAndFileComponentInRoute() throws Exception {
        ejb.testFileComponent();
        final File output = new File("target/" + SimpleRouteBuilder.CORE_COMPONENT_TEST_RESULT_FILENAME);
        Assert.assertTrue(output.exists());
        final List<String> lines = Files.readAllLines(Paths.get(output.getPath()), Charset.forName("UTF-8"));
        Assert.assertEquals(SimpleRouteBuilder.CORE_COMPONENT_TEST_MSG_BODY, lines.get(0));
    }

    @Test
    public void testTransactedRoute_REQUIRED_WHEN_TX_EXISTS() throws Exception {
        final String txKey = ejb.testTransactedRoute_REQUIRED();
        final File output = new File("target/" + SimpleRouteBuilder.TX_REQUIRED_TEST_RESULT_FILENAME);
        Assert.assertTrue(output.exists());
        final List<String> lines = Files.readAllLines(Paths.get(output.getPath()), Charset.forName("UTF-8"));
        Assert.assertEquals(txKey, lines.get(0));
    }

    @Test
    public void testTransactedRoute_REQUIRED_WHEN_NO_TX() throws Exception {
        ejb.testTransactedRoute_REQUIRED_WHEN_NO_TX();
        final File output = new File("target/" + SimpleRouteBuilder.TX_REQUIRED_TEST_NO_TX_RESULT_FILENAME);
        Assert.assertTrue(output.exists());
        final List<String> lines = Files.readAllLines(Paths.get(output.getPath()), Charset.forName("UTF-8"));
        Assert.assertNotSame("NULL", lines.get(0));
    }

}
