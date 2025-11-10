// @/app/src/main/java/com/example/learnquiz_fe/ui/fragments/myquizzes/MyQuizzesFragment.java
package com.example.learnquiz_fe.ui.fragments.myquizzes;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.ui.adapter.myquizzes.MyQuizAdapter; // Import MyQuizAdapter
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
// Removed unused imports: SimpleDateFormat, ParseException, Date, Locale

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyQuizzesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyQuizzesFragment extends Fragment {

    private static final String TAG = "MyQuizzesFragment";

    private MyQuizAdapter quizAdapter; // Changed type to MyQuizAdapter
    private boolean isFabMenuOpen = false;
    private QuizRepository quizRepository;

    // Declare all views that were previously accessed via binding
    private RecyclerView rvQuizzes;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
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

    public MyQuizzesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyQuizzesFragment.
     */
    public static MyQuizzesFragment newInstance(String param1, String param2) {
        MyQuizzesFragment fragment = new MyQuizzesFragment();
        Bundle args = new Bundle();
        // args.putString(ARG_PARAM1, param1);
        // args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_quizzes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all views using findViewById
        rvQuizzes = view.findViewById(R.id.rv_quizzes);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvQuizCountInfo = view.findViewById(R.id.tv_quiz_count_info);
        btnCreateQuizHeader = view.findViewById(R.id.btn_create_quiz_header);
        etSearchQuizzes = view.findViewById(R.id.et_search_quizzes);
        ivFilterIcon = view.findViewById(R.id.iv_filter_icon);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        chipAllQuizzes = view.findViewById(R.id.chip_all_quizzes);
        fabMainCreate = view.findViewById(R.id.fab_main_create);
        fabFromText = view.findViewById(R.id.fab_from_text);
        tvFromTextLabel = view.findViewById(R.id.tv_from_text_label);
        fabFromImage = view.findViewById(R.id.fab_from_image);
        tvFromImageLabel = view.findViewById(R.id.tv_from_image_label);

        quizRepository = new QuizRepository(requireContext());

        setupRecyclerView();
        setupFabMenu();
        setupCategoryChips();

        loadMyQuizzes(); // Call without query, MyQuizAdapter handles filtering if query passed

        btnCreateQuizHeader.setOnClickListener(v -> Toast.makeText(getContext(), "Create Quiz button clicked!", Toast.LENGTH_SHORT).show());
        btnUpgrade.setOnClickListener(v -> Toast.makeText(getContext(), "Upgrade button clicked!", Toast.LENGTH_SHORT).show());
        etSearchQuizzes.setOnClickListener(v -> Toast.makeText(getContext(), "Search input clicked!", Toast.LENGTH_SHORT).show());
        ivFilterIcon.setOnClickListener(v -> Toast.makeText(getContext(), "Filter icon clicked!", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView() {
        quizAdapter = new MyQuizAdapter(new ArrayList<>()); // Pass an empty list initially
        rvQuizzes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvQuizzes.setAdapter(quizAdapter);
    }

    private void setupFabMenu() {
        fabMainCreate.setOnClickListener(v -> toggleFabMenu());
        fabFromText.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Navigate to HomeFragment for Text creation!", Toast.LENGTH_SHORT).show();
            toggleFabMenu();
        });
        fabFromImage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Navigate to HomeFragment for Image creation!", Toast.LENGTH_SHORT).show();
            toggleFabMenu();
        });
        setFabMenuVisibility(false);
    }

    private void toggleFabMenu() {
        isFabMenuOpen = !isFabMenuOpen;
        setFabMenuVisibility(isFabMenuOpen);

        float rotation = isFabMenuOpen ? 45f : 0f;
        fabMainCreate.animate().rotation(rotation).setDuration(300).start();
    }

    private void setFabMenuVisibility(boolean show) {
        if (show) {
            fabFromText.setVisibility(View.VISIBLE);
            tvFromTextLabel.setVisibility(View.VISIBLE);
            fabFromImage.setVisibility(View.VISIBLE);
            tvFromImageLabel.setVisibility(View.VISIBLE);

            // Animate appearance
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(fabFromText, "translationY", -getResources().getDimension(R.dimen.fab_margin_offset_large)),
                    ObjectAnimator.ofFloat(tvFromTextLabel, "translationY", -getResources().getDimension(R.dimen.fab_margin_offset_large)),
                    ObjectAnimator.ofFloat(fabFromImage, "translationY", -getResources().getDimension(R.dimen.fab_margin_offset_small)),
                    ObjectAnimator.ofFloat(tvFromImageLabel, "translationY", -getResources().getDimension(R.dimen.fab_margin_offset_small))
            );
            animatorSet.setDuration(300);
            animatorSet.start();

        } else {
            // Animate disappearance
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(fabFromText, "translationY", 0f),
                    ObjectAnimator.ofFloat(tvFromTextLabel, "translationY", 0f),
                    ObjectAnimator.ofFloat(fabFromImage, "translationY", 0f),
                    ObjectAnimator.ofFloat(tvFromImageLabel, "translationY", 0f)
            );
            animatorSet.setDuration(300);
            animatorSet.start();

            fabFromText.postDelayed(() -> {
                fabFromText.setVisibility(View.GONE);
                tvFromTextLabel.setVisibility(View.GONE);
                fabFromImage.setVisibility(View.GONE);
                tvFromImageLabel.setVisibility(View.GONE);
            }, 300);
        }
    }

    private void setupCategoryChips() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAllQuizzes.setChecked(true);
            } else {
                for (int id : checkedIds) {
                    Chip chip = group.findViewById(id);
                    if (chip != null) {
                        Toast.makeText(getContext(), "Category selected: " + chip.getText(), Toast.LENGTH_SHORT).show();
                        // TODO: Implement filtering logic here and then call loadMyQuizzes()
                        // Example: loadMyQuizzes(chip.getText().toString());
                    }
                }
            }
        });
    }

    // --- Method to load quizzes from repository ---
    // Removed 'String query' parameter as getMyQuizzes from repository doesn't take one.
    // If you add search/filter to the API, re-add the parameter.
    private void loadMyQuizzes() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        rvQuizzes.setVisibility(View.GONE);

        Log.d(TAG, "Loading my quizzes.");

        quizRepository.getMyQuizzes(new QuizRepository.GenericQuizCallback() {
            @Override
            public void onSuccess(List<QuizResponseDTO> quizResponseDTOList) {
                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Loaded " + quizResponseDTOList.size() + " quizzes.");

                // Directly pass the DTO list to the adapter
                quizAdapter.setQuizzes(quizResponseDTOList);
                handleEmptyState(quizResponseDTOList.isEmpty());
            }

            @Override
            public void onError(String message, int errorCode) {
                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading my quizzes: " + message + " (Code: " + errorCode + ")");
                Toast.makeText(getContext(), "Error loading quizzes: " + message, Toast.LENGTH_LONG).show();
                handleEmptyState(quizAdapter.getItemCount() == 0);
            }
        });
    }

    private void handleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvQuizzes.setVisibility(View.GONE);
            tvQuizCountInfo.setText(String.format(getString(R.string.quiz_count_info), 0, 0));
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvQuizzes.setVisibility(View.VISIBLE);
            tvQuizCountInfo.setText(String.format(getString(R.string.quiz_count_info), quizAdapter.getItemCount(), quizAdapter.getItemCount()));
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