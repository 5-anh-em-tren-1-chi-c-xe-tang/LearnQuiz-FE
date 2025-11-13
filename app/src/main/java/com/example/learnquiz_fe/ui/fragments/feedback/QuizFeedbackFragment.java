package com.example.learnquiz_fe.ui.fragments.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.feedback.Feedback;
import com.example.learnquiz_fe.data.network.RetrofitClient;
import com.example.learnquiz_fe.ui.adapter.feedback.FeedbackAdapter;
import com.example.learnquiz_fe.ui.viewmodel.FeedbackViewModel;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.widget.AppCompatButton; // Đã sửa import

import java.util.Optional;

public class QuizFeedbackFragment extends Fragment {

    private FeedbackViewModel feedbackViewModel;
    private ImageButton btnBack;
    private RatingBar statsRatingBar;
    private TextView tvStatsAverage, tvStatsCount;
    private RatingBar ratingBar;
    private TextInputEditText etComment;
    private AppCompatButton btnSubmitFeedback; // Đã sửa kiểu dữ liệu
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private String quizId;
    private String currentUserId;
    private Feedback myFeedback;
    private RetrofitClient client;
    public static QuizFeedbackFragment newInstance(String quizId) {
        QuizFeedbackFragment fragment = new QuizFeedbackFragment();
        Bundle args = new Bundle();
        args.putString("QUIZ_ID", quizId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            quizId = getArguments().getString("QUIZ_ID");
        }
        client = RetrofitClient.getInstance(getContext());
        currentUserId = client.getUserId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_feedback, container, false);
        bindViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        feedbackViewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        if (quizId != null) {
            loadAllFeedbackData();
        }
    }

    private void bindViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        statsRatingBar = view.findViewById(R.id.statsRatingBar);
        tvStatsAverage = view.findViewById(R.id.tvStatsAverage);
        tvStatsCount = view.findViewById(R.id.tvStatsCount);
        ratingBar = view.findViewById(R.id.ratingBar);
        etComment = view.findViewById(R.id.etComment);
        btnSubmitFeedback = view.findViewById(R.id.btnSubmitFeedback); // Sẽ không còn lỗi cast
        feedbackRecyclerView = view.findViewById(R.id.feedbackRecyclerView);
    }

    private void setupRecyclerView() {
        feedbackAdapter = new FeedbackAdapter(currentUserId, (feedback) -> {
            myFeedback = feedback;
            ratingBar.setRating(feedback.getRating());
            etComment.setText(feedback.getComment());
            btnSubmitFeedback.setText("Update Feedback");
            Toast.makeText(getContext(), "Editing your feedback", Toast.LENGTH_SHORT).show();
        });
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed()); // Quay lại
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void loadAllFeedbackData() {
        feedbackViewModel.getQuizStats(quizId);
        feedbackViewModel.getFeedbackList(quizId);
        feedbackViewModel.getMyFeedback();
    }

    private void observeViewModel() {
        feedbackViewModel.getQuizStats(quizId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                statsRatingBar.setRating((float) response.getData().getAverageRating());
                tvStatsAverage.setText(String.format(Locale.US, "%.1f", response.getData().getAverageRating()));
                tvStatsCount.setText("(" + response.getData().getTotalFeedback() + " reviews)");
            }
        });

        feedbackViewModel.getFeedbackList(quizId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                feedbackAdapter.submitList(response.getData());
            }
        });

        feedbackViewModel.getMyFeedback().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                Optional<Feedback> foundFeedback = response.getData().stream()
                        .filter(f -> f.getQuizId().equals(quizId))
                        .findFirst();
                if (foundFeedback.isPresent()) {
                    myFeedback = foundFeedback.get();
                    ratingBar.setRating(myFeedback.getRating());
                    etComment.setText(myFeedback.getComment());
                    btnSubmitFeedback.setText("Update Feedback");
                }
            }
        });

        feedbackViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void submitFeedback() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();
        if (rating == 0) {
            Toast.makeText(getContext(), "Please provide a rating (1-5 stars)", Toast.LENGTH_SHORT).show();
            return;
        }
        feedbackViewModel.submitFeedback(quizId, rating, comment, myFeedback)
                .observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                        resetInputForm();
                        loadAllFeedbackData();
                    } else if (response != null) {
                        Toast.makeText(getContext(), "Error: " + response.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "An unknown error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetInputForm() {
        myFeedback = null;
        ratingBar.setRating(0);
        etComment.setText("");
        btnSubmitFeedback.setText("Submit Feedback");
    }
}