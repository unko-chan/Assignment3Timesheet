package ca.bcit.infosys.liangk.dao;

import ca.bcit.infosys.liangk.entity.AuthToken;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class AuthTokenDAO {

    @PersistenceContext(unitName = "TimesheetsPU")
    private EntityManager em;

    public AuthToken findByToken(String token) {
        return em.find(AuthToken.class, token);
    }

    public AuthToken create(AuthToken authToken) {
        em.persist(authToken);
        return authToken;
    }

    public AuthToken update(AuthToken authToken) {
        return em.merge(authToken);
    }

    public List<AuthToken> findActiveTokensForUser(long userId) {
        return em.createQuery(
                "SELECT a FROM AuthToken a WHERE a.user.id = :userId AND a.active = true",
                AuthToken.class)
            .setParameter("userId", userId)
            .getResultList();
    }
}
