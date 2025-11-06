package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizRequestDTO;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizResponseDTO;
import com.example.learnquiz_fe.data.network.ApiService;
import com.example.learnquiz_fe.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for quiz history operations
 * Handles quiz submission and result retrieval
 */
public class QuizHistoryRepository {
    
    private static final String TAG = "QuizHistoryRepository";
    private final ApiService apiService;
    private final RetrofitClient retrofitClient;
    
    /**
     * Constructor
     * @param context Application context
     */
    public QuizHistoryRepository(Context context) {
        retrofitClient = RetrofitClient.getInstance(context);
        apiService = retrofitClient.getApiService();
    }
    
    /**
     * Submit quiz answers to server
     * @param request Quiz submission request
     * @param callback Result callback
     */
    public void submitQuiz(@NonNull SubmitQuizRequestDTO request, 
                          @NonNull QuizSubmissionCallback callback) {
        
        // Validate request
        if (!request.isValid()) {
            callback.onError("Invalid quiz submission data");
            return;
        }
        
        Log.d(TAG, "Submitting quiz: " + request.getQuizId() 
            + " with " + request.getAnswers().size() + " answers");
        
        // Make API call (response wrapped in ApiResponse)
        Call<ApiResponse<SubmitQuizResponseDTO>> call = apiService.submitQuiz(request);
        call.enqueue(new Callback<ApiResponse<SubmitQuizResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SubmitQuizResponseDTO>> call,
                                 @NonNull Response<ApiResponse<SubmitQuizResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<SubmitQuizResponseDTO> apiResponse = response.body();
                    
                    // Check if API call was successful
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        SubmitQuizResponseDTO data = apiResponse.getData();
                        Log.d(TAG, "Quiz submitted successfully. Score: " + data.getScore() 
                            + ", Total: " + data.getTotalQuestions() 
                            + ", Correct: " + data.getCorrectCount());
                        callback.onSuccess(data);
                    } else {
                        String errorMsg = apiResponse.getMessage() != null 
                            ? apiResponse.getMessage() 
                            : "Quiz submission failed";
                        Log.e(TAG, "API returned error: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "Quiz submission failed";
                    if (response.code() == 400) {
                        errorMsg = "Invalid quiz data. Please try again.";
                    } else if (response.code() >= 500) {
                        errorMsg = "Server error. Please try again later.";
                    }
                    Log.e(TAG, "Quiz submission error: " + response.code() 
                        + " - " + response.message());
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<SubmitQuizResponseDTO>> call, 
                                @NonNull Throwable t) {
                Log.e(TAG, "Network error during quiz submission", t);
                String errorMsg = "Network error. Please check your connection.";
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMsg = "Request timeout. Please try again.";
                }
                callback.onError(errorMsg);
            }
        });
    }
    
    /**
     * Callback interface for quiz submission
     */
    public interface QuizSubmissionCallback {
        /**
         * Called when quiz is successfully submitted
         * @param response Server response with score and results
         */
        void onSuccess(SubmitQuizResponseDTO response);
        
        /**
         * Called when quiz submission fails
         * @param errorMessage Error message
         */
        void onError(String errorMessage);
    }
}
