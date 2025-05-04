package org.example;

import org.example.enums.OtpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

public class OtpExpirationTask implements Runnable {
    @Override
    public void run() {
        while (true) {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT id, created_at FROM otp_codes WHERE status = ?");
                ps.setString(1, OtpStatus.ACTIVE.name());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    long ttlSeconds = getTtl();

                    if (createdAt.toLocalDateTime().plusSeconds(ttlSeconds).isBefore(LocalDateTime.now())) {
                        PreparedStatement update = conn.prepareStatement("UPDATE otp_codes SET status = ? WHERE id = ?");
                        update.setString(1, OtpStatus.EXPIRED.name());
                        update.setInt(2, id);
                        update.executeUpdate();
                    }
                }

                Thread.sleep(60_000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long getTtl() throws Exception {
        try (Connection conn = Database.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT ttl_seconds FROM otp_config LIMIT 1");
            if (rs.next()) return rs.getInt("ttl_seconds");
            throw new IllegalStateException("OTP config missing");
        }
    }
}