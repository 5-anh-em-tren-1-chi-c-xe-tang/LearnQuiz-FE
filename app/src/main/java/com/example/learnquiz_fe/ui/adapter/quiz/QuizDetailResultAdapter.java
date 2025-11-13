package com.example.learnquiz_fe.ui.adapter.quiz;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quizhistory.AnswerResultDTO;

import java.util.List;


public class QuizDetailResultAdapter extends RecyclerView.Adapter<QuizDetailResultAdapter.ResultViewHolder> {

    private List<AnswerResultDTO> results;
    private Context context;

    public QuizDetailResultAdapter(Context context, List<AnswerResultDTO> results) {
        this.context = context;
        this.results = results;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_detail_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        AnswerResultDTO result = results.get(position);

        holder.tvQuestionText.setText(result.getQuestionText());
        holder.tvExplanation.setText("Explanation: " + result.getExplanation());

        // Hiển thị câu trả lời đã chọn
        String selectedAns = "Your answer: (No answer)";
        if (result.getSelectedAnswers() != null && !result.getSelectedAnswers().isEmpty()) {
            selectedAns = "Your answer: " + String.join(", ", result.getSelectedAnswers());
        }
        holder.tvYourAnswer.setText(selectedAns);

        // Hiển thị câu trả lời đúng
        String correctAns = "Correct answer: " + String.join(", ", result.getCorrectAnswers());
        holder.tvCorrectAnswer.setText(correctAns);

        // Xử lý hiển thị dựa trên kết quả đúng/sai
        if (result.isCorrect()) {
            holder.tvYourAnswer.setTextColor(Color.parseColor("#008000")); // Màu xanh
            holder.ivStatus.setImageResource(R.drawable.ic_correct_check); // Thay bằng icon check của bạn
            holder.tvCorrectAnswer.setVisibility(View.GONE);
        } else {
            holder.tvYourAnswer.setTextColor(Color.RED);
            holder.ivStatus.setImageResource(R.drawable.ic_incorrect_cross); // Thay bằng icon X của bạn
            holder.tvCorrectAnswer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText, tvYourAnswer, tvCorrectAnswer, tvExplanation;
        ImageView ivStatus;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tv_question_text_detail);
            tvYourAnswer = itemView.findViewById(R.id.tv_your_answer_detail);
            tvCorrectAnswer = itemView.findViewById(R.id.tv_correct_answer_detail);
            tvExplanation = itemView.findViewById(R.id.tv_explanation_detail);
            ivStatus = itemView.findViewById(R.id.iv_correct_status);
        }
    }
}