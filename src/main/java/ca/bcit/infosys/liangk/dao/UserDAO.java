package ca.bcit.infosys.liangk.dao;

import ca.bcit.infosys.liangk.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class UserDAO {

    @PersistenceContext(unitName = "TimesheetsPU")
    private EntityManager em;

    public User findById(long id) {
        return em.find(User.class, id);
    }

    public User findByUsername(String username) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
        q.setParameter("username", username);
        List<User> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByEmployeeNumber(int employeeNumber) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.employeeNumber = :emp", User.class);
        q.setParameter("emp", employeeNumber);
        List<User> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class)
                .getResultList();
    }

    public User create(User user) {
        em.persist(user);
        return user;
    }

    public User update(User user) {
        return em.merge(user);
    }

    public void delete(long id) {
        User u = em.find(User.class, id);
        if (u != null) {
            em.remove(u);
        }
    }
}
