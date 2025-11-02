package com.example.learnquiz_fe.data.network;

/**
 * API endpoint constants
 * 
 * IMPORTANT: Update BASE_URL in AppConfig.java before deploying
 * This class keeps literal constants for Retrofit annotations
 */
public class ApiEndpoints {
    
    /**
     * Base URL for the backend API
     * TODO: Update this value to match your backend
     * Or configure in AppConfig.API.BASE_URL
     */
    public static final String BASE_URL = "https://82b68fb70340.ngrok-free.app/";
    
    /**
     * Quiz generation endpoint
     */
    public static final String GENERATE_QUIZ = "api/quiz/generate";
    
    /**
     * Authentication endpoints
     */
    public static final String LOGIN = "api/auth/login";
    public static final String REGISTER = "api/auth/register";
    public static final String REFRESH_TOKEN = "api/auth/refresh";
    
    /**
     * User profile endpoints
     */
    public static final String USER_PROFILE = "api/user/profile";
    
    /**
     * Quiz management endpoints
     */
    public static final String GET_QUIZZES = "api/quiz/list";
    public static final String GET_QUIZ_DETAIL = "api/quiz/{id}";
    public static final String DELETE_QUIZ = "api/quiz/{id}";
    
    /**
     * Quiz history endpoints
     */
    public static final String SUBMIT_QUIZ = "api/quizhistory/submit";
    
    // Request timeout configuration (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
}
