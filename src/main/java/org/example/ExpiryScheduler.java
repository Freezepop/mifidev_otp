package org.example;

import java.sql.*;
import java.time.*;
import java.util.Timer;
import java.util.TimerTask;

public class ExpiryScheduler {
    public static void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try (Connection conn = Database.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE otp_codes SET status='EXPIRED' WHERE status='ACTIVE' AND created_at < ?");
                    stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusSeconds(getTtlSeconds(conn))));
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60 * 1000);
    }

    private static int getTtlSeconds(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ttl_seconds FROM otp_config LIMIT 1");
        if (rs.next()) return rs.getInt("ttl_seconds");
        return 300;
    }
}