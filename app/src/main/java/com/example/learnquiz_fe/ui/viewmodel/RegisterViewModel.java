package com.example.learnquiz_fe.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.RegisterRequestDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.repository.AuthRepository;

public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository repository;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<ApiResponse<AuthResponse>> register(String username, String email, String password) {
        var request = new RegisterRequestDTO(username, email, password);
        return repository.register(request);
    }
}