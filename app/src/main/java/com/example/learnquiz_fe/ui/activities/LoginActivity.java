package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnquiz_fe.BuildConfig;
import com.example.learnquiz_fe.MainActivity;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.ui.viewmodel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import android.content.SharedPreferences;


/**
 * Login Activity
 * Handles user authentication with email/username and password
 * Navigates to HomeActivity on successful login
 */
public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    // Hardcoded test credentials
    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "admin";
    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private SharedPreferences preferences;

    private GoogleSignInClient googleSignInClient;
    private LoginViewModel loginViewModel;

    // UI Components
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSignIn;
    private ProgressBar progressBar;
    private TextView tvError;
    private MaterialButton btnGGLogin;
    private TextView tvSignUp;
    private MaterialCheckBox cbRememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        checkRememberMe();
        initGoogleSignIn();
        setupViewModel();
        setupListeners();
    }

    private void checkRememberMe() {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

// Check if user previously chose "Remember Me"
        boolean isRemembered = preferences.getBoolean(KEY_REMEMBER, false);

        if (isRemembered) {
            String savedEmail = preferences.getString(KEY_EMAIL, "");
            String savedPassword = preferences.getString(KEY_PASSWORD, "");

            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);
            cbRememberMe.setChecked(true);
//             handleNormalLogin();  // Uncomment if you want instant login
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
        btnGGLogin = findViewById(R.id.btn_login_google);
        tvSignUp = findViewById(R.id.tv_sign_up);
        cbRememberMe = findViewById(R.id.cb_remember_me);
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupViewModel() {
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    private void setupListeners() {
        // Email/Password login
        btnSignIn.setOnClickListener(v -> handleNormalLogin());
        tvSignUp.setOnClickListener(v -> navigateToSignup());
        // Google login
        btnGGLogin.setOnClickListener(v -> startGoogleLogin());
    }

    /**
     * Handle email/password login with hardcoded credentials for testing
     */
    private void handleNormalLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Hide error
        tvError.setVisibility(View.GONE);

        // Validate input
        if (email.isEmpty()) {
            tvError.setText("Please enter email/username");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.isEmpty()) {
            tvError.setText("Please enter password");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        showLoading(true);
        loginViewModel.login(email, password).observe(this, response -> {
            if (response.isSuccess() && response.getData() != null) {
                var userData = response.getData();
                showLoading(false);
                Toast.makeText(this, "Welcome " + response.getData().getUsername(), Toast.LENGTH_SHORT).show();
                saveRememberMeState(email, password);
                navigateToMain();
            } else {
                showLoading(false);
                tvError.setText("Invalid username or password.");
                tvError.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Remember me
    private void saveRememberMeState(String email, String password) {
        if (cbRememberMe.isChecked()) {
            preferences.edit()
                    .putString(KEY_EMAIL, email)
                    .putString(KEY_PASSWORD, password)
                    .putBoolean(KEY_REMEMBER, true)
                    .apply();
        } else {
            preferences.edit().clear().apply();
        }
    }

    private void handleEmailPasswordLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Hide error
        tvError.setVisibility(View.GONE);

        // Validate input
        if (email.isEmpty()) {
            tvError.setText("Please enter email/username");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.isEmpty()) {
            tvError.setText("Please enter password");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        // Show loading
        showLoading(true);

        // Check hardcoded credentials
        if (email.equals(TEST_USERNAME) && password.equals(TEST_PASSWORD)) {
            // Login successful
            new android.os.Handler().postDelayed(() -> {
                showLoading(false);
                Toast.makeText(this, "Welcome " + TEST_USERNAME + "!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }, 500); // Simulate network delay
        } else {
            // Login failed
            new android.os.Handler().postDelayed(() -> {
                showLoading(false);
                tvError.setText("Invalid username or password. Try: admin/admin");
                tvError.setVisibility(View.VISIBLE);
            }, 500);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        btnSignIn.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        btnGGLogin.setEnabled(!show); // Thêm dòng này để vô hiệu hóa nút Google Login
        tvSignUp.setEnabled(!show); // Thêm dòng này để vô hiệu hóa nút Sign Up
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startGoogleLogin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) handleGoogleLoginResult(data);
    }

    private void handleGoogleLoginResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) loginWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithGoogle(String idToken) {
        loginViewModel.loginWithGoogle(idToken).observe(this, response -> {
            if (response.isSuccess() && response.getData() != null) {
                Toast.makeText(this, "Welcome " + response.getData().getUserResponseDto().username, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSignup() {
        showLoading(true);

        Intent intent = new Intent(this, RegisterActivity.class);
        // Xóa các activity trước đó khỏi stack để người dùng không thể quay lại màn hình đăng ký
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}