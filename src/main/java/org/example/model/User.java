package org.example.model;

import org.example.enums.Role;

public class User {
    private final int id;
    private final String login;
    private final String password;
    private final Role role;

    public User(int id, String login, String password, Role role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
}
