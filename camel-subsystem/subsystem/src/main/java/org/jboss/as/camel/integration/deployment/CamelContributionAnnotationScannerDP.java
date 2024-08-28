package org.jboss.as.camel.integration.deployment;

import static org.jboss.as.server.deployment.Attachments.*;

import java.util.*;

import org.jboss.as.server.deployment.*;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.camel.annotations.EventHandler;
import org.jboss.jandex.*;
import org.jboss.logging.Logger;

/**
 * Deployment processor that will search through annotation index and find all camel contribution related annotations It will look for these
 * annotations:
 * 
 * @CamelBean
 * 
 *            annotation marking bean as camel related one
 */
public class CamelContributionAnnotationScannerDP extends AbstractAnnotationScannerDP {

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

    Logger log = Logger.getLogger(CamelContributionAnnotationScannerDP.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        List<ResourceRoot> resourceRoots = DeploymentUtils.allResourceRoots(deploymentUnit);
        final ResourceRoot topLevelRoot = resourceRoots.get(0);
        if (log.isTraceEnabled()) {
            log.trace("Processing deployment unit: " + deploymentUnit);
        }

        if (topLevelRoot.getRootName().endsWith(".ear")) {
            if (log.isTraceEnabled()) {
                log.trace("Skipping ear resource root, we will process subdeployments");
            }
            return;
        }
        if (topLevelRoot.getRootName().endsWith(".war")) {
            if (log.isTraceEnabled()) {
                log.trace("Filtering WEB-INF/lib from war");
            }
            resourceRoots = filterResourceRoots(resourceRoots, SKIP_WEB_INF_LIB);
        }
        final CamelContributionMetaInfoHolder metaInfoHolder = new CamelContributionMetaInfoHolder(deploymentUnit);
        for (final ResourceRoot resourceRoot : resourceRoots) {
            if (log.isTraceEnabled()) {
                log.trace("Scanning resource root:" + resourceRoot.getRoot().getPathName() + " for annotations.");
            }
            final Index index = resourceRoot.getAttachment(ANNOTATION_INDEX);
            if (index != null) {
                try {
                    final Set<CamelContributionBeamMetaInfo> beanMetaInfoSet = processAnnotations(index, resourceRoot, deploymentUnit);
                    if (!beanMetaInfoSet.isEmpty()) {
                        metaInfoHolder.setCamelBeanMetaInfo(beanMetaInfoSet);
                    }
                } catch (final Exception e) {
                    throw new DeploymentUnitProcessingException(e);
                }
            }
        }
        if (!metaInfoHolder.getCamelBeanMetaInfo().isEmpty()) {
            deploymentUnit.putAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER, metaInfoHolder);
        }

    }

    /**
     * Process annotations from annotation index and find ones we are interested in
     * 
     * @param index
     *            Annotation index prepared by jandex
     * @param resourceRoot
     *            Resource root for this deployment
     * @param unit
     *            Deployment unit that we are processing
     * @return Set of meta info, stored in CamelContributionBeanMetaInfo
     * @throws Exception
     *             in case something has gone wrong
     */
    protected final Set<CamelContributionBeamMetaInfo> processAnnotations(final Index index, final ResourceRoot resourceRoot,
                                                                          final DeploymentUnit unit) throws Exception {
        final List<AnnotationInstance> annotationList = index.getAnnotations(DotName.createSimple(EventHandler.class.getName()));
        final Set<CamelContributionBeamMetaInfo> beanMetaInfoSet = new HashSet<CamelContributionBeamMetaInfo>();
        if (!annotationList.isEmpty()) {

            for (final AnnotationInstance inst : annotationList) {
                if (inst.target() instanceof ClassInfo) {
                    final ClassInfo classInfo = (ClassInfo) inst.target();
                    if (log.isTraceEnabled()) {
                        log.trace("Found @EventHandler annotation on: " + classInfo.name());
                    }
                    final CamelContributionBeamMetaInfo beanMetaInfo = new CamelContributionBeamMetaInfo(inst);
                    beanMetaInfoSet.add(beanMetaInfo);
                }
            }
        }
        return beanMetaInfoSet;
    }

}
