package com.example.learnquiz_fe.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learnquiz_fe.data.model.User;
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.LoginRequestDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.user.UserRequestDTO;
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

}
