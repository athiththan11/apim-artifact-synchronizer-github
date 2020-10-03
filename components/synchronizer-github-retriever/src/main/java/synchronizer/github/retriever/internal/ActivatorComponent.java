package synchronizer.github.retriever.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;

import synchronizer.github.retriever.GHRetriever;

@Component(name = "synchronizer.github.retriever", immediate = true)
public class ActivatorComponent {
    private static final Log log = LogFactory.getLog(ActivatorComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("---------- Activating GitHub Artifact Synchronizer OSGi ------------");
        }
        BundleContext bundleContext = context.getBundleContext();
        GHRetriever ghRetriever = new GHRetriever();
        bundleContext.registerService(ArtifactRetriever.class.getName(), ghRetriever, null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("component deactivated");
        }
    }
}
