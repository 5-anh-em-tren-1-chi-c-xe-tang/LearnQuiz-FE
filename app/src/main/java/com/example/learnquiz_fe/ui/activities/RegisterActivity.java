package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.ui.viewmodel.RegisterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;

    // UI Components
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvSignIn; // Link để quay lại trang đăng nhập

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
        tvSignIn = findViewById(R.id.tv_sign_in);
    }

    private void setupViewModel() {
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        // Thêm listener cho text "Sign in"
        tvSignIn.setOnClickListener(v -> {
            // Kết thúc activity hiện tại để quay lại màn hình trước đó (LoginActivity)
            finish();
        });
    }

    private void handleRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Ẩn thông báo lỗi cũ
        tvError.setVisibility(View.GONE);

        // Kiểm tra dữ liệu đầu vào
        if (username.isEmpty()) {
            tvError.setText("Please enter a username");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (email.isEmpty()) {
            tvError.setText("Please enter an email");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.isEmpty()) {
            tvError.setText("Please enter a password");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.length() < 6) {
            tvError.setText("Password must be at least 6 characters");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        showLoading(true);

        // Gọi phương thức register từ ViewModel
        registerViewModel.register(username, email, password).observe(this, response -> {
            showLoading(false);
            if (response.isSuccess() && response.getData() != null) {
                // Đăng ký thành công
                Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show();
                navigateToLogin();
            } else {
                // Đăng ký thất bại
                String errorMessage = response.getMessage() != null ? response.getMessage() : "Registration failed. Please try again.";
                tvError.setText(errorMessage);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        btnRegister.setEnabled(!show);
        etUsername.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private void navigateToLogin() {
        // Quay trở lại màn hình Login sau khi đăng ký thành công
        Intent intent = new Intent(this, LoginActivity.class);
        // Xóa các activity trước đó khỏi stack để người dùng không thể quay lại màn hình đăng ký
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}