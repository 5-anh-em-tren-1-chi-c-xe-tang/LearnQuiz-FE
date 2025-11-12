package com.example.learnquiz_fe.ui.adapter.feedback; // Sửa lại package nếu cần

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.feedback.Feedback;
// import com.bumptech.glide.Glide; // Bỏ comment nếu bạn dùng Glide để tải avatar

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FeedbackAdapter extends ListAdapter<Feedback, FeedbackAdapter.FeedbackViewHolder> {

    private final String currentUserId;
    private final EditClickListener editClickListener;

    // Interface để xử lý sự kiện click "Edit"
    public interface EditClickListener {
        void onEditClicked(Feedback feedback);
    }

    public FeedbackAdapter(String currentUserId, EditClickListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.editClickListener = listener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = getItem(position);
        holder.bind(feedback, currentUserId, editClickListener);
    }

    // ViewHolder class
    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUserName, tvTimestamp, tvCommentBody;
        RatingBar ratingBarItem;
        ImageButton btnEditFeedback;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvCommentBody = itemView.findViewById(R.id.tvCommentBody);
            ratingBarItem = itemView.findViewById(R.id.ratingBarItem);
            btnEditFeedback = itemView.findViewById(R.id.btnEditFeedback);
        }

        public void bind(Feedback feedback, String currentUserId, EditClickListener listener) {
            // TODO: API của bạn chỉ trả về userId. Bạn cần 1 cách để lấy
            // tên và avatar từ userId đó. Hiện tại, chúng ta sẽ để tên giả.
            tvUserName.setText("User " + feedback.getUserId().substring(0, 6) + "..."); // Tên tạm

            // TODO: Tải avatar bằng Glide
            // Glide.with(itemView.getContext()).load(user.getAvatarUrl()).into(ivAvatar);
            ivAvatar.setImageResource(R.drawable.ic_profile); // Ảnh profile mặc định

            tvCommentBody.setText(feedback.getComment());
            ratingBarItem.setRating(feedback.getRating());

            // Format ngày tháng
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            tvTimestamp.setText(sdf.format(feedback.getCreatedAt()));

            // Logic hiển thị nút Edit
            if (feedback.getUserId().equals(currentUserId)) {
                btnEditFeedback.setVisibility(View.VISIBLE);
                btnEditFeedback.setOnClickListener(v -> listener.onEditClicked(feedback));
            } else {
                btnEditFeedback.setVisibility(View.GONE);
            }
        }
    }

    // DiffUtil để RecyclerView cập nhật hiệu quả
    private static final DiffUtil.ItemCallback<Feedback> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Feedback>() {
                @Override
                public boolean areItemsTheSame(@NonNull Feedback oldItem, @NonNull Feedback newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Feedback oldItem, @NonNull Feedback newItem) {
                    return oldItem.getComment().equals(newItem.getComment()) &&
                            oldItem.getRating() == newItem.getRating();
                }
            };
}