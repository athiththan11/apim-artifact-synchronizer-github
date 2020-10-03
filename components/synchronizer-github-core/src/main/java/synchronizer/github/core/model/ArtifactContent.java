package synchronizer.github.core.model;

import com.google.gson.annotations.SerializedName;

/**
 * GitHub artifact content model
 */
public class ArtifactContent {
    
    @SerializedName("name")
    private String name;
    @SerializedName("path")
    private String path;
    @SerializedName("sha")
    private String sha;
    @SerializedName("type")
    private String type;
    @SerializedName("content")
    private String content;
    @SerializedName("encoding")
    private String encoding;
    @SerializedName("message")
    private String message;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
