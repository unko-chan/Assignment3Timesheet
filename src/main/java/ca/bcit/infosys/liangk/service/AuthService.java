package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.AuthTokenDAO;
import ca.bcit.infosys.liangk.dao.UserDAO;
import ca.bcit.infosys.liangk.dto.LoginRequest;
import ca.bcit.infosys.liangk.dto.LoginResponse;
import ca.bcit.infosys.liangk.dto.UserDTO;
import ca.bcit.infosys.liangk.entity.AuthToken;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.exception.UnauthorizedException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import ca.bcit.infosys.liangk.util.Mapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Stateless
public class AuthService {

    private static final long TOKEN_TTL_HOURS = 8L;

    @Inject
    private UserDAO userDAO;

    @Inject
    private AuthTokenDAO tokenDAO;

    public LoginResponse login(LoginRequest req) {
        if (req == null) throw new ValidationException("Request body is required");
        if (isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw new ValidationException("Username and password must be provided");
        }
        User u = userDAO.findByUsername(req.getUsername());
        if (u == null) throw new UnauthorizedException("Invalid username or password");
        if (!u.isActive()) throw new UnauthorizedException("User account is inactive");

        String providedHash = hashPassword(req.getPassword());
        if (!providedHash.equals(u.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        AuthToken token = new AuthToken();
        token.setToken(generateToken());
        token.setUser(u);
        LocalDateTime now = LocalDateTime.now();
        token.setIssuedAt(now);
        token.setExpiresAt(now.plusHours(TOKEN_TTL_HOURS));
        token.setActive(true);
        tokenDAO.create(token);

        UserDTO userDTO = Mapper.toUserDTO(u);
        return new LoginResponse(token.getToken(), userDTO);
    }

    public User validateToken(String tokenString) {
        if (isBlank(tokenString)) {
            throw new UnauthorizedException("Missing token");
        }
        AuthToken token = tokenDAO.findByToken(tokenString);
        if (token == null) throw new UnauthorizedException("Invalid token");
        if (!token.isActive()) throw new UnauthorizedException("Token inactive");
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Token expired");
        }
        User user = token.getUser();
        if (user == null || !user.isActive()) {
            throw new UnauthorizedException("User inactive");
        }
        return user;
    }

    public void logout(String tokenString) {
        if (isBlank(tokenString)) return;
        AuthToken token = tokenDAO.findByToken(tokenString);
        if (token != null && token.isActive()) {
            token.setActive(false);
            tokenDAO.update(token);
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String generateToken() {
        byte[] bytes = new byte[32]; // 256-bit -> 64 hex chars
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
