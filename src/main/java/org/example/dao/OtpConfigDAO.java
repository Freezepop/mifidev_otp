package org.example.dao;

import org.example.Database;

import java.sql.*;

public class OtpConfigDAO {
    public static void updateConfig(int ttl, int length) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE otp_config SET ttl_seconds = ?, length = ?");
            ps.setInt(1, ttl);
            ps.setInt(2, length);
            ps.executeUpdate();
        }
    }

    public static int getLength() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT length FROM otp_config LIMIT 1");
            rs.next();
            return rs.getInt("length");
        }
    }
}
