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

import javax.ejb.*;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EnterpriseJavaBeanInTestImpl {

    @EJB
    private EnterpriseJavaBeanOne ejbOne;
    @EJB
    private EnterpriseJavaBeanTwo ejbTwo;

    /**
     * @return the ejbOne
     */
    public EnterpriseJavaBeanOne getEjbOne() {
        return ejbOne;
    }

    /**
     * @return the ejbTwo
     */
    public EnterpriseJavaBeanTwo getEjbTwo() {
        return ejbTwo;
    }

}
