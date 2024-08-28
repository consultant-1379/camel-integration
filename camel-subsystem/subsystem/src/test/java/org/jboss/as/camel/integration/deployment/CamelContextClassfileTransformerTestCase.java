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
package org.jboss.as.camel.integration.deployment;

import java.util.*;

import junit.framework.Assert;

import org.apache.camel.CamelContext;
import org.jboss.as.camel.integration.service.CamelContextProxyFactory;
import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.jandex.*;
import org.jboss.jandex.Type.Kind;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamelContextClassfileTransformerTestCase {

    @Mock
    CamelContextServiceMetaInfo metaInfoHolder;
    @Mock
    CamelContextService camelService;

    @Mock
    CamelContextClassfileTransformer classTransformer;

    CamelContextClassfileTransformer realClassTransformer;

    @Before
    public void setupTestCases() {
        realClassTransformer = new CamelContextClassfileTransformer(metaInfoHolder);
    }

    @Test
    public void injectCamelContextProxyTest() {
        final String fieldName = "ctx";
        final String deploymentName = "test.war";
        final String expected = "this.ctx=" + CamelContextProxyFactory.class.getCanonicalName() + ".createProxy(\"test.war\");\n";
        final String actual = realClassTransformer.createConstructorInjectionUsingProxy(fieldName, deploymentName);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void transformTest_MatchesClass() {
        final ClassForEnhancment tic = new ClassForEnhancment();
        final DeploymentUnit du = Mockito.mock(DeploymentUnit.class);
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        final DotName dotName = DotName.createSimple(ClassForEnhancment.class.getCanonicalName());
        final Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>();
        final ClassInfo classInfo = ClassInfo.create(dotName, null, (short) 0, null, annotations);
        final DotName dotNameForField = DotName.createSimple(CamelContext.class.getCanonicalName());
        final Type type = Type.create(dotNameForField, Kind.CLASS);
        final FieldInfo fieldInfo = FieldInfo.create(classInfo, "ctx", type, (short) 0);

        fieldInfoSet.add(fieldInfo);
        Mockito.when(metaInfoHolder.getInjectionPoints()).thenReturn(fieldInfoSet);
        Mockito.when(metaInfoHolder.getDu()).thenReturn(du);
        Mockito.when(du.getParent()).thenReturn(null);
        Mockito.when(du.getName()).thenReturn("test.war");
        Mockito.when(classTransformer._transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx", "test.war")).thenReturn(
                "Test".getBytes());
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();
        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);
        classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                .getClass().getProtectionDomain(), null);
        Mockito.verify(classTransformer)._transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx", "test.war");
    }

    @Test
    public void transformTest_DoesNotMatchClass() {
        final ClassForEnhancment tic = new ClassForEnhancment();
        final DeploymentUnit du = Mockito.mock(DeploymentUnit.class);
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        final DotName dotName = DotName.createSimple(ClassForEnhancment.class.getCanonicalName());
        final Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>();
        final ClassInfo classInfo = ClassInfo.create(dotName, null, (short) 0, null, annotations);
        final DotName dotNameForField = DotName.createSimple(CamelContext.class.getCanonicalName());
        final Type type = Type.create(dotNameForField, Kind.CLASS);
        final FieldInfo fieldInfo = FieldInfo.create(classInfo, "ctx", type, (short) 0);

        fieldInfoSet.add(fieldInfo);
        Mockito.when(metaInfoHolder.getInjectionPoints()).thenReturn(fieldInfoSet);
        Mockito.when(metaInfoHolder.getDu()).thenReturn(du);
        Mockito.when(du.getParent()).thenReturn(null);
        Mockito.when(du.getName()).thenReturn("test.war");
        Mockito.when(classTransformer._transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx", "test.war")).thenReturn(
                "Test".getBytes());
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), this.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();
        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);
        classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                .getClass().getProtectionDomain(), null);
        Mockito.verify(classTransformer, Mockito.times(0))._transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx",
                "test.war");
    }
}
