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
package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jboss.camel.annotations.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventHandler(id = "myProcessor")
public class SomeCamelBean implements Processor {

    private static final Logger log = LoggerFactory.getLogger(SomeCamelBean.class);

    @Override
    public void process(final Exchange exchange) throws Exception {
        log.debug("Exchange incomming body---->{}", exchange.toString());
        final String incoming = (String) exchange.getIn().getBody();
        final String outgoing = incoming + " is camel contributed processor";
        exchange.getOut().setBody(outgoing);
        log.debug("Exchange outgoing body---->{}", outgoing);

    }

}
