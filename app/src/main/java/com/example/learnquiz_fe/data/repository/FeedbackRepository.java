package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learnquiz_fe.data.model.feedback.CreateFeedbackRequest;
import com.example.learnquiz_fe.data.model.feedback.Feedback;
import com.example.learnquiz_fe.data.model.feedback.QuizStats;
import com.example.learnquiz_fe.data.model.feedback.UpdateFeedbackRequest;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.network.ApiService;
import com.example.learnquiz_fe.data.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository để xử lý tất cả các hoạt động liên quan đến Feedback.
 * Giao tiếp với ApiService và trả về LiveData cho ViewModel.
 */
public class FeedbackRepository {

    private final ApiService apiService;

    public FeedbackRepository(Context context) {
        // Lấy một instance của ApiService từ RetrofitClient
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    /**
     * Lấy thống kê đánh giá cho một quiz (rating trung bình và tổng số).
     * @param quizId ID của quiz.
     * @return LiveData chứa ApiResponse với QuizStats.
     */
    public LiveData<ApiResponse<QuizStats>> getQuizRatingStats(String quizId) {
        MutableLiveData<ApiResponse<QuizStats>> liveData = new MutableLiveData<>();

        apiService.getQuizRatingStats(quizId).enqueue(new Callback<ApiResponse<QuizStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuizStats>> call, Response<ApiResponse<QuizStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    liveData.postValue(new ApiResponse<>(false, "Server error retrieving stats", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<QuizStats>> call, Throwable t) {
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    /**
     * Lấy tất cả feedback cho một quiz cụ thể.
     * @param quizId ID của quiz.
     * @return LiveData chứa ApiResponse với một danh sách Feedback.
     */
    public LiveData<ApiResponse<List<Feedback>>> getFeedbackByQuiz(String quizId) {
        MutableLiveData<ApiResponse<List<Feedback>>> liveData = new MutableLiveData<>();

        apiService.getFeedbackByQuiz(quizId).enqueue(new Callback<ApiResponse<List<Feedback>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Feedback>>> call, Response<ApiResponse<List<Feedback>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    liveData.postValue(new ApiResponse<>(false, "Server error getting feedback", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Feedback>>> call, Throwable t) {
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    /**
     * Lấy tất cả feedback của người dùng hiện tại (yêu cầu xác thực).
     * @return LiveData chứa ApiResponse với một danh sách Feedback.
     */
    public LiveData<ApiResponse<List<Feedback>>> getMyFeedback() {
        MutableLiveData<ApiResponse<List<Feedback>>> liveData = new MutableLiveData<>();

        apiService.getMyFeedback().enqueue(new Callback<ApiResponse<List<Feedback>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Feedback>>> call, Response<ApiResponse<List<Feedback>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    // Có thể là lỗi 401 Unauthorized
                    liveData.postValue(new ApiResponse<>(false, "Error fetching user feedback", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Feedback>>> call, Throwable t) {
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    /**
     * Gửi một feedback mới (yêu cầu xác thực).
     * @param request Đối tượng chứa thông tin feedback mới.
     * @return LiveData chứa ApiResponse với Feedback vừa được tạo.
     */
    public LiveData<ApiResponse<Feedback>> createFeedback(CreateFeedbackRequest request) {
        MutableLiveData<ApiResponse<Feedback>> liveData = new MutableLiveData<>();

        apiService.createFeedback(request).enqueue(new Callback<ApiResponse<Feedback>>() {
            @Override
            public void onResponse(Call<ApiResponse<Feedback>> call, Response<ApiResponse<Feedback>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API trả về thành công (ví dụ: 201 Created)
                    liveData.postValue(response.body());
                } else {
                    // Lỗi (ví dụ: 400 Bad Request, 404 Not Found, 401 Unauthorized)
                    liveData.postValue(new ApiResponse<>(false, "Error creating feedback", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Feedback>> call, Throwable t) {
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    /**
     * Cập nhật một feedback đã có (yêu cầu xác thực).
     * @param feedbackId ID của feedback cần cập nhật.
     * @param request Đối tượng chứa thông tin cập nhật.
     * @return LiveData chứa ApiResponse với Feedback đã được cập nhật.
     */
    public LiveData<ApiResponse<Feedback>> updateFeedback(String feedbackId, UpdateFeedbackRequest request) {
        MutableLiveData<ApiResponse<Feedback>> liveData = new MutableLiveData<>();

        apiService.updateFeedback(feedbackId, request).enqueue(new Callback<ApiResponse<Feedback>>() {
            @Override
            public void onResponse(Call<ApiResponse<Feedback>> call, Response<ApiResponse<Feedback>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    // Lỗi (ví dụ: 403 Forbidden, 404 Not Found)
                    liveData.postValue(new ApiResponse<>(false, "Error updating feedback", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Feedback>> call, Throwable t) {
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    /**
     * Xóa một feedback (yêu cầu xác thực).
     * @param feedbackId ID của feedback cần xóa.
     * @return LiveData chứa ApiResponse (data có thể là null hoặc Object).
     */
    public LiveData<ApiResponse<Object>> deleteFeedback(String feedbackId) {
        MutableLiveData<ApiResponse<Object>> liveData = new MutableLiveData<>();

        apiService.deleteFeedback(feedbackId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    liveData.postValue(new ApiResponse<>(false, "Error deleting feedback", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }
}