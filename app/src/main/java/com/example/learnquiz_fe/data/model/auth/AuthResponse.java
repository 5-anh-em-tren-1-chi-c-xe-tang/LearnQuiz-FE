package com.example.learnquiz_fe.data.model.auth;

public class AuthResponse {
    public String accessToken;
    public String refreshToken;
    public User user;

    public static class User {
        public String id;
        public String name;
        public String email;
        public String avatarUrl;
    }
}

