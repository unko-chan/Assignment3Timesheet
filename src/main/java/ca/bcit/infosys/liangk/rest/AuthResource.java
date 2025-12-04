package ca.bcit.infosys.liangk.rest;

import ca.bcit.infosys.liangk.dto.LoginRequest;
import ca.bcit.infosys.liangk.dto.LoginResponse;
import ca.bcit.infosys.liangk.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Authentication endpoints.
 */
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    /**
     * Test-friendly variant for unit tests to bypass Response building.
     */
    LoginResponse loginRaw(LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Authenticates credentials and returns a bearer token with basic user info.
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        LoginResponse resp = loginRaw(request);
        return Response.ok(resp).build();
    }
}
