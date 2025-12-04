package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps application-specific AppException instances to JSON HTTP responses.
 * Produces a consistent ErrorResponse payload for clients.
 */
@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    /**
     * Converts an AppException into a JAX-RS Response with JSON body.
     *
     * @param exception the application exception to convert
     * @return response with appropriate HTTP status and payload
     */
    @Override
    public Response toResponse(AppException exception) {
        ErrorResponse payload = new ErrorResponse(
                exception.getErrorCode(),
                exception.getMessage(),
                exception.getStatusCode()
        );
        return Response.status(exception.getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(payload)
                .build();
    }
}
