package com.example.learnquiz_fe.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnquiz_fe.MainActivity;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.model.quiz.QuizAnswer;
import com.example.learnquiz_fe.data.model.quiz.QuizQuestion;
import com.example.learnquiz_fe.ui.adapter.QuizReviewAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying quiz results and review
 */
public class QuizResultActivity extends AppCompatActivity {
    
    private static final String TAG = "QuizResultActivity";
    
    public static final String EXTRA_QUIZ_TITLE = "quiz_title";
    public static final String EXTRA_TOTAL_QUESTIONS = "total_questions";
    public static final String EXTRA_CORRECT_ANSWERS = "correct_answers";
    public static final String EXTRA_QUIZ_DATA = "quiz_data";
    public static final String EXTRA_USER_ANSWERS = "user_answers";
    
    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvResultTitle;
    private TextView tvScore;
    private TextView tvPercentage;
    private TextView tvResultMessage;
    private RecyclerView rvReview;
    private MaterialButton btnRetakeQuiz;
    private MaterialButton btnHome;
    
    // Data
    private String quizTitle;
    private int totalQuestions;
    private int correctAnswers;
    private GenerateQuizResponse quizData;
    private Map<Integer, Integer> userAnswers;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);
        
        // Get data from intent
        quizTitle = getIntent().getStringExtra(EXTRA_QUIZ_TITLE);
        totalQuestions = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 0);
        correctAnswers = getIntent().getIntExtra(EXTRA_CORRECT_ANSWERS, 0);
        
        Log.d(TAG, "Received results - Total: " + totalQuestions + ", Correct: " + correctAnswers);
        
        String quizJson = getIntent().getStringExtra(EXTRA_QUIZ_DATA);
        String answersJson = getIntent().getStringExtra(EXTRA_USER_ANSWERS);
        
        quizData = new Gson().fromJson(quizJson, GenerateQuizResponse.class);
        userAnswers = new Gson().fromJson(answersJson, 
            new TypeToken<Map<Integer, Integer>>(){}.getType());
        
        initViews();
        setupToolbar();
        displayResults();
        setupReviewList();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvScore = findViewById(R.id.tvScore);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        rvReview = findViewById(R.id.rvReview);
        btnRetakeQuiz = findViewById(R.id.btnRetakeQuiz);
        btnHome = findViewById(R.id.btnHome);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.quiz_result_title);
        }
        toolbar.setNavigationIcon(null); // No back button
    }
    
    private void displayResults() {
        // Display quiz title
        tvResultTitle.setText(quizTitle);
        
        // Calculate percentage
        float percentage = (float) correctAnswers / totalQuestions * 100;
        
        // Display score
        tvScore.setText(getString(R.string.quiz_result_score, correctAnswers, totalQuestions));
        tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));
        
        // Display result message based on percentage
        String message;
        int textColor;
        
        if (percentage >= 80) {
            message = getString(R.string.quiz_result_excellent);
            textColor = getColor(android.R.color.holo_green_dark);
        } else if (percentage >= 60) {
            message = getString(R.string.quiz_result_good);
            textColor = getColor(android.R.color.holo_blue_dark);
        } else if (percentage >= 40) {
            message = getString(R.string.quiz_result_fair);
            textColor = getColor(android.R.color.holo_orange_dark);
        } else {
            message = getString(R.string.quiz_result_poor);
            textColor = getColor(android.R.color.holo_red_dark);
        }
        
        tvResultMessage.setText(message);
        tvPercentage.setTextColor(textColor);
    }
    
    private void setupReviewList() {
        QuizReviewAdapter adapter = new QuizReviewAdapter(
            quizData.getQuestions(), 
            userAnswers
        );
        
        rvReview.setLayoutManager(new LinearLayoutManager(this));
        rvReview.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnRetakeQuiz.setOnClickListener(v -> retakeQuiz());
        btnHome.setOnClickListener(v -> navigateToHome());
    }
    
    private void retakeQuiz() {
        Intent intent = new Intent(this, QuizTakingActivity.class);
        intent.putExtra(QuizTakingActivity.EXTRA_QUIZ_DATA, new Gson().toJson(quizData));
        startActivity(intent);
        finish();
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
