package org.jboss.as.camel.integration.service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.util.jndi.JndiContext;
import org.jboss.as.camel.integration.deployment.CamelContributionBeamMetaInfo;
import org.jboss.as.camel.integration.deployment.CamelContributionMetaInfoHolder;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.camel.tx.TransactionPolicy;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;
import org.jboss.msc.service.StartException;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Camel context service Encapsulates ability to create, star/stop and bind beans into either shared or per-deployment camel context.
 * 
 * @author edejket
 */
public class CamelContextService {

    /**
     * Suffix for deployment names, final string is deployment_name + this suffix
     */
    public static final String CAMEL_CONTEXT_NAME_SUFFIX = "-camel-context";

    /**
     * Second part of the log statement
     */
    private static final String WITH_ID = ", with id: ";

    /**
     * first part of log statement
     */
    private static final String BOUND_BEAN = "Bound bean:";

    private static final Logger log = Logger.getLogger(CamelContextService.class);

    final Map<String, DeploymentMetaInfo> instantiatedContexts;
    final Map<String, CamelContributionMetaInfoHolder> boundBeans;
    final JtaTransactionManager jtaTransactionManager;
    final boolean shareContextEnabled;
    DeploymentMetaInfo sharedContext;

    /**
     * Full argument constructor
     * 
     * @param shareContextEnabled
     *            Boolean value that controls if context will be shared or created for each deployment
     * @param ctxName
     *            Name of the shared context - (configured in subsystem xml, or toplevel deployment name, if context is created per deployment)
     * @throws StartException
     *             in case we can't execute all operations
     */
    public CamelContextService(final boolean shareContextEnabled, final String ctxName) throws StartException {
        this.shareContextEnabled = shareContextEnabled;
        this.instantiatedContexts = new ConcurrentHashMap<String, DeploymentMetaInfo>();
        this.boundBeans = new HashMap<String, CamelContributionMetaInfoHolder>();
        this.jtaTransactionManager = new JtaTransactionManager(TransactionUtil.getTransactionManager());
        if (shareContextEnabled) {
            final JndiContext jndiCtx = createJNDIContext();
            this.sharedContext = createCamelContext(ctxName, jndiCtx);
        } else {
            sharedContext = null;
        }
    }

    /**
     * Bind all scanned beans to camel context
     * 
     * @param metaInfoHolder
     *            Meta information on all found beans
     * @throws Exception
     *             in case something went wrong
     */
    public void bindBeans(final CamelContributionMetaInfoHolder metaInfoHolder) throws Exception {

        for (final CamelContributionBeamMetaInfo beanMeta : metaInfoHolder.getCamelBeanMetaInfo()) {
            final DotName classDotName = beanMeta.getClassInfo().name();
            if (shareContextEnabled) {
                this.sharedContext.getRegistry().bind(
                        beanMeta.getBeanId(),
                        metaInfoHolder.getDeploymentUnit().getAttachment(Attachments.MODULE).getClassLoader().loadClass(classDotName.toString())
                                .getDeclaredConstructor().newInstance());
                log.trace(BOUND_BEAN + classDotName + WITH_ID + beanMeta.getBeanId());
            } else {
                //TODO check this logic, we have to find correct camel context, if such exists to bind there
                final String contextName = beanMeta.getContextName();
                final Collection<DeploymentMetaInfo> deployments = this.instantiatedContexts.values();
                boolean found = false;
                for (final DeploymentMetaInfo dmi : deployments) {
                    if (contextName.equals(((DefaultCamelContext) dmi.getCamelContext()).getName())) {
                        dmi.getRegistry().bind(
                                beanMeta.getBeanId(),
                                metaInfoHolder.getDeploymentUnit().getAttachment(Attachments.MODULE).getClassLoader()
                                        .loadClass(classDotName.toString()).getDeclaredConstructor().newInstance());
                        log.trace(BOUND_BEAN + classDotName + WITH_ID + beanMeta.getBeanId());
                        found = true;
                    }
                }
                if (!found) {
                    throw new Exception("Unable to bind bean " + beanMeta.getBeanId() + " to non-existing camel context with name " + contextName
                            + ". Context with that name does not exist");
                }

            }
        }

    }

