package org.jboss.as.camel.integration.deployment;

import static org.jboss.as.server.deployment.Attachments.*;

import java.util.*;

import org.jboss.as.server.deployment.*;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.jandex.*;
import org.jboss.logging.Logger;

/**
 * Deployment processor that will search through annotation index and find all camel integration related annotations. It will look for annotations:
 * 
 * <pre>
 * CamelContextService injection point for camel context
 * </pre>
 */
public class CamelContextServiceAnnotationScannerDP extends AbstractAnnotationScannerDP {

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.PARSE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4000;

    /**
     * Skip all in web-inf/lib
     */
    private static final String SKIP_WEB_INF_LIB = "/WEB-INF/LIB";

    Logger log = Logger.getLogger(CamelContextServiceAnnotationScannerDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final List<ResourceRoot> resourceRoots = DeploymentUtils.allResourceRoots(deploymentUnit);
        final ResourceRoot topLevelRoot = resourceRoots.get(0);
        if (log.isTraceEnabled()) {
            log.trace("Processing deployment unit: " + deploymentUnit);
        }

        //        if (topLevelRoot.getRootName().endsWith(".ear")) {
        //            if (log.isTraceEnabled()) {
        //                log.trace("Skipping ear resource root, we will process subdeployments");
        //            }
        //            return;
        //        }
        //        if (topLevelRoot.getRootName().endsWith(".war")) {
        //            if (log.isTraceEnabled()) {
        //                log.trace("Filtering WEB-INF/lib from war");
        //            }
        //            resourceRoots = filterResourceRoots(resourceRoots, SKIP_WEB_INF_LIB);
        //        }
        final CamelContextServiceMetaInfo metaInfoHolder = new CamelContextServiceMetaInfo(deploymentUnit);
        for (final ResourceRoot resourceRoot : resourceRoots) {
            if (log.isTraceEnabled()) {
                log.trace("Scanning resource root:" + resourceRoot.getRoot().getPathName() + " for annotations.");
            }
            final Index index = resourceRoot.getAttachment(ANNOTATION_INDEX);
            if (index != null) {
                try {
                    final Set<FieldInfo> fieldInfoSet = processAnnotations(index, resourceRoot, deploymentUnit);
                    if (!fieldInfoSet.isEmpty()) {
                        metaInfoHolder.setInjectionPoints(fieldInfoSet);
                    }
                } catch (final Exception e) {
                    throw new DeploymentUnitProcessingException(e);
                }
            }
        }
        if (!metaInfoHolder.getInjectionPoints().isEmpty()) {
            deploymentUnit.putAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO, metaInfoHolder);
        }

    }

    private Set<FieldInfo> processAnnotations(final Index index, final ResourceRoot resourceRoot, final DeploymentUnit unit) throws Exception {
        final List<AnnotationInstance> annotationList = index.getAnnotations(DotName.createSimple(CamelContextService.class.getName()));
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        if (!annotationList.isEmpty()) {

            for (final AnnotationInstance inst : annotationList) {
                if (inst.target() instanceof FieldInfo) {
                    final FieldInfo fieldInfo = (FieldInfo) inst.target();
                    if (log.isTraceEnabled()) {
                        log.trace("Found injection point " + fieldInfo.declaringClass() + " into field " + fieldInfo.name());
                    }
                    fieldInfoSet.add(fieldInfo);
                }
            }
        }
        return fieldInfoSet;
    }

}
