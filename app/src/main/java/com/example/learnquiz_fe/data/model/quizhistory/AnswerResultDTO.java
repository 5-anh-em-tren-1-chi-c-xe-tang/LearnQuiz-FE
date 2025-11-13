package com.example.learnquiz_fe.data.model.quizhistory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for individual answer result from server
 */
public class AnswerResultDTO implements Serializable {
    
    @SerializedName("questionId")
    private String questionId;
    
    @SerializedName("questionText")
    private String questionText;
    
    @SerializedName("selectedAnswers")
    private List<String> selectedAnswers;
    
    @SerializedName("correctAnswers")
    private List<String> correctAnswers;
    
    @SerializedName("isCorrect")
    private boolean isCorrect;
    
    @SerializedName("explanation")
    private String explanation;
    
    public AnswerResultDTO() {
    }
    
    public AnswerResultDTO(String questionId, String questionText, List<String> selectedAnswers,
                          List<String> correctAnswers, boolean isCorrect, String explanation) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.selectedAnswers = selectedAnswers;
        this.correctAnswers = correctAnswers;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
    }
    
    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public List<String> getSelectedAnswers() {
        return selectedAnswers;
    }
    
    public void setSelectedAnswers(List<String> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }
    
    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
