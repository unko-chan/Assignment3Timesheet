package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.AuthTokenDAO;
import ca.bcit.infosys.liangk.dao.UserDAO;
import ca.bcit.infosys.liangk.dto.LoginRequest;
import ca.bcit.infosys.liangk.entity.AuthToken;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.exception.UnauthorizedException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceValidationTest {

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
    void login_nullRequest_400() {
        assertThrows(ValidationException.class, () -> service.login(null));
    }

    @Test
    void login_missingUsernameOrPassword_400() {
        LoginRequest req1 = new LoginRequest();
        req1.setUsername(null); req1.setPassword("secret");
        assertThrows(ValidationException.class, () -> service.login(req1));

        LoginRequest req2 = new LoginRequest();
        req2.setUsername(" "); req2.setPassword("secret");
        assertThrows(ValidationException.class, () -> service.login(req2));

        LoginRequest req3 = new LoginRequest();
        req3.setUsername("jdoe"); req3.setPassword(null);
        assertThrows(ValidationException.class, () -> service.login(req3));

        LoginRequest req4 = new LoginRequest();
        req4.setUsername("jdoe"); req4.setPassword(" ");
        assertThrows(ValidationException.class, () -> service.login(req4));
    }

    @Test
    void login_unknownUser_401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("nouser");
        req.setPassword("anything");
        userDAO.user = null; // not found
        assertThrows(UnauthorizedException.class, () -> service.login(req));
    }

    @Test
    void login_inactiveUser_401() {
        User u = new User();
        u.setId(1L);
        u.setUsername("jdoe");
        u.setActive(false);
        u.setPassword("secret");
        userDAO.user = u;

        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("secret");

        assertThrows(UnauthorizedException.class, () -> service.login(req));
    }

    // ===== Helpers and fakes reused from other tests =====
    private static void setField(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static class FakeUserDAO extends UserDAO {
        User user;
        @Override public User findByUsername(String username) { return Objects.equals(user==null?null:user.getUsername(), username) ? user : null; }
    }

    private static class FakeAuthTokenDAO extends AuthTokenDAO {
        AuthToken token;
        @Override public AuthToken findByToken(String t) { return token != null && Objects.equals(token.getToken(), t) ? token : null; }
        @Override public AuthToken create(AuthToken a) { this.token = a; return a; }
        @Override public AuthToken update(AuthToken a) { this.token = a; return a; }
    }
}
