package com.example.learnquiz_fe.utils;

import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;

/**
 * Singleton holder for large quiz data to avoid TransactionTooLargeException
 * when passing data between activities via Intent.
 * 
 * This is a temporary holder - data will be cleared after being consumed.
 */
public class QuizDataHolder {
    
    private static QuizDataHolder instance;
    private GenerateQuizResponse quizResponse;
    
    private QuizDataHolder() {
        // Private constructor for singleton
    }
    
    public static synchronized QuizDataHolder getInstance() {
        if (instance == null) {
            instance = new QuizDataHolder();
        }
        return instance;
    }
    
    /**
     * Store quiz response temporarily
     */
    public void setQuizResponse(GenerateQuizResponse response) {
        this.quizResponse = response;
    }
    
    /**
     * Get and consume quiz response (will be cleared after retrieval)
     */
    public GenerateQuizResponse getAndClearQuizResponse() {
        GenerateQuizResponse response = this.quizResponse;
        this.quizResponse = null; // Clear after retrieval
        return response;
    }
    
    /**
     * Get quiz response without clearing
     */
    public GenerateQuizResponse getQuizResponse() {
        return this.quizResponse;
    }
    
    /**
     * Clear stored data
     */
    public void clear() {
        this.quizResponse = null;
    }
}
