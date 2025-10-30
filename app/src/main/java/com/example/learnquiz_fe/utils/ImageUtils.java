package com.example.learnquiz_fe.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

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
     * 
     * @param source Source bitmap
     * @param region Region to crop
     * @return Cropped bitmap
     */
    public static Bitmap cropBitmap(Bitmap source, Rect region) {
        if (source == null || region == null) {
            return source;
        }
        
        // Ensure region is within bounds
        int left = Math.max(0, region.left);
        int top = Math.max(0, region.top);
        int right = Math.min(source.getWidth(), region.right);
        int bottom = Math.min(source.getHeight(), region.bottom);
        
        int width = right - left;
        int height = bottom - top;
        
        if (width <= 0 || height <= 0) {
            return source;
        }
        
        return Bitmap.createBitmap(source, left, top, width, height);
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
     * Create thumbnail from bitmap
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
        
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
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
