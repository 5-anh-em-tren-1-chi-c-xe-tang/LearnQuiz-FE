package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.camera.CapturedImage;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.ui.adapter.PhotoThumbnailAdapter;
import com.example.learnquiz_fe.ui.viewmodel.PhotoSessionViewModel;
import com.example.learnquiz_fe.ui.viewmodel.QuizGenerationViewModel;
import com.example.learnquiz_fe.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Activity for quiz generation with photo gallery and settings
 */
public class QuizGenerationActivity extends AppCompatActivity {
    
    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvPhotos;
    private MaterialButton btnAddPhoto;
    private AutoCompleteTextView actvLanguage;
    private Slider sliderQuestionCount;
    private TextView tvQuestionCountValue;
    private RadioGroup rgVisibility;
    private TextInputEditText etTimeLimit;
    private MaterialButton btnGenerate;
    private FrameLayout loadingOverlay;
    private TextView tvLoadingText;
    
    // Adapter
    private PhotoThumbnailAdapter photoAdapter;
    
    // ViewModels
    private PhotoSessionViewModel sessionViewModel;
    private QuizGenerationViewModel quizViewModel;
    
    // Language mapping
    private String[] languages;
    private String[] languageCodes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_generation);
        
        initViews();
        initViewModels();
        setupLanguageDropdown();
        setupPhotoGallery();
        setupListeners();
        observeData();
        setupBackPressHandler();
    }
    
    private void setupBackPressHandler() {
        // Modern back handling for Android 13+ (SDK 36)
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Warn about losing progress
                if (sessionViewModel.getImageCount().getValue() != null && 
                    sessionViewModel.getImageCount().getValue() > 0) {
                    new AlertDialog.Builder(QuizGenerationActivity.this)
                            .setTitle(R.string.generation_exit_title)
                            .setMessage(R.string.generation_exit_message)
                            .setPositiveButton(R.string.generation_exit_confirm, (dialog, which) -> {
                                sessionViewModel.clearSession();
                                setEnabled(false);
                                getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvPhotos = findViewById(R.id.rvPhotos);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        actvLanguage = findViewById(R.id.actvLanguage);
        sliderQuestionCount = findViewById(R.id.sliderQuestionCount);
        tvQuestionCountValue = findViewById(R.id.tvQuestionCountValue);
        rgVisibility = findViewById(R.id.rgVisibility);
        etTimeLimit = findViewById(R.id.etTimeLimit);
        btnGenerate = findViewById(R.id.btnGenerate);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        
        setSupportActionBar(toolbar);
    }
    
    private void initViewModels() {
        sessionViewModel = new ViewModelProvider(this).get(PhotoSessionViewModel.class);
        quizViewModel = new ViewModelProvider(this).get(QuizGenerationViewModel.class);
    }
    
    private void setupLanguageDropdown() {
        // Setup language options
        languages = new String[]{
            getString(R.string.generation_language_vi),
            getString(R.string.generation_language_en)
        };
        languageCodes = new String[]{"vi", "en"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            languages
        );
        actvLanguage.setAdapter(adapter);
        actvLanguage.setText(languages[0], false); // Default to Vietnamese
    }
    
    private void setupPhotoGallery() {
        photoAdapter = new PhotoThumbnailAdapter(new PhotoThumbnailAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(CapturedImage image, int position) {
                // Could show full-size preview in dialog
                showImagePreview(image);
            }
            
            @Override
            public void onDeleteClick(CapturedImage image, int position) {
                showDeleteConfirmation(image, position);
            }
        });
        
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, 
            LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);
    }
    
    private void setupListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Add photo button
        btnAddPhoto.setOnClickListener(v -> navigateToCamera());
        
        // Question count slider
        sliderQuestionCount.addOnChangeListener((slider, value, fromUser) -> {
            tvQuestionCountValue.setText(String.valueOf((int) value));
        });
        
        // Generate button
        btnGenerate.setOnClickListener(v -> validateAndGenerate());
    }
    
    private void observeData() {
        // Observe photo session
        sessionViewModel.getImages().observe(this, images -> {
            if (images != null) {
                photoAdapter.setImages(images);
                updateGenerateButtonState(images.size());
            }
        });
        
        // Observe quiz generation result
        quizViewModel.getQuizResult().observe(this, resource -> {
            if (resource == null) return;
            
            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;
                    
                case SUCCESS:
                    showLoading(false);
                    handleSuccess(resource.getData());
                    break;
                    
                case ERROR:
                    showLoading(false);
                    handleError(resource.getMessage());
                    break;
            }
        });
    }
    
    private void updateGenerateButtonState(int imageCount) {
        // Require at least 1 image
        btnGenerate.setEnabled(imageCount > 0);
        
        // Update add photo button text
        if (imageCount >= Constants.MAX_IMAGES_PER_SESSION) {
            btnAddPhoto.setEnabled(false);
            btnAddPhoto.setText(R.string.generation_max_photos_reached);
        } else {
            btnAddPhoto.setEnabled(true);
            btnAddPhoto.setText(R.string.generation_add_photo);
        }
    }
    
    private void navigateToCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
    
    private void showImagePreview(CapturedImage image) {
        // Simple preview dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.generation_photo_preview)
                .setMessage(String.format("Captured: %s\nFrom Camera: %s", 
                    new java.text.SimpleDateFormat("HH:mm:ss", 
                        java.util.Locale.getDefault()).format(new java.util.Date(image.getTimestamp())),
                    image.isFromCamera() ? "Yes" : "No"))
                .setPositiveButton(R.string.ok, null)
                .show();
    }
    
    private void showDeleteConfirmation(CapturedImage image, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.generation_delete_photo)
                .setMessage(R.string.generation_delete_confirm)
                .setPositiveButton(R.string.generation_delete, (dialog, which) -> {
                    sessionViewModel.removeImage(image.getId());
                    photoAdapter.removeImage(position);
                    Toast.makeText(this, R.string.generation_photo_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void validateAndGenerate() {
        // Get images
        List<CapturedImage> images = sessionViewModel.getImages().getValue();
        if (images == null || images.isEmpty()) {
            Toast.makeText(this, R.string.generation_error_no_photos, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get language
        int languagePosition = 0;
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(actvLanguage.getText().toString())) {
                languagePosition = i;
                break;
            }
        }
        String languageCode = languageCodes[languagePosition];
        
        // Get question count
        int questionCount = (int) sliderQuestionCount.getValue();
        
        // Get visibility
        int selectedVisibilityId = rgVisibility.getCheckedRadioButtonId();
        String visibility = selectedVisibilityId == R.id.rbPublic ? "public" : "private";
        
        // Get time limit
        String timeLimitStr = etTimeLimit.getText() != null ? 
            etTimeLimit.getText().toString() : "30";
        int timeLimit = 30;
        if (!TextUtils.isEmpty(timeLimitStr)) {
            try {
                timeLimit = Integer.parseInt(timeLimitStr);
                if (timeLimit < 10 || timeLimit > 300) {
                    Toast.makeText(this, R.string.generation_error_time_limit, 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.generation_error_invalid_time, 
                    Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Extract base64 images
        List<String> base64Images = sessionViewModel.getPhotoSession().getBase64Images();
        if (base64Images.isEmpty()) {
            Toast.makeText(this, R.string.generation_error_processing, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create request
        GenerateQuizRequest request = new GenerateQuizRequest();
        request.setImages(base64Images);
        request.setLanguage(languageCode);
        request.setQuestionCount(questionCount);
        request.setVisibility(visibility);
        request.setQuizExamTimeLimit(timeLimit);
        request.setFolderId(null); // Optional folder ID
        
        // Validate request
        if (!request.isValid()) {
            Toast.makeText(this, R.string.generation_error_invalid_request, 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Generate quiz
        quizViewModel.generateQuiz(request);
    }
    
    private void handleSuccess(GenerateQuizResponse response) {
        if (response == null) {
            handleError(getString(R.string.error_unknown));
            return;
        }
        
        // Clear session
        sessionViewModel.clearSession();
        
        // Show success dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.generation_success_title)
                .setMessage(getString(R.string.generation_success_message, 
                    response.getTitle(), 
                    response.getQuestions().size()))
                .setPositiveButton(R.string.generation_view_quiz, (dialog, which) -> {
                    // TODO: Navigate to QuizDetailActivity with quizId
                    // For now, just go back to home
                    navigateToHome();
                })
                .setNegativeButton(R.string.generation_create_another, (dialog, which) -> {
                    // Go back to camera
                    navigateToCamera();
                })
                .setCancelable(false)
                .show();
    }
    
    private void handleError(String message) {
        String errorMessage = message != null ? message : getString(R.string.error_unknown);
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.generation_error_title)
                .setMessage(errorMessage)
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    validateAndGenerate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!show);
        btnAddPhoto.setEnabled(!show);
    }
    
    private void navigateToHome() {
        // Clear activity stack and return to home
        Intent intent = new Intent(this, com.example.learnquiz_fe.ui.activities.HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
