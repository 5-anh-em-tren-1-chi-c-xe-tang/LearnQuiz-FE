package com.example.learnquiz_fe.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.learnquiz_fe.data.model.camera.ImageRegion;

/**
 * Custom view for selecting and dragging image regions
 * Provides visual feedback with semi-transparent overlay and selection rectangle
 */
public class RegionSelectorView extends View {
    
    private static final String TAG = "RegionSelectorView";
    private static final float HANDLE_SIZE = 60f; // Corner handle size in pixels
    private static final float MIN_REGION_SIZE = 100f; // Minimum region size
    
    // Paint objects
    private Paint overlayPaint;
    private Paint selectionPaint;
    private Paint handlePaint;
    private Paint clearPaint;
    
    // Selection region
    private RectF selectedRegion;
    private float startX, startY;
    private boolean isDrawing = false;
    private boolean isMoving = false;
    private boolean isResizing = false;
    private int resizeCorner = -1; // 0=TL, 1=TR, 2=BR, 3=BL
    private float lastTouchX, lastTouchY;
    
    // Listener
    private OnRegionSelectedListener listener;
    
    public interface OnRegionSelectedListener {
        void onRegionSelected(ImageRegion region);
        void onRegionChanged(ImageRegion region);
    }
    
    public RegionSelectorView(Context context) {
        super(context);
        init();
    }
    
    public RegionSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public RegionSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * Initialize paint objects
     */
    private void init() {
        // Semi-transparent black overlay
        overlayPaint = new Paint();
        overlayPaint.setColor(Color.parseColor("#80000000"));
        overlayPaint.setStyle(Paint.Style.FILL);
        
        // Selection border
        selectionPaint = new Paint();
        selectionPaint.setColor(Color.parseColor("#4CAF50"));
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(4f);
        
        // Corner handles
        handlePaint = new Paint();
        handlePaint.setColor(Color.parseColor("#4CAF50"));
        handlePaint.setStyle(Paint.Style.FILL);
        
        // Clear paint for making region transparent
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        
        selectedRegion = new RectF();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (selectedRegion.isEmpty()) {
            // Draw full overlay if no selection
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        } else {
            // Draw overlay with clear selection
            int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
            
            // Draw overlay
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
            
            // Clear selected region
            canvas.drawRect(selectedRegion, clearPaint);
            
            canvas.restoreToCount(saveCount);
            
            // Draw selection border
            canvas.drawRect(selectedRegion, selectionPaint);
            
            // Draw corner handles
            drawCornerHandles(canvas);
        }
    }
    
    /**
     * Draw corner handles for resizing
     */
    private void drawCornerHandles(Canvas canvas) {
        float radius = HANDLE_SIZE / 2;
        
        // Top-left
        canvas.drawCircle(selectedRegion.left, selectedRegion.top, radius, handlePaint);
        
        // Top-right
        canvas.drawCircle(selectedRegion.right, selectedRegion.top, radius, handlePaint);
        
        // Bottom-right
        canvas.drawCircle(selectedRegion.right, selectedRegion.bottom, radius, handlePaint);
        
        // Bottom-left
        canvas.drawCircle(selectedRegion.left, selectedRegion.bottom, radius, handlePaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(x, y);
                
            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(x, y);
                
            case MotionEvent.ACTION_UP:
                return handleTouchUp(x, y);
        }
        
        return super.onTouchEvent(event);
    }
    
    /**
     * Handle touch down event
     */
    private boolean handleTouchDown(float x, float y) {
        if (selectedRegion.isEmpty()) {
            // Start drawing new region
            startX = x;
            startY = y;
            isDrawing = true;
            return true;
        }
        
        // Check if touching corner handle
        resizeCorner = getTouchedCorner(x, y);
        if (resizeCorner >= 0) {
            isResizing = true;
            lastTouchX = x;
            lastTouchY = y;
            return true;
        }
        
        // Check if touching inside region
        if (selectedRegion.contains(x, y)) {
            isMoving = true;
            lastTouchX = x;
            lastTouchY = y;
            return true;
        }
        
        // Start new selection
        selectedRegion.setEmpty();
        startX = x;
        startY = y;
        isDrawing = true;
        invalidate();
        return true;
    }
    
    /**
     * Handle touch move event
     */
    private boolean handleTouchMove(float x, float y) {
        if (isDrawing) {
            // Update selection region
            float left = Math.min(startX, x);
            float top = Math.min(startY, y);
            float right = Math.max(startX, x);
            float bottom = Math.max(startY, y);
            
            selectedRegion.set(left, top, right, bottom);
            invalidate();
            return true;
        }
        
        if (isResizing) {
            // Resize region from corner
            resizeRegion(x, y);
            invalidate();
            notifyRegionChanged();
            return true;
        }
        
        if (isMoving) {
            // Move region
            float dx = x - lastTouchX;
            float dy = y - lastTouchY;
            
            selectedRegion.offset(dx, dy);
            
            // Keep region within bounds
            constrainRegionToBounds();
            
            lastTouchX = x;
            lastTouchY = y;
            invalidate();
            notifyRegionChanged();
            return true;
        }
        
        return super.onTouchEvent(null);
    }
    
