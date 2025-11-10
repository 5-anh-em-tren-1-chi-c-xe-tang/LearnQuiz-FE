package com.example.learnquiz_fe.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * User data model representing authenticated user information.
 * This class is designed to map the user data from the API response.
 */
public class User {

    @SerializedName("id")
    public String id;

    @SerializedName("username")
    public String username;

    @SerializedName("passwordHash")
    public String passwordHash;

    @SerializedName("email")
    public String email;

    /**
     * User role (e.g., "Admin", "User", "Teacher", "Student")
     */
    @SerializedName("role")
    public String role;

    /**
     * Avatar image URL
     */
    @SerializedName("avatarUrl")
    public String avatarUrl;

    /**
     * Refresh token for JWT authentication
     */
    @SerializedName("refreshToken")
    public String refreshToken;

    /**
     * Expiry date of the refresh token (represented as a String in ISO format)
     */
    @SerializedName("refreshTokenExpiry")
    public String refreshTokenExpiry;

    /**
     * Account creation timestamp (represented as a String in ISO format)
     */
    @SerializedName("createdAt")
    public String createdAt;

    /**
     * Last update timestamp (represented as a String in ISO format)
     */
    @SerializedName("updatedAt")
    public String updatedAt;

    /**
     * The number of quizzes can be created by the free user in a day
     */
    @SerializedName("createQuizCount")
    public int createQuizCount;

    // This property might not come from the backend, so we don't use SerializedName
    private boolean isAuthenticated;

    // --- Constructors ---

    /**
     * Default constructor for libraries like GSON.
     */
    public User() {
    }

    // --- Getters and Setters (Optional, but good practice) ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    // You can add getters and setters for other fields as needed.

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isAuthenticated=" + isAuthenticated +
                '}';
    }
}
