package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.GoogleAuthResponse;
import com.example.learnquiz_fe.data.model.auth.IdTokenRequest;
import com.example.learnquiz_fe.data.model.auth.LoginRequestDTO;
import com.example.learnquiz_fe.data.model.auth.RegisterRequestDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.network.ApiService;

import com.example.learnquiz_fe.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final ApiService apiService;

    public AuthRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    /**
     * Attempts to log in with a Google ID token and returns a LiveData of ApiResponse<AuthResponse>.
     * @param idToken The Google ID token.
     * @return A LiveData object wrapping the API response.
     */
    public LiveData<ApiResponse<GoogleAuthResponse>> loginWithGoogle(String idToken) {
        // LiveData giờ sẽ chứa toàn bộ ApiResponse
        MutableLiveData<ApiResponse<GoogleAuthResponse>> liveData = new MutableLiveData<>();

        // Giả định rằng apiService.loginWithGoogle đã được cập nhật để trả về Call<ApiResponse<AuthResponse>>
        apiService.loginWithGoogle(new IdTokenRequest(idToken)).enqueue(new Callback<ApiResponse<GoogleAuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<GoogleAuthResponse>> call, Response<ApiResponse<GoogleAuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<GoogleAuthResponse> apiResponse = response.body();
                    // Nếu backend trả về success = true và có dữ liệu user
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        GoogleAuthResponse googleAuthData = apiResponse.getData();
                        // Lưu token nếu cần
                        if (googleAuthData.getAccessToken() != null) {
                            RetrofitClient.getInstance(null)
                                    .setAuthToken(googleAuthData.getAccessToken());
                            // Create an AuthResponse object to save user data, assuming it has a suitable constructor or setters
                            AuthResponse authData = new AuthResponse(
                                    googleAuthData.getUserResponseDto().getId(),
                                    googleAuthData.getUserResponseDto().getUsername(),
                                    googleAuthData.getUserResponseDto().getEmail(),
                                    googleAuthData.getUserResponseDto().getRole()
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
            public void onFailure(Call<ApiResponse<GoogleAuthResponse>> call, Throwable t) {
                // Lỗi mạng hoặc lỗi không xác định
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    public LiveData<ApiResponse<AuthResponse>> login(String usernameOrEmail, String password) {
        // LiveData giờ sẽ chứa toàn bộ ApiResponse
        MutableLiveData<ApiResponse<AuthResponse>> liveData = new MutableLiveData<>();

        // Giả định rằng apiService.loginWithGoogle đã được cập nhật để trả về Call<ApiResponse<AuthResponse>>
        apiService.login(new LoginRequestDTO(usernameOrEmail,password)).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    // Nếu backend trả về success = true và có dữ liệu user
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authData = apiResponse.getData();
                        // Lưu token nếu cần
                        if (authData.getAccessToken() != null) {
                            RetrofitClient.getInstance(null)
                                    .setAuthToken(authData.getAccessToken());
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
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Lỗi mạng hoặc lỗi không xác định
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    public LiveData<ApiResponse<AuthResponse>> register(RegisterRequestDTO registerRequestDTO) {
        MutableLiveData<ApiResponse<AuthResponse>> liveData = new MutableLiveData<>();

        apiService.register(registerRequestDTO).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authData = apiResponse.getData();
                        // Save token and user data upon successful registration
                        if (authData.getAccessToken() != null) {
                            RetrofitClient.getInstance(null).setAuthToken(authData.getAccessToken());
                            RetrofitClient.getInstance(null).saveAuthData(authData);
                        }
                        liveData.postValue(apiResponse);
                    } else {
                        // API call successful, but registration failed (e.g., user exists)
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Registration failed";
                        liveData.postValue(new ApiResponse<>(false, errorMessage, null));
                    }
                } else {
                    // HTTP error (e.g., 400 Bad Request, 500 Internal Server Error)
                    liveData.postValue(new ApiResponse<>(false, "Server error, please try again.", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Network error or other issues
                liveData.postValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

}
