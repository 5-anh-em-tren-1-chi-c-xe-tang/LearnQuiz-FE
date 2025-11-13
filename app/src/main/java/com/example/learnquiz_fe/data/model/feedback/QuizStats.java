package com.example.learnquiz_fe.data.model.feedback;

import com.google.gson.annotations.SerializedName;

public class QuizStats {

    @SerializedName("quizId")
    private String quizId;

    @SerializedName("averageRating")
    private double averageRating;

    @SerializedName("totalFeedback")
    private int totalFeedback;

    // --- Getters and Setters ---

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalFeedback() { return totalFeedback; }
    public void setTotalFeedback(int totalFeedback) { this.totalFeedback = totalFeedback; }
}