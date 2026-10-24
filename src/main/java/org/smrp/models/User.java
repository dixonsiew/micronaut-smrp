package org.smrp.models;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.micronaut.serde.annotation.Serdeable;
import io.r2dbc.spi.Row;


@Serdeable
public class User {

    private Long id;
    private String username;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @JsonProperty("last_login")
    private LocalDateTime lastLogin;
    
    @JsonProperty("roles")
    private List<Role> roles;

    public static User fromRow(Row row) {
        User user = new User();
        user.setId(row.get("id", Long.class));
        user.setUsername(row.get("username", String.class));
        user.setFirstName(row.get("first_name", String.class));
        user.setLastName(row.get("last_name", String.class));
        user.setPassword(row.get("password", String.class));
        user.setLastLogin(row.get("last_login", LocalDateTime.class));
        return user;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
