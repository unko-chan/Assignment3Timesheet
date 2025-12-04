package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception indicating that a requested resource could not be found.
 */
public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(Response.Status.NOT_FOUND.getStatusCode(), "NOT_FOUND", message);
    }
}
