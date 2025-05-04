package org.example.dao;

import org.example.Database;
import org.example.enums.OtpStatus;

import java.sql.*;
import java.time.LocalDateTime;

public class OtpCodeDAO {
    public static void insert(int userId, String code, String opId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO otp_codes (user_id, code, status, created_at, operation_id) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.setString(3, OtpStatus.ACTIVE.name());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, opId);
            ps.executeUpdate();
        }
    }

    public static boolean validate(int userId, String code) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM otp_codes WHERE user_id=? AND code=? AND status='ACTIVE'");
            ps.setInt(1, userId);
            ps.setString(2, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                PreparedStatement update = conn.prepareStatement("UPDATE otp_codes SET status='USED' WHERE id=?");
                update.setInt(1, id);
                update.executeUpdate();
                return true;
            }
            return false;
        }
    }

    public static void deleteByUserId(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM otp_codes WHERE user_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}