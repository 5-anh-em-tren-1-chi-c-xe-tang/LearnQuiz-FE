package com.example.learnquiz_fe.data.model.camera;

import android.graphics.Rect;

/**
 * Represents a selected region within an image
 * Stores coordinates and scale information for cropping
 */
public class ImageRegion {
    
    /**
     * Rectangle bounds of the selected region
     */
    private Rect bounds;
    
    /**
     * Scale factor relative to original image width
     */
    private float scaleX;
    
    /**
     * Scale factor relative to original image height
     */
    private float scaleY;
    
    /**
     * Original image width in pixels
     */
    private int originalImageWidth;
    
    /**
     * Original image height in pixels
     */
    private int originalImageHeight;
    
    // Constructor
    public ImageRegion() {
        this.bounds = new Rect();
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
    }
    
    public ImageRegion(Rect bounds, int originalImageWidth, int originalImageHeight) {
        this.bounds = bounds;
        this.originalImageWidth = originalImageWidth;
        this.originalImageHeight = originalImageHeight;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
    }
    
    // Getters and Setters
    public Rect getBounds() {
        return bounds;
    }
    
    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }
    
    public float getScaleX() {
        return scaleX;
    }
    
    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }
    
    public float getScaleY() {
        return scaleY;
    }
    
    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }
    
    public int getOriginalImageWidth() {
        return originalImageWidth;
    }
    
    public void setOriginalImageWidth(int originalImageWidth) {
        this.originalImageWidth = originalImageWidth;
    }
    
    public int getOriginalImageHeight() {
        return originalImageHeight;
    }
    
    public void setOriginalImageHeight(int originalImageHeight) {
        this.originalImageHeight = originalImageHeight;
    }
    
    /**
     * Get scaled bounds relative to original image
     */
    public Rect getScaledBounds() {
        if (bounds == null) return new Rect();
        
        return new Rect(
            (int) (bounds.left * scaleX),
            (int) (bounds.top * scaleY),
            (int) (bounds.right * scaleX),
            (int) (bounds.bottom * scaleY)
        );
    }
    
    /**
     * Check if the region is valid
     */
    public boolean isValid() {
        return bounds != null && bounds.width() > 0 && bounds.height() > 0;
    }
    
    /**
     * Get region width
     */
    public int getWidth() {
        return bounds != null ? bounds.width() : 0;
    }
    
    /**
     * Get region height
     */
    public int getHeight() {
        return bounds != null ? bounds.height() : 0;
    }
}
