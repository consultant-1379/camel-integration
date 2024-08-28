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
package org.jboss.as.camel.testsuite.smoke.deployment.common;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.CamelContext;
import org.jboss.camel.annotations.CamelContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SomeManagedBean {

    private static final Logger log = LoggerFactory.getLogger(SomeManagedBean.class);

    @CamelContextService
    private CamelContext ctx;

    /**
     * @return the ctx
     */
    public CamelContext getCtx() {
        return ctx;
    }

    /**
     * @param ctx
     *            the ctx to set
     */
    public void setCtx(final CamelContext ctx) {
        this.ctx = ctx;
    }

    @PostConstruct
    public void doSomeTest() {
        log.debug("Injected camel context has id: {}", ctx.getName());
    }

}
