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

import java.util.List;

/**
 * Business logic for managing users. Validates uniqueness constraints and basic fields.
 * Note: Passwords are stored and compared in plaintext per assignment constraints.
 */
@Stateless
public class UserService {

    @Inject
    private UserDAO userDAO;

    /**
     * Creates a new user after validating input and uniqueness constraints.
     *
     * @param req create request
     * @return created User
     * @throws ValidationException if fields invalid or unique constraints violated
     */
    public User createUser(CreateUserRequest req) {
        if (req == null) throw new ValidationException("Request body is required");
        // Validate username
        if (isBlank(req.getUsername())) {
            throw new ValidationException("Username must not be blank");
        }
        if (userDAO.findByUsername(req.getUsername()) != null) {
            throw new ValidationException("Username already exists");
        }
        // Validate first/last name
        if (isBlank(req.getFirstName())) {
            throw new ValidationException("First name must not be blank");
        }
        if (isBlank(req.getLastName())) {
            throw new ValidationException("Last name must not be blank");
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
        // Store plaintext password directly
        u.setPassword(req.getPassword());
        return userDAO.create(u);
    }

    /**
     * Retrieves a user by id.
     *
     * @param id user id
     * @return User
     * @throws NotFoundException if user does not exist
     */
    public User getById(long id) {
        User u = userDAO.findById(id);
        if (u == null) throw new NotFoundException("User not found: id=" + id);
        return u;
    }

    /**
     * Lists all users.
     *
     * @return list of users
     */
    public List<User> listAll() {
        return userDAO.findAll();
    }

    /**
     * Updates a user with provided fields; validates uniqueness where applicable.
     *
     * @param id  user id
     * @param req update request
     * @return updated User
     * @throws ValidationException if invalid input
     * @throws NotFoundException   if user not found
     */
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

        // Validate names if provided
        if (req.getFirstName() != null && isBlank(req.getFirstName())) {
            throw new ValidationException("First name must not be blank");
        }
        if (req.getLastName() != null && isBlank(req.getLastName())) {
            throw new ValidationException("Last name must not be blank");
        }

        // Apply other fields
        Mapper.applyUpdateToUser(existing, req);

        // Password update if provided (store plaintext)
        if (req.getPassword() != null) {
            if (isBlank(req.getPassword())) {
                throw new ValidationException("Password must not be blank");
            }
            existing.setPassword(req.getPassword());
        }

        return userDAO.update(existing);
    }

    /**
     * Deletes a user by id.
     *
     * @param id user id
     * @throws NotFoundException if user does not exist
     */
    public void deleteUser(long id) {
        // ensure exists
        getById(id);
        userDAO.delete(id);
    }

    // ===== Helpers =====
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
