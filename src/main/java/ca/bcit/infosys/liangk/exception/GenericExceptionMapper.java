package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        // Optionally log the exception here using your logging framework
        ErrorResponse payload = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(payload)
                .build();
    }
}
