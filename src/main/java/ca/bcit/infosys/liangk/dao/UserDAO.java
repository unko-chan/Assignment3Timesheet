package ca.bcit.infosys.liangk.dao;

import ca.bcit.infosys.liangk.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * DAO for User entities encapsulating JPA access.
 */
@Stateless
public class UserDAO {

    @PersistenceContext(unitName = "TimesheetsPU")
    private EntityManager em;

    /**
     * Finds a user by primary key id.
     *
     * @param id user id
     * @return User or null if not found
     */
    public User findById(long id) {
        return em.find(User.class, id);
    }

    /**
     * Finds a user by unique username.
     *
     * @param username username
     * @return User or null if none
     */
    public User findByUsername(String username) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
        q.setParameter("username", username);
        List<User> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Finds a user by unique employee number.
     *
     * @param employeeNumber employee number
     * @return User or null if none
     */
    public User findByEmployeeNumber(int employeeNumber) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.employeeNumber = :emp", User.class);
        q.setParameter("emp", employeeNumber);
        List<User> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Lists all users ordered by username.
     *
     * @return list of users
     */
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class)
                .getResultList();
    }

    /**
     * Persists a new user.
     *
     * @param user entity to persist
     * @return same instance after persistence
     */
    public User create(User user) {
        em.persist(user);
        return user;
    }

    /**
     * Merges a user and returns the managed instance.
     *
     * @param user detached user with changes
     * @return managed, merged instance
     */
    public User update(User user) {
        return em.merge(user);
    }

    /**
     * Deletes a user by id. No-op if not found.
     *
     * @param id identifier
     */
    public void delete(long id) {
        User u = em.find(User.class, id);
        if (u != null) {
            em.remove(u);
        }
    }
}
