package com.example.learnquiz_fe.ui.viewmodel; // Sửa lại package nếu cần

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learnquiz_fe.data.model.feedback.CreateFeedbackRequest;
import com.example.learnquiz_fe.data.model.feedback.Feedback;
import com.example.learnquiz_fe.data.model.feedback.QuizStats;
import com.example.learnquiz_fe.data.model.feedback.UpdateFeedbackRequest;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.repository.FeedbackRepository;

import java.util.List;

public class FeedbackViewModel extends AndroidViewModel {

    private final FeedbackRepository feedbackRepository;

    // LiveData cho các sự kiện (ví dụ: hiển thị Toast)
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public FeedbackViewModel(@NonNull Application application) {
        super(application);
        feedbackRepository = new FeedbackRepository(application);
    }

    // Lấy LiveData cho thông báo
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    // Lấy thống kê quiz (stats)
    public LiveData<ApiResponse<QuizStats>> getQuizStats(String quizId) {
        return feedbackRepository.getQuizRatingStats(quizId);
    }

    // Lấy danh sách bình luận
    public LiveData<ApiResponse<List<Feedback>>> getFeedbackList(String quizId) {
        return feedbackRepository.getFeedbackByQuiz(quizId);
    }

    // Lấy bình luận của riêng tôi
    public LiveData<ApiResponse<List<Feedback>>> getMyFeedback() {
        return feedbackRepository.getMyFeedback();
    }

    // Gửi hoặc Cập nhật Feedback
    public LiveData<ApiResponse<Feedback>> submitFeedback(String quizId, int rating, String comment, Feedback existingFeedback) {
        if (existingFeedback != null) {
            // Đây là CẬP NHẬT
            UpdateFeedbackRequest request = new UpdateFeedbackRequest(rating, comment);
            return feedbackRepository.updateFeedback(existingFeedback.getId(), request);
        } else {
            // Đây là TẠO MỚI
            CreateFeedbackRequest request = new CreateFeedbackRequest(rating, comment, quizId);
            return feedbackRepository.createFeedback(request);
        }
    }

    // Xóa Feedback
    public LiveData<ApiResponse<Object>> deleteFeedback(String feedbackId) {
        return feedbackRepository.deleteFeedback(feedbackId);
    }
}