package com.example.learnquiz_fe.ui.fragments.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.learnquiz_fe.ui.activities.quiz.QuizDetailActivity;
import com.example.learnquiz_fe.ui.adapter.quiz.QuizListAdapter;

import java.util.List;

public class HomeFragment extends Fragment {

    private QuizRepository quizRepository;
    private QuizListAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etSearch;
    private ImageButton btnClear, btnSearch;
    private TextView tvEmptyState;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_quiz_list, container, false);

        // Find and assign all the possible btn click / views
        findViews(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // require context instead of this because we are in the fragment
        adapter = new QuizListAdapter();
        recyclerView.setAdapter(adapter);

        quizRepository = new QuizRepository(requireContext());
        loadPublicQuizzes();

        // Set on item click listener for quiz items
        adapter.setOnItemClickListener(quiz -> {
           // Navigate to quiz detail fragment
            Intent intent = new Intent(requireContext(), QuizDetailActivity.class);
            intent.putExtra("quiz_id", quiz.getId());
            startActivity(intent);
        });

        // setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::reloadQuizzes);

        // Show or hide clear button dynamically
        addClearTextListener();

        handleSearchInput();
        return view;
    }

    private void addClearTextListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void handleSearchInput() {
        // Clear input and reload quizzes
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            loadPublicQuizzes(); // reload full list when cleared
        });

        // Trigger search when clicking the search icon
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            searchQuizzes(query);
        });

        // OPTIONAL (DEBUG ONLY) trigger search when pressing "Enter" on keyboard
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                searchQuizzes(query);
                return true;
            }
            return false;
        });
    }

    private void findViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerQuizzes);
        progressBar = view.findViewById(R.id.progressBar);
        etSearch = view.findViewById(R.id.etSearch);
        btnClear = view.findViewById(R.id.btnClear);
        btnSearch = view.findViewById(R.id.btnSearch);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void handleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void searchQuizzes(String query) {
        if (query.isEmpty()) {
            loadPublicQuizzes();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d("QuizListActivity", "Loading public quizzes");
        quizRepository.getPublicQuizzes(new QuizRepository.GenericCallback<List<QuizResponseDTO>>() {
            @Override
            public void onSuccess(List<QuizResponseDTO> quizzes) {
                Log.d("QuizListActivity", "Loaded " + quizzes.size() + " public quizzes");
                progressBar.setVisibility(View.GONE);
                adapter.setQuizzes(quizzes);
                handleEmptyState(adapter.getItemCount() == 0);
            }

            @Override
            public void onError(String message, int errorCode) {
                Log.d("QuizListActivity", "Error loading public quizzes: " + message);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                handleEmptyState(adapter.getItemCount() == 0);
            }
        }, query);
    }

    // Load public quizzes from repository
    // Note that this pass empty query to get all quizzes
    private void loadPublicQuizzes() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("QuizListActivity", "Loading public quizzes");
        quizRepository.getPublicQuizzes(new QuizRepository.GenericCallback<List<QuizResponseDTO>>() {
            @Override
            public void onSuccess(List<QuizResponseDTO> quizzes) {
                Log.d("QuizListActivity", "Loaded " + quizzes.size() + " public quizzes");
                progressBar.setVisibility(View.GONE);
                adapter.setQuizzes(quizzes);

                handleEmptyState(quizzes.isEmpty());
            }

            @Override
            public void onError(String message, int errorCode) {
                Log.d("QuizListActivity", "Error loading public quizzes: " + message);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                handleEmptyState(adapter.getItemCount() == 0);
            }
        }, null);
    }

    private void reloadQuizzes() {
        swipeRefreshLayout.setRefreshing(true);
        loadPublicQuizzes();
        swipeRefreshLayout.setRefreshing(false);
        handleEmptyState(adapter.getItemCount() == 0);
    }

}
