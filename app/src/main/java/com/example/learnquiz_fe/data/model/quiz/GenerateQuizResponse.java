package com.example.learnquiz_fe.data.model.quiz;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO for quiz generation response from backend
 * Contains the generated quiz with questions and metadata
 */
public class GenerateQuizResponse {
    
    /**
     * Quiz ID from MongoDB
     */
    @SerializedName("id")
    private String id;
    
    /**
     * Quiz title
     */
    @SerializedName("title")
    private String title;
    
    /**
     * Quiz description
     */
    @SerializedName("description")
    private String description;
    
    /**
     * Quiz context/topic
     */
    @SerializedName("context")
    private String context;
    
    /**
     * Quiz visibility (public/private)
     */
    @SerializedName("visibility")
    private String visibility;
    
    /**
     * Language code of the quiz
     */
    @SerializedName("language")
    private String language;
    
    /**
     * Generated questions with answers
     */
    @SerializedName("questions")
    private List<QuizQuestion> questions;
    
    /**
     * Time limit in seconds
     */
    @SerializedName("quizExamTimeLimit")
    private int quizExamTimeLimit;
    
    /**
     * When the quiz was created
     */
    @SerializedName("createdAt")
    private Date createdAt;
    
    /**
     * Source image URL or identifier
     */
    @SerializedName("imageSource")
    private String imageSource;
    
    // Constructor
    public GenerateQuizResponse() {
        this.questions = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public List<QuizQuestion> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }
    
    public int getQuizExamTimeLimit() {
        return quizExamTimeLimit;
    }
    
    public void setQuizExamTimeLimit(int quizExamTimeLimit) {
        this.quizExamTimeLimit = quizExamTimeLimit;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getImageSource() {
        return imageSource;
    }
    
    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }
    
    /**
     * Get total number of questions
     */
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }
}
