package com.example.learnquiz_fe.data.dtos.quiz;

import java.util.List;

public class QuizQuestionDTO {
    private String question;
    private List<QuizAnswerDTO> answers;
    private String explanation;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<QuizAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAnswerDTO> answers) {
        this.answers = answers;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
