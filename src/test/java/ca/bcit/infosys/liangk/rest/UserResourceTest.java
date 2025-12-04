package ca.bcit.infosys.liangk.rest;

import ca.bcit.infosys.liangk.dto.CreateUserRequest;
import ca.bcit.infosys.liangk.dto.UpdateUserRequest;
import ca.bcit.infosys.liangk.dto.UserDTO;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.exception.ForbiddenException;
import ca.bcit.infosys.liangk.service.UserService;
import ca.bcit.infosys.liangk.security.CurrentUserHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class UserResourceTest {

    private UserResource resource;
    private FakeUserService userService;
    private CurrentUserHolder holder;

    @BeforeEach
    void setup() throws Exception {
        resource = new UserResource();
        userService = new FakeUserService();
        holder = new CurrentUserHolder();
        setField(resource, "userService", userService);
        setField(resource, "currentUserHolder", holder);
    }

    @Test
    void listUsers_userRole_forbidden() {
        holder.setUser(buildUser(1L, UserRole.USER));
        assertThrows(ForbiddenException.class, () -> resource.listUsers());
    }

    @Test
    void getUser_userRole_forbidden() {
        holder.setUser(buildUser(1L, UserRole.USER));
        assertThrows(ForbiddenException.class, () -> resource.getUser(1L));
    }

    @Test
    void createUser_userRole_forbidden() {
        holder.setUser(buildUser(1L, UserRole.USER));
        assertThrows(ForbiddenException.class, () -> resource.createUser(new CreateUserRequest(), new FakeUriInfo()));
    }

    @Test
    void updateUser_userRole_forbidden() {
        holder.setUser(buildUser(1L, UserRole.USER));
        assertThrows(ForbiddenException.class, () -> resource.updateUser(1L, new UpdateUserRequest()));
    }

    @Test
    void deleteUser_userRole_forbidden() {
        holder.setUser(buildUser(1L, UserRole.USER));
        assertThrows(ForbiddenException.class, () -> resource.deleteUser(1L));
    }

    @Test
    void admin_canListAndGetAndCreateUpdateDelete() {
        holder.setUser(buildUser(99L, UserRole.ADMIN));

        // Setup fake service data
        User u1 = new User(); u1.setId(1L); u1.setUsername("a"); u1.setFirstName("A"); u1.setLastName("A"); u1.setEmployeeNumber(1); u1.setRole(UserRole.USER); u1.setActive(true);
        User u2 = new User(); u2.setId(2L); u2.setUsername("b"); u2.setFirstName("B"); u2.setLastName("B"); u2.setEmployeeNumber(2); u2.setRole(UserRole.ADMIN); u2.setActive(true);
        userService.users = List.of(u1, u2);
        userService.userById = u1;

        // list
        var list = resource.listUsers();
        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(dto -> dto instanceof UserDTO));

        // get
        UserDTO dto = resource.getUser(1L);
        assertEquals(1L, dto.getId());
        assertEquals("a", dto.getUsername());

        // create
        CreateUserRequest creq = new CreateUserRequest();
        creq.setUsername("new"); creq.setFirstName("N"); creq.setLastName("N"); creq.setEmployeeNumber(3); creq.setPassword("p");
        UserDTO created = resource.createUserRaw(creq);
        assertNotNull(created);
        assertEquals("new", created.getUsername());

        // update
        UpdateUserRequest ureq = new UpdateUserRequest();
        ureq.setFirstName("NewName");
        UserDTO updated = resource.updateUser(1L, ureq);
        assertEquals("NewName", updated.getFirstName());

        // delete no-content
        resource.deleteUserRaw(1L);
    }

    // ===== Helpers & fakes =====
    private static User buildUser(Long id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
        
    }

    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeUserService extends UserService {
        List<User> users = List.of();
        User userById;
        AtomicLong seq = new AtomicLong(100);
        @Override public List<User> listAll() { return users; }
        @Override public User getById(long id) { return userById; }
        @Override public User createUser(CreateUserRequest req) {
            User u = new User();
            u.setId(seq.getAndIncrement());
            u.setUsername(req.getUsername());
            u.setFirstName(req.getFirstName());
            u.setLastName(req.getLastName());
            u.setEmployeeNumber(req.getEmployeeNumber());
            u.setRole(UserRole.USER);
            u.setActive(true);
            return u;
        }
        @Override public User updateUser(long id, UpdateUserRequest req) {
            User u = userById;
            if (req.getFirstName() != null) u.setFirstName(req.getFirstName());
            return u;
        }
        @Override public void deleteUser(long id) { /* no-op */ }
    }

    // Minimal UriInfo impl to build Location
    private static class FakeUriInfo implements jakarta.ws.rs.core.UriInfo {
        @Override public String getPath() { return "/users"; }
        @Override public String getPath(boolean decode) { return getPath(); }
        @Override public java.util.List<jakarta.ws.rs.core.PathSegment> getPathSegments() { return java.util.List.of(); }
        @Override public java.util.List<jakarta.ws.rs.core.PathSegment> getPathSegments(boolean decode) { return java.util.List.of(); }
        @Override public java.net.URI getRequestUri() { return URI.create("http://localhost/users"); }
        @Override public jakarta.ws.rs.core.UriBuilder getRequestUriBuilder() { return jakarta.ws.rs.core.UriBuilder.fromUri(getRequestUri()); }
        @Override public java.net.URI getAbsolutePath() { return getRequestUri(); }
        @Override public jakarta.ws.rs.core.UriBuilder getAbsolutePathBuilder() { return jakarta.ws.rs.core.UriBuilder.fromUri(getAbsolutePath()); }
        @Override public java.net.URI getBaseUri() { return URI.create("http://localhost"); }
        @Override public jakarta.ws.rs.core.UriBuilder getBaseUriBuilder() { return jakarta.ws.rs.core.UriBuilder.fromUri(getBaseUri()); }
        @Override public jakarta.ws.rs.core.MultivaluedMap<String, String> getPathParameters() { return new jakarta.ws.rs.core.MultivaluedHashMap<>(); }
        @Override public jakarta.ws.rs.core.MultivaluedMap<String, String> getPathParameters(boolean decode) { return new jakarta.ws.rs.core.MultivaluedHashMap<>(); }
        @Override public jakarta.ws.rs.core.MultivaluedMap<String, String> getQueryParameters() { return new jakarta.ws.rs.core.MultivaluedHashMap<>(); }
        @Override public jakarta.ws.rs.core.MultivaluedMap<String, String> getQueryParameters(boolean decode) { return new jakarta.ws.rs.core.MultivaluedHashMap<>(); }
        @Override public java.util.List<String> getMatchedURIs() { return java.util.List.of(); }
        @Override public java.util.List<String> getMatchedURIs(boolean decode) { return java.util.List.of(); }
        @Override public java.util.List<Object> getMatchedResources() { return java.util.List.of(); }
        @Override public java.net.URI resolve(java.net.URI uri) { return uri; }
        @Override public java.net.URI relativize(java.net.URI uri) { return uri; }
    }
}
