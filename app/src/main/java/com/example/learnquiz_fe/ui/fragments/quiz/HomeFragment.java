package com.example.learnquiz_fe.ui.fragments.quiz;

import android.os.Bundle;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.example.learnquiz_fe.ui.adapter.quiz.QuizListAdapter;

import java.util.List;

public class HomeFragment extends Fragment {

    private QuizRepository quizRepository;
    private QuizListAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_quiz_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerQuizzes);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // require context instead of this because we are in the fragment
        adapter = new QuizListAdapter();
        recyclerView.setAdapter(adapter);

        quizRepository = new QuizRepository(requireContext());
        loadPublicQuizzes();

        // setup swipe to refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this::reloadQuizzes);

        return view;
    }

    private void loadPublicQuizzes() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("QuizListActivity", "Loading public quizzes");
        quizRepository.getPublicQuizzes(new QuizRepository.GenericQuizCallback() {
            @Override
            public void onSuccess(List<QuizResponseDTO> quizzes) {
                Log.d("QuizListActivity", "Loaded " + quizzes.size() + " public quizzes");
                progressBar.setVisibility(View.GONE);
                adapter.setQuizzes(quizzes);
            }

            @Override
            public void onError(String message, int errorCode) {
                Log.d("QuizListActivity", "Error loading public quizzes: " + message);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reloadQuizzes() {
        swipeRefreshLayout.setRefreshing(true);
        loadPublicQuizzes();
        swipeRefreshLayout.setRefreshing(false);
    }
}
