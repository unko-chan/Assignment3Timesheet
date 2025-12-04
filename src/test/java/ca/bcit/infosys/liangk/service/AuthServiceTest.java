package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.AuthTokenDAO;
import ca.bcit.infosys.liangk.dao.UserDAO;
import ca.bcit.infosys.liangk.dto.LoginRequest;
import ca.bcit.infosys.liangk.dto.LoginResponse;
import ca.bcit.infosys.liangk.entity.AuthToken;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.exception.UnauthorizedException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService service;
    private FakeUserDAO userDAO;
    private FakeAuthTokenDAO tokenDAO;

    @BeforeEach
    void setup() throws Exception {
        service = new AuthService();
        userDAO = new FakeUserDAO();
        tokenDAO = new FakeAuthTokenDAO();
        setField(service, "userDAO", userDAO);
        setField(service, "tokenDAO", tokenDAO);
    }

    @Test
    void login_successCreatesActiveToken() {
        User user = userWithPassword("jdoe", "secret");
        userDAO.user = user;

        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("secret");

        LoginResponse resp = service.login(req);
        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertEquals("jdoe", resp.getUser().getUsername());

        AuthToken stored = tokenDAO.lastCreated;
        assertNotNull(stored);
        assertTrue(stored.isActive());
        assertNotNull(stored.getExpiresAt());
        assertTrue(stored.getExpiresAt().isAfter(stored.getIssuedAt()));
    }

    @Test
    void login_rejectsInvalidPassword() {
        User user = userWithPassword("jdoe", "secret");
        userDAO.user = user;

        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("wrong");

        assertThrows(UnauthorizedException.class, () -> service.login(req));
    }

    @Test
    void validateToken_checksExpiryAndActive() {
        User user = userWithPassword("jdoe", "secret");
        userDAO.user = user;

        // Create an expired token in DAO
        AuthToken expired = new AuthToken();
        expired.setToken("tok1");
        expired.setUser(user);
        expired.setIssuedAt(LocalDateTime.now().minusHours(10));
        expired.setExpiresAt(LocalDateTime.now().minusHours(1));
        expired.setActive(true);
        tokenDAO.token = expired;

        assertThrows(UnauthorizedException.class, () -> service.validateToken("tok1"));

        // Now active and not expired
        AuthToken ok = new AuthToken();
        ok.setToken("tok2");
        ok.setUser(user);
        ok.setIssuedAt(LocalDateTime.now().minusMinutes(1));
        ok.setExpiresAt(LocalDateTime.now().plusHours(1));
        ok.setActive(true);
        tokenDAO.token = ok;

        User validated = service.validateToken("tok2");
        assertEquals(user, validated);

        // Inactive token rejected
        ok.setActive(false);
        tokenDAO.token = ok;
        assertThrows(UnauthorizedException.class, () -> service.validateToken("tok2"));
    }

    @Test
    void logout_deactivatesTokenIfActive() {
        User user = new User();
        AuthToken t = new AuthToken();
        t.setToken("tok");
        t.setUser(user);
        t.setIssuedAt(LocalDateTime.now());
        t.setExpiresAt(LocalDateTime.now().plusHours(1));
        t.setActive(true);
        tokenDAO.token = t;

        service.logout("tok");
        assertFalse(tokenDAO.token.isActive());
    }

    // Helpers
    private static User userWithPassword(String username, String plain) {
        User u = new User();
        u.setId(1L);
        u.setUsername(username);
        u.setActive(true);
        u.setPassword(plain);
        return u;
    }

    private static void setField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // Fakes
    private static class FakeUserDAO extends UserDAO {
        User user;
        @Override public User findByUsername(String username) { return Objects.equals(user==null?null:user.getUsername(), username) ? user : null; }
    }

    private static class FakeAuthTokenDAO extends AuthTokenDAO {
        AuthToken token;
        AuthToken lastCreated;
        @Override public AuthToken findByToken(String t) { return token != null && java.util.Objects.equals(token.getToken(), t) ? token : null; }
        @Override public AuthToken create(AuthToken a) { this.lastCreated = a; this.token = a; return a; }
        @Override public AuthToken update(AuthToken a) { this.token = a; return a; }
    }
}