    /**
     * Handle touch up event
     */
    private boolean handleTouchUp(float x, float y) {
        if (isDrawing) {
            isDrawing = false;
            
            // Ensure minimum size
            if (selectedRegion.width() < MIN_REGION_SIZE || 
                selectedRegion.height() < MIN_REGION_SIZE) {
                selectedRegion.setEmpty();
            }
            
            invalidate();
            notifyRegionSelected();
            return true;
        }
        
        if (isResizing) {
            isResizing = false;
            resizeCorner = -1;
            notifyRegionSelected();
            return true;
        }
        
        if (isMoving) {
            isMoving = false;
            notifyRegionSelected();
            return true;
        }
        
        return super.onTouchEvent(null);
    }
    
    /**
     * Get which corner is being touched
     * @return 0=TL, 1=TR, 2=BR, 3=BL, -1=none
     */
    private int getTouchedCorner(float x, float y) {
        float touchRadius = HANDLE_SIZE;
        
        // Top-left
        if (isNear(x, selectedRegion.left, touchRadius) && 
            isNear(y, selectedRegion.top, touchRadius)) {
            return 0;
        }
        
        // Top-right
        if (isNear(x, selectedRegion.right, touchRadius) && 
            isNear(y, selectedRegion.top, touchRadius)) {
            return 1;
        }
        
        // Bottom-right
        if (isNear(x, selectedRegion.right, touchRadius) && 
            isNear(y, selectedRegion.bottom, touchRadius)) {
            return 2;
        }
        
        // Bottom-left
        if (isNear(x, selectedRegion.left, touchRadius) && 
            isNear(y, selectedRegion.bottom, touchRadius)) {
            return 3;
        }
        
        return -1;
    }
    
    /**
     * Check if two values are near each other
     */
    private boolean isNear(float a, float b, float threshold) {
        return Math.abs(a - b) <= threshold;
    }
    
    /**
     * Resize region from corner
     */
    private void resizeRegion(float x, float y) {
        switch (resizeCorner) {
            case 0: // Top-left
                selectedRegion.left = Math.min(x, selectedRegion.right - MIN_REGION_SIZE);
                selectedRegion.top = Math.min(y, selectedRegion.bottom - MIN_REGION_SIZE);
                break;
                
            case 1: // Top-right
                selectedRegion.right = Math.max(x, selectedRegion.left + MIN_REGION_SIZE);
                selectedRegion.top = Math.min(y, selectedRegion.bottom - MIN_REGION_SIZE);
                break;
                
            case 2: // Bottom-right
                selectedRegion.right = Math.max(x, selectedRegion.left + MIN_REGION_SIZE);
                selectedRegion.bottom = Math.max(y, selectedRegion.top + MIN_REGION_SIZE);
                break;
                
            case 3: // Bottom-left
                selectedRegion.left = Math.min(x, selectedRegion.right - MIN_REGION_SIZE);
                selectedRegion.bottom = Math.max(y, selectedRegion.top + MIN_REGION_SIZE);
                break;
        }
        
        constrainRegionToBounds();
    }
    
    /**
     * Keep region within view bounds
     */
    private void constrainRegionToBounds() {
        if (selectedRegion.left < 0) {
            selectedRegion.offset(-selectedRegion.left, 0);
        }
        if (selectedRegion.top < 0) {
            selectedRegion.offset(0, -selectedRegion.top);
        }
        if (selectedRegion.right > getWidth()) {
            selectedRegion.offset(getWidth() - selectedRegion.right, 0);
        }
        if (selectedRegion.bottom > getHeight()) {
            selectedRegion.offset(0, getHeight() - selectedRegion.bottom);
        }
    }
    
    /**
     * Get selected region as ImageRegion
     */
    public ImageRegion getSelectedRegion() {
        if (selectedRegion.isEmpty()) {
            return null;
        }
        
        Rect bounds = new Rect(
            (int) selectedRegion.left,
            (int) selectedRegion.top,
            (int) selectedRegion.right,
            (int) selectedRegion.bottom
        );
        
        ImageRegion region = new ImageRegion(bounds, getWidth(), getHeight());
        return region;
    }
    
    /**
     * Clear selection
     */
    public void clearSelection() {
        selectedRegion.setEmpty();
        invalidate();
    }
    
    /**
     * Set listener
     */
    public void setOnRegionSelectedListener(OnRegionSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Notify region selected
     */
    private void notifyRegionSelected() {
        if (listener != null && !selectedRegion.isEmpty()) {
            listener.onRegionSelected(getSelectedRegion());
        }
    }
    
    /**
     * Notify region changed (during drag/resize)
     */
    private void notifyRegionChanged() {
        if (listener != null && !selectedRegion.isEmpty()) {
            listener.onRegionChanged(getSelectedRegion());
        }
    }
}
