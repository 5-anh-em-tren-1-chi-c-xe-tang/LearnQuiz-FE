package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.ui.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Login Activity
 * Handles user authentication with email/username and password
 * Navigates to HomeActivity on successful login
 */
public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvForgotPassword;

    // ViewModel
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI components
        initializeViews();

        // Setup listeners
        setupListeners();

        // Observe ViewModel LiveData
        observeViewModel();
    }

    /**
     * Initialize all view references
     */
    private void initializeViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    /**
     * Setup click and text change listeners
     */
    private void setupListeners() {
        // Login button click
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Clear errors when user starts typing
        etUsername.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilUsername.setError(null);
                tvError.setVisibility(View.GONE);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                tvError.setVisibility(View.GONE);
            }
        });

        // Forgot password click
        tvForgotPassword.setOnClickListener(v -> 
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Observe ViewModel LiveData for state changes
     */
    private void observeViewModel() {
        // Observe authenticated user
        loginViewModel.getAuthenticatedUser().observe(this, user -> {
            if (user != null && user.isAuthenticated()) {
                // Login successful - navigate to HomeActivity
                Toast.makeText(this, "Welcome, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });

        // Observe error messages
        loginViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                tvError.setText(errorMessage);
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        // Observe loading state
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });
    }

    /**
     * Attempt to login with entered credentials
     */
    private void attemptLogin() {
        // Clear previous errors
        tilUsername.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        // Get input values
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Validate inputs
        boolean isValid = true;

        if (username.isEmpty()) {
            tilUsername.setError("Username or email is required");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (isValid) {
            // Call ViewModel to perform login
            loginViewModel.login(username, password);
        }
    }

    /**
     * Show loading indicator
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnLogin.setText("");
    }

    /**
     * Hide loading indicator
     */
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        btnLogin.setText(R.string.login_button);
    }

    /**
     * Navigate to Home Activity
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }

    /**
     * Simple TextWatcher implementation to reduce boilerplate
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
