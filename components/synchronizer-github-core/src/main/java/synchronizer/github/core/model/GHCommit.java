package synchronizer.github.core.model;

import com.google.gson.annotations.SerializedName;

public class GHCommit {

    @SerializedName("sha")
    private String sha;
    @SerializedName("commit")
    private Commit commit;

    public class Commit {

        @SerializedName("message")
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }
}
