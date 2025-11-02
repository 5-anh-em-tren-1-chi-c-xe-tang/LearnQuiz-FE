package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.learnquiz_fe.data.model.camera.ImageRegion;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.ui.adapter.PhotoThumbnailAdapter;
import com.example.learnquiz_fe.ui.viewmodel.PhotoSessionViewModel;
import com.example.learnquiz_fe.ui.viewmodel.QuizGenerationViewModel;
import com.example.learnquiz_fe.utils.Constants;
import com.example.learnquiz_fe.utils.ImageUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.List;

/**
 * Activity for quiz generation with photo gallery and settings
 */
public class QuizGenerationActivity extends AppCompatActivity {
    
    private static final String TAG = "QuizGenerationActivity";
    
    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvPhotos;
    private TextView tvEmptyPhotos;
    private TextView tvPhotoCount;
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
    
    // Activity result launchers
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> filePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_generation);
        
        initViews();
        initViewModels();
        setupFilePickerLauncher();
        // Handle incoming document URIs forwarded from HomeActivity
        handleIncomingIntent();
        setupLanguageDropdown();
        setupPhotoGallery();
        setupListeners();
        observeData();
        setupBackPressHandler();
    }
    
    /**
     * Setup file picker launcher for multi-select
     */
    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.content.Intent data = result.getData();
                    
                    // Check if multiple files selected
                    android.content.ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        // Multiple files
                        int count = clipData.getItemCount();
                        Log.d(TAG, "Selected " + count + " files");
                        processMultipleFiles(clipData);
                    } else if (data.getData() != null) {
                        // Single file
                        android.net.Uri uri = data.getData();
                        Log.d(TAG, "Selected single file: " + uri);
                        processSingleFile(uri);
                    }
                }
            }
        );
    }

    /**
     * If activity was started with a document URI (pdf/doc/docx), process and add it to session.
     * Also handles multiple files forwarded from HomeActivity.
     */
    private void handleIncomingIntent() {
        try {
            if (getIntent() == null) return;
            
            // Check for multiple files from HomeActivity
            if (getIntent().hasExtra("HAS_MULTIPLE_FILES") && getIntent().getClipData() != null) {
                android.content.ClipData clipData = getIntent().getClipData();
                Log.d(TAG, "Received " + clipData.getItemCount() + " files from HomeActivity");
                processMultipleFiles(clipData);
                return;
            }
            
            // Handle single document URI
            String docUriStr = getIntent().getStringExtra(com.example.learnquiz_fe.utils.Constants.EXTRA_DOCUMENT_URI);
            String mime = getIntent().getStringExtra(com.example.learnquiz_fe.utils.Constants.EXTRA_MIME_TYPE);
            if (docUriStr != null) {
                android.net.Uri docUri = android.net.Uri.parse(docUriStr);
                showLoading(true);
                com.example.learnquiz_fe.data.repository.ImageRepository repo = new com.example.learnquiz_fe.data.repository.ImageRepository(this);
                repo.processDocument(docUri, mime, new com.example.learnquiz_fe.data.repository.ImageRepository.ImageCallback() {
                    @Override
                    public void onSuccess(com.example.learnquiz_fe.data.model.camera.CapturedImage image) {
                        runOnUiThread(() -> {
                            boolean added = sessionViewModel.addImage(image);
                            Log.d(TAG, "Document added to session: " + added);
                            showLoading(false);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(QuizGenerationActivity.this, "Failed to process document: " + message, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling incoming intent", e);
        }
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
        tvEmptyPhotos = findViewById(R.id.tvEmptyPhotos);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
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
        sessionViewModel = PhotoSessionViewModel.getInstance();
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
                Log.d(TAG, "Thumbnail clicked at position: " + position);
                // Show full-size preview in dialog
                showImagePreview(image);
            }
            
            @Override
            public void onDeleteClick(CapturedImage image, int position) {
                Log.d(TAG, "Delete clicked at position: " + position);
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
        
        // Add photo button - show options dialog
        btnAddPhoto.setOnClickListener(v -> {
            Log.d(TAG, "Add Photo button clicked");
            showAddPhotoOptionsDialog();
        });
        
        // Question count slider
        sliderQuestionCount.addOnChangeListener((slider, value, fromUser) -> {
            tvQuestionCountValue.setText(String.valueOf((int) value));
        });
        
        // Generate button
        btnGenerate.setOnClickListener(v -> validateAndGenerate());
    }
    
    /**
     * Show dialog to choose between camera or file selection
     */
    private void showAddPhotoOptionsDialog() {
        String[] options = new String[]{
            getString(R.string.add_photo_take_photo),
            getString(R.string.add_photo_select_files)
        };
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_photo_choose_option)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Take photo with camera
                        navigateToCamera();
                    } else {
                        // Select files (images/PDF)
                        openFilePicker();
                    }
                })
                .show();
    }
    
    /**
     * Open file picker with multi-select support
     */
    private void openFilePicker() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = new String[] {
            "image/*",
            "application/pdf"
        };
        intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true); // Enable multi-select
        
        filePickerLauncher.launch(intent);
    }
    
    private void observeData() {
        // Observe photo session
        sessionViewModel.getImages().observe(this, images -> {
            if (images != null) {
                Log.d(TAG, "Images updated in QuizGenerationActivity: " + images.size());
                for (int i = 0; i < images.size(); i++) {
                    CapturedImage img = images.get(i);
                    Log.d(TAG, "Image " + i + ": " + img.getId() + 
                        ", Base64 length: " + (img.getBase64Data() != null ? img.getBase64Data().length() : 0));
                }
                photoAdapter.setImages(images);
                updatePhotoGalleryUI(images.size());
                updateGenerateButtonState(images.size());
            } else {
                Log.d(TAG, "Images list is null in QuizGenerationActivity");
                updatePhotoGalleryUI(0);
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
    
    /**
     * Update photo gallery UI based on image count
     */
    private void updatePhotoGalleryUI(int imageCount) {
        // Update photo count text
        String countText = imageCount + " " + 
            (imageCount == 1 ? "photo" : "photos");
        tvPhotoCount.setText(countText);
        
        // Show/hide empty state
        if (imageCount == 0) {
            rvPhotos.setVisibility(View.GONE);
            tvEmptyPhotos.setVisibility(View.VISIBLE);
        } else {
            rvPhotos.setVisibility(View.VISIBLE);
            tvEmptyPhotos.setVisibility(View.GONE);
        }
    }
    
    private void navigateToCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
    
    /**
     * Process multiple selected files
     */
    private void processMultipleFiles(android.content.ClipData clipData) {
        int count = clipData.getItemCount();
        int maxAllowed = Constants.MAX_IMAGES_PER_SESSION - sessionViewModel.getCurrentImageCount();
        
        if (maxAllowed <= 0) {
            Toast.makeText(this, R.string.generation_max_photos_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (count > maxAllowed) {
            Toast.makeText(this, 
                String.format("Can only add %d more files (current: %d, max: %d)", 
                    maxAllowed, sessionViewModel.getCurrentImageCount(), Constants.MAX_IMAGES_PER_SESSION),
                Toast.LENGTH_LONG).show();
            count = maxAllowed;
        }
        
        showLoading(true);
        tvLoadingText.setText(getString(R.string.add_photo_processing));
        
        com.example.learnquiz_fe.data.repository.ImageRepository repo = 
            new com.example.learnquiz_fe.data.repository.ImageRepository(this);
        
        final int[] processed = {0};
        final int[] errors = {0};
        final int totalToProcess = count;
        
        for (int i = 0; i < count; i++) {
            android.net.Uri uri = clipData.getItemAt(i).getUri();
            processFileUri(uri, repo, () -> {
                processed[0]++;
                if (processed[0] + errors[0] >= totalToProcess) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, 
                            String.format("Processed: %d, Errors: %d", processed[0], errors[0]),
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }, () -> {
                errors[0]++;
                if (processed[0] + errors[0] >= totalToProcess) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, 
                            String.format("Processed: %d, Errors: %d", processed[0], errors[0]),
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
    
    /**
     * Process single selected file
     */
    private void processSingleFile(android.net.Uri uri) {
        showLoading(true);
        tvLoadingText.setText(getString(R.string.add_photo_processing));
        
        com.example.learnquiz_fe.data.repository.ImageRepository repo = 
            new com.example.learnquiz_fe.data.repository.ImageRepository(this);
        
        processFileUri(uri, repo, 
            () -> runOnUiThread(() -> showLoading(false)),
            () -> runOnUiThread(() -> showLoading(false))
        );
    }
    
    /**
     * Process file URI - determine if image or PDF and handle accordingly
     */
    private void processFileUri(android.net.Uri uri, 
                                com.example.learnquiz_fe.data.repository.ImageRepository repo,
                                Runnable onComplete, Runnable onError) {
        String mimeType = null;
        try {
            mimeType = getContentResolver().getType(uri);
        } catch (Exception e) {
            Log.e(TAG, "Error getting mime type", e);
        }
        
        if (mimeType != null && mimeType.contains("pdf")) {
            // Process PDF
            repo.processDocument(uri, mimeType, new com.example.learnquiz_fe.data.repository.ImageRepository.ImageCallback() {
                @Override
                public void onSuccess(com.example.learnquiz_fe.data.model.camera.CapturedImage image) {
                    runOnUiThread(() -> {
                        boolean added = sessionViewModel.addImage(image);
                        Log.d(TAG, "PDF added to session: " + added);
                        if (!added) {
                            Toast.makeText(QuizGenerationActivity.this, 
                                R.string.generation_max_photos_reached, Toast.LENGTH_SHORT).show();
                        }
                        onComplete.run();
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizGenerationActivity.this, 
                            "Error: " + message, Toast.LENGTH_SHORT).show();
                        onError.run();
                    });
                }
            });
        } else {
            // Process as image - forward to PhotoPreviewActivity for region selection
            Intent intent = new Intent(this, com.example.learnquiz_fe.ui.activities.PhotoPreviewActivity.class);
            intent.putExtra(Constants.EXTRA_IMAGE_URI, uri.toString());
            intent.putExtra(Constants.EXTRA_FROM_CAMERA, false);
            startActivity(intent);
            onComplete.run();
        }
    }
    
    private void showImagePreview(CapturedImage image) {
        // Check if this is a PDF (no region means it's a document)
        if (!image.hasValidRegion() && image.getImageUri() != null) {
            // Try to determine if it's a PDF by checking mime type
            String mimeType = null;
            try {
                mimeType = getContentResolver().getType(image.getImageUri());
            } catch (Exception e) {
                Log.e(TAG, "Error getting mime type", e);
            }
            
            if (mimeType != null && mimeType.contains("pdf")) {
                showPdfPreview(image);
                return;
            }
        }
        
        // Original image preview code
        // Create custom dialog with PhotoView for zoom capability
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_image_preview, null);
        
        PhotoView photoView = dialogView.findViewById(R.id.photo_view);
        TextView tvInfo = dialogView.findViewById(R.id.tv_image_info);
        
        // Load image
        try {
            Bitmap bitmap = ImageUtils.loadBitmapFromUri(this, image.getImageUri());
            
            if (bitmap != null) {
                Log.d(TAG, "=== showImagePreview() ===");
                Log.d(TAG, "Loaded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                
                // Crop to selected region if available
                if (image.hasValidRegion()) {
                    ImageRegion region = image.getRegion();
                    
                    // ✅ DEBUG: Log region info BEFORE conversion
                    Log.d(TAG, "Image has valid region");
                    Log.d(TAG, "Display bounds: " + region.getBounds());
                    Log.d(TAG, "Scale factors: X=" + region.getScaleX() + ", Y=" + region.getScaleY());
                    
                    // Use scaled bounds to get actual pixel coordinates
                    android.graphics.Rect cropRect = region.getScaledBounds();
                    
                    // ✅ DEBUG: Log scaled coordinates
                    Log.d(TAG, "Scaled crop rect: " + cropRect);
                    Log.d(TAG, "  Position: (" + cropRect.left + ", " + cropRect.top + ")");
                    Log.d(TAG, "  Size: " + cropRect.width() + "x" + cropRect.height());
                    
                    // Validate crop rect within bitmap bounds
                    if (cropRect.left >= 0 && cropRect.top >= 0 && 
                        cropRect.right <= bitmap.getWidth() && 
                        cropRect.bottom <= bitmap.getHeight() &&
                        cropRect.width() > 0 && cropRect.height() > 0) {
                        
                        Log.d(TAG, "✅ Crop rect VALID, proceeding to crop...");
                        
                        Bitmap croppedBitmap = ImageUtils.cropBitmap(bitmap, cropRect);
                        
                        if (croppedBitmap != null) {
                            Log.d(TAG, "✅ Crop SUCCESS: " + croppedBitmap.getWidth() + 
                                      "x" + croppedBitmap.getHeight());
                            photoView.setImageBitmap(croppedBitmap);
                            
                            // Recycle original if different
                            if (croppedBitmap != bitmap) {
                                bitmap.recycle();
                            }
                        } else {
                            Log.e(TAG, "❌ Crop returned null, showing original");
                            photoView.setImageBitmap(bitmap);
                        }
                    } else {
                        // Region out of bounds, show original
                        Log.w(TAG, "❌ Crop rect OUT OF BOUNDS");
                        Log.w(TAG, "  Crop rect: " + cropRect);
                        Log.w(TAG, "  Bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        photoView.setImageBitmap(bitmap);
                    }
                } else {
                    // No region selected, show full image
                    Log.d(TAG, "No region selected, showing full image");
                    photoView.setImageBitmap(bitmap);
                }
            } else if (image.getThumbnail() != null) {
                // Fallback to thumbnail (already cropped)
                Log.w(TAG, "Bitmap load failed, using thumbnail");
                photoView.setImageBitmap(image.getThumbnail());
            }
            
            // Show image info
            String info = buildImageInfo(image);
            tvInfo.setText(info);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading image for preview", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.generation_delete, (dialog, which) -> {
                    // Find position and delete
                    List<CapturedImage> images = sessionViewModel.getImages().getValue();
                    if (images != null) {
                        int position = images.indexOf(image);
                        if (position >= 0) {
                            showDeleteConfirmation(image, position);
                        }
                    }
                })
                .create()
                .show();
    }
    
    /**
     * Show PDF preview dialog with page navigation
     */
    private void showPdfPreview(CapturedImage image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pdf_preview, null);
        
        android.widget.ImageView ivPdfPage = dialogView.findViewById(R.id.iv_pdf_page);
        TextView tvPdfInfo = dialogView.findViewById(R.id.tv_pdf_info);
        TextView tvPdfTitle = dialogView.findViewById(R.id.tv_pdf_title);
        MaterialButton btnPrevPage = dialogView.findViewById(R.id.btn_prev_page);
        MaterialButton btnNextPage = dialogView.findViewById(R.id.btn_next_page);
        
        final int[] currentPage = {0};
        final android.graphics.pdf.PdfRenderer[] pdfRenderer = {null};
        
        try {
            // Open PDF
            android.os.ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(image.getImageUri(), "r");
            if (pfd != null) {
                pdfRenderer[0] = new android.graphics.pdf.PdfRenderer(pfd);
                final int pageCount = pdfRenderer[0].getPageCount();
                
                tvPdfTitle.setText("PDF Document");
                
                // Function to render page
                Runnable renderPage = new Runnable() {
                    @Override
                    public void run() {
                        if (pdfRenderer[0] == null) return;
                        
                        // Close previous page if open
                        android.graphics.pdf.PdfRenderer.Page page = pdfRenderer[0].openPage(currentPage[0]);
                        
                        // Create bitmap for page
                        int width = page.getWidth() * 2; // Higher resolution
                        int height = page.getHeight() * 2;
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        
                        // Render page to bitmap
                        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        
                        // Display bitmap
                        ivPdfPage.setImageBitmap(bitmap);
                        
                        // Update info
                        tvPdfInfo.setText("Page " + (currentPage[0] + 1) + " of " + pageCount);
                        
                        // Update button states
                        btnPrevPage.setEnabled(currentPage[0] > 0);
                        btnNextPage.setEnabled(currentPage[0] < pageCount - 1);
                        
                        page.close();
                    }
                };
                
                // Initial render
                renderPage.run();
                
                // Previous page button
                btnPrevPage.setOnClickListener(v -> {
                    if (currentPage[0] > 0) {
                        currentPage[0]--;
                        renderPage.run();
                    }
                });
                
                // Next page button
                btnNextPage.setOnClickListener(v -> {
                    if (currentPage[0] < pageCount - 1) {
                        currentPage[0]++;
                        renderPage.run();
                    }
                });
                
                AlertDialog dialog = builder.setView(dialogView)
                        .setPositiveButton(R.string.ok, (d, which) -> {
                            try {
                                if (pdfRenderer[0] != null) {
                                    pdfRenderer[0].close();
                                }
                                if (pfd != null) {
                                    pfd.close();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error closing PDF", e);
                            }
                        })
                        .setNegativeButton(R.string.generation_delete, (d, which) -> {
                            try {
                                if (pdfRenderer[0] != null) {
                                    pdfRenderer[0].close();
                                }
                                if (pfd != null) {
                                    pfd.close();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error closing PDF", e);
                            }
                            
                            // Find position and delete
                            List<CapturedImage> images = sessionViewModel.getImages().getValue();
                            if (images != null) {
                                int position = images.indexOf(image);
                                if (position >= 0) {
                                    showDeleteConfirmation(image, position);
                                }
                            }
                        })
                        .create();
                
                // Cleanup when dialog is dismissed
                dialog.setOnDismissListener(d -> {
                    try {
                        if (pdfRenderer[0] != null) {
                            pdfRenderer[0].close();
                        }
                        if (pfd != null) {
                            pfd.close();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing PDF", e);
                    }
                });
                
                dialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF", e);
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Build detailed info text for image
     */
    private String buildImageInfo(CapturedImage image) {
        StringBuilder info = new StringBuilder();
        
        // Timestamp
        info.append("Captured: ")
            .append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", 
                java.util.Locale.getDefault()).format(new java.util.Date(image.getTimestamp())))
            .append("\n");
        
        // Source
        info.append("Source: ")
            .append(image.isFromCamera() ? "Camera" : "Gallery")
            .append("\n");
        
        // Original dimensions
        if (image.getOriginalWidth() > 0 && image.getOriginalHeight() > 0) {
            info.append("Original Size: ")
                .append(image.getOriginalWidth())
                .append(" × ")
                .append(image.getOriginalHeight())
                .append(" px\n");
        }
        
        // Selected region
        ImageRegion region = image.getRegion();
        if (region != null && region.isValid()) {
            info.append("Selected Region: ")
                .append(region.getWidth())
                .append(" × ")
                .append(region.getHeight())
                .append(" px\n");
        }
        
        // Data size
        if (image.getBase64Data() != null) {
            int sizeKB = image.getBase64Data().length() / 1024;
            info.append("Data Size: ")
                .append(sizeKB)
                .append(" KB");
        }
        
        return info.toString();
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
                    // Store quiz data in holder to avoid TransactionTooLargeException
                    com.example.learnquiz_fe.utils.QuizDataHolder.getInstance().setQuizResponse(response);
                    
                    // Navigate to QuizTakingActivity
                    Intent intent = new Intent(QuizGenerationActivity.this, QuizTakingActivity.class);
                    startActivity(intent);
                    finish();
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
