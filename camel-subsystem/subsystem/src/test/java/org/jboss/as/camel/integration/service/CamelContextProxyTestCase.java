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
package org.jboss.as.camel.integration.service;

import java.lang.reflect.InvocationTargetException;

import javax.transaction.TransactionManager;

import org.apache.camel.CamelContext;
import org.jboss.as.server.ServiceContainerUtil;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

//for service controller
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(MockitoJUnitRunner.class)
public class CamelContextProxyTestCase {

    /**
     * 
     */
    private static final String DEFAULT_CONTEXT_NAME = "CamelContext1";
    /**
     * 
     */
    private static final String DEPLOYMENT_NAME = "test.war";

    @Mock
    TransactionManager txMgr;

    @Mock
    ServiceController serviceController;

    @Mock
    CamelIntegrationService service;

    @Mock
    ServiceContainer sc;

    private CamelContextService ccs;

    @Test
    public void testCamelContextProxy_when_CAMEL_CONTEXT_IS_SHARED() throws Exception {
        init_shared_context();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        Assert.assertNotNull(proxy);
        Assert.assertNotNull(proxy.camelContext);
        Assert.assertEquals(DEFAULT_CONTEXT_NAME, proxy.camelContext.getName());
    }

    @Test
    public void testCreateProxy_when_CAMEL_CONTEXT_IS_NOT_SHARED() throws Exception {
        init_unshared_context();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        Assert.assertNotNull(proxy);
        Assert.assertEquals("test.war-camel-context", proxy.camelContext.getName());
    }

    @Test
    public void testProxy_invoke_when_CONTEXT_IS_NOT_SHARED() throws Exception, Throwable {
        init_unshared_context();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenReturn(DEPLOYMENT_NAME + "-camel-context");
        final String result = (String) proxy.invoke(this, CamelContext.class.getMethod("getName"), null);
        Mockito.verify(mock, Mockito.times(1)).getName();
        Assert.assertEquals(DEPLOYMENT_NAME + "-camel-context", result);

    }

    @Test
    public void testProxy_invoke_when_CONTEXT_IS_SHARED() throws Exception, Throwable {
        init_shared_context();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenReturn(DEFAULT_CONTEXT_NAME);
        final String result = (String) proxy.invoke(this, CamelContext.class.getMethod("getName"), null);
        Mockito.verify(mock, Mockito.times(1)).getName();
        Assert.assertEquals(DEFAULT_CONTEXT_NAME, result);

    }

    @Test(expected = InvocationTargetException.class)
    public void testProxy_invoke_when_INVOCATION_TARGET_EXCEPTION() throws Exception, Throwable {
        init_shared_context();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenThrow(InvocationTargetException.class);
        proxy.invoke(this, CamelContext.class.getMethod("getName"), null);

    }

    @Test(expected = Exception.class)
    public void testProxy_invoke_when_EXCEPTION() throws Exception, Throwable {
        init_shared_context();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenThrow(Exception.class);
        proxy.invoke(this, CamelContext.class.getMethod("getName"), null);

    }

    private void init_unshared_context() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        final CamelContextService ccs = new CamelContextService(false, DEFAULT_CONTEXT_NAME);
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        Mockito.when(sc.getService(CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME)).thenReturn(serviceController);
        Mockito.when(serviceController.getService()).thenReturn(service);
        Mockito.when(service.getCamelService()).thenReturn(ccs);
        final DeploymentMetaInfo dmi = ccs.createCamelContextForDeployment(DEPLOYMENT_NAME);
        ccs.registerCamelContext(dmi, DEPLOYMENT_NAME);
    }

    private void init_shared_context() throws Exception {
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        TransactionUtil.setTransactionManager(txMgr);
        ccs = new CamelContextService(true, DEFAULT_CONTEXT_NAME);
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        Mockito.when(sc.getService(CamelIntegrationService.CAMEL_INTEGRATION_SERVICE_NAME)).thenReturn(serviceController);
        Mockito.when(serviceController.getService()).thenReturn(service);
        Mockito.when(service.getCamelService()).thenReturn(ccs);
    }
}
