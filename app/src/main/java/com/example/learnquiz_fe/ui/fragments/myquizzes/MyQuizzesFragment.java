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
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.example.learnquiz_fe.ui.adapter.myquizzes.MyQuizAdapter;
import com.example.learnquiz_fe.ui.fragments.myquizzes.QuizUpdateFragment;
import java.util.ArrayList;
import java.util.List;

public class MyQuizzesFragment extends Fragment {

    private RecyclerView rvQuizzes;
    private ProgressBar progressBar;
    private MyQuizAdapter quizAdapter;
    private QuizRepository quizRepository;

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

        quizRepository = new QuizRepository(requireContext());

        setupRecyclerView();
        loadMyQuizzes();
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
}