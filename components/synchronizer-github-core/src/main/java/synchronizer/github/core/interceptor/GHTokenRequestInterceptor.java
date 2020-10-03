package synchronizer.github.core.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class GHTokenRequestInterceptor implements RequestInterceptor {

    private final String githubAccessToken;

    public GHTokenRequestInterceptor(String githubAccessToken) {
        this.githubAccessToken = githubAccessToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "token " + this.githubAccessToken);
    }
}
