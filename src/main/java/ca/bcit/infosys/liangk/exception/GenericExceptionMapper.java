package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Catch-all exception mapper that converts any uncaught Throwable into a JSON response.
 *
 * Behavior:
 * - Unwraps EJBException (common on WildFly) to expose the actual cause.
 * - If the cause is an AppException, returns a structured payload with its status.
 * - If it's a WebApplicationException, returns its provided response.
 * - Otherwise, logs the error and returns HTTP 500 with a generic message.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    /**
     * Maps the provided Throwable to an HTTP response.
     *
     * @param exception the exception thrown by resource processing
     * @return JAX-RS Response suitable for the client
     */
    @Override
    public Response toResponse(Throwable exception) {
        // 1. Unwrap EJBException if present (Common in WildFly/JBoss)
        Throwable cause = exception;
        if (exception instanceof EJBException && exception.getCause() != null) {
            cause = exception.getCause();
        }

        // 2. If the unwrapped cause is one of our AppExceptions, delegate to logic consistent with AppExceptionMapper
        if (cause instanceof AppException) {
            AppException appEx = (AppException) cause;
            ErrorResponse payload = new ErrorResponse(
                    appEx.getErrorCode(),
                    appEx.getMessage(),
                    appEx.getStatusCode()
            );
            return Response.status(appEx.getStatusCode())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(payload)
                    .build();
        }

        // 3. Handle standard JAX-RS exceptions
        if (cause instanceof jakarta.ws.rs.WebApplicationException) {
            return ((jakarta.ws.rs.WebApplicationException) cause).getResponse();
        }

        // 4. Fallback for truly unexpected errors
        LOGGER.log(Level.SEVERE, "Uncaught exception: " + cause.getMessage(), cause);
        
        ErrorResponse payload = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred: " + cause.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(payload)
                .build();
    }
}
