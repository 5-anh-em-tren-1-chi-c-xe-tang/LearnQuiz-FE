package com.example.learnquiz_fe.data.model.quiz;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a question within the quiz
 */
public class QuizQuestion {
    
    /**
     * Question ID from server
     */
    @SerializedName("questionId")
    private String questionId;
    
    /**
     * Question text
     */
    @SerializedName("question")
    private String question;
    
    /**
     * List of possible answers
     */
    @SerializedName("answers")
    private List<QuizAnswer> answers;
    
    /**
     * Optional explanation for the correct answer
     */
    @SerializedName("explanation")
    private String explanation;
    
    // Constructor
    public QuizQuestion() {
        this.answers = new ArrayList<>();
    }
    
    public QuizQuestion(String question, List<QuizAnswer> answers, String explanation) {
        this.question = question;
        this.answers = answers;
        this.explanation = explanation;
    }
    
    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public List<QuizAnswer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<QuizAnswer> answers) {
        this.answers = answers;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    /**
     * Get the correct answer from the list
     */
    public QuizAnswer getCorrectAnswer() {
        if (answers != null) {
            for (QuizAnswer answer : answers) {
                if (answer.isTrue()) {
                    return answer;
                }
            }
        }
        return null;
    }
}
