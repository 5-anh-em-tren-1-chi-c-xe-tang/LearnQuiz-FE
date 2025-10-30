package com.example.learnquiz_fe.data.model.quiz;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for quiz generation request
 * Supports multiple images (1-10), language selection, and quiz configuration
 */
public class GenerateQuizRequest {
    
    /**
     * Image sources - can be URLs or base64 encoded strings
     * Base64 format: "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
     * URL format: "https://example.com/image.jpg"
     */
    @SerializedName("images")
    private List<String> images;
    
    /**
     * Language code for quiz content (vi, en, ja, fr, etc.)
     * Must be a 2-letter code
     */
    @SerializedName("language")
    private String language;
    
    /**
     * Number of questions to generate (1-20)
     */
    @SerializedName("questionCount")
    private int questionCount;
    
    /**
     * Quiz visibility setting (public/private)
     */
    @SerializedName("visibility")
    private String visibility;
    
    /**
     * Time limit for quiz exam in seconds (0-7200)
     */
    @SerializedName("quizExamTimeLimit")
    private int quizExamTimeLimit;
    
    /**
     * Optional: Folder ID to organize the quiz
     */
    @SerializedName("folderId")
    private String folderId;
    
    // Constructor
    public GenerateQuizRequest() {
        this.images = new ArrayList<>();
        this.language = "en";
        this.questionCount = 5;
        this.visibility = "public";
        this.quizExamTimeLimit = 0;
    }
    
    // Getters and Setters
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public void addImage(String imageBase64) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imageBase64);
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public int getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    
    public int getQuizExamTimeLimit() {
        return quizExamTimeLimit;
    }
    
    public void setQuizExamTimeLimit(int quizExamTimeLimit) {
        this.quizExamTimeLimit = quizExamTimeLimit;
    }
    
    public String getFolderId() {
        return folderId;
    }
    
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }
    
    /**
     * Validate request before sending
     */
    public boolean isValid() {
        return images != null && !images.isEmpty() && images.size() <= 10
                && language != null && language.matches("^[a-z]{2}$")
                && questionCount >= 1 && questionCount <= 20
                && quizExamTimeLimit >= 0 && quizExamTimeLimit <= 7200;
    }
}
