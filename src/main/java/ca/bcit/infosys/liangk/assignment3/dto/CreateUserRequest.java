package ca.bcit.infosys.liangk.assignment3.dto;

import ca.bcit.infosys.liangk.assignment3.entity.UserRole;

public class CreateUserRequest {
    private String username;
    // Raw password; service layer should hash it
    private String password;
    private String firstName;
    private String lastName;
    private Integer employeeNumber;
    private UserRole role; // optional; default USER if null
    private Boolean active; // optional; default true if null

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
