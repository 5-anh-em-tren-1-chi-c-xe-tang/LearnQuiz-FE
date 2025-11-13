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
import com.example.learnquiz_fe.helpers.LoginPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private LoginViewModel loginViewModel;

    // Preferences wrapper
    private LoginPreferences loginPrefs;

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
        initGoogleSignIn();
        loginPrefs = new LoginPreferences(this);
        setupViewModel();
        setupListeners();
        checkRememberMe();
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
        btnSignIn.setOnClickListener(v -> handleNormalLogin());
        tvSignUp.setOnClickListener(v -> navigateToSignup());
        btnGGLogin.setOnClickListener(v -> startGoogleLogin());
    }

    /**
     * Auto-fill fields if Remember Me is enabled
     */
    private void checkRememberMe() {
        if (loginPrefs.isRemembered()) {
            etEmail.setText(loginPrefs.getSavedEmail());
            etPassword.setText(loginPrefs.getSavedPassword());
            cbRememberMe.setChecked(true);

            // Auto-login (optional)
            // handleNormalLogin();
        }
    }

    /**
     * Regular email-password login
     */
    private void handleNormalLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tvError.setVisibility(View.GONE);

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
            showLoading(false);
            if (response.isSuccess() && response.getData() != null) {

                Toast.makeText(this, "Welcome " + response.getData().getUsername(), Toast.LENGTH_SHORT).show();

                if (cbRememberMe.isChecked()) {
                    loginPrefs.saveCredentials(email, password);
                } else {
                    loginPrefs.clear();
                }

                navigateToMain();
            } else {
                tvError.setText("Invalid username or password.");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Google login flow
     */
    private void startGoogleLogin() {
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == RC_SIGN_IN) handleGoogleLoginResult(data);
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
        showLoading(true);

        loginViewModel.loginWithGoogle(idToken).observe(this, response -> {
            showLoading(false);

            if (response.isSuccess() && response.getData() != null) {
                Toast.makeText(this, "Welcome " + response.getData().getUserResponseDto().username, Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        btnSignIn.setEnabled(!show);
        btnGGLogin.setEnabled(!show);
        tvSignUp.setEnabled(!show);
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void navigateToSignup() {
        showLoading(true);
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }
}
