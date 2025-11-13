package com.example.learnquiz_fe.ui.activities.quiz;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.network.RetrofitClient;
import com.example.learnquiz_fe.data.repository.QuizHistoryRepository;
import com.example.learnquiz_fe.data.model.quizhistory.QuizHistoryItemDTO;
import com.example.learnquiz_fe.ui.adapter.quiz.QuizHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class QuizHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuizHistoryAdapter adapter;
    private QuizHistoryRepository repository;
    private ProgressBar progressBar;
    private TextView tvNoHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);
        // Khởi tạo Views
        Toolbar toolbar = findViewById(R.id.toolbar_quiz_history);
        recyclerView = findViewById(R.id.recycler_view_history);
        progressBar = findViewById(R.id.progress_bar_history);
        tvNoHistory = findViewById(R.id.tv_no_history);

        // Cài đặt Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Khởi tạo Repository
        repository = new QuizHistoryRepository(this);

        // Cài đặt RecyclerView
        setupRecyclerView();

        // Tải dữ liệu
        loadHistoryData();
    }

    private void setupRecyclerView() {
        adapter = new QuizHistoryAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadHistoryData() {
        showLoading(true);
        repository.getMyHistory().observe(this, response -> {
            showLoading(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<QuizHistoryItemDTO> historyList = response.getData();
                if (historyList.isEmpty()) {
                    // Hiển thị thông báo không có lịch sử
                    tvNoHistory.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Cập nhật dữ liệu cho adapter
                    adapter.updateData(historyList);
                    tvNoHistory.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                // Xử lý lỗi
                Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
                tvNoHistory.setVisibility(View.VISIBLE);
                tvNoHistory.setText("Error loading data.");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}

