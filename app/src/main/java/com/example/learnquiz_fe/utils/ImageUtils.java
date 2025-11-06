package com.example.learnquiz_fe.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image processing operations
 * Handles loading, cropping, resizing, compression, and Base64 encoding
 */
public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    
    /**
     * Load bitmap from URI
     * 
     * @param context Application context
     * @param uri Image URI
     * @return Loaded bitmap or null if failed
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            return null;
        }
        
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        
        // Fix orientation if needed
        return fixOrientation(context, uri, bitmap);
    }
    
    /**
     * Fix image orientation based on EXIF data
     */
    private static Bitmap fixOrientation(Context context, Uri uri, Bitmap bitmap) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return bitmap;
            
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 
                ExifInterface.ORIENTATION_NORMAL
            );
            inputStream.close();
            
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );
            
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }
            
            return rotatedBitmap;
            
        } catch (IOException e) {
            return bitmap;
        }
    }
    
    /**
     * Crop bitmap to specified region
     * ✅ FIX: Added detailed logging and validation
     * 
     * @param source Source bitmap
     * @param region Region to crop (in actual pixel coordinates)
     * @return Cropped bitmap
     */
    public static Bitmap cropBitmap(Bitmap source, Rect region) {
        if (source == null) {
            Log.w(TAG, "cropBitmap: source is null");
            return null;
        }
        
        if (region == null || region.isEmpty()) {
            Log.w(TAG, "cropBitmap: region is null or empty, returning original");
            return source;
        }
        
        // ✅ DEBUG: Log input parameters
        Log.d(TAG, "=== cropBitmap() ===");
        Log.d(TAG, "Source bitmap: " + source.getWidth() + "x" + source.getHeight());
        Log.d(TAG, "Crop region: " + region.toString());
        Log.d(TAG, "  left=" + region.left + ", top=" + region.top);
        Log.d(TAG, "  right=" + region.right + ", bottom=" + region.bottom);
        Log.d(TAG, "  width=" + region.width() + ", height=" + region.height());
        
        // ✅ CRITICAL: Extract x, y, width, height from Rect
        int x = region.left;
        int y = region.top;
        int width = region.width();
        int height = region.height();
        
        // ✅ Validation: Ensure crop rect is within bitmap bounds
        if (x < 0 || y < 0 || x + width > source.getWidth() || y + height > source.getHeight()) {
            Log.w(TAG, "cropBitmap: Crop rect out of bounds, clamping...");
            Log.w(TAG, "  Original: x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);
            
            // Clamp to valid bounds
            x = Math.max(0, x);
            y = Math.max(0, y);
            width = Math.min(width, source.getWidth() - x);
            height = Math.min(height, source.getHeight() - y);
            
            Log.w(TAG, "  Clamped: x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);
        }
        
        // ✅ Edge case: Invalid dimensions after clamping
        if (width <= 0 || height <= 0) {
            Log.e(TAG, "cropBitmap: Invalid dimensions after clamping, returning original");
            return source;
        }
        
        try {
            // ✅ THE KEY: Use x, y coordinates, not (0, 0)
            Bitmap cropped = Bitmap.createBitmap(source, x, y, width, height);
            
            Log.d(TAG, "cropBitmap: ✅ SUCCESS - Created " + cropped.getWidth() + 
                      "x" + cropped.getHeight() + " bitmap");
            Log.d(TAG, "cropBitmap: Cropped from position (" + x + ", " + y + ")");
            
            return cropped;
            
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "cropBitmap: ❌ IllegalArgumentException", e);
            Log.e(TAG, "  Parameters: x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);
            Log.e(TAG, "  Bitmap: " + source.getWidth() + "x" + source.getHeight());
            return source;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "cropBitmap: ❌ OutOfMemoryError", e);
            return source;
        }
    }
    
    /**
     * Resize bitmap for upload
     * Maintains aspect ratio
     * 
     * @param source Source bitmap
     * @param maxWidth Maximum width
     * @return Resized bitmap
     */
    public static Bitmap resizeForUpload(Bitmap source, int maxWidth) {
        if (source == null) {
            return null;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        // Already smaller than max, return original
        if (width <= maxWidth) {
            return source;
        }
        
        // Calculate new dimensions maintaining aspect ratio
        float ratio = (float) maxWidth / width;
        int newWidth = maxWidth;
        int newHeight = (int) (height * ratio);
        
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
    
    /**
     * Create thumbnail from bitmap with high quality
     * 
     * @param source Source bitmap
     * @param size Thumbnail size (width and height)
     * @return Thumbnail bitmap
     */
    public static Bitmap createThumbnail(Bitmap source, int size) {
        if (source == null) {
            return null;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        // Calculate scale to fit within square
        float scale = Math.min((float) size / width, (float) size / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        
        // Use high-quality filtering for sharper thumbnails
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
    
    /**
     * Create PDF icon placeholder thumbnail
     * 
     * @param size Thumbnail size
     * @return Bitmap with PDF icon
     */
    public static Bitmap createPdfIconThumbnail(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        
        // Background
        canvas.drawColor(android.graphics.Color.parseColor("#F5F5F5"));
        
        // Red rectangle for PDF look
        android.graphics.Paint bgPaint = new android.graphics.Paint();
        bgPaint.setColor(android.graphics.Color.parseColor("#D32F2F"));
        bgPaint.setStyle(android.graphics.Paint.Style.FILL);
        float margin = size * 0.15f;
        canvas.drawRoundRect(margin, margin, size - margin, size - margin, 10, 10, bgPaint);
        
        // PDF text
        android.graphics.Paint textPaint = new android.graphics.Paint();
        textPaint.setColor(android.graphics.Color.WHITE);
        textPaint.setTextSize(size * 0.25f);
        textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        canvas.drawText("PDF", size / 2f, size / 2f + (size * 0.08f), textPaint);
        
        return bitmap;
    }
    
    /**
     * Encode entire file (PDF) to base64 string with timeout protection
     * Maximum processing time: 30 seconds
     * 
     * @param context Application context
     * @param uri File URI
     * @param mimeType MIME type of file
     * @return Base64 encoded string with data URI prefix or null if failed
     */
    public static String encodeFileToBase64(Context context, Uri uri, String mimeType) {
        final long startTime = System.currentTimeMillis();
        final long TIMEOUT_MS = 30000; // 30 seconds timeout
        
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            long totalBytesRead = 0;
            
            while ((len = inputStream.read(buffer)) != -1) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                    inputStream.close();
                    Log.e(TAG, "File encoding timeout after 30 seconds");
                    return null;
                }
                
                byteBuffer.write(buffer, 0, len);
                totalBytesRead += len;
                
                // Log progress for large files (every 1MB)
                if (totalBytesRead % (1024 * 1024) == 0) {
                    Log.d(TAG, "Encoded " + (totalBytesRead / (1024 * 1024)) + " MB...");
                }
            }
            inputStream.close();
            
            Log.d(TAG, "File encoding completed: " + (totalBytesRead / 1024) + " KB in " + 
                  (System.currentTimeMillis() - startTime) + " ms");
            
            byte[] fileBytes = byteBuffer.toByteArray();
            String base64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP);
            
            // Return with appropriate data URI prefix
            String prefix = "data:" + (mimeType != null ? mimeType : "application/pdf") + ";base64,";
            return prefix + base64;
            
        } catch (Exception e) {
            Log.e(TAG, "Error encoding file to base64", e);
            return null;
        }
    }
    
    /**
     * Compress bitmap to Base64 string
     * 
     * @param bitmap Bitmap to compress
     * @param quality JPEG quality (0-100)
     * @return Base64 encoded string with data URI prefix
     */
    public static String compressToBase64(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        
        String base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        
        // Return with data URI prefix
        return "data:image/jpeg;base64," + base64;
    }
    
    /**
     * Decode Base64 string to bitmap
     * 
     * @param base64String Base64 encoded string (with or without prefix)
     * @return Decoded bitmap or null if failed
     */
    public static Bitmap decodeBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        
        try {
            // Remove data URI prefix if present
            String base64 = base64String;
            if (base64.contains(",")) {
                base64 = base64.split(",")[1];
            }
            
            byte[] decodedBytes = Base64.decode(base64, Base64.NO_WRAP);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Calculate sample size for efficient bitmap loading
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, 
                                           int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight 
                   && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * Load bitmap with sampling for memory efficiency
     */
    public static Bitmap loadSampledBitmap(Context context, Uri uri, 
                                          int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        return bitmap;
    }
    
    /**
     * Get estimated size of Base64 encoded image
     * 
     * @param bitmap Source bitmap
     * @return Estimated size in KB
     */
    public static int getEstimatedBase64Size(Bitmap bitmap) {
        if (bitmap == null) return 0;
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
        int sizeInBytes = stream.toByteArray().length;
        
        // Base64 encoding increases size by ~33%
        return (int) ((sizeInBytes * 1.33) / 1024);
    }
}
