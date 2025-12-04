package ca.bcit.infosys.liangk.dao;

import ca.bcit.infosys.liangk.entity.Timesheet;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@Stateless
public class TimesheetDAO {

    @PersistenceContext(unitName = "TimesheetsPU")
    private EntityManager em;

    public Timesheet findById(long id) {
        // Use a query with LEFT JOIN FETCH to ensure entries are loaded
        // if we expect to map them to DTOs immediately after.
        try {
            return em.createQuery(
                    "SELECT t FROM Timesheet t LEFT JOIN FETCH t.entries WHERE t.id = :id",
                    Timesheet.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    public Timesheet findByUserAndWeek(long userId, LocalDate weekStart) {
        TypedQuery<Timesheet> q = em.createQuery(
            "SELECT t FROM Timesheet t LEFT JOIN FETCH t.entries WHERE t.owner.id = :userId AND t.weekStart = :week",
            Timesheet.class);
        q.setParameter("userId", userId);
        q.setParameter("week", weekStart);
        List<Timesheet> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Timesheet> findByUser(long userId) {
        // Added LEFT JOIN FETCH t.entries to prevent LazyInitializationException in the Controller/Mapper
        return em.createQuery(
                "SELECT DISTINCT t FROM Timesheet t LEFT JOIN FETCH t.entries WHERE t.owner.id = :userId ORDER BY t.weekStart DESC",
                Timesheet.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    public Timesheet create(Timesheet timesheet) {
        em.persist(timesheet);
        return timesheet;
    }

    public Timesheet update(Timesheet timesheet) {
        return em.merge(timesheet);
    }

    public void delete(long id) {
        Timesheet t = em.find(Timesheet.class, id);
        if (t != null) {
            em.remove(t);
        }
    }
}
