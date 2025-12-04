package ca.bcit.infosys.liangk.security;

import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.exception.UnauthorizedException;
import ca.bcit.infosys.liangk.service.AuthService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthFilterTest {

    private AuthFilter filter;
    private FakeAuthService authService;
    private CurrentUserHolder holder;

    @BeforeEach
    void setup() throws Exception {
        filter = new AuthFilter();
        authService = new FakeAuthService();
        holder = new CurrentUserHolder();
        setField(filter, "authService", authService);
        setField(filter, "currentUserHolder", holder);
    }

    @Test
    void optionsBypass_noAuthRequired() throws IOException {
        FakeRequestContext ctx = new FakeRequestContext("OPTIONS", "/timesheets", null);
        filter.filter(ctx); // should not throw
        assertNull(holder.getUser());
    }

    @Test
    void loginPathBypass_noAuthRequired() throws IOException {
        FakeRequestContext ctx = new FakeRequestContext("POST", "/auth/login", null);
        filter.filter(ctx); // should not throw
        assertNull(holder.getUser());
    }

    @Test
    void missingAuthorizationHeader_throws401() {
        FakeRequestContext ctx = new FakeRequestContext("GET", "/timesheets", null);
        assertThrows(UnauthorizedException.class, () -> filter.filter(ctx));
    }

    @Test
    void malformedScheme_throws401() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "BearerX token");
        FakeRequestContext ctx = new FakeRequestContext("GET", "/timesheets", headers);
        assertThrows(UnauthorizedException.class, () -> filter.filter(ctx));
    }

    @Test
    void emptyToken_throws401() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer   ");
        FakeRequestContext ctx = new FakeRequestContext("GET", "/timesheets", headers);
        assertThrows(UnauthorizedException.class, () -> filter.filter(ctx));
    }

    @Test
    void validToken_setsCurrentUserAndRequestProperty() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer abc123");
        FakeRequestContext ctx = new FakeRequestContext("GET", "/timesheets", headers);

        User u = new User();
        u.setId(10L);
        u.setUsername("jdoe");
        u.setRole(UserRole.USER);
        authService.userToReturn = u;

        filter.filter(ctx);

        assertEquals(u, holder.getUser());
        assertEquals(u, ctx.getProperty(AuthFilter.REQ_PROP_CURRENT_USER));
        assertEquals("abc123", authService.lastValidatedToken);
    }

    // ===== Helpers =====
    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeAuthService extends AuthService {
        User userToReturn;
        String lastValidatedToken;
        @Override
        public User validateToken(String tokenString) {
            this.lastValidatedToken = tokenString;
            if (userToReturn == null) throw new UnauthorizedException("Invalid token");
            return userToReturn;
        }
    }

    private static class FakeRequestContext implements ContainerRequestContext {
        private final String method;
        private final String path;
        private final Map<String, String> headers;
        private final Map<String, Object> properties = new HashMap<>();

        FakeRequestContext(String method, String path, Map<String, String> headers) {
            this.method = method;
            this.path = path.startsWith("/") ? path.substring(1) : path;
            this.headers = headers == null ? Collections.emptyMap() : headers;
        }

        @Override public Object getProperty(String name) { return properties.get(name); }
        @Override public Collection<String> getPropertyNames() { return properties.keySet(); }
        @Override public void setProperty(String name, Object object) { properties.put(name, object); }
        @Override public void removeProperty(String name) { properties.remove(name); }
        @Override public String getMethod() { return method; }
        @Override public void setMethod(String method) { throw new UnsupportedOperationException(); }
        @Override public MultivaluedMap<String, String> getHeaders() {
            MultivaluedMap<String, String> m = new MultivaluedHashMap<>();
            headers.forEach((k,v) -> m.add(k, v));
            return m;
        }
        @Override public String getHeaderString(String name) { return headers.get(name); }
        @Override public Date getDate() { return null; }
        @Override public Locale getLanguage() { return null; }
        @Override public int getLength() { return -1; }
        @Override public MediaType getMediaType() { return null; }
        @Override public List<MediaType> getAcceptableMediaTypes() { return List.of(); }
        @Override public List<Locale> getAcceptableLanguages() { return List.of(); }
        @Override public Map<String, Cookie> getCookies() { return Map.of(); }
        @Override public boolean hasEntity() { return false; }
        @Override public InputStream getEntityStream() { return InputStream.nullInputStream(); }
        @Override public void setEntityStream(InputStream input) { }
        @Override public SecurityContext getSecurityContext() { return null; }
        @Override public void setSecurityContext(SecurityContext context) { }
        @Override public void abortWith(jakarta.ws.rs.core.Response response) { }
        @Override public UriInfo getUriInfo() {
            return new UriInfo() {
                @Override public String getPath() { return path; }
                @Override public String getPath(boolean decode) { return path; }
                @Override public List<PathSegment> getPathSegments() { return List.of(); }
                @Override public List<PathSegment> getPathSegments(boolean decode) { return List.of(); }
                @Override public URI getRequestUri() { return URI.create("http://localhost/" + path); }
                @Override public UriBuilder getRequestUriBuilder() { return UriBuilder.fromUri(getRequestUri()); }
                @Override public URI getAbsolutePath() { return URI.create("http://localhost/" + path); }
                @Override public UriBuilder getAbsolutePathBuilder() { return UriBuilder.fromUri(getAbsolutePath()); }
                @Override public URI getBaseUri() { return URI.create("http://localhost/"); }
                @Override public UriBuilder getBaseUriBuilder() { return UriBuilder.fromUri(getBaseUri()); }
                @Override public MultivaluedMap<String, String> getPathParameters() { return new MultivaluedHashMap<>(); }
                @Override public MultivaluedMap<String, String> getPathParameters(boolean decode) { return new MultivaluedHashMap<>(); }
                @Override public MultivaluedMap<String, String> getQueryParameters() { return new MultivaluedHashMap<>(); }
                @Override public MultivaluedMap<String, String> getQueryParameters(boolean decode) { return new MultivaluedHashMap<>(); }
                @Override public List<String> getMatchedURIs() { return List.of(); }
                @Override public List<String> getMatchedURIs(boolean decode) { return List.of(); }
                @Override public List<Object> getMatchedResources() { return List.of(); }
                @Override public URI resolve(URI uri) { return uri; }
                @Override public URI relativize(URI uri) { return uri; }
            };
        }
        @Override public void setRequestUri(URI requestUri) { throw new UnsupportedOperationException(); }
        @Override public void setRequestUri(URI baseUri, URI requestUri) { throw new UnsupportedOperationException(); }
        @Override public Request getRequest() { return null; }
    }
}
