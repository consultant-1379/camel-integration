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
package org.jboss.camel.annotations;

import java.lang.annotation.*;

/**
 * Annotation for marking bean as camel processor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventHandler {
    /**
     * Id of this bean in camel context
     * 
     */
    String id();

    /**
     * Name of the camel context where this bean should be bound
     * 
     */
    String contextName() default "";

}
