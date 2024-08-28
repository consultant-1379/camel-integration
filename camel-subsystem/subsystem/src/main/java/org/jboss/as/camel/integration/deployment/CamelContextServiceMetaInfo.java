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

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.FieldInfo;

/**
 * CamelContext injection metainfo
 */
public class CamelContextServiceMetaInfo {

    private DeploymentUnit du;
    private ResourceRoot rr;
    private Set<FieldInfo> injectionPoints;

    /**
     * Full arg constructor
     * 
     * @param du
     *            Deployment unit
     */
    public CamelContextServiceMetaInfo(final DeploymentUnit du) {
        this.injectionPoints = new HashSet<FieldInfo>();
        this.du = du;
    }

    /**
     * @return the du
     */
    public DeploymentUnit getDu() {
        return du;
    }

    /**
     * @param du
     *            the du to set
     */
    public void setDu(final DeploymentUnit du) {
        this.du = du;
    }

    /**
     * @return the rr
     */
    public ResourceRoot getRr() {
        return rr;
    }

    /**
     * @param rr
     *            the rr to set
     */
    public void setRr(final ResourceRoot rr) {
        this.rr = rr;
    }

    /**
     * @return the injectionPoints
     */
    public Set<FieldInfo> getInjectionPoints() {
        return injectionPoints;
    }

    /**
     * @param injectionPoints
     *            the injectionPoints to set
     */
    public void setInjectionPoints(final Set<FieldInfo> injectionPoints) {
        this.injectionPoints = injectionPoints;
    }

    @Override
    public String toString() {
        return "CamelContextServiceMetaInfo [du=" + du + ", rr=" + rr + ", injectionPoints=" + injectionPoints + "]";
    }

}
