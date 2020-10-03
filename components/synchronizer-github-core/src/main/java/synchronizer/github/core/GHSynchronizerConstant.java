package synchronizer.github.core;

public final class GHSynchronizerConstant {

    private GHSynchronizerConstant() {
    }

    public static final String GH_ENDPOINT = "https://api.github.com";
    public static final String GH_ARTIFACT_SYNCHRONIZER = "GHSynchronizer";
    public static final String GH_SAVER = GH_ARTIFACT_SYNCHRONIZER + "Saver";
    public static final String GH_RETRIEVER = GH_ARTIFACT_SYNCHRONIZER + "Retriever";

    public static final String GH_ACCESS_TOKEN = "GitHubAccessToken";
    public static final String GH_OWNER = "GitHubOwner";
    public static final String GH_REPO = "GitHubRepo";
}
