package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.UserDAO;
import org.example.enums.Role;
import org.example.model.User;
import org.example.util.TokenStore;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;

public class UserController {
    public static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream body = exchange.getRequestBody();
            String req = new String(body.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(req);

            String login = json.getString("login");
            String password = json.getString("password");
            String role = json.getString("role");

            if (!role.equals("ADMIN") && !role.equals("USER")) {
                sendResponse(exchange, 400, "Invalid role");
                return;
            }

            try {
                if (role.equals("ADMIN") && UserDAO.adminExists()) {
                    sendResponse(exchange, 400, "Admin already exists");
                    return;
                }

                String hash = BCrypt.hashpw(password, BCrypt.gensalt());
                UserDAO.insertUser(login, hash, Role.valueOf(role));
                sendResponse(exchange, 200, "User registered");
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Database error");
            }
        }
    }

    public static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream body = exchange.getRequestBody();
            String req = new String(body.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(req);
            String login = json.getString("login");
            String password = json.getString("password");

            try {
                User user = UserDAO.findByLogin(login);
                if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                    sendResponse(exchange, 401, "Invalid credentials");
                    return;
                }

                String token = UUID.randomUUID().toString();
                TokenStore.storeToken(token, user);
                JSONObject res = new JSONObject().put("token", token);
                sendJsonResponse(exchange, 200, res);
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Database error");
            }
        }
    }

    private static void sendResponse(HttpExchange ex, int code, String msg) throws IOException {
        ex.sendResponseHeaders(code, msg.length());
        try (OutputStream os = ex.getResponseBody()) {
            os.write(msg.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void sendJsonResponse(HttpExchange ex, int code, JSONObject json) throws IOException {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}