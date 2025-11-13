package com.example.learnquiz_fe.ui.adapter.myquizzes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu; // Import PopupMenu
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyQuizAdapter extends RecyclerView.Adapter<MyQuizAdapter.QuizViewHolder> {

    private static final String TAG = "MyQuizAdapter";

    // 1. Định nghĩa Interface để Fragment lắng nghe sự kiện
    public interface OnQuizActionClickListener {
        void onEdit(QuizResponseDTO quiz);
        void onDelete(String quizId);
    }

    private List<QuizResponseDTO> quizList;
    private OnQuizActionClickListener actionListener; // Biến listener

    // 2. Cập nhật Constructor để nhận Listener
    public MyQuizAdapter(List<QuizResponseDTO> quizList, OnQuizActionClickListener listener) {
        this.quizList = quizList;
        this.actionListener = listener;
    }

    // Constructor cũ (nếu cần giữ tương thích, có thể để lại nhưng gán listener null)
    public MyQuizAdapter(List<QuizResponseDTO> quizList) {
        this(quizList, null);
    }

    public void setQuizzes(List<QuizResponseDTO> newQuizzes) {
        this.quizList.clear();
        this.quizList.addAll(newQuizzes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myquiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        // Truyền listener vào hàm bind
        holder.bind(quizList.get(position), actionListener);
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizTitle;
        TextView tvQuizDescription;
        Chip chipQuestionsCount;
        Chip chipCategory;
        TextView tvPrivacyStatus;
        ImageView ivPrivacyIcon;
        TextView tvPlaysCount;
        TextView tvQuizDate;
        ImageView ivMoreOptions;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuizTitle = itemView.findViewById(R.id.tv_quiz_title);
            tvQuizDescription = itemView.findViewById(R.id.tv_quiz_description);
            chipQuestionsCount = itemView.findViewById(R.id.chip_questions_count);
            chipCategory = itemView.findViewById(R.id.chip_category);
            tvPrivacyStatus = itemView.findViewById(R.id.tv_privacy_status);
            ivPrivacyIcon = itemView.findViewById(R.id.iv_privacy_icon);
            tvPlaysCount = itemView.findViewById(R.id.tv_plays_count);
            tvQuizDate = itemView.findViewById(R.id.tv_quiz_date);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
        }

        // 3. Cập nhật hàm bind để nhận Listener và xử lý Menu
        public void bind(QuizResponseDTO quizResponseDTO, OnQuizActionClickListener listener) {
            tvQuizTitle.setText(quizResponseDTO.getTitle());
            tvQuizDescription.setText(quizResponseDTO.getDescription());

            // Questions Count
            int questionsCount = quizResponseDTO.getQuestions() != null ? quizResponseDTO.getQuestions().size() : 0;
            chipQuestionsCount.setText(String.format(itemView.getContext().getString(R.string.questions_count), questionsCount));

            // Category
            chipCategory.setText(quizResponseDTO.getContext());

            // Privacy Status
            boolean isPublic = "public".equalsIgnoreCase(quizResponseDTO.getVisibility());
            tvPrivacyStatus.setText(isPublic ? R.string.privacy_status_public : R.string.privacy_status_private);
            ivPrivacyIcon.setImageResource(isPublic ? R.drawable.ic_public : R.drawable.ic_lock);

            // Plays Count
            int playsCount = (int) quizResponseDTO.getRatingCount();
            tvPlaysCount.setText(String.format(itemView.getContext().getString(R.string.plays_count), playsCount));

            // Date Formatting
            String formattedDate = "";
            if (quizResponseDTO.getCreatedAt() != null && !quizResponseDTO.getCreatedAt().isEmpty()) {
                // Định dạng ngày tháng từ backend (có thể cần điều chỉnh tùy API thực tế trả về)
                // Giả sử backend trả về ISO 8601 hoặc format đơn giản
                SimpleDateFormat inputSdf;
                // Kiểm tra nhanh xem string có ký tự 'T' (ISO 8601) hay không để chọn format
                if (quizResponseDTO.getCreatedAt().contains("T")) {
                    inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                } else {
                    // Format fallback cho dd-MM-yyyy như thấy trong Backend service
                    inputSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                }

                SimpleDateFormat outputSdf = new SimpleDateFormat("MMM dd", Locale.getDefault()); // e.g., Oct 25
                try {
                    Date date = inputSdf.parse(quizResponseDTO.getCreatedAt());
                    if (date != null) {
                        formattedDate = outputSdf.format(date);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date string: " + quizResponseDTO.getCreatedAt(), e);
                    formattedDate = quizResponseDTO.getCreatedAt(); // Fallback to original string
                }
            }
            tvQuizDate.setText(formattedDate);

            // --- XỬ LÝ MENU 3 CHẤM (MORE OPTIONS) ---
            ivMoreOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), ivMoreOptions);
                // Thêm item vào menu (ID 1: Edit, ID 2: Delete)
                popup.getMenu().add(0, 1, 0, "Edit");
                popup.getMenu().add(0, 2, 1, "Delete");

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;

                    if (item.getItemId() == 1) {
                        // Click Edit -> Gọi callback onEdit
                        listener.onEdit(quizResponseDTO);
                        return true;
                    } else if (item.getItemId() == 2) {
                        // Click Delete -> Gọi callback onDelete
                        listener.onDelete(quizResponseDTO.getId());
                        return true;
                    }
                    return false;
                });
                popup.show();
            });

            // Click vào card để xem chi tiết (nếu cần)
            itemView.setOnClickListener(v -> {
                // Có thể thêm logic điều hướng đến trang chơi Quiz tại đây
            });
        }
    }
}