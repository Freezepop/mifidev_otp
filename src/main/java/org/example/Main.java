package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.controller.AdminController;
import org.example.controller.AuthController;
import org.example.controller.OtpController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {

        Database.init();
        ExpiryScheduler.start();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/register", new AuthController.RegisterHandler());
        server.createContext("/login", new AuthController.LoginHandler());

        server.createContext("/admin/update_config", new AdminController.UpdateConfigHandler());
        server.createContext("/admin/users", new AdminController.UsersHandler());
        server.createContext("/admin/delete_user", new AdminController.DeleteUserHandler());

        server.createContext("/otp/generate", new OtpController.GenerateOtpHandler());
        server.createContext("/otp/validate", new OtpController.ValidateOtpHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Server started on port 8080");

        new Thread(new OtpExpirationTask()).start();
    }
}