package com.example.learnquiz_fe.ui.adapter.quiz;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quizhistory.QuizHistoryItemDTO;
import com.example.learnquiz_fe.ui.activities.quiz.QuizHistoryDetailActivity; // Sẽ tạo ở bước sau

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<QuizHistoryItemDTO> historyList;
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    public QuizHistoryAdapter(Context context, List<QuizHistoryItemDTO> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public void updateData(List<QuizHistoryItemDTO> newHistoryList) {
        this.historyList.clear();
        this.historyList.addAll(newHistoryList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        QuizHistoryItemDTO item = historyList.get(position);

        // Lấy quizName từ DTO và hiển thị thay vì ID
        holder.tvQuizName.setText(item.getQuizName());

        // Định dạng ngày
        if (item.getCompletedAt() != null) {
            holder.tvCompletionDate.setText("Completed on: " + outputFormat.format(item.getCompletedAt()));
        }

        // Hiển thị điểm
        String scoreText = "Score: " + item.getCorrectCount() + "/" + item.getTotalQuestions() +
                " (" + String.format(Locale.US, "%.0f", item.getPercentage()) + "%)";
        holder.tvScore.setText(scoreText);

        // Xử lý sự kiện click
        holder.btnViewDetails.setOnClickListener(v -> {
            // Tạo Intent để mở QuizHistoryDetailActivity
            Intent intent = new Intent(context, QuizHistoryDetailActivity.class);

            // Đóng gói toàn bộ đối tượng item và gửi đi
            // QuizHistoryItemDTO đã implement Serializable nên có thể gửi trực tiếp
            intent.putExtra("QUIZ_HISTORY_ITEM", item);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizName, tvCompletionDate, tvScore;
        Button btnViewDetails;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ chính xác các ID từ item_quiz_history.xml
            // Lưu ý: Tôi đổi tên tv_quiz_id_history thành tvQuizName để dễ hiểu hơn
            tvQuizName = itemView.findViewById(R.id.tv_quiz_id_history);
            tvCompletionDate = itemView.findViewById(R.id.tv_completion_date);
            tvScore = itemView.findViewById(R.id.tv_score);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}