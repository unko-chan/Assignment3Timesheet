package ca.bcit.infosys.liangk.dao;

import ca.bcit.infosys.liangk.entity.Timesheet;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO for Timesheet entities. Encapsulates all persistence operations using JPA.
 */
@Stateless
public class TimesheetDAO {

    @PersistenceContext(unitName = "TimesheetsPU")
    private EntityManager em;

    /**
     * Finds a timesheet by id, eagerly fetching entries to avoid lazy-loading issues
     * when mapping to DTOs.
     *
     * @param id timesheet identifier
     * @return Timesheet or null if not found
     */
    public Timesheet findById(long id) {
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

    /**
     * Finds a timesheet for a specific user and week start date.
     *
     * @param userId    owner user id
     * @param weekStart ISO date for the week start
     * @return Timesheet or null if none exists
     */
    public Timesheet findByUserAndWeek(long userId, LocalDate weekStart) {
        TypedQuery<Timesheet> q = em.createQuery(
            "SELECT t FROM Timesheet t LEFT JOIN FETCH t.entries WHERE t.owner.id = :userId AND t.weekStart = :week",
            Timesheet.class);
        q.setParameter("userId", userId);
        q.setParameter("week", weekStart);
        List<Timesheet> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Lists all timesheets for a user, newest week first, with entries eagerly fetched.
     *
     * @param userId owner user id
     * @return list of timesheets
     */
    public List<Timesheet> findByUser(long userId) {
        return em.createQuery(
                "SELECT DISTINCT t FROM Timesheet t LEFT JOIN FETCH t.entries WHERE t.owner.id = :userId ORDER BY t.weekStart DESC",
                Timesheet.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    /**
     * Persists a new timesheet.
     *
     * @param timesheet entity to persist
     * @return the same instance after persistence
     */
    public Timesheet create(Timesheet timesheet) {
        em.persist(timesheet);
        return timesheet;
    }

    /**
     * Updates a timesheet and returns the managed instance.
     *
     * @param timesheet detached entity with changes
     * @return managed, merged instance
     */
    public Timesheet update(Timesheet timesheet) {
        return em.merge(timesheet);
    }

    /**
     * Deletes a timesheet by id. No-op if not found.
     *
     * @param id identifier
     */
    public void delete(long id) {
        Timesheet t = em.find(Timesheet.class, id);
        if (t != null) {
            em.remove(t);
        }
    }
}
