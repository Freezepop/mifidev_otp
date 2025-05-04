package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.util.TokenStore;
import org.example.dao.OtpCodeDAO;
import org.example.dao.OtpConfigDAO;
import org.example.dao.UserDAO;
import org.example.enums.Role;
import org.example.model.User;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class AdminController {

    static boolean checkAdmin(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        User user = TokenStore.getUserByToken(token);
        if (user == null || user.getRole() != Role.ADMIN) {
            exchange.sendResponseHeaders(403, -1);
            return false;
        }
        return true;
    }

    public static class UpdateConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAdmin(exchange)) return;
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject req = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            int ttl = req.getInt("ttl_seconds");
            int length = req.getInt("length");

            try {
                OtpConfigDAO.updateConfig(ttl, length);
                send(exchange, 200, "Updated");
            } catch (SQLException e) {
                e.printStackTrace();
                send(exchange, 500, "DB Error");
            }
        }
    }

    public static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAdmin(exchange)) return;
            try {
                JSONObject res = new JSONObject().put("users", UserDAO.getAllNonAdmins());
                byte[] bytes = res.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
            } catch (SQLException e) {
                e.printStackTrace();
                send(exchange, 500, "DB Error");
            }
        }
    }

    public static class DeleteUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAdmin(exchange)) return;
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject req = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String login = req.getString("login");

            try {
                User user = UserDAO.findByLogin(login);
                if (user == null || user.getRole() == Role.ADMIN) {
                    send(exchange, 400, "Cannot delete");
                    return;
                }
                OtpCodeDAO.deleteByUserId(user.getId());
                UserDAO.deleteById(user.getId());
                send(exchange, 200, "Deleted");
            } catch (SQLException e) {
                e.printStackTrace();
                send(exchange, 500, "DB Error");
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