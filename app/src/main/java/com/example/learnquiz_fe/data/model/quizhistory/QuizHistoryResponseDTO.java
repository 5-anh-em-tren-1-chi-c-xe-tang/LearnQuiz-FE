package com.example.learnquiz_fe.data.model.quizhistory;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Lớp này dùng để bọc toàn bộ JSON response
public class QuizHistoryResponseDTO {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<QuizHistoryItemDTO> data;

    // Getters and Setters...
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<QuizHistoryItemDTO> getData() { return data; }
}
