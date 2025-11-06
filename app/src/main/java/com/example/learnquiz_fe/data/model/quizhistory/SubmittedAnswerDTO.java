package com.example.learnquiz_fe.data.model.quizhistory;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Single submitted answer DTO
 */
public class SubmittedAnswerDTO {
    
    @SerializedName("questionId")
    private String questionId;
    
    @SerializedName("selectedAnswers")
    private List<String> selectedAnswers;
    
    public SubmittedAnswerDTO() {
        this.selectedAnswers = new ArrayList<>();
    }
    
    public SubmittedAnswerDTO(String questionId, List<String> selectedAnswers) {
        this.questionId = questionId;
        this.selectedAnswers = selectedAnswers;
    }
    
    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public List<String> getSelectedAnswers() {
        return selectedAnswers;
    }
    
    public void setSelectedAnswers(List<String> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }
}
