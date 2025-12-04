package ca.bcit.infosys.liangk.dao;

import ca.bcit.infosys.liangk.entity.AuthToken;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * DAO for AuthToken entities providing persistence operations.
 */
@Stateless
public class AuthTokenDAO {

    @PersistenceContext(unitName = "TimesheetsPU")
    private EntityManager em;

    /**
     * Finds an auth token by its primary key token string.
     *
     * @param token token value
     * @return AuthToken or null if not found
     */
    public AuthToken findByToken(String token) {
        return em.find(AuthToken.class, token);
    }

    /**
     * Persists a new auth token.
     *
     * @param authToken token entity
     * @return the same instance after persistence
     */
    public AuthToken create(AuthToken authToken) {
        em.persist(authToken);
        return authToken;
    }

    /**
     * Merges and returns the managed token instance.
     *
     * @param authToken detached token entity
     * @return managed merged instance
     */
    public AuthToken update(AuthToken authToken) {
        return em.merge(authToken);
    }

    /**
     * Lists all active tokens for the given user id.
     *
     * @param userId user identifier
     * @return list of active tokens
     */
    public List<AuthToken> findActiveTokensForUser(long userId) {
        return em.createQuery(
                "SELECT a FROM AuthToken a WHERE a.user.id = :userId AND a.active = true",
                AuthToken.class)
            .setParameter("userId", userId)
            .getResultList();
    }
}
