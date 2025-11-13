package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.model.quiz.QuizAnswer;
import com.example.learnquiz_fe.data.model.quiz.QuizQuestion;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizRequestDTO;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizResponseDTO;
import com.example.learnquiz_fe.data.model.quizhistory.SubmittedAnswerDTO;
import com.example.learnquiz_fe.data.repository.QuizHistoryRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for taking a quiz
 * Displays questions one by one with timer and tracks answers
 */
public class QuizTakingActivity extends AppCompatActivity {
    
    private static final String TAG = "QuizTakingActivity";
    public static final String EXTRA_QUIZ_DATA = "quiz_data";
    
    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvQuizTitle;
    private TextView tvQuestionNumber;
    private TextView tvTimer;
    private ProgressBar progressBar;
    private MaterialCardView cardQuestion;
    private TextView tvQuestion;
    private RadioGroup rgAnswers;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnSubmit;
    private ColorStateList defaultCardBackgroundColor;
    // Data
    private GenerateQuizResponse quizData;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private Map<Integer, Integer> userAnswers; // questionIndex -> answerIndex
    private Map<Integer, Boolean> answerResults; // questionIndex -> isCorrect
    private CountDownTimer questionTimer;
    private long remainingTimeMs;
    private int timeLimitPerQuestion; // seconds
    private boolean answerSelected = false; // Track if answer is selected for current question
    private QuizHistoryRepository quizHistoryRepository;
    private boolean isSubmittingQuiz = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_taking);
        
        // Get quiz data from QuizDataHolder (to avoid TransactionTooLargeException)
        quizData = com.example.learnquiz_fe.utils.QuizDataHolder.getInstance().getAndClearQuizResponse();
        // Fallback: Try to get from intent if holder is empty (for backward compatibility)
        if (quizData == null) {
            String quizJson = getIntent().getStringExtra(EXTRA_QUIZ_DATA);
            if (quizJson != null) {
                quizData = new Gson().fromJson(quizJson, GenerateQuizResponse.class);
            }
        }
        
        // Validate quiz data
        if (quizData == null || quizData.getQuestions() == null || quizData.getQuestions().isEmpty()) {
            Toast.makeText(this, "Error: No quiz data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        questions = quizData.getQuestions();
        userAnswers = new HashMap<>();
        answerResults = new HashMap<>();
        timeLimitPerQuestion = quizData.getQuizExamTimeLimit();
        
        // Initialize repository
        quizHistoryRepository = new QuizHistoryRepository(this);
        
        initViews();
        setupToolbar();
        setupListeners();
        displayQuestion(0);
        setupBackPressHandler();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);
        cardQuestion = findViewById(R.id.cardQuestion);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgAnswers = findViewById(R.id.rgAnswers);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSubmit = findViewById(R.id.btnSubmit);
        defaultCardBackgroundColor = cardQuestion.getCardBackgroundColor();
        // Set quiz title
        tvQuizTitle.setText(quizData.getTitle());
        
        // Setup progress bar
        progressBar.setMax(questions.size());
        progressBar.setProgress(0);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> showExitConfirmation());
    }
    
    private void setupListeners() {
        btnPrevious.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                displayQuestion(currentQuestionIndex - 1);
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                displayQuestion(currentQuestionIndex + 1);
            } else {
                // Last question - submit quiz
                submitQuiz();
            }
        });
        
        btnSubmit.setOnClickListener(v -> {
            submitQuiz();
        });
        
        // Radio group listener for instant feedback
        rgAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            if (!answerSelected && checkedId != -1) {
                handleAnswerSelection();
            }
        });
    }
    
    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        // 1. Reset màu nền và viền của CardView
        cardQuestion.setCardBackgroundColor(defaultCardBackgroundColor);
        cardQuestion.setStrokeWidth(0); // Bỏ viền

        // 2. Ẩn và xóa nội dung giải thích (nếu có)
        TextView tvExplanation = cardQuestion.findViewById(R.id.tvExplanation);
        if (tvExplanation != null) {
            tvExplanation.setVisibility(View.GONE);
            tvExplanation.setText("");
        }

        currentQuestionIndex = index;
        answerSelected = false; // Reset for new question
        QuizQuestion question = questions.get(index);
        
        // Update question number
        tvQuestionNumber.setText(getString(R.string.quiz_question_number, 
            index + 1, questions.size()));
        
        // Update progress
        progressBar.setProgress(index + 1);
        
        // Display question text
        tvQuestion.setText(question.getQuestion());
        
        // Clear previous answers
        rgAnswers.removeAllViews();
        rgAnswers.clearCheck();
        
        // Add answer options
        List<QuizAnswer> answers = question.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(answers.get(i).getAnswer());
            radioButton.setTextSize(16);
            radioButton.setPadding(16, 16, 16, 16);
            radioButton.setEnabled(true); // Enable for new question
            rgAnswers.addView(radioButton);
        }
        
        // Check if this question was already answered
        if (userAnswers.containsKey(index)) {
            answerSelected = true;
            int answerIndex = userAnswers.get(index);

            if (answerIndex == -1) {
                // question is timeout
                tvQuestion.setTextColor(getColor(android.R.color.white));

                for (int i = 0; i < rgAnswers.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) rgAnswers.getChildAt(i);
                    rb.setEnabled(false);
                    rb.setTextColor(getColor(android.R.color.white));

                    // Highlight right answer
                    if (question.getAnswers().get(i).isTrue()) {
                        rb.setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        rb.setAlpha(0.7f);
                    }
                }

                // Cập nhật thẻ sang màu xám
                cardQuestion.setCardBackgroundColor(getColor(R.color.gray_400));
                cardQuestion.setStrokeColor(getColor(android.R.color.darker_gray));
                cardQuestion.setStrokeWidth(4);

                // (Nếu muốn, hiển thị cả giải thích)

            }
            else if (answerIndex >= 0 && answerIndex < rgAnswers.getChildCount()) {
                RadioButton selectedBtn = (RadioButton) rgAnswers.getChildAt(answerIndex);
                selectedBtn.setChecked(true);
                
                // Show the result again
                showAnswerFeedback(answerIndex, question);
            }
        }
        
        // Update button states
        btnPrevious.setEnabled(index > 0);
        
        if (answerSelected || userAnswers.containsKey(index)) {
            // Already answered - show next/submit button
            if (index < questions.size() - 1) {
                btnNext.setVisibility(View.VISIBLE);
                btnSubmit.setVisibility(View.GONE);
            } else {
                btnNext.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.VISIBLE);
            }
        } else {
            // Not answered yet - hide navigation buttons
            btnNext.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.GONE);
        }
        
        // Start timer for this question
        startQuestionTimer();
        
        Log.d(TAG, "Displaying question " + (index + 1) + "/" + questions.size());
    }
    
    /**
     * Handle answer selection with instant feedback (flashcard style)
     */
    private void handleAnswerSelection() {
        answerSelected = true;
        
        // Stop timer
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        
        // Get selected answer index
        int selectedId = rgAnswers.getCheckedRadioButtonId();
        int selectedIndex = -1;
        
        for (int i = 0; i < rgAnswers.getChildCount(); i++) {
            if (rgAnswers.getChildAt(i).getId() == selectedId) {
                selectedIndex = i;
                break;
            }
        }
        
        if (selectedIndex == -1) return;
        
        // Save answer
        userAnswers.put(currentQuestionIndex, selectedIndex);
        
        QuizQuestion question = questions.get(currentQuestionIndex);
        showAnswerFeedback(selectedIndex, question);
    }
    
    /**
     * Show visual feedback for selected answer
     */
    private void showAnswerFeedback(int selectedIndex, QuizQuestion question) {
        QuizAnswer selectedAnswer = question.getAnswers().get(selectedIndex);
        boolean isCorrect = selectedAnswer.isTrue();
        
        // Save result
        answerResults.put(currentQuestionIndex, isCorrect);

        // Disable all radio buttons
        for (int i = 0; i < rgAnswers.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgAnswers.getChildAt(i);
            rb.setEnabled(false);

            QuizAnswer answer = question.getAnswers().get(i);
            
            // Highlight correct answer in green
            if (answer.isTrue()) {
                rb.setTextColor(getColor(android.R.color.holo_green_dark));
                rb.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            // Highlight wrong selected answer in red
            else if (i == selectedIndex) {
                rb.setTextColor(getColor(android.R.color.holo_red_dark));
                rb.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
        
        // Update card background color
        if (isCorrect) {
//            cardQuestion.setCardBackgroundColor(getColor(R.color.correct_answer_bg));
            cardQuestion.setCardBackgroundColor(getColor(R.color.green_400));
            cardQuestion.setStrokeColor(getColor(android.R.color.holo_green_dark));
            cardQuestion.setStrokeWidth(4);
        } else {
//            cardQuestion.setCardBackgroundColor(getColor(R.color.wrong_answer_bg));
            cardQuestion.setCardBackgroundColor(getColor(R.color.red_400));
            cardQuestion.setStrokeColor(getColor(android.R.color.holo_red_dark));
            cardQuestion.setStrokeWidth(4);
        }
        
        // Show explanation if available
        if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
            // Add explanation TextView below question if not exists
            TextView tvExplanation = cardQuestion.findViewById(R.id.tvExplanation);
            if (tvExplanation == null) {
                tvExplanation = new TextView(this);
                tvExplanation.setId(R.id.tvExplanation);
                tvExplanation.setPadding(20, 16, 20, 0);
                tvExplanation.setTextSize(14);
                tvExplanation.setTextColor(getColor(android.R.color.darker_gray));
                
                // Add to card layout
                ViewGroup cardLayout = (ViewGroup) cardQuestion.getChildAt(0);
                if (cardLayout instanceof android.widget.LinearLayout) {
                    cardLayout.addView(tvExplanation);
                }
            }
            tvExplanation.setText("💡 " + question.getExplanation());
            tvExplanation.setVisibility(View.VISIBLE);
        }
        
        // Show next/submit button
        if (currentQuestionIndex < questions.size() - 1) {
            btnNext.setVisibility(View.VISIBLE);
            btnNext.setText(R.string.quiz_next);
        } else {
            btnSubmit.setVisibility(View.VISIBLE);
            btnSubmit.setText(R.string.quiz_view_results);
        }
        
        // Show toast feedback
        if (isCorrect) {
            Toast.makeText(this, "✓ Correct! 🎉", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "✗ Incorrect", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveCurrentAnswer() {
        // Not needed in flashcard mode - answers are saved immediately
    }
    
    private void startQuestionTimer() {
        // Cancel previous timer
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        
        // Don't start timer if already answered
        if (answerSelected || userAnswers.containsKey(currentQuestionIndex)) {
            tvTimer.setText("--:--");
            tvTimer.setTextColor(getColor(android.R.color.darker_gray));
            return;
        }
        
        remainingTimeMs = timeLimitPerQuestion * 1000L;
        
        questionTimer = new CountDownTimer(remainingTimeMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMs = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }
            
            @Override
            public void onFinish() {
                // Time's up for this question - mark as skipped
                tvTimer.setText("00:00");
                tvTimer.setTextColor(getColor(android.R.color.holo_red_dark));
                
                answerSelected = true;

                // Dùng -1 để đánh dấu câu này đã hết giờ/bị bỏ qua
                userAnswers.put(currentQuestionIndex, -1);
                // Disable radio buttons
                for (int i = 0; i < rgAnswers.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) rgAnswers.getChildAt(i);
                    rb.setEnabled(false);

                    // Highlight correct answer
                    QuizQuestion question = questions.get(currentQuestionIndex);
                    if (question.getAnswers().get(i).isTrue()) {
                        rb.setTextColor(getColor(android.R.color.holo_green_dark));
                        rb.setTypeface(null, android.graphics.Typeface.BOLD);
                    }
                }
                
                // Save as incorrect (no answer)
                answerResults.put(currentQuestionIndex, false);
                
                // Update card to gray
//                cardQuestion.setCardBackgroundColor(getColor(R.color.skipped_answer_bg));
                cardQuestion.setCardBackgroundColor(getColor(R.color.gray_400));
                cardQuestion.setStrokeColor(getColor(android.R.color.darker_gray));
                cardQuestion.setStrokeWidth(4);
                
                Toast.makeText(QuizTakingActivity.this, "⏰ Time's up!", Toast.LENGTH_SHORT).show();
                
                // Show next/submit button
                if (currentQuestionIndex < questions.size() - 1) {
                    btnNext.setVisibility(View.VISIBLE);
                } else {
                    btnSubmit.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }
    
    private void updateTimerDisplay(long millisUntilFinished) {
        int seconds = (int) (millisUntilFinished / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeText);
        
        // Change color when time is running out
        if (seconds < 10 && minutes == 0) {
            tvTimer.setTextColor(getColor(android.R.color.holo_red_dark));
        } else {
            tvTimer.setTextColor(getColor(android.R.color.holo_blue_dark));
        }
    }
    
    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.quiz_exit_title)
            .setMessage(R.string.quiz_exit_message)
            .setPositiveButton(R.string.quiz_exit_confirm, (dialog, which) -> {
                if (questionTimer != null) {
                    questionTimer.cancel();
                }
                finish();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showSubmitConfirmation() {
        // Not needed in flashcard mode
    }
    
    private void submitQuiz() {
        // Prevent duplicate submissions
        if (isSubmittingQuiz) {
            return;
        }
        
        // Validate quiz ID
        if (quizData.getId() == null || quizData.getId().isEmpty()) {
            Toast.makeText(this, "Quiz ID not found", Toast.LENGTH_SHORT).show();
            showLocalResults();
            return;
        }
        
        // Show loading
        isSubmittingQuiz = true;
        showLoadingDialog();
        
        // Build submission request
        SubmitQuizRequestDTO request = buildSubmissionRequest();
        
        // Submit to API
        quizHistoryRepository.submitQuiz(request, new QuizHistoryRepository.QuizSubmissionCallback() {
            @Override
            public void onSuccess(SubmitQuizResponseDTO response) {
                isSubmittingQuiz = false;
                dismissLoadingDialog();
                
                Log.d(TAG, "Quiz submitted successfully. Score: " + response.getScore());
                showServerResults(response);
            }
            
            @Override
            public void onError(String errorMessage) {
                isSubmittingQuiz = false;
                dismissLoadingDialog();
                
                Log.e(TAG, "Quiz submission failed: " + errorMessage);
                
                // Show error and fallback to local results
                new AlertDialog.Builder(QuizTakingActivity.this)
                    .setTitle("Submission Failed")
                    .setMessage(errorMessage + "\n\nShowing local results instead.")
                    .setPositiveButton("OK", (dialog, which) -> showLocalResults())
                    .setCancelable(false)
                    .show();
            }
        });
    }
    
    /**
     * Build API submission request from user answers
     */
    private SubmitQuizRequestDTO buildSubmissionRequest() {
        SubmitQuizRequestDTO request = new SubmitQuizRequestDTO();
        request.setQuizId(quizData.getId());
        
        List<SubmittedAnswerDTO> submittedAnswers = new ArrayList<>();
        
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            Integer answerIndex = userAnswers.get(i);
            
            // Create submission for each question
            SubmittedAnswerDTO submittedAnswer = new SubmittedAnswerDTO();
            
            // Use questionId from server if available, otherwise fallback to index
            String questionId = question.getQuestionId();
            if (questionId == null || questionId.isEmpty()) {
                questionId = String.valueOf(i);
            }
            submittedAnswer.setQuestionId(questionId);
            
            // Get selected answer text
            List<String> selectedAnswers = new ArrayList<>();
            if (answerIndex != null && answerIndex >= 0 && answerIndex < question.getAnswers().size()) {
                QuizAnswer answer = question.getAnswers().get(answerIndex);
                selectedAnswers.add(answer.getAnswer());
            }
            submittedAnswer.setSelectedAnswers(selectedAnswers);
            
            submittedAnswers.add(submittedAnswer);
        }
        
        request.setAnswers(submittedAnswers);
        return request;
    }
    
    /**
     * Show results from server response
     */
    private void showServerResults(SubmitQuizResponseDTO response) {
        // Cancel timer
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        
        Log.d(TAG, "Server results - Total: " + response.getTotalQuestions() 
            + ", Correct: " + response.getCorrectCount() 
            + ", Score: " + response.getScore()
            + ", Percentage: " + response.getPercentage());
        
        // Navigate to result screen with server data
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_QUIZ_TITLE, quizData.getTitle());
        intent.putExtra(QuizResultActivity.EXTRA_TOTAL_QUESTIONS, response.getTotalQuestions());
        intent.putExtra(QuizResultActivity.EXTRA_CORRECT_ANSWERS, response.getCorrectCount());
        intent.putExtra(QuizResultActivity.EXTRA_QUIZ_DATA, new Gson().toJson(quizData));
        intent.putExtra(QuizResultActivity.EXTRA_USER_ANSWERS, convertMapToJson(userAnswers));
        
        // Add server-specific data
        intent.putExtra("server_score", response.getScore());
        intent.putExtra("server_percentage", response.getPercentage());
        intent.putExtra("submission_id", response.getId());
        intent.putExtra("completed_at", response.getCompletedAt());
        
        startActivity(intent);
        finish();
    }
    
    /**
     * Show local results as fallback
     */
    private void showLocalResults() {
        // Cancel timer
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        
        // Calculate score from userAnswers by checking correct answers
        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer answerIndex = userAnswers.get(i);
            if (answerIndex != null) {
                QuizQuestion question = questions.get(i);
                if (answerIndex < question.getAnswers().size()) {
                    QuizAnswer selectedAnswer = question.getAnswers().get(answerIndex);
                    if (selectedAnswer.isTrue()) {
                        correctCount++;
                    }
                }
            }
        }
        
        Log.d(TAG, "Local results - Correct: " + correctCount + "/" + questions.size());
        
        // Navigate to result screen
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_QUIZ_TITLE, quizData.getTitle());
        intent.putExtra(QuizResultActivity.EXTRA_TOTAL_QUESTIONS, questions.size());
        intent.putExtra(QuizResultActivity.EXTRA_CORRECT_ANSWERS, correctCount);
        intent.putExtra(QuizResultActivity.EXTRA_QUIZ_DATA, new Gson().toJson(quizData));
        intent.putExtra(QuizResultActivity.EXTRA_USER_ANSWERS, convertMapToJson(userAnswers));
        startActivity(intent);
        finish();
    }
    
    private AlertDialog loadingDialog;
    
    private void showLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Submitting Quiz");
        builder.setMessage("Please wait while we submit your answers...");
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }
    
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
    
    private String convertMapToJson(Map<Integer, Integer> map) {
        return new Gson().toJson(map);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
    }
}
