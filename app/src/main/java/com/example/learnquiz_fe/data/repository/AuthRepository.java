package com.example.learnquiz_fe.data.repository;

import com.example.learnquiz_fe.data.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for handling authentication operations
 * In production, this would connect to a backend API
 * Currently uses mock data for demonstration
 */
public class AuthRepository {
    
    // Mock user database for demonstration
    private static final Map<String, User> mockUsers = new HashMap<>();
    
    static {
        // Initialize with demo users
        User demoUser = new User("1", "demo", "demo@learnquiz.com", "password123");
        mockUsers.put("demo", demoUser);
        mockUsers.put("demo@learnquiz.com", demoUser);
    }

    /**
     * Authenticate user with username/email and password
     * @param usernameOrEmail Username or email
     * @param password Password
     * @return User object if authentication successful, null otherwise
     */
    public User login(String usernameOrEmail, String password) {
        // Simulate network delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if user exists and password matches
        User user = mockUsers.get(usernameOrEmail);
        if (user != null && user.getPassword().equals(password)) {
            user.setAuthenticated(true);
            return user;
        }
        
        return null;
    }

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid email format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Register new user (placeholder for future implementation)
     * @param username Username
     * @param email Email
     * @param password Password
     * @return true if registration successful
     */
    public boolean register(String username, String email, String password) {
        // TODO: Implement registration with backend API
        return false;
    }

    /**
     * Logout current user
     */
    public void logout() {
        // Clear session data
        // TODO: Implement session clearing
    }
}
