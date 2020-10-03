package synchronizer.github.saver.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import synchronizer.github.saver.GHSaver;

@Component(name = "synchronizer.github.saver", immediate = true)
public class ActivatorComponent {
    private static final Log log = LogFactory.getLog(ActivatorComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("---------- Activating GitHub Artifact Synchronizer OSGi ------------");
        }
        BundleContext bundleContext = context.getBundleContext();
        GHSaver ghSaver = new GHSaver();
        bundleContext.registerService(ArtifactSaver.class.getName(), ghSaver, null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("component deactivated");
        }
    }

    @Reference(
        name = "api.manager.config.service", 
        service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class, 
        cardinality = ReferenceCardinality.MANDATORY, 
        policy = ReferencePolicy.STATIC, 
        unbind = "unsetAPIManagerConfigurationService"
    )
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}
