package synchronizer.github.core.client;

import java.util.List;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import synchronizer.github.core.exception.GHClientException;
import synchronizer.github.core.model.ArtifactContent;
import synchronizer.github.core.model.DirectoryContent;
import synchronizer.github.core.model.GHCommit;

@Headers("Accept: application/vnd.github.v3+json")
public interface GHClient {

    @RequestLine("GET /repos/{owner}/{repo}/contents/{path}")
    public ArtifactContent getArtifactContent(@Param("owner") String owner, @Param("repo") String repo,
            @Param("path") String path) throws GHClientException;

    @RequestLine("GET /repos/{owner}/{repo}/contents/{path}")
    public List<DirectoryContent> getDirectoryContent(@Param("owner") String owner, @Param("repo") String repo,
            @Param("path") String path) throws GHClientException;

    @RequestLine("PUT /repos/{owner}/{repo}/contents/{path}")
    public Response createArtifactContent(@Param("owner") String owner, @Param("repo") String repo,
            @Param("path") String path, ArtifactContent content);

    @RequestLine("GET /repos/{owner}/{repo}/commits?path={path}")
    public List<GHCommit> getCommitsofArtifactContent(@Param("owner") String owner, @Param("repo") String repo,
            @Param("path") String path);
}
