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

import org.jboss.jandex.*;

/**
 * Meta info holder for camel bean annotation
 */
public class CamelContributionBeamMetaInfo {

    private AnnotationInstance annotationInstance;

    /**
     * Full arg constructor
     * 
     * @param annotationInstance
     *            Annotation instance for which we are storing info
     */
    public CamelContributionBeamMetaInfo(final AnnotationInstance annotationInstance) {
        this.annotationInstance = annotationInstance;
    }

    /**
     * ClassInfo for this annotation
     * 
     * @return ClassInfo related to this annotation
     */
    public ClassInfo getClassInfo() {
        return (ClassInfo) this.annotationInstance.target();
    }

    /**
     * Getter for bean id
     * 
     * @return Id of this camel bean, as specified in annotation
     */
    public String getBeanId() {
        final AnnotationValue av = this.annotationInstance.value("id");
        return av.asString();
    }

    /**
     * Getter for context name
     * 
     * @return Name of the camel context where this bean will be bound
     */
    public String getContextName() {
        final AnnotationValue av = this.annotationInstance.value("contextName");
        return av.asString();
    }
}
