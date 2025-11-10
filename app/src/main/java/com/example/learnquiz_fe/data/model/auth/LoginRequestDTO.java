package com.example.learnquiz_fe.data.model.auth;

import com.google.gson.annotations.SerializedName;

public class LoginRequestDTO {
    @SerializedName("usernameOrEmail")
    public String usernameOrEmail;

    @SerializedName("password")
    public String password;

    public LoginRequestDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
