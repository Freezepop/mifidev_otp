package org.example.util;

import org.example.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStore {
    private static final Map<String, User> tokenUserMap = new ConcurrentHashMap<>();

    public static void storeToken(String token, User user) {
        tokenUserMap.put(token, user);
    }

    public static User getUserByToken(String token) {
        return tokenUserMap.get(token);


    }

    public static String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenUserMap.put(token, user);
        return token;
    }

}