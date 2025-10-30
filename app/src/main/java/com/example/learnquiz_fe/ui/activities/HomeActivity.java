package com.example.learnquiz_fe.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.learnquiz_fe.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Home Activity
 * Main screen for creating quizzes from photos
 * Features: Take photo, select photo, select region, generate quiz
 */
public class HomeActivity extends AppCompatActivity {

    // Constants
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 100;

    // UI Components
    private MaterialToolbar toolbar;
    private PreviewView previewView;
    private ImageView ivPhotoPreview;
    private View viewSelectionOverlay;
    private LinearLayout llPlaceholder;
    private MaterialButton btnTakePhoto;
    private MaterialButton btnSelectPhoto;
    private MaterialButton btnSelectRegion;
    private MaterialButton btnGenerateQuiz;
    private TextView tvSelectionInfo;
    private View cardSelectionInfo;
    private FrameLayout loadingOverlay;

    // Camera
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    // Selection state
    private boolean isSelectionMode = false;
    private Rect selectedRegion;
    private float startX, startY;
    private Paint selectionPaint;
    private Bitmap currentBitmap;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> selectPhotoLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize paint for selection overlay
        initializeSelectionPaint();

        // Setup activity result launchers
        setupActivityResultLaunchers();

        // Setup listeners
        setupListeners();
    }

    /**
     * Initialize all view references
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        previewView = findViewById(R.id.preview_view);
        ivPhotoPreview = findViewById(R.id.iv_photo_preview);
        viewSelectionOverlay = findViewById(R.id.view_selection_overlay);
        llPlaceholder = findViewById(R.id.ll_placeholder);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        btnSelectRegion = findViewById(R.id.btn_select_region);
        btnGenerateQuiz = findViewById(R.id.btn_generate_quiz);
        tvSelectionInfo = findViewById(R.id.tv_selection_info);
        cardSelectionInfo = findViewById(R.id.card_selection_info);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    /**
     * Setup toolbar with menu
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    /**
     * Initialize paint for drawing selection rectangle
     */
    private void initializeSelectionPaint() {
        selectionPaint = new Paint();
        selectionPaint.setColor(Color.parseColor("#4CAF50"));
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(5f);
    }

    /**
     * Setup activity result launchers for camera permissions and photo selection
     */
    private void setupActivityResultLaunchers() {
        // Launcher for selecting photo from gallery
        selectPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    loadPhotoFromUri(selectedImageUri);
                }
            }
        );

        // Launcher for camera permission
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupListeners() {
        // Take photo button - Launch new camera workflow
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Select photo button
        btnSelectPhoto.setOnClickListener(v -> openGallery());

        // Select region button
        btnSelectRegion.setOnClickListener(v -> enableSelectionMode());

        // Generate quiz button
        btnGenerateQuiz.setOnClickListener(v -> generateQuiz());

        // Selection overlay touch listener
        viewSelectionOverlay.setOnTouchListener((v, event) -> {
            if (!isSelectionMode) return false;
            handleSelectionTouch(event);
            return true;
        });
    }

    /**
     * Check if camera permission is granted
     */
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request camera permission
     */
    private void requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    /**
     * Start camera for taking photos
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Bind camera use cases
     */
    private void bindCameraUseCases() {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture
        imageCapture = new ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build();

        // Select back camera
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            // Show camera preview
            llPlaceholder.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            ivPhotoPreview.setVisibility(View.GONE);

            // Change button to capture
            btnTakePhoto.setText(R.string.home_capture_photo);
            btnTakePhoto.setOnClickListener(v -> capturePhoto());

        } catch (Exception e) {
            Toast.makeText(this, "Camera binding failed: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Capture photo from camera
     */
    private void capturePhoto() {
        if (imageCapture == null) return;

        // Create temp file for photo
        File photoFile = new File(getCacheDir(), "temp_photo_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = 
            new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    Uri savedUri = Uri.fromFile(photoFile);
                    loadPhotoFromUri(savedUri);
                    stopCamera();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Toast.makeText(HomeActivity.this, 
                        "Photo capture failed: " + exception.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    /**
     * Stop camera preview
     */
    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        previewView.setVisibility(View.GONE);
        btnTakePhoto.setText(R.string.home_take_photo);
        btnTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startCamera();
            } else {
                requestCameraPermission();
            }
        });
    }

    /**
     * Open gallery to select photo
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectPhotoLauncher.launch(intent);
    }

    /**
     * Load photo from URI
     */
    private void loadPhotoFromUri(Uri uri) {
        try {
            currentBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ivPhotoPreview.setImageBitmap(currentBitmap);
            
            // Show photo preview, hide placeholder
            llPlaceholder.setVisibility(View.GONE);
            previewView.setVisibility(View.GONE);
            ivPhotoPreview.setVisibility(View.VISIBLE);
            
            // Show select region button
            btnSelectRegion.setVisibility(View.VISIBLE);
            
            Toast.makeText(this, "Photo loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load photo: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enable selection mode for region selection
     */
    private void enableSelectionMode() {
        isSelectionMode = true;
        viewSelectionOverlay.setVisibility(View.VISIBLE);
        selectedRegion = null;
        btnSelectRegion.setText(R.string.home_selecting_region);
        btnSelectRegion.setEnabled(false);
        Toast.makeText(this, "Drag to select a region on the photo", Toast.LENGTH_LONG).show();
    }

    /**
     * Handle touch events for region selection
     */
    private void handleSelectionTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                
                // Create selected region
                int left = (int) Math.min(startX, endX);
                int top = (int) Math.min(startY, endY);
                int right = (int) Math.max(startX, endX);
                int bottom = (int) Math.max(startY, endY);
                
                selectedRegion = new Rect(left, top, right, bottom);
                
                // Draw selection on overlay
                drawSelection();
                
                // Update UI
                isSelectionMode = false;
                btnSelectRegion.setText(R.string.home_select_region);
                btnSelectRegion.setEnabled(true);
                
                // Show selection info
                showSelectionInfo();
                
                // Show generate quiz button
                btnGenerateQuiz.setVisibility(View.VISIBLE);
                
                Toast.makeText(this, "Region selected!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Draw selection rectangle on overlay
     */
    private void drawSelection() {
        if (selectedRegion == null) return;

        Bitmap overlayBitmap = Bitmap.createBitmap(
            viewSelectionOverlay.getWidth(),
            viewSelectionOverlay.getHeight(),
            Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(overlayBitmap);
        
        // Draw semi-transparent background
        canvas.drawColor(Color.parseColor("#80000000"));
        
        // Clear selected region (make it transparent)
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(selectedRegion, clearPaint);
        
        // Draw selection border
        canvas.drawRect(selectedRegion, selectionPaint);
        
        // Apply to overlay view
        viewSelectionOverlay.setBackground(new android.graphics.drawable.BitmapDrawable(
            getResources(), overlayBitmap));
    }

    /**
     * Show selection information
     */
    private void showSelectionInfo() {
        if (selectedRegion != null) {
            int width = selectedRegion.width();
            int height = selectedRegion.height();
            tvSelectionInfo.setText(String.format("Region selected: %d×%d pixels", width, height));
            cardSelectionInfo.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Generate quiz from selected region
     */
    private void generateQuiz() {
        if (currentBitmap == null || selectedRegion == null) {
            Toast.makeText(this, "Please select a photo and region first", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        loadingOverlay.setVisibility(View.VISIBLE);

        // TODO: Implement actual quiz generation with backend API
        // For now, simulate processing
        viewSelectionOverlay.postDelayed(() -> {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(this, 
                "Quiz generation feature coming soon!\nSelected region will be sent to AI for processing.", 
                Toast.LENGTH_LONG).show();
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
    }
}
