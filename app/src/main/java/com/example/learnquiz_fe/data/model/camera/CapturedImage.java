package com.example.learnquiz_fe.data.model.camera;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Represents a captured image with its metadata and selected region
 * Used for managing photo session in memory
 */
public class CapturedImage {
    
    /**
     * Unique identifier for this image
     */
    private String id;
    
    /**
     * Base64 encoded image data (for API upload)
     */
    private String base64Data;
    
    /**
     * Selected region within the image
     */
    private ImageRegion region;
    
    /**
     * Thumbnail bitmap for display in gallery
     */
    private Bitmap thumbnail;
    
    /**
     * Original image URI (file path or content URI)
     */
    private Uri imageUri;
    
    /**
     * Timestamp when the image was captured/added
     */
    private long timestamp;
    
    /**
     * Original image width in pixels
     */
    private int originalWidth;
    
    /**
     * Original image height in pixels
     */
    private int originalHeight;
    
    /**
     * Whether this image was captured from camera or selected from gallery
     */
    private boolean fromCamera;
    
    // Constructor
    public CapturedImage() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
    }
    
    public CapturedImage(Uri imageUri, boolean fromCamera) {
        this();
        this.imageUri = imageUri;
        this.fromCamera = fromCamera;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getBase64Data() {
        return base64Data;
    }
    
    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }
    
    public ImageRegion getRegion() {
        return region;
    }
    
    public void setRegion(ImageRegion region) {
        this.region = region;
    }
    
    public Bitmap getThumbnail() {
        return thumbnail;
    }
    
    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public Uri getImageUri() {
        return imageUri;
    }
    
    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getOriginalWidth() {
        return originalWidth;
    }
    
    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }
    
    public int getOriginalHeight() {
        return originalHeight;
    }
    
    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }
    
    public boolean isFromCamera() {
        return fromCamera;
    }
    
    public void setFromCamera(boolean fromCamera) {
        this.fromCamera = fromCamera;
    }
    
    /**
     * Check if the image has a valid selected region
     */
    public boolean hasValidRegion() {
        return region != null && region.isValid();
    }
    
    /**
     * Check if the image is ready for upload (has base64 data)
     */
    public boolean isReadyForUpload() {
        return base64Data != null && !base64Data.isEmpty();
    }
    
    /**
     * Cleanup resources (recycle bitmaps)
     */
    public void cleanup() {
        if (thumbnail != null && !thumbnail.isRecycled()) {
            thumbnail.recycle();
            thumbnail = null;
        }
    }
}
