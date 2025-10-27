package com.example.learnquiz_fe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.learnquiz_fe.data.model.User;
import com.example.learnquiz_fe.data.repository.AuthRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for Login screen
 * Handles authentication logic and communicates with AuthRepository
 */
public class LoginViewModel extends ViewModel {
    
    private final AuthRepository authRepository;
    private final ExecutorService executorService;
    
    // LiveData for observing authentication state
    private final MutableLiveData<User> authenticatedUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    public LoginViewModel() {
        this.authRepository = new AuthRepository();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Attempt to login with provided credentials
     * @param usernameOrEmail Username or email
     * @param password Password
     */
    public void login(String usernameOrEmail, String password) {
        // Validate inputs
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            errorMessage.setValue("Username or email is required");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }
        
        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }
        
        // Set loading state
        isLoading.setValue(true);
        
        // Perform login on background thread
        executorService.execute(() -> {
            User user = authRepository.login(usernameOrEmail.trim(), password);
            
            // Update LiveData on main thread
            if (user != null) {
                authenticatedUser.postValue(user);
                errorMessage.postValue(null);
            } else {
                authenticatedUser.postValue(null);
                errorMessage.postValue("Invalid username/email or password");
            }
            
            isLoading.postValue(false);
        });
    }

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid
     */
    public boolean isValidEmail(String email) {
        return authRepository.isValidEmail(email);
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    // Getters for LiveData
    public LiveData<User> getAuthenticatedUser() {
        return authenticatedUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
