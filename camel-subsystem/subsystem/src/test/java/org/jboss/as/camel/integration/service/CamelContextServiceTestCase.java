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

import javax.transaction.TransactionManager;

import junit.framework.Assert;

import org.apache.camel.CamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.jboss.camel.tx.TransactionPolicy;
import org.jboss.msc.service.StartException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamelContextServiceTestCase {

    CamelContextService service;
    final String contextName = "test";
    final String key = "test.war";

    @Mock
    private TransactionManager txMgr;

    @Test
    public void testCamelContextServiceInstanceCreation_verify_all_attributes_initialized_WHEN_SHARING_CONTEXT() throws Exception {
        final String contextName = "testContext";
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        Assert.assertTrue(service.shareContextEnabled);
        Assert.assertNotNull(service.instantiatedContexts);
        Assert.assertEquals(0, service.instantiatedContexts.size());
        Assert.assertNotNull(service.boundBeans);
        Assert.assertEquals(0, service.boundBeans.size());
        Assert.assertNotNull(service.jtaTransactionManager);
        Assert.assertNotNull(service.sharedContext);

    }

    @Rule
    public ExpectedException expectedExceptionForNullContextName = ExpectedException.none();

    @Test
    public void testCamelContextServiceInstanceCreation_verify_all_attributes_initialized_WHEN_SHARING_CONTEXT_WITH_NULL_NAME() throws Exception {
        expectedExceptionForNullContextName.expect(StartException.class);
        expectedExceptionForNullContextName.expectMessage("CamelContext name  must be specified and not empty");
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, null);
        Assert.assertTrue(service.shareContextEnabled);
        Assert.assertNotNull(service.instantiatedContexts);
        Assert.assertEquals(0, service.instantiatedContexts.size());
        Assert.assertNotNull(service.boundBeans);
        Assert.assertEquals(0, service.boundBeans.size());
        Assert.assertNotNull(service.jtaTransactionManager);
        Assert.assertNotNull(service.sharedContext);

    }

    @Test
    public void testCamelContextServiceInstanceCreation_verify_all_attributes_initialized_WHEN_NOT_SHARING_CONTEXT() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, "testContext");
        Assert.assertFalse(service.shareContextEnabled);
        Assert.assertNotNull(service.instantiatedContexts);
        Assert.assertEquals(0, service.instantiatedContexts.size());
        Assert.assertNotNull(service.boundBeans);
        Assert.assertEquals(0, service.boundBeans.size());
        Assert.assertNotNull(service.jtaTransactionManager);
        Assert.assertNull(service.sharedContext);

    }

    @Test
    public void testCreateCamelContextForDeployment_WHEN_CONTEXT_IS_SHARED() throws Exception {
        final String contextName = "test";
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(contextName);
        Assert.assertNotNull(dmi);
        Assert.assertEquals(service.sharedContext, dmi);

    }

    @Test
    public void testCreateCamelContextForDeployment_WHEN_CONTEXT_IS_SHARED_AND_NULL_SUPPLIED() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(null);
        Assert.assertNotNull(dmi);
        Assert.assertEquals(service.sharedContext, dmi);

    }

    @Test
    public void testCreateCamelContextForDeployment_WHEN_CONTEXT_IS_NOT_SHARED() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(contextName);
        Assert.assertNotNull(dmi);
        Assert.assertNotSame(service.sharedContext, dmi);
    }

    @Test
    public void testCreateCamelContextForDeployment_WHEN_CONTEXT_IS_NOT_SHARED_AND_NULL_SUPPLIED() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(null);
        Assert.assertNotNull(dmi);
        Assert.assertNotSame(service.sharedContext, dmi);
        Assert.assertEquals("null-camel-context", dmi.getCamelContext().getName());
    }

    @Test
    public void testStartCamelContext_WHEN_CONTEXT_IS_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.startCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(0)).getCamelContext();
        Mockito.verify(mock, Mockito.times(0)).start();

    }

    @Test
    public void testStartCamelContext_WHEN_CONTEXT_IS_NOT_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.startCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(1)).getCamelContext();
        Mockito.verify(mock, Mockito.times(1)).start();

    }

    @Test
    public void testStopCamelContext_WHEN_CONTEXT_IS_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.stopCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(0)).getCamelContext();
        Mockito.verify(mock, Mockito.times(0)).stop();

    }

    @Test
    public void testStopCamelContext_WHEN_CONTEXT_IS_NOT_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.stopCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(1)).getCamelContext();
        Mockito.verify(mock, Mockito.times(1)).stop();

    }

    @Test
    public void testRegisterCamelContext_WHEN_CONTEXT_IS_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.registerCamelContext(dmi, key);
        Assert.assertNull(service.instantiatedContexts.get(key));
    }

    @Test
    public void testRegisterCamelContext_WHEN_CONTEXT_IS_NOT_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.registerCamelContext(dmi, key);
        Assert.assertNotNull(service.instantiatedContexts.get(key));
        Assert.assertEquals(dmi, service.instantiatedContexts.get(key));
    }

    @Test
    public void testDeregisterCamelContext_WHEN_CONTEXT_IS_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.deregisterCamelContext(key);
        Assert.assertNull(service.instantiatedContexts.get(key));
    }

    @Test
    public void testDeregisterCamelContext_WHEN_CONTEXT_IS_NOT_SHARED() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.registerCamelContext(dmi, key);
        service.deregisterCamelContext(key);
        Assert.assertNull(service.instantiatedContexts.get(key));
    }

    @Test
    public void testGetCamelContextForDeployment_WHEN_CONTEXT_IS_SHARED() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.getCamelContextForDeployment(key);
        Assert.assertEquals(service.sharedContext, dmi);
    }

    @Rule
    public ExpectedException expectedExceptionForNonRegisterContextLookup = ExpectedException.none();

    @Test
    public void testGetCamelContextForDeployment_WHEN_CONTEXT_IS_NOT_SHARED_AND_NONEXISTING() throws Exception {
        expectedExceptionForNonRegisterContextLookup.expect(IllegalStateException.class);
        expectedExceptionForNonRegisterContextLookup.expectMessage("Deployment " + key + " did not register any camel contexts");
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.getCamelContextForDeployment(key);
    }

    @Test
    public void testGetCamelContextForDeployment_WHEN_CONTEXT_IS_NOT_SHARED_AND_EXISTING() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.registerCamelContext(dmi, key);
        final DeploymentMetaInfo retVal = service.getCamelContextForDeployment(key);
        Assert.assertNotNull(retVal);
        Assert.assertEquals(dmi, retVal);
    }

    @Test
    public void testGetCamelContextForDeployment_WHEN_CONTEXT_IS_SHARED_AND_EXISTING() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo retVal = service.getCamelContextForDeployment(key);
        Assert.assertNotNull(retVal);
        Assert.assertEquals(service.sharedContext, retVal);
    }

    @Test
    public void testDestroy_WHEN_CONTEXT_IS_SHARED() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        final JndiContext jndiMock = Mockito.mock(JndiContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        Mockito.when(dmi.getRegistry()).thenReturn(jndiMock);
        service = new CamelContextService(true, contextName);
        service.setSharedContext(dmi);
        service.destroy();
        Mockito.verify(mock, Mockito.times(1)).stop();
        Mockito.verify(jndiMock, Mockito.times(1)).close();
        Mockito.verify(dmi, Mockito.times(1)).setCamelContext(null);
        Mockito.verify(dmi, Mockito.times(1)).setRegistry(null);
    }

    @Test
    public void testDestroy_WHEN_CONTEXT_IS_NOT_SHARED() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final DeploymentMetaInfo dmi1 = service.createCamelContextForDeployment("deployment1.war");
        final DeploymentMetaInfo dmi2 = service.createCamelContextForDeployment("deployment2.war");
        final DeploymentMetaInfo dmi3 = service.createCamelContextForDeployment("deployment3.war");
        service.registerCamelContext(dmi1, "deployment1.war");
        service.registerCamelContext(dmi2, "deployment2.war");
        service.registerCamelContext(dmi3, "deployment3.war");
        service.destroy();
        Assert.assertTrue(service.instantiatedContexts.isEmpty());

    }

    @Test
    public void testCreateCamelContext() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final JndiContext jndiCtx = service.createJNDIContext();
        final DeploymentMetaInfo dmi = service.createCamelContext(key, jndiCtx);
        Assert.assertNotNull(dmi);
        Assert.assertNotNull(dmi.getCamelContext());
        Assert.assertNotNull(dmi.getRegistry());
        Assert.assertEquals(key, dmi.getCamelContext().getName());
        Assert.assertNotNull(dmi.getRegistry().lookup(TransactionPolicy.PROPAGATION_REQUIRED));
        Assert.assertNotNull(dmi.getRegistry().lookup(TransactionPolicy.PROPAGATION_REQUIRES_NEW));
    }

}
