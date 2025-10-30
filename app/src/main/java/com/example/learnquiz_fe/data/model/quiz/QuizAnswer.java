package com.example.learnquiz_fe.data.model.quiz;

import com.google.gson.annotations.SerializedName;

/**
 * DTO representing an answer option within a quiz question
 */
public class QuizAnswer {
    
    /**
     * Answer text
     */
    @SerializedName("answer")
    private String answer;
    
    /**
     * Whether this is the correct answer
     */
    @SerializedName("isTrue")
    private boolean isTrue;
    
    // Constructor
    public QuizAnswer() {
    }
    
    public QuizAnswer(String answer, boolean isTrue) {
        this.answer = answer;
        this.isTrue = isTrue;
    }
    
    // Getters and Setters
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public boolean isTrue() {
        return isTrue;
    }
    
    public void setTrue(boolean isTrue) {
        this.isTrue = isTrue;
    }
}
