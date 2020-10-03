package synchronizer.github.core.exception;

public class GHClientException extends Exception {

    private final int statusCode;
    private static final long serialVersionUID = 1L;

    public GHClientException(int statusCode, String reason) {
        super("Recieved :: status-code: " + statusCode + ", reason: " + reason);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
