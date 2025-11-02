package com.example.learnquiz_fe.data.model.quizhistory;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for submitting quiz answers
 */
public class SubmitQuizRequestDTO {
    
    @SerializedName("quizId")
    private String quizId;
    
    @SerializedName("answers")
    private List<SubmittedAnswerDTO> answers;
    
    public SubmitQuizRequestDTO() {
        this.answers = new ArrayList<>();
    }
    
    public SubmitQuizRequestDTO(String quizId, List<SubmittedAnswerDTO> answers) {
        this.quizId = quizId;
        this.answers = answers;
    }
    
    // Getters and Setters
    public String getQuizId() {
        return quizId;
    }
    
    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
    
    public List<SubmittedAnswerDTO> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<SubmittedAnswerDTO> answers) {
        this.answers = answers;
    }
    
    /**
     * Validate request
     */
    public boolean isValid() {
        return quizId != null && !quizId.isEmpty() 
            && answers != null && !answers.isEmpty();
    }
}
