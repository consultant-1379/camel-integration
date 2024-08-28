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

import java.io.*;

import org.apache.camel.CamelContext;
import org.jboss.as.server.deployment.*;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamelContextServiceAnnotationScannerDpTestCase {

    public class DummyClass implements Serializable {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;
        @CamelContextService
        private CamelContext ctx;
    }

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private MountHandle mh;

    Index index;

    private CamelContextServiceAnnotationScannerDP scanner;

    public Index createTestIndex() throws IOException {
        final Indexer indexer = new Indexer();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        final Index index = indexer.complete();
        return index;
    }

    public void setUpEARTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.ear");
        final ResourceRoot topLevel = new ResourceRoot(topLevelDeployment, mh);
        topLevel.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevel);
    }

    public void setUpWARTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        topLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevelRoot);

    }

    @Test
    public void testDeploy_PROCESS_WAR() throws Exception {
        setUpWARTestCase();
        scanner = new CamelContextServiceAnnotationScannerDP();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Mockito.any(AttachmentKey.class), Mockito.any(CamelContextServiceMetaInfo.class));
    }

    public void setUpWARFileterTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        final VirtualFile subLevel = VirtualFileFactory.createVirtualFileWithParent("/web-inf/lib/test.jar", topLevelDeployment);
        final ResourceRoot subLevelRoot = new ResourceRoot(subLevel, mh);
        final AttachmentList<ResourceRoot> resourceRoots = new AttachmentList<ResourceRoot>(ResourceRoot.class);
        resourceRoots.add(subLevelRoot);
        //Add annotation index to both sublevel and toplevel
        topLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        subLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());

        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevelRoot);
        Mockito.when(du.getAttachment(Attachments.RESOURCE_ROOTS)).thenReturn(resourceRoots);

    }

    @Test
    public void testDeploy_PROCESS_WAR_verify_WEB_INF_LIB_IS_FILTERED() throws Exception {
        setUpWARFileterTestCase();
        scanner = new CamelContextServiceAnnotationScannerDP();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Mockito.any(AttachmentKey.class), Mockito.any(CamelContextServiceMetaInfo.class));
    }
}
