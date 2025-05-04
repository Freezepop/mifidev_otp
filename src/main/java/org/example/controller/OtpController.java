package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.OtpCodeDAO;
import org.example.dao.OtpConfigDAO;
import org.example.model.User;
import org.example.service.OtpService;
import org.example.util.NotificationService;
import org.example.util.TokenStore;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class OtpController {
    static User auth(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        User user = TokenStore.getUserByToken(token);
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }
        return user;
    }

    public static class GenerateOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            User user = auth(exchange);
            if (user == null) return;

            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject req = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String method = req.getString("method"); // "file", "email", "sms", "telegram"
            String destination = req.optString("destination", ""); // email/phone/chat_id
            String operationId = req.optString("operation_id", null);

            try {
                int length = OtpConfigDAO.getLength();
                String code = OtpService.generateOtp(length);
                OtpCodeDAO.insert(user.getId(), code, operationId);

                String message = "Your OTP code is: " + code;

                switch (method) {
                    case "file" -> NotificationService.writeToFile(message);
                    case "email" -> NotificationService.sendEmail(destination, "Your OTP code", message);
                    case "sms" -> NotificationService.sendSms(destination, message);
                    case "telegram" -> NotificationService.sendTelegram(destination, message);
                    default -> {
                        send(exchange, 400, "Unknown method");
                        return;
                    }
                }

                send(exchange, 200, "OTP generated and sent via " + method);
            } catch (Exception e) {
                e.printStackTrace();
                send(exchange, 500, "Internal error");
            }
        }
    }

    public static class ValidateOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            User user = auth(exchange);
            if (user == null) return;

            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject req = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String code = req.getString("code");

            try {
                boolean valid = OtpCodeDAO.validate(user.getId(), code);
                send(exchange, valid ? 200 : 400, valid ? "Valid" : "Invalid or expired");
            } catch (SQLException e) {
                e.printStackTrace();
                send(exchange, 500, "DB error");
            }
        }
    }

    static void send(HttpExchange e, int code, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        e.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = e.getResponseBody()) {
            os.write(bytes);
        }
    }
}
