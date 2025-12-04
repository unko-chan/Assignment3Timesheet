package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.UserDAO;
import ca.bcit.infosys.liangk.dto.CreateUserRequest;
import ca.bcit.infosys.liangk.dto.UpdateUserRequest;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.exception.NotFoundException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import ca.bcit.infosys.liangk.util.Mapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Stateless
public class UserService {

    @Inject
    private UserDAO userDAO;

    public User createUser(CreateUserRequest req) {
        if (req == null) throw new ValidationException("Request body is required");
        // Validate username
        if (isBlank(req.getUsername())) {
            throw new ValidationException("Username must not be blank");
        }
        if (userDAO.findByUsername(req.getUsername()) != null) {
            throw new ValidationException("Username already exists");
        }
        // Validate employee number
        if (req.getEmployeeNumber() == null) {
            throw new ValidationException("Employee number is required");
        }
        if (userDAO.findByEmployeeNumber(req.getEmployeeNumber()) != null) {
            throw new ValidationException("Employee number already exists");
        }
        // Validate password
        if (isBlank(req.getPassword())) {
            throw new ValidationException("Password must not be blank");
        }
        User u = Mapper.buildUserFromCreate(req);
        u.setPasswordHash(hashPassword(req.getPassword()));
        return userDAO.create(u);
    }

    public User getById(long id) {
        User u = userDAO.findById(id);
        if (u == null) throw new NotFoundException("User not found: id=" + id);
        return u;
    }

    public List<User> listAll() {
        return userDAO.findAll();
    }

    public User updateUser(long id, UpdateUserRequest req) {
        if (req == null) throw new ValidationException("Request body is required");
        User existing = getById(id);

        // If username changes, ensure unique
        if (req.getUsername() != null && !req.getUsername().equals(existing.getUsername())) {
            if (isBlank(req.getUsername())) {
                throw new ValidationException("Username must not be blank");
            }
            User byName = userDAO.findByUsername(req.getUsername());
            if (byName != null && !byName.getId().equals(existing.getId())) {
                throw new ValidationException("Username already exists");
            }
        }
        // If employee number changes, ensure unique
        if (req.getEmployeeNumber() != null && !req.getEmployeeNumber().equals(existing.getEmployeeNumber())) {
            User byEmp = userDAO.findByEmployeeNumber(req.getEmployeeNumber());
            if (byEmp != null && !byEmp.getId().equals(existing.getId())) {
                throw new ValidationException("Employee number already exists");
            }
        }

        // Apply other fields
        Mapper.applyUpdateToUser(existing, req);

        // Password hashing if provided
        if (req.getPassword() != null) {
            if (isBlank(req.getPassword())) {
                throw new ValidationException("Password must not be blank");
            }
            existing.setPasswordHash(hashPassword(req.getPassword()));
        }

        return userDAO.update(existing);
    }

    public void deleteUser(long id) {
        // ensure exists
        getById(id);
        userDAO.delete(id);
    }

    // ===== Helpers =====
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
