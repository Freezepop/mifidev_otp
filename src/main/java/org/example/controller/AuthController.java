package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.util.TokenStore;
import org.example.dao.UserDAO;
import org.example.enums.Role;
import org.example.model.User;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthController {

    public static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject req = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String login = req.getString("login");
            String password = req.getString("password");
            String role = req.getString("role");

            try {
                if (Role.ADMIN.name().equals(role.toUpperCase()) && UserDAO.adminExists()) {
                    send(exchange, 400, "Admin already exists");
                    return;
                }

                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                UserDAO.insertUser(login, hashed, Role.valueOf(role.toUpperCase()));
                send(exchange, 200, "User registered");
            } catch (Exception e) {
                e.printStackTrace();
                send(exchange, 500, "Server error");
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

            JSONObject req = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String login = req.getString("login");
            String password = req.getString("password");

            try {
                User user = UserDAO.findByLogin(login);
                if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                    send(exchange, 401, "Invalid credentials");
                    return;
                }

                String token = TokenStore.generateToken(user);
                send(exchange, 200, new JSONObject().put("token", token).toString());
            } catch (Exception e) {
                e.printStackTrace();
                send(exchange, 500, "Server error");
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