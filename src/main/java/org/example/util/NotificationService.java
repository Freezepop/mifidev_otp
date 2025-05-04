package org.example.util;

import org.smpp.Connection;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.SubmitSM;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class NotificationService {

    public static void sendSms(String phone, String message) {
        String host = "127.0.0.1";
        int port = 2776;
        String systemId = "otp_sms_service";
        String password = "";
        String systemType = "java_otp_client";
        String sourceAddress = "otp_sms_service";

        try {
            Connection connection = new TCPIPConnection(host, port);
            Session session = new Session(connection);

            // BIND-запрос
            BindTransmitter bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setAddressRange(sourceAddress);
            bindRequest.setInterfaceVersion((byte) 0x34);

            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new RuntimeException("SMPP Bind failed with status: " + bindResponse.getCommandStatus());
            }

            SubmitSM submit = new SubmitSM();
            submit.setSourceAddr(sourceAddress);
            submit.setDestAddr(phone);
            submit.setShortMessage(message);

            session.submit(submit);
            session.unbind();
            session.close();

            System.out.println("SMS sent via SMPP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void sendEmail(String to, String subject, String text) {
        final String from = "@gmail.com";
        final String password = "";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void sendTelegram(String chatId, String text) {
        try {
            String token = "";
            String urlStr = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s", token, chatId, text);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String message) {
        try (FileWriter fw = new FileWriter("otp_code.txt", true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}