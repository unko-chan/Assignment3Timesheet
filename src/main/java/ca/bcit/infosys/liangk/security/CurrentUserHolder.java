package ca.bcit.infosys.liangk.security;

import ca.bcit.infosys.liangk.entity.User;
import jakarta.enterprise.context.RequestScoped;

/**
 * Request-scoped holder for the authenticated User placed by AuthFilter.
 * This enables injection of the current user into REST resources and services
 * without passing it explicitly through every method call.
 */
@RequestScoped
public class CurrentUserHolder {
    private User user;

    /**
     * Returns the authenticated user associated with the current request, or null if not set.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the authenticated user for the current request context.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
