package com.example.learnquiz_fe.ui.activities.quiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.dtos.quiz.QuizQuestionDTO;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.example.learnquiz_fe.ui.activities.QuizTakingActivity;
import com.example.learnquiz_fe.utils.QuizDataHolder;
import com.google.android.flexbox.FlexboxLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizDetailActivity extends AppCompatActivity {

    private TextView tvQuizTitle, tvQuizDescription, tvRating, tvRatingCount,
            tvPlaysCount, tvPlaysLabel, tvQuestionsCount, tvQuestionsLabel,
            tvDuration, tvDurationLabel, tvDifficulty, tvAuthorName, tvQuizCount,
            tvDifficultyValue, tvDurationValue, tvVisibilityValue,
            tvLastUpdated, tvMultipleChoiceCount;
    private ImageView ivAuthorAvatar, ivHeaderImage;
    private FlexboxLayout tagsContainer;
    private ImageButton btnBack, btnShare;
    private Button btnFollow, btnStartQuiz;

    private QuizRepository quizRepository;
    private ProgressBar progressBar;
    private ConstraintLayout mainContent;
    private String quizId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_details);

        findViews();
        setupButtons();

        quizRepository = new QuizRepository(this);

        // Get quiz ID from Intent
        quizId = getIntent().getStringExtra("quiz_id");
        if (quizId != null && !quizId.isBlank()) {
            loadQuizDetails(quizId);
        } else {
            Toast.makeText(this, "No quiz ID provided!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public static void start(Context context, int quizId) {
        Intent intent = new Intent(context, QuizDetailActivity.class);
        intent.putExtra("quiz_id", quizId);
        context.startActivity(intent);
    }

    private void findViews() {

        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnFollow = findViewById(R.id.btnFollow);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);

        progressBar = findViewById(R.id.progressBar);
        mainContent = findViewById(R.id.mainContent);

        ivHeaderImage = findViewById(R.id.ivHeaderImage);
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvQuizDescription = findViewById(R.id.tvQuizDescription);
        tvRating = findViewById(R.id.tvRating);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvPlaysCount = findViewById(R.id.tvPlaysCount);
        tvPlaysLabel = findViewById(R.id.tvPlaysLabel);
        tvQuestionsCount = findViewById(R.id.tvQuestionsCount);
        tvQuestionsLabel = findViewById(R.id.tvQuestionsLabel);
        tvDuration = findViewById(R.id.tvDuration);
        tvDurationLabel = findViewById(R.id.tvDurationLabel);
//        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvQuizCount = findViewById(R.id.tvQuizCount);
        tvDifficultyValue = findViewById(R.id.tvDifficultyValue);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        tvVisibilityValue = findViewById(R.id.tvVisibilityValue);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvMultipleChoiceCount = findViewById(R.id.tvMultipleChoiceCount);

        ivAuthorAvatar = findViewById(R.id.ivAuthorAvatar);
        tagsContainer = findViewById(R.id.tagsContainer);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareQuiz());
        btnFollow.setOnClickListener(v -> followAuthor());
        btnStartQuiz.setOnClickListener(v -> startQuiz());
    }

    // 🔹 Load quiz details from backend
    private void loadQuizDetails(String quizId) {
        progressBar.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);
        quizRepository.getQuizDetail(new QuizRepository.GenericCallback<QuizResponseDTO>() {
            @Override
            public void onSuccess(QuizResponseDTO data) {
                displayQuizDetails(data);
                progressBar.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(String message, int errorCode) {
                Toast.makeText(QuizDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);
                finish();
            }
        }, quizId);
    }

    private void displayQuizDetails(QuizResponseDTO data) {
        if (data.getImageSource() != null) {
            Glide.with(this)
                    .load(data.getImageSource())
                    .centerCrop()
                    .into(ivHeaderImage);
        }
        tvQuizTitle.setText(data.getTitle());
        tvQuizDescription.setText(data.getDescription());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", data.getAverageRating()));
        tvRatingCount.setText(data.getRatingCount() + " ratings");
        tvPlaysCount.setText(String.valueOf(data.getPlaysCount()));
        tvPlaysLabel.setText("plays");
        tvQuestionsCount.setText(String.valueOf(data.getQuestions().size()));
        tvQuestionsLabel.setText("questions");
        tvDuration.setText(String.valueOf(data.getQuizExamTimeLimit()));
        tvDurationLabel.setText("minutes");

        String difficulty = getDifficultyLevel(data.getQuestions().size(), data.getQuizExamTimeLimit());
//        tvDifficulty.setText(difficulty);
        tvDifficultyValue.setText(difficulty);

        tvAuthorName.setText(data.getAuthor().getUsername());
        tvQuizCount.setText(data.getAuthor().getCreateQuizCount() + " quizzes");

        if (data.getAuthor().getAvatarUrl() != null) {
            Glide.with(this)
                    .load(data.getAuthor().getAvatarUrl())
                    .circleCrop()
                    .into(ivAuthorAvatar);
        } else {
            // Load placeholder avatar
            Glide.with(this)
                    .load(R.drawable.img_placeholder_user)
                    .circleCrop()
                    .into(ivAuthorAvatar);
        }

        tvDurationValue.setText(data.getQuizExamTimeLimit() + " minutes");
        if (data.getVisibility() != null) {
            String vis = data.getVisibility();
            tvVisibilityValue.setText(
                    vis.substring(0, 1).toUpperCase(Locale.getDefault()) + vis.substring(1));
        }

        tvLastUpdated.setText(formatDate(data.getCreatedAt()));

        Pair<Integer, Integer> types = getQuestionTypes(data.getQuestions());
        tvMultipleChoiceCount.setText(types.first + " questions");

        displayTags(data.getTags());
    }

    private String getDifficultyLevel(int questionCount, int timeLimit) {
        float avg = questionCount > 0 ? (float) timeLimit / questionCount : 0f;
        if (avg < 0.5f) return "Hard";
        else if (avg < 1f) return "Medium";
        else return "Easy";
    }

    private Pair<Integer, Integer> getQuestionTypes(List<QuizQuestionDTO> list) {
        int mc = 0, tf = 0;
        for (QuizQuestionDTO q : list) {
            if (q.getAnswers().size() == 2 &&
                    q.getAnswers().stream().allMatch(a ->
                            a.getAnswer().equalsIgnoreCase("true") ||
                                    a.getAnswer().equalsIgnoreCase("false"))) tf++;
            else mc++;
        }
        return new Pair<>(mc, tf);
    }

    private String formatDate(String iso) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = input.parse(iso);
            return date != null ? output.format(date) : iso;
        } catch (Exception e) {
            return iso;
        }
    }

    private void displayTags(List<String> tags) {
        tagsContainer.removeAllViews();
        for (String tag : tags) {
            View tagView = getLayoutInflater().inflate(R.layout.item_tag, tagsContainer, false);
            TextView tv = tagView.findViewById(R.id.tvTag);
            String tagName = "#" + tag;
            tv.setText(tagName);
            tagsContainer.addView(tagView);
        }
    }

    private void shareQuiz() {
        Toast.makeText(this, "Share quiz feature not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void followAuthor() {
        btnFollow.setText("Following");
    }

    // Trong file QuizDetailActivity.java

    private void startQuiz() {
        // 1. Kiểm tra xem quizId có tồn tại không
        if (quizId == null || quizId.isEmpty()) {
            Toast.makeText(this, "Cannot start: Quiz ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Hiển thị trạng thái loading
        progressBar.setVisibility(View.VISIBLE);
        mainContent.setAlpha(0.5f);
        btnStartQuiz.setEnabled(false);

        // 3. Gọi hàm getQuizDetail với đúng QuizCallback
        quizRepository.getQuizDetail(quizId, new QuizRepository.QuizCallback() {
            @Override
            public void onSuccess(GenerateQuizResponse response) {
                // Tắt loading
                progressBar.setVisibility(View.GONE);
                mainContent.setAlpha(1.0f);
                btnStartQuiz.setEnabled(true);

                // 4. Kiểm tra dữ liệu trả về
                if (response != null && response.getQuestions() != null && !response.getQuestions().isEmpty()) {

                    // 5. Đặt dữ liệu vào QuizDataHolder
                    QuizDataHolder.getInstance().setQuizResponse(response);

                    // 6. Chuyển sang màn hình làm bài
                    Intent intent = new Intent(QuizDetailActivity.this, QuizTakingActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(QuizDetailActivity.this, "Failed to get quiz data or quiz has no questions.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message, int errorCode) {
                // Tắt loading và báo lỗi
                progressBar.setVisibility(View.GONE);
                mainContent.setAlpha(1.0f);
                btnStartQuiz.setEnabled(true);
                Toast.makeText(QuizDetailActivity.this, "Error getting quiz data: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }


    // Simple Pair class
    private static class Pair<F, S> {
        final F first;
        final S second;

        Pair(F f, S s) {
            first = f;
            second = s;
        }
    }
}
