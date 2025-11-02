package com.example.learnquiz_fe.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.camera.CapturedImage;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying photo thumbnails in horizontal RecyclerView
 */
public class PhotoThumbnailAdapter extends RecyclerView.Adapter<PhotoThumbnailAdapter.ThumbnailViewHolder> {
    
    private static final String TAG = "PhotoThumbnailAdapter";
    
    private List<CapturedImage> images;
    private OnImageClickListener listener;
    
    public interface OnImageClickListener {
        void onImageClick(CapturedImage image, int position);
        void onDeleteClick(CapturedImage image, int position);
    }
    
    public PhotoThumbnailAdapter(OnImageClickListener listener) {
        this.images = new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "Adapter created with listener: " + (listener != null ? "SET" : "NULL"));
    }
    
    public void setImages(List<CapturedImage> images) {
        this.images = images != null ? images : new ArrayList<>();
        Log.d(TAG, "setImages called: " + this.images.size() + " images");
        notifyDataSetChanged();
    }
    
    public void addImage(CapturedImage image) {
        if (image != null) {
            images.add(image);
            notifyItemInserted(images.size() - 1);
            Log.d(TAG, "addImage: total=" + images.size());
        }
    }
    
    public void removeImage(int position) {
        if (position >= 0 && position < images.size()) {
            images.remove(position);
            notifyItemRemoved(position);
            Log.d(TAG, "removeImage: position=" + position + ", remaining=" + images.size());
        }
    }
    
    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        CapturedImage image = images.get(position);
        
        Log.d(TAG, "onBindViewHolder: position=" + position + ", imageId=" + image.getId());
        
        // Load thumbnail with Glide (high quality for sharp display)
        // Use .dontAnimate() to prevent Glide from caching recycled bitmaps
        if (image.getThumbnail() != null && !image.getThumbnail().isRecycled()) {
            Glide.with(holder.itemView.getContext())
                    .load(image.getThumbnail())
                    .centerCrop()
                    .skipMemoryCache(true) // Don't cache bitmap in memory to avoid recycled bitmap issues
                    .into(holder.ivThumbnail);
        } else if (image.getImageUri() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(image.getImageUri())
                    .centerCrop()
                    .into(holder.ivThumbnail);
        }
        
        // Click listeners
        // Set thumbnail click (on the ImageView directly to avoid conflicts)
        holder.ivThumbnail.setOnClickListener(v -> {
            Log.d(TAG, "Thumbnail clicked: position=" + position);
            if (listener != null) {
                listener.onImageClick(image, position);
            } else {
                Log.w(TAG, "Listener is null!");
            }
        });
        
        // Also set on card view for larger touch target
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "ItemView clicked: position=" + position);
            if (listener != null) {
                listener.onImageClick(image, position);
            } else {
                Log.w(TAG, "Listener is null!");
            }
        });
        
        // Delete button - stop propagation to prevent triggering itemView click
        holder.btnDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete button clicked: position=" + position);
            if (listener != null) {
                listener.onDeleteClick(image, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return images.size();
    }
    
    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        MaterialButton btnDelete;
        
        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
