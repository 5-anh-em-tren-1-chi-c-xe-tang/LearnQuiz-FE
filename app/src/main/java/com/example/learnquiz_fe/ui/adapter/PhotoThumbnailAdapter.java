package com.example.learnquiz_fe.ui.adapter;

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
    
    private List<CapturedImage> images;
    private OnImageClickListener listener;
    
    public interface OnImageClickListener {
        void onImageClick(CapturedImage image, int position);
        void onDeleteClick(CapturedImage image, int position);
    }
    
    public PhotoThumbnailAdapter(OnImageClickListener listener) {
        this.images = new ArrayList<>();
        this.listener = listener;
    }
    
    public void setImages(List<CapturedImage> images) {
        this.images = images != null ? images : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addImage(CapturedImage image) {
        if (image != null) {
            images.add(image);
            notifyItemInserted(images.size() - 1);
        }
    }
    
    public void removeImage(int position) {
        if (position >= 0 && position < images.size()) {
            images.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        CapturedImage image = images.get(position);
        
        // Load thumbnail with Glide
        if (image.getThumbnail() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(image.getThumbnail())
                    .centerCrop()
                    .into(holder.ivThumbnail);
        } else if (image.getImageUri() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(image.getImageUri())
                    .centerCrop()
                    .into(holder.ivThumbnail);
        }
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(image, position);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
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
