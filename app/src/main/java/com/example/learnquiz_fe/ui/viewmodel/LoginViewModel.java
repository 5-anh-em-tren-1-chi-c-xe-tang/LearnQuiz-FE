package com.example.learnquiz_fe.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.GoogleAuthResponse;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.repository.AuthRepository;

/**
 * ViewModel for Login screen
 * Handles authentication logic and communicates with AuthRepository
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository repository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<ApiResponse<GoogleAuthResponse>> loginWithGoogle(String idToken) {
        return repository.loginWithGoogle(idToken);
    }
    public LiveData<ApiResponse<AuthResponse>> login(String usernameOrEmail, String password) {
        return repository.login(usernameOrEmail,password);
    }
}

