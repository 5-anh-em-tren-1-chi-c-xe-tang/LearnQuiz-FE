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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    
    private GoogleSignInClient googleSignInClient;
    private LoginViewModel loginViewModel;
    
    // UI Components
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSignIn;
    private ProgressBar progressBar;
    private TextView tvError;
    private MaterialButton btnGGLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initGoogleSignIn();
        setupViewModel();
        setupListeners();
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
        btnGGLogin = findViewById(R.id.btn_login_google);
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
        
        // Google login
        btnGGLogin.setOnClickListener(v -> startGoogleLogin());
    }
    
    /**
     * Handle email/password login with hardcoded credentials for testing
     */
    private void handleNormalLogin(){
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
        loginViewModel.login(email,password).observe(this, response -> {
            if (response.isSuccess() && response.getData() != null) {
                showLoading(false);
                Toast.makeText(this, "Welcome " + response.getData().getUsername(), Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                showLoading(false);
                tvError.setText("Invalid username or password.");
                tvError.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
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
                navigateToHome();
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
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
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
}


