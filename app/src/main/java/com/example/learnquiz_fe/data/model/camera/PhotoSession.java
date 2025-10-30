package com.example.learnquiz_fe.data.model.camera;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of captured images in a photo session
 * Used as in-memory storage before generating quiz
 */
public class PhotoSession {
    
    /**
     * Maximum number of images allowed per session
     */
    public static final int MAX_IMAGES = 10;
    
    /**
     * List of captured images
     */
    private List<CapturedImage> images;
    
    /**
     * Session creation timestamp
     */
    private long createdAt;
    
    // Constructor
    public PhotoSession() {
        this.images = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters
    public List<CapturedImage> getImages() {
        return images;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Add an image to the session
     * @return true if added successfully, false if session is full
     */
    public boolean addImage(CapturedImage image) {
        if (images.size() >= MAX_IMAGES) {
            return false;
        }
        return images.add(image);
    }
    
    /**
     * Remove an image by ID
     */
    public boolean removeImage(String imageId) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getId().equals(imageId)) {
                CapturedImage removed = images.remove(i);
                removed.cleanup();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get an image by ID
     */
    public CapturedImage getImage(String imageId) {
        for (CapturedImage image : images) {
            if (image.getId().equals(imageId)) {
                return image;
            }
        }
        return null;
    }
    
    /**
     * Get total number of images
     */
    public int getImageCount() {
        return images.size();
    }
    
    /**
     * Check if session is empty
     */
    public boolean isEmpty() {
        return images.isEmpty();
    }
    
    /**
     * Check if session is full
     */
    public boolean isFull() {
        return images.size() >= MAX_IMAGES;
    }
    
    /**
     * Get all base64 encoded images ready for upload
     */
    public List<String> getBase64Images() {
        List<String> base64List = new ArrayList<>();
        for (CapturedImage image : images) {
            if (image.isReadyForUpload()) {
                base64List.add(image.getBase64Data());
            }
        }
        return base64List;
    }
    
    /**
     * Check if all images are ready for upload
     */
    public boolean areAllImagesReady() {
        if (images.isEmpty()) return false;
        
        for (CapturedImage image : images) {
            if (!image.isReadyForUpload()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Clear all images and cleanup resources
     */
    public void clear() {
        for (CapturedImage image : images) {
            image.cleanup();
        }
        images.clear();
    }
    
    /**
     * Get number of images with valid regions
     */
    public int getImagesWithRegionCount() {
        int count = 0;
        for (CapturedImage image : images) {
            if (image.hasValidRegion()) {
                count++;
            }
        }
        return count;
    }
}
