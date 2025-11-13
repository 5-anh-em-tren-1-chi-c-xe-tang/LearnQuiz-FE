package com.example.learnquiz_fe.ui.fragments.myquizzes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.model.quiz.QuizAnswer;
import com.example.learnquiz_fe.data.model.quiz.QuizQuestion;
import com.example.learnquiz_fe.data.repository.QuizRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class QuizUpdateFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private String quizId;
    private QuizRepository quizRepository;
    private GenerateQuizResponse currentQuizData;

    // Views
    private TextInputEditText etQuizTitle, etQuizDescription, etDuration;
    private LinearLayout questionsContainer;
    private MaterialButton btnAddQuestion, btnUpdateQuiz;
    private ImageButton btnBack, btnSave;
    private ProgressBar progressBar;

    public static QuizUpdateFragment newInstance(String quizId) {
        QuizUpdateFragment fragment = new QuizUpdateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUIZ_ID, quizId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) quizId = getArguments().getString(ARG_QUIZ_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        quizRepository = new QuizRepository(requireContext());

        // Load dữ liệu
        loadQuizDetail();

        // Sự kiện Add Question
        btnAddQuestion.setOnClickListener(v -> addQuestionView(null)); // Null = tạo câu hỏi trống

        // Sự kiện Save
        View.OnClickListener saveAction = v -> collectAndSaveData();
        btnUpdateQuiz.setOnClickListener(saveAction);
        btnSave.setOnClickListener(saveAction);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void initViews(View view) {
        etQuizTitle = view.findViewById(R.id.etQuizTitle);
        etQuizDescription = view.findViewById(R.id.etQuizDescription);
        etDuration = view.findViewById(R.id.etDuration);
        questionsContainer = view.findViewById(R.id.questionsContainer);
        btnAddQuestion = view.findViewById(R.id.btnAddQuestion);
        btnUpdateQuiz = view.findViewById(R.id.btnUpdateQuiz);
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadQuizDetail() {
        progressBar.setVisibility(View.VISIBLE);
        quizRepository.getQuizDetail(quizId, new QuizRepository.QuizCallback() {
            @Override
            public void onSuccess(GenerateQuizResponse response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                currentQuizData = response;

                // Fill thông tin cơ bản
                etQuizTitle.setText(response.getTitle());
                etQuizDescription.setText(response.getDescription());
                etDuration.setText(String.valueOf(response.getQuizExamTimeLimit()));

                // Fill danh sách câu hỏi
                questionsContainer.removeAllViews(); // Xóa view hardcode trong XML
                if (response.getQuestions() != null) {
                    for (QuizQuestion q : response.getQuestions()) {
                        addQuestionView(q);
                    }
                }
            }

            @Override
            public void onError(String message, int errorCode) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Load error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm quan trọng: Tạo giao diện cho 1 câu hỏi và thêm vào Container
     */
    private void addQuestionView(@Nullable QuizQuestion questionData) {
        View questionView = LayoutInflater.from(getContext()).inflate(R.layout.item_question_edit, questionsContainer, false);

        TextView tvIndex = questionView.findViewById(R.id.tvQuestionIndex);
        ImageButton btnDelete = questionView.findViewById(R.id.btnDeleteQuestion);
        TextInputEditText etContent = questionView.findViewById(R.id.etQuestionContent);
        LinearLayout answersContainer = questionView.findViewById(R.id.answersContainer);

        // Set index
        int index = questionsContainer.getChildCount() + 1;
        tvIndex.setText("Question " + index);

        // Logic xóa câu hỏi
        btnDelete.setOnClickListener(v -> {
            questionsContainer.removeView(questionView);
            refreshQuestionIndexes(); // Cập nhật lại số thứ tự (Q1, Q2...)
        });

        // Nếu có dữ liệu cũ (Load từ API) -> Fill vào
        if (questionData != null) {
            etContent.setText(questionData.getQuestion());
            // Fill 4 đáp án
            if (questionData.getAnswers() != null) {
                for (QuizAnswer ans : questionData.getAnswers()) {
                    addAnswerRow(answersContainer, ans);
                }
            }
        } else {
            // Nếu tạo mới -> Tạo sẵn 4 ô đáp án trống
            for (int i = 0; i < 4; i++) addAnswerRow(answersContainer, null);
        }

        questionsContainer.addView(questionView);
    }

    private void refreshQuestionIndexes() {
        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View child = questionsContainer.getChildAt(i);
            TextView tv = child.findViewById(R.id.tvQuestionIndex);
            tv.setText("Question " + (i + 1));
        }
    }

    // Tạo 1 dòng đáp án (Checkbox + EditText)
    private void addAnswerRow(LinearLayout container, @Nullable QuizAnswer ansData) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(0, 8, 0, 8);

        CheckBox cb = new CheckBox(getContext());
        TextInputEditText et = new TextInputEditText(getContext());
        et.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        et.setHint("Option");

        if (ansData != null) {
            cb.setChecked(ansData.isTrue());
            et.setText(ansData.getAnswer());
        }

        row.addView(cb);
        row.addView(et);
        container.addView(row);
    }

    // Thu thập dữ liệu từ giao diện để gửi API
    private void collectAndSaveData() {
        if (currentQuizData == null) return;

        // 1. Lấy Basic Info
        currentQuizData.setTitle(etQuizTitle.getText().toString());
        currentQuizData.setDescription(etQuizDescription.getText().toString());
        try {
            currentQuizData.setQuizExamTimeLimit(Integer.parseInt(etDuration.getText().toString()));
        } catch (NumberFormatException e) {
            currentQuizData.setQuizExamTimeLimit(0);
        }

        // 2. Duyệt qua questionsContainer để lấy danh sách câu hỏi mới
        List<QuizQuestion> newQuestions = new ArrayList<>();

        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View qView = questionsContainer.getChildAt(i);
            TextInputEditText etContent = qView.findViewById(R.id.etQuestionContent);
            LinearLayout ansContainer = qView.findViewById(R.id.answersContainer);

            QuizQuestion qObj = new QuizQuestion();
            qObj.setQuestion(etContent.getText().toString());

            List<QuizAnswer> answers = new ArrayList<>();
            for (int j = 0; j < ansContainer.getChildCount(); j++) {
                LinearLayout row = (LinearLayout) ansContainer.getChildAt(j);
                CheckBox cb = (CheckBox) row.getChildAt(0);
                TextInputEditText etAns = (TextInputEditText) row.getChildAt(1);

                QuizAnswer ansObj = new QuizAnswer();
                ansObj.setTrue(cb.isChecked());
                ansObj.setAnswer(etAns.getText().toString());
                answers.add(ansObj);
            }
            qObj.setAnswers(answers);
            newQuestions.add(qObj);
        }

        currentQuizData.setQuestions(newQuestions);

        // 3. Gọi API
        progressBar.setVisibility(View.VISIBLE);
        quizRepository.updateQuiz(quizId, currentQuizData, new QuizRepository.GenericCallback<Object>() {
            @Override
            public void onSuccess(Object response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onError(String message, int errorCode) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}