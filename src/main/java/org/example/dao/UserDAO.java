package org.example.dao;

import org.example.Database;
import org.example.enums.Role;
import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static void insertUser(String login, String passwordHash, Role role) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (login, password, role) VALUES (?, ?, ?)");
            ps.setString(1, login);
            ps.setString(2, passwordHash);
            ps.setString(3, role.name());
            ps.executeUpdate();
        }
    }

    public static boolean adminExists() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1");
            return rs.next();
        }
    }

    public static User findByLogin(String login) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE login = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                );
            }
            return null;
        }
    }
    public static void deleteById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public static List<String> getAllNonAdmins() throws SQLException {
        List<String> users = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT login FROM users WHERE role != 'ADMIN'");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(rs.getString("login"));
            }
        }
        return users;
    }
}