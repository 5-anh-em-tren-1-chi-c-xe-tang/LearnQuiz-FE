package com.example.learnquiz_fe.data.model.quizhistory;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for quiz submission result
 */
public class SubmitQuizResponseDTO {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("quizId")
    private String quizId;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("score")
    private int score;
    
    @SerializedName("totalQuestions")
    private int totalQuestions;
    
    @SerializedName("percentage")
    private double percentage;
    
    @SerializedName("correctCount")
    private int correctCount;
    
    @SerializedName("completedAt")
    private String completedAt;
    
    @SerializedName("results")
    private List<AnswerResultDTO> results;
    
    public SubmitQuizResponseDTO() {
    }
    
    public SubmitQuizResponseDTO(String id, String quizId, String userId, int score,
                                int totalQuestions, double percentage, int correctCount,
                                String completedAt, List<AnswerResultDTO> results) {
        this.id = id;
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.correctCount = correctCount;
        this.completedAt = completedAt;
        this.results = results;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getQuizId() {
        return quizId;
    }
    
    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    
    public int getCorrectCount() {
        return correctCount;
    }
    
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }
    
    public String getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
    
    public List<AnswerResultDTO> getResults() {
        return results;
    }
    
    public void setResults(List<AnswerResultDTO> results) {
        this.results = results;
    }
}
