package com.example.learnquiz_fe.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.learnquiz_fe.data.model.User;
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.repository.AuthRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public LiveData<ApiResponse<AuthResponse>> loginWithGoogle(String idToken) {
        return repository.loginWithGoogle(idToken);
    }
}

