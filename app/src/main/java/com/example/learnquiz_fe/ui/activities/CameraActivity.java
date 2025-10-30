package com.example.learnquiz_fe.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Full-screen camera activity for capturing photos
 * Uses CameraX API for modern camera implementation
 */
public class CameraActivity extends AppCompatActivity {
    
    private static final String TAG = "CameraActivity";
    
    // UI Components
    private PreviewView previewView;
    private MaterialToolbar toolbar;
    private FloatingActionButton btnCapture;
    private ImageButton btnFlipCamera;
    private ImageButton btnGallery;
    private ImageButton btnFlash;
    private View loadingOverlay;
    
    // Camera
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Camera camera;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    
    // Activity Result Launchers
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
        initializeViews();
        setupToolbar();
        setupActivityResultLaunchers();
        setupListeners();
        
        // Request camera permission
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    /**
     * Initialize view references
     */
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        toolbar = findViewById(R.id.toolbar);
        btnCapture = findViewById(R.id.btn_capture);
        btnFlipCamera = findViewById(R.id.btn_flip_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnFlash = findViewById(R.id.btn_flash);
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
     * Setup activity result launchers
     */
    private void setupActivityResultLaunchers() {
        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        );
        
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        navigateToPreview(selectedImageUri, false);
                    }
                }
            }
        );
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        btnCapture.setOnClickListener(v -> capturePhoto());
        btnFlipCamera.setOnClickListener(v -> flipCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnFlash.setOnClickListener(v -> toggleFlash());
    }
    
    /**
     * Check camera permission
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
     * Start camera
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, R.string.camera_error, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    /**
     * Bind camera use cases
     */
    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        
        // Unbind all use cases before rebinding
        cameraProvider.unbindAll();
        
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        // Image capture use case
        imageCapture = new ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build();
        
        // Select camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build();
        
        try {
            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture);
            
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            Toast.makeText(this, R.string.camera_error, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Capture photo
     */
    private void capturePhoto() {
        if (imageCapture == null) return;
        
        // Show loading
        loadingOverlay.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(false);
        
        // Create file for photo
        File photoFile = new File(getCacheDir(), "camera_" + System.currentTimeMillis() + ".jpg");
        
        ImageCapture.OutputFileOptions outputOptions = 
            new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    Uri savedUri = Uri.fromFile(photoFile);
                    Log.d(TAG, "Photo captured: " + savedUri);
                    
                    // Navigate to preview
                    navigateToPreview(savedUri, true);
                }
                
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "Photo capture failed", exception);
                    loadingOverlay.setVisibility(View.GONE);
                    btnCapture.setEnabled(true);
                    Toast.makeText(CameraActivity.this, 
                        "Photo capture failed: " + exception.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    /**
     * Flip camera (front/back)
     */
    private void flipCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) 
            ? CameraSelector.LENS_FACING_FRONT 
            : CameraSelector.LENS_FACING_BACK;
        
        bindCameraUseCases();
    }
    
    /**
     * Toggle flash mode
     */
    private void toggleFlash() {
        if (imageCapture == null) return;
        
        flashMode = (flashMode == ImageCapture.FLASH_MODE_OFF) 
            ? ImageCapture.FLASH_MODE_ON 
            : ImageCapture.FLASH_MODE_OFF;
        
        imageCapture.setFlashMode(flashMode);
        
        // Update icon
        btnFlash.setImageResource(flashMode == ImageCapture.FLASH_MODE_ON 
            ? R.drawable.ic_flash_on 
            : R.drawable.ic_flash_off);
    }
    
    /**
     * Open gallery to select photo
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    
    /**
     * Navigate to PhotoPreviewActivity
     */
    private void navigateToPreview(Uri imageUri, boolean fromCamera) {
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtra(Constants.EXTRA_IMAGE_URI, imageUri.toString());
        intent.putExtra(Constants.EXTRA_FROM_CAMERA, fromCamera);
        startActivity(intent);
        
        // Reset UI
        loadingOverlay.setVisibility(View.GONE);
        btnCapture.setEnabled(true);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
