package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception indicating that authentication is required or has failed.
 */
public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(Response.Status.UNAUTHORIZED.getStatusCode(), "UNAUTHORIZED", message);
    }
}
