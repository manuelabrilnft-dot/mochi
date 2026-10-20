package com.example.mochiapi.security;

import java.time.Instant;

public class TokenInfo {
    private int user_id;
    private Instant expiresAt;

    public TokenInfo(int user_id, Instant expiresAt){
        this.user_id = user_id;
        this.expiresAt = expiresAt;
    }

    public int getUser_id() {
        return user_id;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
