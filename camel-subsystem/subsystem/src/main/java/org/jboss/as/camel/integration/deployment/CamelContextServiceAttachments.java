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

import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.AttachmentKey;

/**
 * Utility class for all attachment keys
 */
public class CamelContextServiceAttachments {

    public static final AttachmentKey<CamelContextServiceMetaInfo> CAMEL_SERVICE_META_INFO = AttachmentKey.create(CamelContextServiceMetaInfo.class);

    public static final AttachmentKey<CamelContributionMetaInfoHolder> CAMEL_CONTRIBUTIONS_META_INFO_HOLDER = AttachmentKey
            .create(CamelContributionMetaInfoHolder.class);

    public static final AttachmentKey<DeploymentMetaInfo> DEPLOYMENT_META_INFO = AttachmentKey.create(DeploymentMetaInfo.class);
}