    /**
     * Unbind all bound beans, happens as part of undeploy
     * 
     * @param deploymentName
     *            Name of the deployment unit who's beans were are unbinding.
     * @throws Exception
     *             in case something went wrong
     */
    public void unbindBeans(final String deploymentName) throws Exception {
        final CamelContributionMetaInfoHolder metaHolder = this.boundBeans.get(deploymentName);
        if (metaHolder != null) {
            final Set<CamelContributionBeamMetaInfo> boundBeans = metaHolder.getCamelBeanMetaInfo();
            for (final CamelContributionBeamMetaInfo beanMetaInfo : boundBeans) {
                if (shareContextEnabled) {
                    try {
                        this.sharedContext.getRegistry().unbind(beanMetaInfo.getBeanId());
                    } catch (final Exception e) {
                        log.error(e);
                    }
                } else {
                    final String contextName = beanMetaInfo.getContextName();
                    //TODO check this logic!!!!
                    final Collection<DeploymentMetaInfo> deployments = this.instantiatedContexts.values();
                    for (final DeploymentMetaInfo dmi : deployments) {
                        if (contextName.equals(((DefaultCamelContext) dmi.getCamelContext()).getName())) {
                            dmi.getRegistry().unbind(beanMetaInfo.getBeanId());
                        }
                    }
                }
            }
        }
    }

    /**
     * Create camel context for this deployment name
     * 
     * @param deploymentName
     *            Name of the deployment (top level deployment is used as a key)
     * @return Instance of DeploymentMetaInfo that holds reference to created context and jndi registry associated with that context
     * @throws Exception
     *             in case something went wrong
     */
    public DeploymentMetaInfo createCamelContextForDeployment(final String deploymentName) throws Exception {
        if (shareContextEnabled) {
            return sharedContext;
        } else {
            final JndiContext jndiContext = createJNDIContext();
            return createCamelContext(deploymentName + CAMEL_CONTEXT_NAME_SUFFIX, jndiContext);
        }
    }

    /**
     * Start camel context
     * 
     * @param dmi
     *            DeploymentMetaInfo who's camel context we are about to start
     * @throws Exception
     *             in case something went wrong
     */
    public void startCamelContext(final DeploymentMetaInfo dmi) throws Exception {
        if (!shareContextEnabled) {
            dmi.getCamelContext().start();
        }

    }

    /**
     * Stop camel context
     * 
     * @param dmi
     *            DeploymentMetaInfo who's camel context we are about to stop
     * @throws Exception
     *             in case something went wrong
     */
    public void stopCamelContext(final DeploymentMetaInfo dmi) throws Exception {
        if (!shareContextEnabled) {
            dmi.getCamelContext().stop();
        }
    }

    /**
     * Register this DeploymentMetaInfo with key
     * 
     * @param dmi
     *            DeploymentMetaInfo to register
     * @param key
     *            String key, name of the top level deployment will be used as key
     */
    public void registerCamelContext(final DeploymentMetaInfo dmi, final String key) {
        if (!shareContextEnabled) {
            instantiatedContexts.put(key, dmi);
        }
    }

    /**
     * Remove association between this DeploymentMetaInfo and key
     * 
     * @param key
     *            Top level deployment name will be used as a key
     */
    public void deregisterCamelContext(final String key) {
        if (!shareContextEnabled) {
            instantiatedContexts.remove(key);
        }
    }

