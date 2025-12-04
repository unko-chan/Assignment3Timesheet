package ca.bcit.infosys.liangk.assignment3.dto;

import ca.bcit.infosys.liangk.assignment3.entity.UserRole;

public class UpdateUserRequest {
    private String username; // optional
    private String password; // optional raw; service should hash later
    private String firstName; // optional
    private String lastName; // optional
    private Integer employeeNumber; // optional
    private UserRole role; // optional
    private Boolean active; // optional

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(Integer employeeNumber) { this.employeeNumber = employeeNumber; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
