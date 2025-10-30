package com.example.learnquiz_fe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.learnquiz_fe.data.model.camera.CapturedImage;
import com.example.learnquiz_fe.data.model.camera.PhotoSession;

import java.util.List;

/**
 * ViewModel for managing PhotoSession across activities
 * Shared between CameraActivity, PhotoPreviewActivity, and QuizGenerationActivity
 */
public class PhotoSessionViewModel extends ViewModel {
    
    private final PhotoSession photoSession;
    private final MutableLiveData<List<CapturedImage>> imagesLiveData;
    private final MutableLiveData<Integer> imageCountLiveData;
    private final MutableLiveData<Boolean> isFullLiveData;
    
    public PhotoSessionViewModel() {
        this.photoSession = new PhotoSession();
        this.imagesLiveData = new MutableLiveData<>();
        this.imageCountLiveData = new MutableLiveData<>(0);
        this.isFullLiveData = new MutableLiveData<>(false);
        
        updateLiveData();
    }
    
    /**
     * Get live list of images
     */
    public LiveData<List<CapturedImage>> getImages() {
        return imagesLiveData;
    }
    
    /**
     * Get live image count
     */
    public LiveData<Integer> getImageCount() {
        return imageCountLiveData;
    }
    
    /**
     * Get whether session is full
     */
    public LiveData<Boolean> isFull() {
        return isFullLiveData;
    }
    
    /**
     * Add image to session
     * @return true if added, false if session is full
     */
    public boolean addImage(CapturedImage image) {
        boolean added = photoSession.addImage(image);
        if (added) {
            updateLiveData();
        }
        return added;
    }
    
    /**
     * Remove image by ID
     */
    public boolean removeImage(String imageId) {
        boolean removed = photoSession.removeImage(imageId);
        if (removed) {
            updateLiveData();
        }
        return removed;
    }
    
    /**
     * Get image by ID
     */
    public CapturedImage getImage(String imageId) {
        return photoSession.getImage(imageId);
    }
    
    /**
     * Get all Base64 encoded images
     */
    public List<String> getBase64Images() {
        return photoSession.getBase64Images();
    }
    
    /**
     * Check if all images are ready for upload
     */
    public boolean areAllImagesReady() {
        return photoSession.areAllImagesReady();
    }
    
    /**
     * Check if session is empty
     */
    public boolean isEmpty() {
        return photoSession.isEmpty();
    }
    
    /**
     * Get current image count
     */
    public int getCurrentImageCount() {
        return photoSession.getImageCount();
    }
    
    /**
     * Clear all images
     */
    public void clearSession() {
        photoSession.clear();
        updateLiveData();
    }
    
    /**
     * Get PhotoSession object directly
     */
    public PhotoSession getPhotoSession() {
        return photoSession;
    }
    
    /**
     * Update all LiveData values
     */
    private void updateLiveData() {
        imagesLiveData.setValue(photoSession.getImages());
        imageCountLiveData.setValue(photoSession.getImageCount());
        isFullLiveData.setValue(photoSession.isFull());
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup resources when ViewModel is destroyed
        photoSession.clear();
    }
}
