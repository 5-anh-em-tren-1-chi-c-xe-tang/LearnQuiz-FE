package com.example.learnquiz_fe.ui.adapter.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {

    private final List<QuizResponseDTO> quizList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onQuizClick(QuizResponseDTO quiz);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setQuizzes(List<QuizResponseDTO> quizzes) {
        quizList.clear();
        if (quizzes != null) quizList.addAll(quizzes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizResponseDTO quiz = quizList.get(position);

        holder.tvTitle.setText(quiz.getTitle());
        holder.tvDescription.setText(quiz.getDescription());
        holder.tvCategory.setText(quiz.getTags() != null && !quiz.getTags().isEmpty()
                ? quiz.getTags().get(0)
                : "General");
        holder.tvQuestionCount.setText(quiz.getQuestions() != null
                ? quiz.getQuestions().size() + " questions"
                : "0 questions");
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f rating", quiz.getAverageRating()));
        holder.tvRatingCount.setText(String.format(Locale.getDefault(), "• %d users", quiz.getRatingCount()));
        holder.tvAuthor.setText(quiz.getAuthor().getUsername() != null ? "by " + quiz.getAuthor().getUsername() : "Unknown");

        Glide.with(holder.itemView.getContext())
                .load(quiz.getImageSource())
                .placeholder(R.drawable.img_loading_placeholder)
                .into(holder.imgQuizBanner);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onQuizClick(quiz);
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        ImageView imgQuizBanner;
        TextView tvTitle, tvDescription, tvCategory, tvQuestionCount, tvRating, tvRatingCount, tvAuthor;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            imgQuizBanner = itemView.findViewById(R.id.imgQuizBanner);
            tvTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvDescription = itemView.findViewById(R.id.tvQuizDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}
