package synchronizer.github.core;

public class GHSynchronizerUtil {
    /**
     * method to construct the content path
     * 
     * @param env   Gateway environment name
     * @param apiId API ID
     * @return Constructed Path
     */
    public static String constructPath(String env, String apiId) {
        return env.replaceAll("\\s", "_") + "/" + apiId + ".json";
    }
}
