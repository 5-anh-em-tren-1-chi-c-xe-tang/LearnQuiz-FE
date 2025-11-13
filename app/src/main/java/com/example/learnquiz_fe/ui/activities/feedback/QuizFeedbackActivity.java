package com.example.learnquiz_fe.ui.activities.feedback;



import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.ui.fragments.feedback.QuizFeedbackFragment;

public class QuizFeedbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_feedback_container); // Bạn cần tạo layout này

        String quizId = getIntent().getStringExtra("QUIZ_ID");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, QuizFeedbackFragment.newInstance(quizId))
                    .commit();
        }
    }
}