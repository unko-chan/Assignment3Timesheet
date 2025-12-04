package ca.bcit.infosys.liangk.security;

import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.exception.UnauthorizedException;
import ca.bcit.infosys.liangk.service.AuthService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    public static final String REQ_PROP_CURRENT_USER = "currentUser";

    @Inject
    AuthService authService;

    @Inject
    CurrentUserHolder currentUserHolder;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Allow preflight requests without auth
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getPath(); // relative to ApplicationPath (e.g., "auth/login")
        if (path != null) {
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            if (normalized.equals("auth/login") || normalized.startsWith("auth/login")) {
                return; // Skip auth for login endpoint
            }
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header");
        }
        String prefix = "Bearer ";
        if (!authHeader.startsWith(prefix)) {
            throw new UnauthorizedException("Authorization header must be in the format 'Bearer <token>'");
        }
        String token = authHeader.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Token is empty");
        }

        User user = authService.validateToken(token);

        // Store in request-scoped holder
        currentUserHolder.setUser(user);
        // Also make available via request properties for non-CDI lookups
        requestContext.setProperty(REQ_PROP_CURRENT_USER, user);
    }
}
