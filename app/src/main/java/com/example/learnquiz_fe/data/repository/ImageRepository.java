package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.learnquiz_fe.data.model.camera.CapturedImage;
import com.example.learnquiz_fe.data.model.camera.ImageRegion;
import com.example.learnquiz_fe.data.model.camera.PhotoSession;
import com.example.learnquiz_fe.utils.Constants;
import com.example.learnquiz_fe.utils.ImageUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Image processing operations
 * Handles image loading, cropping, compression, and Base64 encoding
 */
public class ImageRepository {
    
    private static final String TAG = "ImageRepository";
    
    private final Context context;
    private final ExecutorService executorService;
    
    /**
     * Constructor
     */
    public ImageRepository(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Process image and add to photo session
     * Runs on background thread
     * 
     * @param imageUri Image URI
     * @param region Selected region
     * @param fromCamera Whether image is from camera
     * @param callback Callback for success/error
     */
    public void processImage(Uri imageUri, ImageRegion region, boolean fromCamera, 
                            ImageCallback callback) {
        executorService.execute(() -> {
            try {
                // Load original bitmap
                Bitmap originalBitmap = ImageUtils.loadBitmapFromUri(context, imageUri);
                
                if (originalBitmap == null) {
                    callback.onError("Failed to load image");
                    return;
                }
                
                // Crop if region is specified
                Bitmap processedBitmap = originalBitmap;
                if (region != null && region.isValid()) {
                    processedBitmap = ImageUtils.cropBitmap(originalBitmap, region.getScaledBounds());
                }
                
                // Resize for upload (max 1920x1080)
                Bitmap resizedBitmap = ImageUtils.resizeForUpload(processedBitmap, 1920);
                
                // Compress to Base64
                String base64Data = ImageUtils.compressToBase64(resizedBitmap, 85);
                
                // Create thumbnail (larger size for better quality)
                Bitmap thumbnail = ImageUtils.createThumbnail(processedBitmap, 200);
                
                // Create CapturedImage object
                CapturedImage capturedImage = new CapturedImage(imageUri, fromCamera);
                capturedImage.setBase64Data(base64Data);
                capturedImage.setRegion(region);
                capturedImage.setThumbnail(thumbnail);
                capturedImage.setOriginalWidth(originalBitmap.getWidth());
                capturedImage.setOriginalHeight(originalBitmap.getHeight());
                
                // Cleanup - IMPORTANT: Don't recycle bitmaps that might be referenced
                // Only recycle intermediate processing bitmaps, NOT the thumbnail
                if (resizedBitmap != processedBitmap && resizedBitmap != originalBitmap && resizedBitmap != thumbnail) {
                    resizedBitmap.recycle();
                }
                if (processedBitmap != originalBitmap && processedBitmap != thumbnail) {
                    processedBitmap.recycle();
                }
                if (originalBitmap != thumbnail) {
                    originalBitmap.recycle();
                }
                
                callback.onSuccess(capturedImage);
                
            } catch (IOException e) {
                callback.onError("Error processing image: " + e.getMessage());
            }
        });
    }

    /**
     * Process a document (PDF). For PDFs, encode entire file to base64 for Gemini API.
     * Create a PDF icon thumbnail for display in gallery.
     * Maximum file size: 10MB
     */
    public void processDocument(Uri docUri, String mimeType, ImageCallback callback) {
        executorService.execute(() -> {
            try {
                // Validate that it's a PDF
                if (mimeType == null || !mimeType.contains("pdf")) {
                    callback.onError("Only PDF files are supported");
                    return;
                }

                // Check file size (max 10MB)
                long fileSize = getFileSize(docUri);
                
                if (fileSize < 0) {
                    callback.onError("Cannot read PDF file");
                    return;
                }
                
                if (fileSize > Constants.MAX_PDF_SIZE_BYTES) {
                    double sizeMB = fileSize / (1024.0 * 1024.0);
                    callback.onError(String.format("PDF file too large (%.1f MB). Maximum size is 10 MB", sizeMB));
                    return;
                }

                // Encode entire PDF file to base64 (for Gemini API processing)
                // Use timeout for large files
                String base64Data = ImageUtils.encodeFileToBase64(context, docUri, mimeType);
                
                if (base64Data == null || base64Data.isEmpty()) {
                    callback.onError("Failed to encode PDF file");
                    return;
                }

                // Create PDF icon thumbnail for gallery display (larger size for clarity)
                Bitmap thumbnail = ImageUtils.createPdfIconThumbnail(200);

                // Create CapturedImage with PDF data
                com.example.learnquiz_fe.data.model.camera.CapturedImage capturedImage =
                        new com.example.learnquiz_fe.data.model.camera.CapturedImage(docUri, false);
                capturedImage.setBase64Data(base64Data);
                capturedImage.setThumbnail(thumbnail);
                // Set reasonable dimensions for PDF "image"
                capturedImage.setOriginalWidth(800);
                capturedImage.setOriginalHeight(1000);

                callback.onSuccess(capturedImage);
                
            } catch (Exception e) {
                callback.onError("Error processing PDF: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get file size from URI
     * @param uri File URI
     * @return File size in bytes, or -1 if cannot be determined
     */
    private long getFileSize(Uri uri) {
        try {
            android.database.Cursor cursor = context.getContentResolver().query(
                uri, null, null, null, null);
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                cursor.moveToFirst();
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error getting file size", e);
        }
        return -1;
    }

    private int MAX_IMAGE_DIM_FOR_DOCUMENT() {
        // Use same sizing rule as images but smaller ceiling for documents
        return 1280;
    }
    
    /**
     * Process multiple images in batch
     */
    public void processBatch(PhotoSession session, BatchCallback callback) {
        executorService.execute(() -> {
            int successCount = 0;
            int errorCount = 0;
            
            for (CapturedImage image : session.getImages()) {
                if (!image.isReadyForUpload()) {
                    try {
                        // Process image if not already processed
                        Bitmap bitmap = ImageUtils.loadBitmapFromUri(context, image.getImageUri());
                        
                        if (bitmap != null) {
                            String base64 = ImageUtils.compressToBase64(bitmap, 85);
                            image.setBase64Data(base64);
                            bitmap.recycle();
                            successCount++;
                        } else {
                            errorCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                    }
                }
            }
            
            final int finalSuccess = successCount;
            final int finalError = errorCount;
            callback.onComplete(finalSuccess, finalError);
        });
    }
    
    /**
     * Cleanup executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Callback interface for image processing
     */
    public interface ImageCallback {
        void onSuccess(CapturedImage image);
        void onError(String message);
    }
    
    /**
     * Callback interface for batch processing
     */
    public interface BatchCallback {
        void onComplete(int successCount, int errorCount);
    }
}
