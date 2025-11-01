package com.example.learnquiz_fe.data.dtos.quiz;

public class QuizAnswerDTO {
    private String answer;
    private boolean isTrue;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isTrue() {
        return isTrue;
    }

    public void setTrue(boolean aTrue) {
        isTrue = aTrue;
    }
}
