package com.example.learnquiz_fe.ui.fragments.myquizzes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.network.RetrofitClient;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.example.learnquiz_fe.ui.adapter.myquizzes.MyQuizAdapter;
import com.example.learnquiz_fe.ui.fragments.myquizzes.QuizUpdateFragment;
import com.example.learnquiz_fe.ui.adapter.myquizzes.MyQuizAdapter; // Import MyQuizAdapter
import com.example.learnquiz_fe.ui.fragments.payment.UpgradePremiumFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MyQuizzesFragment extends Fragment {

    private RecyclerView rvQuizzes;
    private ProgressBar progressBar;
    private MyQuizAdapter quizAdapter;
    private QuizRepository quizRepository;
    private LinearLayout tvEmptyState;
    private TextView tvQuizCountInfo;
    private MaterialButton btnCreateQuizHeader;
    private EditText etSearchQuizzes;
    private ImageView ivFilterIcon;
    private MaterialButton btnUpgrade;
    private ChipGroup chipGroupCategories;
    private Chip chipAllQuizzes;
    private FloatingActionButton fabMainCreate;
    private FloatingActionButton fabFromText;
    private TextView tvFromTextLabel;
    private FloatingActionButton fabFromImage;
    private TextView tvFromImageLabel;
    private MaterialCardView cardUpgradePrompt;

    public MyQuizzesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_quizzes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvQuizzes = view.findViewById(R.id.rv_quizzes);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvQuizCountInfo = view.findViewById(R.id.tv_quiz_count_info);
        btnCreateQuizHeader = view.findViewById(R.id.btn_create_quiz_header);
        etSearchQuizzes = view.findViewById(R.id.et_search_quizzes);
        ivFilterIcon = view.findViewById(R.id.iv_filter_icon);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        cardUpgradePrompt = view.findViewById(R.id.limit_banner_card);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        chipAllQuizzes = view.findViewById(R.id.chip_all_quizzes);
        fabMainCreate = view.findViewById(R.id.fab_main_create);
        fabFromText = view.findViewById(R.id.fab_from_text);
        tvFromTextLabel = view.findViewById(R.id.tv_from_text_label);
        fabFromImage = view.findViewById(R.id.fab_from_image);
        tvFromImageLabel = view.findViewById(R.id.tv_from_image_label);

        quizRepository = new QuizRepository(requireContext());

        setupRecyclerView();
        loadMyQuizzes();

        boolean isPremium = RetrofitClient.getInstance(getContext()).getIsPremium();

        if (!isPremium) {
            cardUpgradePrompt.setVisibility(View.VISIBLE);
            btnUpgrade.setOnClickListener(v -> {
                // Start upgrade fragment
                Fragment fragment = new UpgradePremiumFragment();

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            cardUpgradePrompt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại danh sách khi quay lại từ màn hình Update
        loadMyQuizzes();
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter với Callback
        quizAdapter = new MyQuizAdapter(new ArrayList<>(), new MyQuizAdapter.OnQuizActionClickListener() {
            @Override
            public void onEdit(QuizResponseDTO quiz) {
                // Chuyển sang màn hình Update
                QuizUpdateFragment updateFragment = QuizUpdateFragment.newInstance(quiz.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, updateFragment) // ID container trong MainActivity
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(String quizId) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this quiz?")
                        .setPositiveButton("Delete", (dialog, which) -> performDelete(quizId))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rvQuizzes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvQuizzes.setAdapter(quizAdapter);
    }

    private void performDelete(String quizId) {
        progressBar.setVisibility(View.VISIBLE);
        quizRepository.deleteQuiz(quizId, new QuizRepository.GenericCallback<Object>() {
            @Override
            public void onSuccess(Object response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                loadMyQuizzes(); // Refresh list
            }

            @Override
            public void onError(String message, int errorCode) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Delete failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyQuizzes() {
        progressBar.setVisibility(View.VISIBLE);
        quizRepository.getMyQuizzes(new QuizRepository.GenericCallback<List<QuizResponseDTO>>() {
            @Override
            public void onSuccess(List<QuizResponseDTO> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                quizAdapter.setQuizzes(response);
            }

            @Override
            public void onError(String message, int errorCode) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                // Handle empty or error state
            }
        });
    }

    private void handleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvQuizzes.setVisibility(View.GONE);
//            tvQuizCountInfo.setText(String.format(getString(R.string.quiz_count_info), 0, 0));
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvQuizzes.setVisibility(View.VISIBLE);
//            tvQuizCountInfo.setText(String.format(getString(R.string.quiz_count_info), quizAdapter.getItemCount(), quizAdapter.getItemCount()));
        }
    }

    // REMOVED: No longer needed as adapter directly uses QuizResponseDTO
    // private Quiz mapQuizResponseDTOToQuiz(QuizResponseDTO dto) { ... }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // No binding to clean up
    }

    // REMOVED: No longer needed as MyQuizAdapter directly uses QuizResponseDTO
    // public static class Quiz { ... }
}