package com.example.learnquiz_fe.ui.activities.quiz;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quizhistory.QuizHistoryItemDTO;
import com.example.learnquiz_fe.ui.adapter.quiz.QuizDetailResultAdapter;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class QuizHistoryDetailActivity extends AppCompatActivity {

    private QuizHistoryItemDTO historyItem;
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history_detail);

        // Lấy dữ liệu được gửi từ QuizHistoryAdapter
        if (getIntent().hasExtra("QUIZ_HISTORY_ITEM")) {
            historyItem = (QuizHistoryItemDTO) getIntent().getSerializableExtra("QUIZ_HISTORY_ITEM");
        }

        if (historyItem == null) {
            Toast.makeText(this, "Error: No history data found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Cài đặt Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_quiz_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Ánh xạ views
        TextView tvQuizName = findViewById(R.id.tv_detail_quiz_name);
        TextView tvScore = findViewById(R.id.tv_detail_score);
        TextView tvDate = findViewById(R.id.tv_detail_date);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_detail_results);

        // Gán dữ liệu cho các views
        tvQuizName.setText(historyItem.getQuizName());
        getSupportActionBar().setTitle(historyItem.getQuizName()); // Đặt tiêu đề Toolbar

        String scoreText = "Final Score: " + historyItem.getCorrectCount() + "/" + historyItem.getTotalQuestions() +
                " (" + String.format(Locale.US, "%.0f", historyItem.getPercentage()) + "%)";
        tvScore.setText(scoreText);

        if (historyItem.getCompletedAt() != null) {
            tvDate.setText("Completed on: " + outputFormat.format(historyItem.getCompletedAt()));
        }

        // Cài đặt RecyclerView cho danh sách kết quả
        QuizDetailResultAdapter adapter = new QuizDetailResultAdapter(this, historyItem.getResults());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}