    /**
     * Return camel context for given deployment
     * 
     * @param deployment
     *            Deployment name, here top level deployment name is used as a key
     * @return DeploymentMetaInfo holding reference to requesting camel context
     */
    public DeploymentMetaInfo getCamelContextForDeployment(final String deployment) {
        if (shareContextEnabled) {
            return this.sharedContext;
        } else {
            final DeploymentMetaInfo dmi = instantiatedContexts.get(deployment);
            if (dmi == null) {
                throw new java.lang.IllegalStateException("Deployment " + deployment + " did not register any camel contexts");
            }
            return dmi;
        }
    }

    /**
     * Stop and deregister all contexts, and free all resources
     */
    public void destroy() {
        final Set<Entry<String, DeploymentMetaInfo>> entrySet = instantiatedContexts.entrySet();
        for (final Entry<String, DeploymentMetaInfo> entry : entrySet) {
            if (entry.getValue() != null) {
                tryToStopContext(entry.getValue());
            }
        }
        if (sharedContext != null) {
            tryToStopContext(sharedContext);
        }
        instantiatedContexts.clear();

    }

    /**
     * @return the sharedContext
     */
    DeploymentMetaInfo getSharedContext() {
        return sharedContext;
    }

    /**
     * @param sharedContext
     *            the sharedContext to set
     */
    void setSharedContext(final DeploymentMetaInfo sharedContext) {
        this.sharedContext = sharedContext;
    }

    /**
     * Try to stop camel context for this deployment meta info
     * 
     * @param meta
     *            DeploymentMetaInfo holding reference to camel context we are trying to stop
     */
    public void tryToStopContext(final DeploymentMetaInfo meta) {
        try {
            if (meta != null && meta.getCamelContext() != null) {
                tryToStopContext(meta.getCamelContext(), meta.getRegistry());
                meta.setRegistry(null);
                meta.setCamelContext(null);
            }
        } catch (final Exception e) {
            log.error("Error trying to stop context:", e);
        }
    }

    private void tryToStopContext(final CamelContext ctx, final JndiContext jndi) {
        try {
            if (ctx != null) {
                log.info("Stopping CamelContext  " + ctx.getName() + "...");
                ctx.stop();
                if (jndi != null) {
                    jndi.close();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create camel context
     * 
     * @param name
     *            Name to be used for this context
     * @param jndiContext
     *            JndiContext instanct that will be used by this camel context
     * @return Constructed and started camel context
     * @throws StartException
     *             in case something goes wrong
     */
    DeploymentMetaInfo createCamelContext(final String name, final JndiContext jndiContext) throws StartException {
        final DefaultCamelContext ctx = new DefaultCamelContext(jndiContext);
        DeploymentMetaInfo dmi = null;
        try {
            ctx.setName(name);
            bindTransactionPolicies(jndiContext, new String[] { TransactionPolicy.PROPAGATION_REQUIRED, TransactionPolicy.PROPAGATION_REQUIRES_NEW });
            ctx.start();
            dmi = new DeploymentMetaInfo(ctx, jndiContext);
            return dmi;
        } catch (final Exception e) {
            tryToStopContext(dmi);
            throw new StartException(e.getMessage(), e.getCause());
        }
    }

    private SpringTransactionPolicy createTransactionPolicy(final String name, final JtaTransactionManager txMgr) {
        final SpringTransactionPolicy policy = new SpringTransactionPolicy();
        policy.setPropagationBehaviorName(name);
        policy.setTransactionManager(this.jtaTransactionManager);
        return policy;
    }

    /**
     * Utility method to create jndi context
     * 
     * @return Instance of jndi context
     * @throws StartException
     *             in case something went wrong
     */
    JndiContext createJNDIContext() throws StartException {
        try {
            final JndiContext jndiCtx = new JndiContext();
            return jndiCtx;
        } catch (final Exception ne) {
            log.error("Unable to bind transaction policies for camel, camel transaction support will not work.", ne);
            throw new StartException(ne);
        }
    }

    private void bindTransactionPolicies(final JndiContext jndi, final String... policies) throws NamingException {
        for (final String policyName : policies) {
            jndi.bind(policyName, createTransactionPolicy(policyName, jtaTransactionManager));
        }
    }

}
