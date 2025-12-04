package ca.bcit.infosys.liangk.rest;

import ca.bcit.infosys.liangk.dto.LoginRequest;
import ca.bcit.infosys.liangk.dto.LoginResponse;
import ca.bcit.infosys.liangk.dto.UserDTO;
import ca.bcit.infosys.liangk.exception.UnauthorizedException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import ca.bcit.infosys.liangk.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

public class AuthResourceTest {

    private AuthResource resource;
    private FakeAuthService authService;

    @BeforeEach
    void setup() throws Exception {
        resource = new AuthResource();
        authService = new FakeAuthService();
        setField(resource, "authService", authService);
    }

    @Test
    void login_validCredentials_returns200WithTokenAndUser() {
        // Arrange
        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("secret");
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmployeeNumber(42);
        user.setRole(ca.bcit.infosys.liangk.entity.UserRole.USER);
        user.setActive(true);
        authService.nextResponse = new LoginResponse("tok123", user);

        // Act
        LoginResponse lr = resource.loginRaw(req);

        // Assert
        assertEquals("tok123", lr.getToken());
        assertEquals("jdoe", lr.getUser().getUsername());
    }

    @Test
    void login_invalidPassword_throwsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("wrong");
        authService.throwUnauthorized = true;
        assertThrows(UnauthorizedException.class, () -> resource.login(req));
    }

    @Test
    void login_missingFields_throwsValidation() {
        LoginRequest req = new LoginRequest();
        // missing username and password
        authService.throwValidation = true;
        assertThrows(ValidationException.class, () -> resource.login(req));
    }

    // ===== Helpers & fakes =====
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
        LoginResponse nextResponse;
        boolean throwUnauthorized;
        boolean throwValidation;
        @Override
        public LoginResponse login(LoginRequest req) {
            if (throwValidation) throw new ValidationException("bad request");
            if (throwUnauthorized) throw new UnauthorizedException("unauth");
            return nextResponse;
        }
    }
}
