package com.example.learnquiz_fe.data.model.feedback;

import com.google.gson.annotations.SerializedName;

public class UpdateFeedbackRequest {

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    // --- Constructor ---

    public UpdateFeedbackRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    // --- Getters and Setters ---

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}