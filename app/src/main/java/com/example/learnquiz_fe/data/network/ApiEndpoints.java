package com.example.learnquiz_fe.data.network;

/**
 * API endpoint constants
 * * IMPORTANT: Update BASE_URL in AppConfig.java before deploying
 * This class keeps literal constants for Retrofit annotations
 */
public class ApiEndpoints {

    /**
     * Base URL for the backend API
     * Or configure in AppConfig.API.BASE_URL
     * use "http://10.0.2.2 to access localhost from Android emulator
     * 5186 for http and 7024 for https
     */

    // ===== BẠN ĐÃ SỬA Ở ĐÂY =====

    public static final String BASE_URL = "https://10.0.2.2:7024/";
    // ============================

    /**
     * Quiz generation endpoint
     */
    public static final String GENERATE_QUIZ = "api/quiz/generate";
    /**
     * Authentication endpoints
     */
    public static final String LOGIN = "api/auth/login";
    public static final String LOGIN_GOOGLE = "api/auth/google";
    public static final String REGISTER = "api/auth/register";
    public static final String REFRESH_TOKEN = "api/auth/refresh";

    /**
     * User profile endpoints
     */
    public static final String USER_PROFILE = "api/user/profile";
    public static final String UPDATE_PROFILE = "api/user";
    public static final String MY_QUIZ_HISTORY = "api/QuizHistory/me";

    /**
     * Quiz endpoints
     */
    public static final String GET_PUBLIC_QUIZ = "api/Quiz/public";
    public static final String GET_QUIZ_DETAIL = "api/quiz/{id}";
    public static final String DELETE_QUIZ = "api/quiz/{id}";

    /**
     * Quiz history endpoints
     */
    public static final String SUBMIT_QUIZ = "api/quizhistory/submit";


    /**
     * Payment endpoints
     */
    public static final String CREATE_PAYMENT_INTENT = "api/payment/create";
    public static final String GET_ORDER = "api/payment/order/{orderId}";

    /**
     * Feedback endpoints
     */
    public static final String FEEDBACK_BASE = "api/Feedback";
    public static final String FEEDBACK_BY_ID = "api/Feedback/{id}";
    public static final String FEEDBACK_BY_QUIZ = "api/Feedback/quiz/{quizId}";
    public static final String FEEDBACK_MY = "api/Feedback/my-feedback";
    public static final String FEEDBACK_STATS = "api/Feedback/quiz/{quizId}/stats";

    // Request timeout configuration (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
}