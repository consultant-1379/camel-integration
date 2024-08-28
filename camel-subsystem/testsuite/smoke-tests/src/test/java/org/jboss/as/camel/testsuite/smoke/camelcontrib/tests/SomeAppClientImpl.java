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

import javax.enterprise.context.RequestScoped;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.jboss.camel.annotations.CamelContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class SomeAppClientImpl implements SomeAppClient {

    /**
     * 
     */
    private static final long serialVersionUID = 7977285921545665045L;
    private static final Logger log = LoggerFactory.getLogger(SomeAppClientImpl.class);

    @CamelContextService
    private CamelContext ctx;

    @Override
    public void createRouteWithContribProcessor() throws Exception {

        log.trace("<-----------------------------INVOKED");

        final RouteBuilder routeBuilder = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                final RouteDefinition routeDef = from("direct:/" + TestConstants.ROUTE_FOUR);
                routeDef.setId(TestConstants.ROUTE_FOUR);
                routeDef.to("myProcessor");
                routeDef.to("file:target?fileName=" + TestConstants.CONTRIB_RESULT_FILENAME);

            }
        };
        this.ctx.addRoutes(routeBuilder);
        final Route route = this.ctx.getRoute(TestConstants.ROUTE_FOUR);
        ctx.startRoute(TestConstants.ROUTE_FOUR);
        final Producer producer = route.getEndpoint().createProducer();
        final Exchange exchange = producer.createExchange();
        exchange.getIn().setBody(TestConstants.CONTRIB_TEST_MSG_BODY);
        exchange.getOut().setBody(TestConstants.CONTRIB_TEST_MSG_BODY);
        producer.process(exchange);

    }
}
