package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception indicating the caller is authenticated but not authorized to perform the action.
 */
public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(Response.Status.FORBIDDEN.getStatusCode(), "FORBIDDEN", message);
    }
}
