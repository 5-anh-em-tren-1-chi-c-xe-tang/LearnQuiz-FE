package com.example.learnquiz_fe.data.model.feedback;

import com.google.gson.annotations.SerializedName;

public class CreateFeedbackRequest {

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("quizId")
    private String quizId;

    // --- Constructor ---

    public CreateFeedbackRequest(int rating, String comment, String quizId) {
        this.rating = rating;
        this.comment = comment;
        this.quizId = quizId;
    }

    // --- Getters and Setters ---

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
}