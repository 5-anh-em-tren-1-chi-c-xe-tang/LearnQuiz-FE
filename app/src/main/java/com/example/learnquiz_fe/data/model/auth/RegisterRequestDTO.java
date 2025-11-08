package com.example.learnquiz_fe.data.model.auth;

public class RegisterRequestDTO {
    public String Username;

    public String Email;

    public String Password;

    public RegisterRequestDTO(String username, String email, String password) {
        Username = username;
        Email = email;
        Password = password;
    }
}
