package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.camera.CapturedImage;
import com.example.learnquiz_fe.data.model.camera.ImageRegion;
import com.example.learnquiz_fe.data.repository.ImageRepository;
import com.example.learnquiz_fe.ui.viewmodel.PhotoSessionViewModel;
import com.example.learnquiz_fe.ui.views.RegionSelectorView;
import com.example.learnquiz_fe.utils.Constants;
import com.example.learnquiz_fe.utils.ImageUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Activity for previewing captured photo and selecting region
 * Allows user to drag and resize selection area
 */
public class PhotoPreviewActivity extends AppCompatActivity {
    
    private static final String TAG = "PhotoPreviewActivity";
    
    // UI Components
    private MaterialToolbar toolbar;
    private PhotoView photoView;
    private RegionSelectorView regionSelector;
    private MaterialButton btnConfirm;
    private MaterialButton btnContinue;
    private MaterialButton btnRetake;
    private TextView tvInstruction;
    private TextView tvRegionInfo;
    private View loadingOverlay;
    
    // Data
    private Uri imageUri;
    private boolean fromCamera;
    private ImageRegion selectedRegion;
    private Bitmap originalBitmap;
    
    // ViewModel
    private PhotoSessionViewModel sessionViewModel;
    
    // Repository
    private ImageRepository imageRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        
        // Get ViewModel - Use requireActivity() to share across activities
        sessionViewModel = PhotoSessionViewModel.getInstance();
        imageRepository = new ImageRepository(this);
        
        // Get intent data
        String uriString = getIntent().getStringExtra(Constants.EXTRA_IMAGE_URI);
        fromCamera = getIntent().getBooleanExtra(Constants.EXTRA_FROM_CAMERA, false);
        
        if (uriString == null) {
            Toast.makeText(this, "Error: No image URI", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        imageUri = Uri.parse(uriString);
        
        initializeViews();
        setupToolbar();
        setupListeners();
        loadImage();
    }
    
    /**
     * Initialize view references
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        photoView = findViewById(R.id.photo_view);
        regionSelector = findViewById(R.id.region_selector);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnContinue = findViewById(R.id.btn_continue);
        btnRetake = findViewById(R.id.btn_retake);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvRegionInfo = findViewById(R.id.tv_region_info);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        // Region selector listener
        regionSelector.setOnRegionSelectedListener(new RegionSelectorView.OnRegionSelectedListener() {
            @Override
            public void onRegionSelected(ImageRegion region) {
                selectedRegion = region;
                updateRegionInfo(region);
                btnContinue.setEnabled(true);
                btnConfirm.setEnabled(true);
            }
            
            @Override
            public void onRegionChanged(ImageRegion region) {
                updateRegionInfo(region);
            }
        });
        
        // Confirm button (toolbar)
        btnConfirm.setOnClickListener(v -> processAndContinue());
        
        // Continue button (bottom)
        btnContinue.setOnClickListener(v -> processAndContinue());
        
        // Retake button
        btnRetake.setOnClickListener(v -> {
            if (fromCamera) {
                finish(); // Return to camera
            } else {
                // Return to home or camera
                finish();
            }
        });
    }
    
    /**
     * Load image into PhotoView
     */
    private void loadImage() {
        try {
            originalBitmap = ImageUtils.loadBitmapFromUri(this, imageUri);
            if (originalBitmap != null) {
                photoView.setImageBitmap(originalBitmap);
                Log.d(TAG, "Image loaded: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
                
                // ✅ FIX: Set image dimensions to RegionSelectorView after layout
                photoView.post(() -> {
                    regionSelector.setImageDimensions(
                        originalBitmap.getWidth(),
                        originalBitmap.getHeight()
                    );
                    Log.d(TAG, "Image dimensions set to RegionSelectorView");
                });
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Update region info text
     */
    private void updateRegionInfo(ImageRegion region) {
        if (region != null && region.isValid()) {
            String info = getString(R.string.preview_region_info, 
                region.getWidth(), region.getHeight());
            tvRegionInfo.setText(info);
            tvRegionInfo.setVisibility(View.VISIBLE);
        } else {
            tvRegionInfo.setVisibility(View.GONE);
        }
    }
    
    /**
     * Process image and continue to next screen
     */
    private void processAndContinue() {
        if (selectedRegion == null || !selectedRegion.isValid()) {
            Toast.makeText(this, "Please select a region", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        loadingOverlay.setVisibility(View.VISIBLE);
        btnContinue.setEnabled(false);
        btnConfirm.setEnabled(false);
        
        // Process image in background
        imageRepository.processImage(imageUri, selectedRegion, fromCamera, 
            new ImageRepository.ImageCallback() {
                @Override
                public void onSuccess(CapturedImage capturedImage) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Image processed successfully");
                        Log.d(TAG, "Base64 length: " + (capturedImage.getBase64Data() != null ? capturedImage.getBase64Data().length() : 0));
                        Log.d(TAG, "Current session count before add: " + sessionViewModel.getCurrentImageCount());
                        
                        // Add to session
                        boolean added = sessionViewModel.addImage(capturedImage);
                        
                        Log.d(TAG, "Image added: " + added);
                        Log.d(TAG, "Current session count after add: " + sessionViewModel.getCurrentImageCount());
                        
                        if (added) {
                            // Check if session is full
                            if (sessionViewModel.isFull().getValue() == Boolean.TRUE) {
                                // Navigate to quiz generation
                                Log.d(TAG, "Session full, navigating to quiz generation");
                                navigateToQuizGeneration();
                            } else {
                                // Ask user: Add more or generate
                                Log.d(TAG, "Showing options dialog");
                                showOptionsDialog();
                            }
                        } else {
                            loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(PhotoPreviewActivity.this, 
                                "Session is full (max 10 images)", Toast.LENGTH_SHORT).show();
                            navigateToQuizGeneration();
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error processing image: " + error);
                        loadingOverlay.setVisibility(View.GONE);
                        btnContinue.setEnabled(true);
                        btnConfirm.setEnabled(true);
                        Toast.makeText(PhotoPreviewActivity.this, 
                            "Error processing image: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
    }
    
    /**
     * Show options dialog: Add more or Generate quiz
     */
    private void showOptionsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Image Added")
            .setMessage("Image " + sessionViewModel.getCurrentImageCount() + " added. What would you like to do?")
            .setPositiveButton("Add More Photos", (dialog, which) -> {
                // Return to camera
                finish();
            })
            .setNegativeButton("Generate Quiz", (dialog, which) -> {
                navigateToQuizGeneration();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Navigate to QuizGenerationActivity
     */
    private void navigateToQuizGeneration() {
        Intent intent = new Intent(this, QuizGenerationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Cleanup
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        
        imageRepository.shutdown();
    }
}
