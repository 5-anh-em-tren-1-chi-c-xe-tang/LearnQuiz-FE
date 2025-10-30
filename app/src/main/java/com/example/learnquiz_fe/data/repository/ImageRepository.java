package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.learnquiz_fe.data.model.camera.CapturedImage;
import com.example.learnquiz_fe.data.model.camera.ImageRegion;
import com.example.learnquiz_fe.data.model.camera.PhotoSession;
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
                
                // Create thumbnail
                Bitmap thumbnail = ImageUtils.createThumbnail(processedBitmap, 120);
                
                // Create CapturedImage object
                CapturedImage capturedImage = new CapturedImage(imageUri, fromCamera);
                capturedImage.setBase64Data(base64Data);
                capturedImage.setRegion(region);
                capturedImage.setThumbnail(thumbnail);
                capturedImage.setOriginalWidth(originalBitmap.getWidth());
                capturedImage.setOriginalHeight(originalBitmap.getHeight());
                
                // Cleanup
                if (processedBitmap != originalBitmap) {
                    processedBitmap.recycle();
                }
                if (resizedBitmap != processedBitmap) {
                    resizedBitmap.recycle();
                }
                originalBitmap.recycle();
                
                callback.onSuccess(capturedImage);
                
            } catch (IOException e) {
                callback.onError("Error processing image: " + e.getMessage());
            }
        });
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
