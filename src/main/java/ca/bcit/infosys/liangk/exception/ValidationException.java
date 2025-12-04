package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception indicating that the client has provided invalid input parameters or payload.
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), "VALIDATION_ERROR", message);
    }
}
