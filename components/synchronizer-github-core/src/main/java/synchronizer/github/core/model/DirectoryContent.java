package synchronizer.github.core.model;

import com.google.gson.annotations.SerializedName;

public class DirectoryContent {
    
    @SerializedName("name")
    private String name;
    @SerializedName("path")
    private String path;
    @SerializedName("sha")
    private String sha;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }
}
