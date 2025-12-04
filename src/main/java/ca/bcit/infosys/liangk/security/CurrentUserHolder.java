package ca.bcit.infosys.liangk.security;

import ca.bcit.infosys.liangk.entity.User;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CurrentUserHolder {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
