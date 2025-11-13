package com.example.learnquiz_fe.data.model.quizhistory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

public class QuizHistoryItemDTO implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("quizId")
    private String quizId;

    @SerializedName("quizName")
    private String quizName;

    @SerializedName("score")
    private int score;

    @SerializedName("totalQuestions")
    private int totalQuestions;

    @SerializedName("correctCount")
    private int correctCount;

    @SerializedName("percentage")
    private double percentage;

    @SerializedName("completedAt")
    private Date completedAt; // Giữ Date, chúng ta đã sửa DateTypeAdapter

    @SerializedName("results")
    private List<AnswerResultDTO> results; // Tận dụng lớp AnswerResultDTO bạn đã có

    // Getters and Setters cho các trường trên...

    public String getId() { return id; }
    public String getQuizId() { return quizId; }
    public String getQuizName() { return quizName; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectCount() { return correctCount; }
    public double getPercentage() { return percentage; }
    public Date getCompletedAt() { return completedAt; }
    public List<AnswerResultDTO> getResults() { return results; }
}
