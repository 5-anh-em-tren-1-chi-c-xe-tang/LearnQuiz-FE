package com.example.learnquiz_fe.data.model.auth;

import com.example.learnquiz_fe.data.model.User;

public class GoogleAuthResponse {
    private String accessToken;
    private String refreshToken;
    private User userResponseDto;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public User getUserResponseDto() {
        return userResponseDto;
    }

    public void setUserResponseDto(User userResponseDto) {
        this.userResponseDto = userResponseDto;
    }
}

