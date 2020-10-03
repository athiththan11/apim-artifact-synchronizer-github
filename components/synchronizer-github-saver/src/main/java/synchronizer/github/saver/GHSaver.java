package synchronizer.github.saver;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import synchronizer.github.core.GHSynchronizerConstant;
import synchronizer.github.core.GHSynchronizerUtil;
import synchronizer.github.core.client.GHClient;
import synchronizer.github.core.decoder.GHErrorDecoder;
import synchronizer.github.core.exception.GHClientException;
import synchronizer.github.core.interceptor.GHTokenRequestInterceptor;
import synchronizer.github.core.model.ArtifactContent;
import synchronizer.github.core.model.GHCommit;

// TODO: make configurable [branch, custom path, etc]
public class GHSaver implements ArtifactSaver {

    private GHClient githubClient = null;
    private String githubRepo;
    private String githubOwner;

    private static final Log log = LogFactory.getLog(GHSaver.class);

    @Override
    public void disconnect() {
        // implement if required
    }

    @Override
    public String getName() {
        return GHSynchronizerConstant.GH_SAVER;
    }

    @Override
    public void init() throws ArtifactSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("------------ GHSaver init() -------------");
        }

        String githubAccessToken = System.getProperty(GHSynchronizerConstant.GH_ACCESS_TOKEN);
        this.githubOwner = System.getProperty(GHSynchronizerConstant.GH_OWNER);
        this.githubRepo = System.getProperty(GHSynchronizerConstant.GH_REPO);

        if (!StringUtils.isNotBlank(githubAccessToken) && !StringUtils.isNotBlank(this.githubOwner)
                && !StringUtils.isNotBlank(this.githubRepo)) {
            throw new ArtifactSynchronizerException("Error while inititating GitHub Artifact Sync Client.\n"
                    + " Missing Credentials: GitHubAccessToken, GitHubOwner, GitHubRepo");
        }

        try {
            githubClient = Feign.builder()
                    .client(new ApacheFeignHttpClient(APIUtil.getHttpClient(GHSynchronizerConstant.GH_ENDPOINT)))
                    .encoder(new GsonEncoder()).decoder(new GsonDecoder()).errorDecoder(new GHErrorDecoder())
                    .logger(new Slf4jLogger()).requestInterceptor(new GHTokenRequestInterceptor(githubAccessToken))
                    .target(GHClient.class, GHSynchronizerConstant.GH_ENDPOINT);
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error occured while initializing GitHub Saver", e);
        }
    }

    @Override
    public boolean isAPIPublished(String apiId) {
        if (log.isDebugEnabled()) {
            log.debug("------------ GHSaver isAPIPublished() -------------");
        }

        APIManagerConfiguration apimConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Map<String, Environment> gatewayEnvironments = apimConfiguration.getApiGatewayEnvironments();

        int count = 0;
        for (String env : gatewayEnvironments.keySet()) {
            String path = GHSynchronizerUtil.constructPath(env, apiId);
            List<GHCommit> commits = githubClient.getCommitsofArtifactContent(this.githubOwner, this.githubRepo, path);
            if (!commits.isEmpty()) {
                // PizzaShackAPI:v1.0.0:@carbon.super::Publish;[Production and Sandbox]
                String commitMessage = commits.get(0).getCommit().getMessage();
                String[] subStrings = commitMessage.split("::");
                String status = null;
                if (subStrings.length > 1 && subStrings[1] != null) {
                    status = subStrings[1].split(";")[0];
                }
                if (status != null && status
                        .equalsIgnoreCase(APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH)) {
                    count++;
                }
            }
        }
        return count != 0;
    }

    @Override
    public void saveArtifact(String gatewayRuntimeArtifacts, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("------------ GHSaver saveArtifact() -------------");
        }

        if (!isGHSaverInitialized()) {
            throw new ArtifactSynchronizerException("GitHub Saver is not initialized");
        }

        JSONObject artifactObject = new JSONObject(gatewayRuntimeArtifacts);
        String apiId = (String) artifactObject.get("apiId");
        String path = GHSynchronizerUtil.constructPath(gatewayLabel, apiId);

        // check whether artifact is exists in the repo to collect the hash values
        ArtifactContent existingContent = null;
        try {
            existingContent = githubClient.getArtifactContent(this.githubOwner, this.githubRepo, path);
        } catch (GHClientException e) {
            if (e.getStatusCode() != 404) {
                throw new ArtifactSynchronizerException("Error occured while retriving existing artifact.", e);
            }
        }

        String apiName = (String) artifactObject.get("name");
        String version = (String) artifactObject.get("version");
        String tenantDomain = (String) artifactObject.get("tenantDomain");

        ArtifactContent artifactContent = new ArtifactContent();
        String encodedContent = Base64.getEncoder().encodeToString(gatewayRuntimeArtifacts.getBytes());
        artifactContent.setContent(encodedContent);

        if (existingContent != null) {
            artifactContent.setSha(existingContent.getSha());
        }

        // commit message used to extract meta information as gateway labels and the
        // status
        String commitMessage = apiName + ":v" + version + ":@" + tenantDomain + "::" + gatewayInstruction + ";["
                + gatewayLabel + "]";
        artifactContent.setMessage(commitMessage);

        githubClient.createArtifactContent(this.githubOwner, this.githubRepo, path, artifactContent);
    }

    /**
     * method to verify whether the GHSaver has been initialized
     * 
     * @return boolean
     */
    private boolean isGHSaverInitialized() {
        return StringUtils.isNotBlank(githubOwner) && StringUtils.isNotBlank(githubRepo) && githubClient != null;
    }
}
