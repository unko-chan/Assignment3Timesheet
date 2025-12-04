package ca.bcit.infosys.liangk.exception;

/**
 * Base unchecked exception for application-specific errors that should be rendered
 * as structured JSON responses by the JAX-RS exception mappers.
 * Carries an HTTP status code and a machine-friendly error code.
 */
public class AppException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    /**
     * Constructs a new AppException.
     *
     * @param statusCode HTTP status code to return
     * @param errorCode  machine-friendly error code identifier
     * @param message    human-readable error message
     */
    public AppException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /**
     * Returns the HTTP status code associated with this error.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the machine-friendly error code.
     */
    public String getErrorCode() {
        return errorCode;
    }
}
