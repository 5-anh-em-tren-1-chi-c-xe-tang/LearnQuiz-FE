package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.network.ApiService;
import com.example.learnquiz_fe.data.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Quiz-related API operations
 * Handles business logic and data transformation
 */
public class QuizRepository {
    
    private static final String TAG = "QuizRepository";
    
    private final ApiService apiService;
    private final Context context;
    
    /**
     * Constructor
     */
    public QuizRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }
    
    /**
     * Generate quiz from images
     * 
     * @param request Quiz generation request
     * @param callback Callback for success/error handling
     */
    public void generateQuiz(GenerateQuizRequest request, QuizCallback callback) {
        // Validate request
        if (!request.isValid()) {
            callback.onError("Invalid request data", 400);
            return;
        }
        
        Log.d(TAG, "Generating quiz with " + request.getImages().size() + " images");
        
        // Make API call
        Call<ApiResponse<GenerateQuizResponse>> call = apiService.generateQuiz(request);
        
        call.enqueue(new Callback<ApiResponse<GenerateQuizResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GenerateQuizResponse>> call,
                                   @NonNull Response<ApiResponse<GenerateQuizResponse>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<GenerateQuizResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Quiz generated successfully: " + apiResponse.getData().getId());
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Unknown error";
                        Log.e(TAG, "API returned error: " + errorMsg);
                        callback.onError(errorMsg, response.code());
                    }
                } else {
                    String errorMsg = handleErrorResponse(response.code());
                    Log.e(TAG, "HTTP error " + response.code() + ": " + errorMsg);
                    callback.onError(errorMsg, response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<GenerateQuizResponse>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }
    
    /**
     * Generate quiz with authentication
     * 
     * @param request Quiz generation request
     * @param token Authorization token
     * @param callback Callback for success/error handling
     */
    public void generateQuizWithAuth(GenerateQuizRequest request, String token, QuizCallback callback) {
        if (!request.isValid()) {
            callback.onError("Invalid request data", 400);
            return;
        }
        
        Call<ApiResponse<GenerateQuizResponse>> call = apiService.generateQuizWithAuth(
            "Bearer " + token, request);
        
        call.enqueue(new Callback<ApiResponse<GenerateQuizResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GenerateQuizResponse>> call,
                                   @NonNull Response<ApiResponse<GenerateQuizResponse>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<GenerateQuizResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage(), response.code());
                    }
                } else {
                    callback.onError(handleErrorResponse(response.code()), response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<GenerateQuizResponse>> call,
                                  @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }
    
    /**
     * Get quiz detail by ID
     * 
     * @param quizId Quiz ID
     * @param callback Callback for success/error handling
     */
    public void getQuizDetail(String quizId, QuizCallback callback) {
        Call<ApiResponse<GenerateQuizResponse>> call = apiService.getQuizDetail(quizId);
        
        call.enqueue(new Callback<ApiResponse<GenerateQuizResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GenerateQuizResponse>> call,
                                   @NonNull Response<ApiResponse<GenerateQuizResponse>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<GenerateQuizResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage(), response.code());
                    }
                } else {
                    callback.onError(handleErrorResponse(response.code()), response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<GenerateQuizResponse>> call,
                                  @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }

    /**
     * Get public quizzes
     * @return {@link ApiResponse<QuizResponseDTO>} API Response containing list of public quizzes
     */
    public void getPublicQuizzes(GenericQuizCallback callback) {
        Call<ApiResponse<List<QuizResponseDTO>>> call = apiService.getPublicQuizzies();

        call.enqueue(new Callback<ApiResponse<List<QuizResponseDTO>>>() {

            @Override
            public void onResponse(Call<ApiResponse<List<QuizResponseDTO>>> call, Response<ApiResponse<List<QuizResponseDTO>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<QuizResponseDTO>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Public quizzes fetched successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API returned error: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage(), response.code());
                    }
                } else {
                    String errorMsg = handleErrorResponse(response.code());
                    Log.e(TAG, "HTTP error " + response.code() + ": " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuizResponseDTO>>> call, Throwable throwable) {
                Log.e(TAG, "HTTP error " + throwable.getMessage());
                callback.onError("Network error: " + throwable.getMessage(), -1);
            }
        });
    }

    /**
     * Handle HTTP error responses
     */
    private String handleErrorResponse(int code) {
        switch (code) {
            case 400:
                return "Invalid request data. Please check your inputs.";
            case 401:
                return "Unauthorized. Please login again.";
            case 403:
                return "Access forbidden.";
            case 404:
                return "Resource not found.";
            case 500:
                return "Server error. Please try again later.";
            case 503:
                return "Service unavailable. Please try again later.";
            default:
                return "Error occurred (Code: " + code + ")";
        }
    }
    
    /**
     * Callback interface for quiz operations
     */
    public interface QuizCallback {
        void onSuccess(GenerateQuizResponse response);
        void onError(String message, int errorCode);
    }

    /**
     * Callback interface for quiz operations
     */
    public interface GenericQuizCallback {
        void onSuccess(List<QuizResponseDTO> response);
        void onError(String message, int errorCode);
    }
}
