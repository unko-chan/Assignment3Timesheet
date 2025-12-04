package ca.bcit.infosys.liangk.exception;

public class AppException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public AppException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
