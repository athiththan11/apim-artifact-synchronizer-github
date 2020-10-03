package synchronizer.github.retriever;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
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
import synchronizer.github.core.model.DirectoryContent;
import synchronizer.github.core.model.GHCommit;

public class GHRetriever implements ArtifactRetriever {

    private GHClient githubClient = null;
    private String githubRepo;
    private String githubOwner;

    private static final Log log = LogFactory.getLog(GHRetriever.class);

    @Override
    public void disconnect() {
        // implement if required
    }

    @Override
    public String getName() {
        return GHSynchronizerConstant.GH_RETRIEVER;
    }

    @Override
    public void init() throws ArtifactSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("------------ GHRetriever init() -------------");
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
    public List<String> retrieveAllArtifacts(String gatewayLabel) throws ArtifactSynchronizerException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("------------ GHRetriever retrieveAllArtifacts() -------------");
        }

        if (!isGHRetrieverInitialized()) {
            throw new ArtifactSynchronizerException("GitHub Retriever is not initialized");
        }

        List<String> artifacts = new ArrayList<>();
        String path = gatewayLabel.replaceAll("\\s", "_");
        try {
            List<DirectoryContent> directoryContents = githubClient.getDirectoryContent(this.githubOwner,
                    this.githubRepo, path);
            for (DirectoryContent dir : directoryContents) {
                String content = retrieveArtifactContent(dir.getPath());
                if (content != null) {
                    artifacts.add(content);
                }
            }
        } catch (GHClientException e) {
            throw new ArtifactSynchronizerException("Error occured while retrieving all artifacts", e);
        }
        return artifacts;
    }

    /**
     * method to retrieve artifact content from github
     * 
     * @param path Artifact path
     * @return Base64 decoded content string
     * @throws ArtifactSynchronizerException
     */
    private String retrieveArtifactContent(String path) throws ArtifactSynchronizerException {
        if (!isGHRetrieverInitialized()) {
            throw new ArtifactSynchronizerException("GitHub Retriever is not initialized");
        }

        try {
            ArtifactContent artifact = githubClient.getArtifactContent(this.githubOwner, this.githubRepo, path);
            if (artifact != null && artifact.getContent() != null) {
                byte[] decodedString = org.apache.commons.codec.binary.Base64.decodeBase64(artifact.getContent());
                return new String(decodedString, StandardCharsets.UTF_8);
            }
        } catch (GHClientException e) {
            if (e.getStatusCode() != 404) {
                throw new ArtifactSynchronizerException("Error occured while retrieving all artifacts", e);
            }
        }
        return null;
    }

    @Override
    public String retrieveArtifact(String apiId, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("------------ GHRetriever retrieveArtifact() -------------");
        }

        if (!isGHRetrieverInitialized()) {
            throw new ArtifactSynchronizerException("GitHub Retriever is not initialized");
        }

        String path = GHSynchronizerUtil.constructPath(gatewayLabel, apiId);
        List<GHCommit> commits = githubClient.getCommitsofArtifactContent(this.githubOwner, this.githubRepo, path);
        if (!commits.isEmpty()) {
            String commitMessage = commits.get(0).getCommit().getMessage();
            String[] subStrings = commitMessage.split("::");
            String status = null;
            if (subStrings.length > 1 && subStrings[1] != null) {
                status = subStrings[1].split(";")[0];
            }
            if (status != null && (gatewayInstruction.equals(status)
                    || gatewayInstruction.equals(APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_ANY))) {
                return retrieveArtifactContent(path);
            }
        }
        return null;
    }

    @Override
    public Map<String, String> retrieveAttributes(String apiName, String version, String tenantDomain)
            throws ArtifactSynchronizerException {
        // TODO Auto-generated method stub
        if (log.isDebugEnabled()) {
            log.debug("------------ GHRetriever retrieveAttributes() -------------");
        }
        return null;
    }

    /**
     * method to verify whether the GHSaver has been initialized
     * 
     * @return boolean
     */
    private boolean isGHRetrieverInitialized() {
        return StringUtils.isNotBlank(githubOwner) && StringUtils.isNotBlank(githubRepo) && githubClient != null;
    }
}
