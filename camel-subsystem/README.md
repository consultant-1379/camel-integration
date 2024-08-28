<a href='https://dekstroza.ci.cloudbees.com/job/camel-jboss-subsystem/'><img src='https://dekstroza.ci.cloudbees.com/buildStatus/icon?job=camel-jboss-subsystem'></a>
Apache Camel Subsystem for EAP 6.1.1
================
<p>Apache Camel integration, with subsystem and deployers</p>
<h2> How to run</h2>
<p>To run do: mvn -U clean install -Dts -Pec2-nexus</p>
How to use in project
----------------
<p>On application server:
<ul>
<li>
Copy camel-jboss-module module and subsystem jboss module to $JBOSS_HOME/modules/...
</li
<li>
Add extension in your standalone-*.xml:<br />  
<em>
<pre>
&lt;extension module="org.jboss.as.camel.camel-subsystem"/&gt; 
</pre>
</em>
to &lt;extensions&gt; section.
</li> 
<li>
Add subsystem in your standalone-*.xml:<br />   
<em>
<pre>
&lt;subsystem xmlns="urn:org.jboss.as:camel-subsystem:1.0"&gt;
			&nbsp;&lt;shared-camel-context enabled="true" context-name="dekstroza" /&gt;
&lt;/subsystem&gt;
</pre>
</em>
to &lt;profile&gt; section.
</li>
<li>
<p>In your application, you only need to declare provided scope dependency towards:</p>
<p>
<em>
<pre>
			&lt;dependency&gt;
			&nbsp;&lt;groupId&gt;org.jboss.as.camel&gt;/groupId&gt;
			&nbsp;&lt;artifactId&gt;camel-integration-api&gt;/artifactId&gt;&nbsp; &nbsp;
			&nbsp;&lt;version&gt;1.0.0-SNAPSHOT&gt;/version&gt;
			&nbsp;&lt;scope&gt;provided&gt;/scope&gt;
			&lt;dependency&gt;
</pre>
</em>
</p>
</li>
 <li>

<p>In your stateless session bean or statefull bean or cdi bean or plain pojo use as shown bellow:</br>
<em>
<pre>
@Stateless
public class SomeEjb {

@CamelContextService
private CamelContext ctx;
...
}
</pre>
</em>
</p>
</li>
<li> 
<p>Camel processors can be contributed now externally, ie create war containing:</p>
<em>
<pre>
package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jboss.camel.annotations.CamelBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CamelBean(id = "myProcessor")
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
</pre>
</em>
<p> and deploy to EAP and use in above stateless session bean like in the example bellow <b>(note the annotation on processor and name of the processor in camel route)</b>:</p>
<p>
<em>
<pre>
package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import javax.enterprise.context.RequestScoped;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.jboss.camel.annotations.CamelContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless //or @RequestScope or @Singleton or @SessionScope or any other pojo
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
</pre>
</em>
</p>
</ul>
