package com.example.mochiapi.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenManager {

    private static Map<String, TokenInfo> tokenMap = new HashMap<>();

    public static String addToken(int user_id){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Instant expiresAt = Instant.now().plusSeconds(30 * 60);
        TokenInfo info = new TokenInfo(user_id, expiresAt);
        int[] ints = new int[bytes.length];
        Arrays.setAll(ints, i -> Byte.toUnsignedInt(bytes[i]));
        String strToken = Arrays.stream(ints).mapToObj(String::valueOf).collect(Collectors.joining());
        tokenMap.put(strToken, info);
        return strToken;
    }

    public static void deleteToken(String token){
        tokenMap.remove(token);
    }

    public static boolean isTokenValid(String token){
        TokenInfo info = tokenMap.get(token);
        if(info != null){
            return !Instant.now().isAfter(info.getExpiresAt());
        }
        return false;
    }

    public static TokenInfo getInfo(String token){
        return tokenMap.get(token);
    }

}
