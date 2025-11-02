package com.example.learnquiz_fe.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quiz.QuizAnswer;
import com.example.learnquiz_fe.data.model.quiz.QuizQuestion;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying quiz review with correct/incorrect answers
 */
public class QuizReviewAdapter extends RecyclerView.Adapter<QuizReviewAdapter.ReviewViewHolder> {
    
    private List<QuizQuestion> questions;
    private Map<Integer, Integer> userAnswers;
    
    public QuizReviewAdapter(List<QuizQuestion> questions, Map<Integer, Integer> userAnswers) {
        this.questions = questions;
        this.userAnswers = userAnswers;
    }
    
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_review, parent, false);
        return new ReviewViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        QuizQuestion question = questions.get(position);
        Integer userAnswerIndex = userAnswers.get(position);
        
        // Display question number and text
        holder.tvQuestionNumber.setText(String.format("Question %d", position + 1));
        holder.tvQuestion.setText(question.getQuestion());
        
        // Find correct answer
        QuizAnswer correctAnswer = question.getCorrectAnswer();
        
        // Display user's answer
        if (userAnswerIndex != null && userAnswerIndex >= 0 && 
            userAnswerIndex < question.getAnswers().size()) {
            QuizAnswer userAnswer = question.getAnswers().get(userAnswerIndex);
            holder.tvUserAnswer.setText(userAnswer.getAnswer());
            
            // Check if correct
            if (userAnswer.isTrue()) {
                holder.tvUserAnswer.setTextColor(Color.parseColor("#4CAF50")); // Green
                holder.tvResult.setText("✓ Correct");
                holder.tvResult.setTextColor(Color.parseColor("#4CAF50"));
                holder.cardView.setStrokeColor(Color.parseColor("#4CAF50"));
                holder.tvCorrectAnswer.setVisibility(View.GONE);
            } else {
                holder.tvUserAnswer.setTextColor(Color.parseColor("#F44336")); // Red
                holder.tvResult.setText("✗ Incorrect");
                holder.tvResult.setTextColor(Color.parseColor("#F44336"));
                holder.cardView.setStrokeColor(Color.parseColor("#F44336"));
                
                // Show correct answer
                if (correctAnswer != null) {
                    holder.tvCorrectAnswer.setVisibility(View.VISIBLE);
                    holder.tvCorrectAnswer.setText("Correct answer: " + correctAnswer.getAnswer());
                }
            }
        } else {
            // Not answered
            holder.tvUserAnswer.setText("Not answered");
            holder.tvUserAnswer.setTextColor(Color.parseColor("#9E9E9E")); // Gray
            holder.tvResult.setText("⊘ Skipped");
            holder.tvResult.setTextColor(Color.parseColor("#9E9E9E"));
            holder.cardView.setStrokeColor(Color.parseColor("#9E9E9E"));
            
            // Show correct answer
            if (correctAnswer != null) {
                holder.tvCorrectAnswer.setVisibility(View.VISIBLE);
                holder.tvCorrectAnswer.setText("Correct answer: " + correctAnswer.getAnswer());
            }
        }
        
        // Display explanation if available
        if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
            holder.tvExplanation.setVisibility(View.VISIBLE);
            holder.tvExplanation.setText("Explanation: " + question.getExplanation());
        } else {
            holder.tvExplanation.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return questions.size();
    }
    
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvQuestionNumber;
        TextView tvQuestion;
        TextView tvUserAnswer;
        TextView tvResult;
        TextView tvCorrectAnswer;
        TextView tvExplanation;
        
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            tvResult = itemView.findViewById(R.id.tvResult);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
        }
    }
}
