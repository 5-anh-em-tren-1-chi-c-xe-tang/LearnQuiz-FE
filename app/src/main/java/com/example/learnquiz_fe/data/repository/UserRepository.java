package com.example.learnquiz_fe.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learnquiz_fe.data.model.User;
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.user.UserRequestDTO;
import com.example.learnquiz_fe.data.model.auth.LoginRequestDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;

import com.example.learnquiz_fe.data.network.ApiService;
import com.example.learnquiz_fe.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final ApiService apiService;

    public UserRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    public LiveData<ApiResponse<User>> updateProfile(UserRequestDTO userRequestDTO) {
        MutableLiveData<ApiResponse<User>> liveData = new MutableLiveData<>();

        apiService.updateProfile(userRequestDTO).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    // Nếu backend trả về success = true và có dữ liệu user
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        // Lưu token nếu cần
                        if (user != null) {
                            AuthResponse authData = new AuthResponse(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getEmail(),
                                    user.getRole()
                            );
                            RetrofitClient.getInstance(null).saveAuthData(authData);
                        }
                        liveData.postValue(apiResponse);
                    } else {
                        // API trả về thành công nhưng logic thất bại (success=false)
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Invalid response from server";
                        liveData.postValue(new ApiResponse<>(false, errorMessage, null));
                    }
                } else {
                    // Lỗi HTTP (ví dụ: 404, 500)
                    liveData.postValue(new ApiResponse<>(false, "Server error, please try again.", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Lỗi mạng hoặc lỗi không xác định
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }
    public void deleteQuiz(String quizId, QuizRepository.GenericCallback<Object> callback) {
        Call<ApiResponse<Object>> call = apiService.deleteQuiz(quizId);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        callback.onSuccess(response.body().getData());
                    } else {
                        callback.onError(response.body().getMessage(), response.code());
                    }
                } else {
                    callback.onError(handleErrorResponse(response.code()), response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }

    /**
     * Update a quiz
     * Note: ApiService yêu cầu body là GenerateQuizResponse
     */
    public void updateQuiz(String quizId, GenerateQuizResponse request, QuizRepository.GenericCallback<Object> callback) {
        Call<ApiResponse<Object>> call = apiService.updateQuiz(quizId, request);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        callback.onSuccess(response.body().getData());
                    } else {
                        callback.onError(response.body().getMessage(), response.code());
                    }
                } else {
                    callback.onError(handleErrorResponse(response.code()), response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }
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


}
