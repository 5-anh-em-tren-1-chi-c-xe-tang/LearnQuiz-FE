// @/app/src/main/java/com/example/learnquiz_fe/ui/adapter/myquizzes/MyQuizAdapter.java
package com.example.learnquiz_fe.ui.adapter.myquizzes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R; // Make sure R is imported
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date; // Import Date class

public class MyQuizAdapter extends RecyclerView.Adapter<MyQuizAdapter.QuizViewHolder> {

    private static final String TAG = "MyQuizAdapter"; // Tag for logging

    private List<QuizResponseDTO> quizList;

    public MyQuizAdapter(List<QuizResponseDTO> quizList) {
        this.quizList = quizList;
    }

    public void setQuizzes(List<QuizResponseDTO> newQuizzes) {
        this.quizList.clear();
        this.quizList.addAll(newQuizzes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myquiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        holder.bind(quizList.get(position));
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        // Declare all views within item_quiz_card.xml
        TextView tvQuizTitle;
        TextView tvQuizDescription;
        Chip chipQuestionsCount;
        Chip chipCategory;
        TextView tvPrivacyStatus;
        ImageView ivPrivacyIcon;
        TextView tvPlaysCount;
        TextView tvQuizDate;
        ImageView ivMoreOptions;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views using findViewById on itemView
            tvQuizTitle = itemView.findViewById(R.id.tv_quiz_title);
            tvQuizDescription = itemView.findViewById(R.id.tv_quiz_description);
            chipQuestionsCount = itemView.findViewById(R.id.chip_questions_count);
            chipCategory = itemView.findViewById(R.id.chip_category);
            tvPrivacyStatus = itemView.findViewById(R.id.tv_privacy_status);
            ivPrivacyIcon = itemView.findViewById(R.id.iv_privacy_icon);
            tvPlaysCount = itemView.findViewById(R.id.tv_plays_count);
            tvQuizDate = itemView.findViewById(R.id.tv_quiz_date);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
        }

        // Updated bind method to accept QuizResponseDTO
        public void bind(QuizResponseDTO quizResponseDTO) {
            tvQuizTitle.setText(quizResponseDTO.getTitle());
            tvQuizDescription.setText(quizResponseDTO.getDescription());

            // Questions Count
            int questionsCount = quizResponseDTO.getQuestions() != null ? quizResponseDTO.getQuestions().size() : 0;
            chipQuestionsCount.setText(String.format(itemView.getContext().getString(R.string.questions_count), questionsCount));

            // Category (using context field)
            chipCategory.setText(quizResponseDTO.getContext());

            // Privacy Status
            boolean isPublic = "public".equalsIgnoreCase(quizResponseDTO.getVisibility());
            tvPrivacyStatus.setText(isPublic ? R.string.privacy_status_public : R.string.privacy_status_private);
            ivPrivacyIcon.setImageResource(isPublic ? R.drawable.ic_public : R.drawable.ic_lock);

            // Plays Count (using ratingCount as a proxy for now)
            int playsCount = (int) quizResponseDTO.getRatingCount(); // Convert double to int
            tvPlaysCount.setText(String.format(itemView.getContext().getString(R.string.plays_count), playsCount));

            // Quiz Date (parsing string to format)
            String formattedDate = "";
            if (quizResponseDTO.getCreatedAt() != null && !quizResponseDTO.getCreatedAt().isEmpty()) {
                // Adjust this input format to match your backend's date string output
                // Common format for ISO 8601 UTC: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat outputSdf = new SimpleDateFormat("MMM dd", Locale.getDefault()); // e.g., Oct 25
                try {
                    Date date = inputSdf.parse(quizResponseDTO.getCreatedAt());
                    if (date != null) {
                        formattedDate = outputSdf.format(date);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date string: " + quizResponseDTO.getCreatedAt(), e);
                    // Fallback: use first 10 chars if parsing fails (e.g., "YYYY-MM-DD")
                    formattedDate = quizResponseDTO.getCreatedAt().substring(0, Math.min(quizResponseDTO.getCreatedAt().length(), 10));
                }
            }
            tvQuizDate.setText(formattedDate);

            ivMoreOptions.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "More options for " + quizResponseDTO.getTitle(), Toast.LENGTH_SHORT).show();
            });

            itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Quiz clicked: " + quizResponseDTO.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}