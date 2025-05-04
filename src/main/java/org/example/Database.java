package org.example;

import java.sql.*;

public class Database {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:6432/otpdb", "postgres", "1");
    }

    public static void init() throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, login TEXT UNIQUE, password TEXT, role TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS otp_config (id SERIAL PRIMARY KEY, ttl_seconds INTEGER, length INTEGER)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS otp_codes (id SERIAL PRIMARY KEY, user_id INTEGER, code TEXT, status TEXT, created_at TIMESTAMP, operation_id TEXT)");
            stmt.executeUpdate("INSERT INTO otp_config (ttl_seconds, length) SELECT 300, 6 WHERE NOT EXISTS (SELECT 1 FROM otp_config)");
        }
    }
}