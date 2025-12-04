package ca.bcit.infosys.liangk.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {
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
