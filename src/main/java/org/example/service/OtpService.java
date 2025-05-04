package org.example.service;

import org.example.dao.OtpCodeDAO;
import org.example.dao.OtpConfigDAO;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class OtpService {
    public static String generateOtp(int length) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    public static void saveToFile(String code) throws IOException {
        try (FileWriter writer = new FileWriter("otp_code.txt")) {
            writer.write(code);
        }
    }

    public static void sendEmail(String email, String code) {
        System.out.println("Sending Email: " + email + " Code: " + code);
    }

    public static void sendSms(String phone, String code) {
        System.out.println("Simulated SMS to " + phone + ": " + code);
    }

    public static void sendTelegram(String chatId, String code) {
        System.out.println("Telegram to " + chatId + ": " + code);
    }
}