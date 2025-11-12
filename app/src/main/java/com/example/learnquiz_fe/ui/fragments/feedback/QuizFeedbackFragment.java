package com.example.learnquiz_fe.ui.fragments.feedback; // Sửa lại package nếu cần

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
import com.example.learnquiz_fe.ui.adapter.feedback.FeedbackAdapter;
import com.example.learnquiz_fe.ui.viewmodel.FeedbackViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Optional;

// TODO: Giả định bạn có 1 class để quản lý session/token
// import com.example.learnquiz_fe.util.SessionManager;

public class QuizFeedbackFragment extends Fragment {

    // ViewModel
    private FeedbackViewModel feedbackViewModel;

    // UI Views
    private ImageButton btnBack;
    private RatingBar statsRatingBar;
    private TextView tvStatsAverage, tvStatsCount;
    private RatingBar ratingBar; // Input
    private TextInputEditText etComment;
    private MaterialButton btnSubmitFeedback;
    private RecyclerView feedbackRecyclerView;

    // Adapter
    private FeedbackAdapter feedbackAdapter;

    // State
    private String quizId;
    private String currentUserId; // Lấy từ session
    private String userToken;     // Lấy từ session
    private Feedback myFeedback; // Lưu lại feedback của mình nếu có

    /**
     * Cách tốt nhất để tạo fragment với tham số
     */
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

        // Lấy quizId từ arguments
        if (getArguments() != null) {
            quizId = getArguments().getString("QUIZ_ID");
        }

        // TODO: Lấy thông tin người dùng từ Session/SharedPreferences
        // currentUserId = SessionManager.getInstance(getContext()).getUserId();
        // userToken = SessionManager.getInstance(getContext()).getToken();

        // --- DÙNG DỮ LIỆU TẠM ĐỂ TEST ---
        currentUserId = "69120ee8ea0cf3582cd7b82d"; // !!! THAY THẾ BẰNG ID THẬT
        userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1laWQiOiI2OTEyMGVlOGVhMGNmMzU4MmNkN2I4MmQiLCJ1bmlxdWVfbmFtZSI6Imh1eWRlcHRyYWkiLCJlbWFpbCI6Imh1eUBleGFtcGxlLmNvbSIsInJvbGUiOiJVc2VyIiwianRpIjoiODk2NjIyYmQtZWU4YS00YjQ0LTgwZTMtYjcxMmNjZWNiYWNiIiwibmJmIjoxNzYyNzkzOTczLCJleHAiOjE3NjI3OTU3NzMsImlhdCI6MTc2Mjc5Mzk3MywiaXNzIjoiTGVhcm5TbmFwQXBpIiwiYXVkIjoiTGVhcm5TbmFwQ2xpZW50In0.tV_Mq74kCZKcWnpbH1DPPwLE7oSaCizamiuQV-itHRs";     // !!! THAY THẾ BẰNG TOKEN THẬT
        // ------------------------------
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_quiz_feedback, container, false);
        bindViews(view); // Gán các view từ layout
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        feedbackViewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        // Tải dữ liệu lần đầu
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
        btnSubmitFeedback = view.findViewById(R.id.btnSubmitFeedback);
        feedbackRecyclerView = view.findViewById(R.id.feedbackRecyclerView);
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter với click listener cho nút Edit
        feedbackAdapter = new FeedbackAdapter(currentUserId, (feedback) -> {
            // Khi người dùng nhấn "Edit"
            myFeedback = feedback; // Đặt feedback này làm feedback đang sửa
            ratingBar.setRating(feedback.getRating());
            etComment.setText(feedback.getComment());
            btnSubmitFeedback.setText("Update Feedback");
            Toast.makeText(getContext(), "Editing your feedback", Toast.LENGTH_SHORT).show();
        });

        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }

    private void setupClickListeners() {
        // Nút Back (giống như template của bạn)
        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Nút Gửi / Cập nhật Feedback
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void loadAllFeedbackData() {
        feedbackViewModel.getQuizStats(quizId);
        feedbackViewModel.getFeedbackList(quizId);
        feedbackViewModel.getMyFeedback(userToken); // Lấy cả feedback của riêng mình
    }

    /**
     * Lắng nghe mọi thay đổi từ ViewModel
     */
    private void observeViewModel() {
        // 1. Lắng nghe thay đổi của Stats
        feedbackViewModel.getQuizStats(quizId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                statsRatingBar.setRating((float) response.getData().getAverageRating());
                tvStatsAverage.setText(String.format(Locale.US, "%.1f", response.getData().getAverageRating()));
                tvStatsCount.setText("(" + response.getData().getTotalFeedback() + " reviews)");
            }
        });

        // 2. Lắng nghe thay đổi của danh sách bình luận
        feedbackViewModel.getFeedbackList(quizId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                feedbackAdapter.submitList(response.getData());
            }
        });

        // 3. Lắng nghe feedback của riêng mình
        feedbackViewModel.getMyFeedback(userToken).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                // Tìm xem feedback cho quizId này có tồn tại không
                Optional<Feedback> foundFeedback = response.getData().stream()
                        .filter(f -> f.getQuizId().equals(quizId))
                        .findFirst();

                if (foundFeedback.isPresent()) {
                    myFeedback = foundFeedback.get();
                    // Nếu đã có, điền vào form
                    ratingBar.setRating(myFeedback.getRating());
                    etComment.setText(myFeedback.getComment());
                    btnSubmitFeedback.setText("Update Feedback");
                }
            }
        });

        // 4. Lắng nghe thông báo
        feedbackViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Gửi hoặc cập nhật feedback
     */
    private void submitFeedback() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(getContext(), "Please provide a rating (1-5 stars)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi ViewModel để xử lý
        feedbackViewModel.submitFeedback(userToken, quizId, rating, comment, myFeedback)
                .observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();

                        // Reset form và tải lại dữ liệu
                        resetInputForm();
                        loadAllFeedbackData(); // Tải lại mọi thứ

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